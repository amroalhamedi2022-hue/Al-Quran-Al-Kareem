#!/usr/bin/env python3
"""Securely assemble approved original Quran artwork into the Android assets.

The downloaded GitHub render artifact is UNAPPROVED and untrusted. This tool
will not treat that artifact as Quran approval: it MUST match an independently
verified and separately pinned HUMAN-APPROVED manifest before being copied.
It never rewrites/transcodes any PNG or Quranic text.
"""
import argparse
import hashlib
import json
import re
import shutil
import tempfile
from pathlib import Path

from validate_png import PngValidationError, validate_png

SHA256 = re.compile(r"^[0-9a-f]{64}$")
REPORT_SIDECARS = {
    "UNAPPROVED_RENDER_REPORT.json",
    "REVIEW_OFFLINE_UNAPPROVED.html",
    "TECHNICAL_AUDIT_ONLY.json",
    "REVIEW_LEDGER_604_UNAPPROVED.csv",
}

class AssetAssemblyError(ValueError):
    pass

def file_sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()

def assemble(source: Path, destination: Path, approved_manifest: Path, count: int = 604):
    source, destination, approved_manifest = source.resolve(), destination.resolve(), approved_manifest.resolve()
    if not source.is_dir() or not approved_manifest.is_file():
        raise AssetAssemblyError("No image source or independently approved manifest; NO RELEASE")
    if not destination.is_dir() or approved_manifest.parent != destination:
        raise AssetAssemblyError("The approved manifest must already exist inside the Android asset destination")
    try:
        metadata = json.loads(approved_manifest.read_text("utf-8"))
    except (OSError, ValueError) as exc:
        raise AssetAssemblyError("Invalid independent approval manifest") from exc
    if (metadata.get("rights_reviewed") is not True or metadata.get("riwaya") != "Hafs 'an Asim"
        or metadata.get("layout") != "Madinah 604-page"):
        raise AssetAssemblyError("Publisher/edition/rights gate failed")
    people = metadata.get("approved_by")
    if (not isinstance(people, list) or
        len({p.strip().casefold() for p in people if isinstance(p, str) and p.strip()}) < 2):
        raise AssetAssemblyError("Two independent reviewers are not recorded")
    entries = metadata.get("pages")
    if not isinstance(entries, list) or len(entries) != count:
        raise AssetAssemblyError(f"Manifest must include exactly {count} ordered pages")
    expected_names = {f"{n:03d}.png" for n in range(1, count + 1)}
    seen = {p.name for p in source.iterdir()}
    if expected_names - seen or seen - expected_names - REPORT_SIDECARS:
        raise AssetAssemblyError("Source contains missing or unexpected files")
    if {p.name for p in destination.iterdir()} != {"approved_manifest.json"}:
        raise AssetAssemblyError("Destination must contain only its independent approved manifest")
    hashes = set()
    # Validate everything BEFORE copying a single page into the Android tree.
    for number, record in enumerate(entries, 1):
        name = f"{number:03d}.png"
        expected = record.get("sha256") if isinstance(record, dict) else None
        if (not isinstance(record, dict) or record.get("page") != number
            or record.get("file") != name or
            not isinstance(expected, str) or not SHA256.fullmatch(expected) or expected in hashes):
            raise AssetAssemblyError(f"Bad page ordering/hash at {number}")
        hashes.add(expected)
        file = source / name
        if file.is_symlink() or not file.is_file():
            raise AssetAssemblyError(f"Missing or symlinked page {name}")
        try:
            validate_png(file)
        except (PngValidationError, OSError) as exc:
            raise AssetAssemblyError(f"Unreadable Quran artwork page {name}") from exc
        if file_sha256(file) != expected:
            raise AssetAssemblyError(f"Page SHA-256 differs from independent approved manifest: {name}")
    # File bytes are copied unchanged; staged temporary files are re-hashed.
    with tempfile.TemporaryDirectory(prefix="quran-asset-assembly-", dir=destination.parent) as temp:
        staged = Path(temp)
        for number, record in enumerate(entries, 1):
            name = record["file"]
            out = staged / name
            shutil.copyfile(source / name, out)
            if file_sha256(out) != record["sha256"]:
                raise AssetAssemblyError(f"Stage copy changed Quran image bytes: {name}")
        for number in range(1, count + 1):
            name = f"{number:03d}.png"
            (staged / name).replace(destination / name)
    return {"result": "COPIED_BITWISE_IDENTICAL_WITH_APPROVED_MANIFEST",
            "pages": count, "source_type": "unapproved_render_artifact",
            "approval_depends_on_independent_pinned_manifest": True}

if __name__ == "__main__":
    p = argparse.ArgumentParser()
    p.add_argument("--source", type=Path, required=True)
    p.add_argument("--destination", type=Path, required=True)
    p.add_argument("--manifest", type=Path, required=True)
    args = p.parse_args()
    print(json.dumps(assemble(args.source, args.destination, args.manifest), indent=2))
