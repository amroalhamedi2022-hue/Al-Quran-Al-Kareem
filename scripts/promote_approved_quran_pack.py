#!/usr/bin/env python3
"""Promote independently reviewed publisher PNGs to the exact approved Android pack.

This is a technical *release packaging* gate, NOT a scholarly approval generator.
Never writes an approval manifest or changes any Quran page bytes. An external,
human-reviewed manifest must already have its SHA-256 pinned independently in
QuranAssetVerifier.kt. The original publisher report is kept out of the APK.
"""
import argparse
import hashlib
import json
import re
from pathlib import Path

from validate_png import validate_png, PngValidationError
from verify_approval_seal import SEAL

PINNED_PUBLISHER_ARCHIVE = "280c5d71ca16aaeeb3a343be1b92c76fa6df71d0671e202c026fde12402d9eef"
HEX_256 = re.compile(r"[0-9a-f]{64}\Z")

class PromotionError(ValueError):
    pass

def _decode_object(raw: bytes, label: str):
    try:
        value = json.loads(raw)
    except (UnicodeError, ValueError) as exc:
        raise PromotionError(f"Malformed {label}") from exc
    if not isinstance(value, dict):
        raise PromotionError(f"Malformed {label} object")
    return value

def approved_pin_from_kotlin(path: Path) -> str:
    try:
        matched = SEAL.search(path.read_text(encoding="utf-8"))
    except OSError as exc:
        raise PromotionError("Cannot read independent Kotlin approval pin") from exc
    if not matched or not HEX_256.fullmatch(matched.group(1)):
        raise PromotionError("No independently pinned approval SHA-256")
    return matched.group(1)

def promote_approved_pack(
    staged: Path, independent_manifest: Path, pinned_manifest_sha: str,
    *, count: int = 604, dimensions: tuple[int, int] = (1600, 2287)
):
    """Verify *both* provenance chains before changing only the sidecar metadata.

    A failed check leaves original staged pages/manifest untouched.
    Synthetic tests may supply a small count/dimensions; production CLI uses 604.
    """
    if not HEX_256.fullmatch(pinned_manifest_sha):
        raise PromotionError("Missing external immutable approval digest")
    if not staged.is_dir() or staged.is_symlink():
        raise PromotionError("Invalid staged source directory")
    src_report = staged / "source_provenance_manifest.json"
    approved_name = staged / "approved_manifest.json"
    if src_report.is_symlink() or not src_report.is_file() or approved_name.exists() or approved_name.is_symlink():
        raise PromotionError("Expected original unapproved report; refuse overwritten approval")
    if independent_manifest.resolve() == approved_name.resolve():
        raise PromotionError("Approved manifest must be independent of staged assets")
    if not independent_manifest.is_file() or independent_manifest.is_symlink():
        raise PromotionError("Missing independent external approved manifest")
    try:
        src_raw = src_report.read_bytes()
        independent_raw = independent_manifest.read_bytes()
    except OSError as exc:
        raise PromotionError("Cannot read staged or independent source report") from exc
    digest = hashlib.sha256(independent_raw).hexdigest()
    if digest != pinned_manifest_sha:
        raise PromotionError("Approved manifest differs from the independently pinned SHA-256")
    src = _decode_object(src_raw, "publisher render report")
    approved = _decode_object(independent_raw, "independent approval manifest")
    src_pages = src.get("pages")
    approved_pages = approved.get("pages")
    if (src.get("status") != "UNAPPROVED_RENDER_ONLY"
            or src.get("source_sha256") != PINNED_PUBLISHER_ARCHIVE
            or src.get("page_count") != count
            or not isinstance(src_pages, list) or len(src_pages) != count):
        raise PromotionError("Wrong/unpinned original publisher render report")
    if (approved.get("source_sha256") != PINNED_PUBLISHER_ARCHIVE
            or approved.get("source_report_sha256") != hashlib.sha256(src_raw).hexdigest()):
        raise PromotionError("Human review does not bind the exact publisher conversion report")
    if approved.get("rights_reviewed") is not True:
        raise PromotionError("Source usage rights are not reviewed")
    if (approved.get("riwaya") != "Hafs 'an Asim"
            or approved.get("layout") != "Madinah 604-page"
            or not all(isinstance(approved.get(x), str) and approved[x].strip()
                       for x in ("source_name", "source_url"))):
        raise PromotionError("Wrong or unidentified Quran edition")
    identities = approved.get("approved_by")
    if not isinstance(identities, list):
        raise PromotionError("No recorded independent review")
    names = [x.strip().casefold() for x in identities if isinstance(x, str) and x.strip()]
    if len(set(names)) < 2:
        raise PromotionError("Two distinct independently reviewed approvals required")
    if not isinstance(approved_pages, list) or len(approved_pages) != count:
        raise PromotionError("Missing approved 604-page mapping")
    page_names = {f"{n:03d}.png" for n in range(1, count + 1)}
    actual_files = {p.name for p in staged.iterdir()}
    if actual_files != page_names | {src_report.name}:
        raise PromotionError("Missing/extra original publisher images or metadata")
    seen_hashes = set()
    for n, (original, accepted) in enumerate(zip(src_pages, approved_pages), start=1):
        expected_file = f"{n:03d}.png"
        if not isinstance(original, dict) or not isinstance(accepted, dict):
            raise PromotionError(f"Invalid page record at {n}")
        sha = original.get("sha256")
        if (original.get("page") != n or original.get("file") != expected_file
                or original.get("review_status") != "PENDING"
                or (original.get("width"), original.get("height")) != dimensions
                or not isinstance(sha, str) or not HEX_256.fullmatch(sha)
                or sha in seen_hashes):
            raise PromotionError(f"Invalid publisher page record at {n}")
        seen_hashes.add(sha)
        if (accepted.get("page") != n or accepted.get("file") != expected_file
                or accepted.get("sha256") != sha):
            raise PromotionError(f"Approved image differs from publisher at page {n}")
        image = staged / expected_file
        if not image.is_file() or image.is_symlink():
            raise PromotionError(f"Publisher page missing or symlinked at {n}")
        try:
            found_dimensions = validate_png(image)
            with image.open("rb") as stream:
                actual_hash = hashlib.file_digest(stream, "sha256").hexdigest()
        except (OSError, PngValidationError) as exc:
            raise PromotionError(f"Corrupt publisher PNG at {n}") from exc
        if found_dimensions != dimensions or actual_hash != sha:
            raise PromotionError(f"Publisher page bytes or dimensions modified at {n}")
    # Nothing has been mutated until this point.
    pending = staged / ".approved_manifest.json.pending"
    if pending.exists() or pending.is_symlink():
        raise PromotionError("Refuse existing pending approval artifact")
    try:
        with pending.open("xb") as file:
            file.write(independent_raw)
        pending.replace(approved_name)
        src_report.unlink()
    finally:
        if pending.exists():
            pending.unlink()
    return {"status": "EXTERNALLY_PINNED_APPROVED_PACK_BYTES_MATCH",
            "pages": count,
            "source_png_bytes_modified": False,
            "approval_generated_by_script": False,
            "independent_human_review_verified_by_script": False}

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--staged", type=Path, required=True)
    parser.add_argument("--approved-manifest", type=Path, required=True)
    parser.add_argument("--pinned-kotlin", type=Path, required=True)
    args = parser.parse_args()
    print(json.dumps(promote_approved_pack(
        args.staged, args.approved_manifest,
        approved_pin_from_kotlin(args.pinned_kotlin)
    ), indent=2))
