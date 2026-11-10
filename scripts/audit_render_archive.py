#!/usr/bin/env python3
"""Audit ALL Quran PNG renders in a GitHub Actions artifact ZIP.

Technical integrity only. Does NOT certify any verse, diacritic or Quran edition.
An artifact remains UNAPPROVED even when this command reports PASS.
"""
import argparse
import hashlib
import json
import re
import struct
import zipfile
from pathlib import Path

OFFICIAL_SOURCE_SHA256 = "280c5d71ca16aaeeb3a343be1b92c76fa6df71d0671e202c026fde12402d9eef"
REPORT_NAME = "UNAPPROVED_RENDER_REPORT.json"
PNG_SIGNATURE = b"\x89PNG\r\n\x1a\n"
MAX_PNG_BYTES = 24 * 1024 * 1024

class ArtifactAuditError(ValueError):
    pass


def audit(zip_path: Path, expected_count: int = 604) -> dict:
    """Fail closed on incomplete, reordered, malformed or modified pages."""
    if expected_count < 1 or expected_count > 604:
        raise ArtifactAuditError("Invalid count")
    try:
        with zipfile.ZipFile(zip_path) as z:
            members = z.infolist()
            names = [item.filename for item in members]
            expected = {f"{n:03d}.png" for n in range(1, expected_count + 1)} | {REPORT_NAME}
            if len(names) != len(expected) or set(names) != expected:
                raise ArtifactAuditError("Expected only numbered PNGs and an unapproved report")
            for info in members:
                if info.file_size == 0 or info.file_size > MAX_PNG_BYTES:
                    raise ArtifactAuditError("Empty or oversized archive member")
                if info.flag_bits & 1:
                    raise ArtifactAuditError("Encrypted archive member")
            report = json.loads(z.read(REPORT_NAME))
            if report.get("status") != "UNAPPROVED_RENDER_ONLY":
                raise ArtifactAuditError("Unreviewed status must remain explicit")
            if report.get("source_sha256") != OFFICIAL_SOURCE_SHA256:
                raise ArtifactAuditError("Missing original official source checksum")
            if report.get("page_count") != expected_count:
                raise ArtifactAuditError("Invalid declared page count")
            pages = report.get("pages")
            if not isinstance(pages, list) or len(pages) != expected_count:
                raise ArtifactAuditError("Missing page manifest entries")
            hashes = set()
            dimensions = set()
            for n, row in enumerate(pages, 1):
                filename = f"{n:03d}.png"
                if row.get("page") != n or row.get("file") != filename:
                    raise ArtifactAuditError(f"Page ordering mismatch: {n}")
                if row.get("review_status") != "PENDING":
                    raise ArtifactAuditError(f"Unexpected approval claim on {filename}")
                digest_expected = row.get("sha256")
                if not isinstance(digest_expected, str) or not re.fullmatch("[0-9a-f]{64}", digest_expected):
                    raise ArtifactAuditError(f"Bad digest for page {n}")
                if digest_expected in hashes:
                    raise ArtifactAuditError(f"Duplicate page hash: {n}")
                hashes.add(digest_expected)
                with z.open(filename, "r") as entry:
                    header = entry.read(33)
                    if len(header) != 33 or not header.startswith(PNG_SIGNATURE):
                        raise ArtifactAuditError(f"Invalid PNG signature: {n}")
                    ihdr_len = struct.unpack(">I", header[8:12])[0]
                    if ihdr_len != 13 or header[12:16] != b"IHDR":
                        raise ArtifactAuditError(f"Invalid PNG IHDR: {n}")
                    w, h, depth, color, comp, filt, interlace = struct.unpack(">IIBBBBB", header[16:29])
                    if not (w == row.get("width") and h == row.get("height") and
                            w >= 100 and h >= 100 and depth == 8 and color == 6 and
                            comp == 0 and filt == 0 and interlace == 0):
                        raise ArtifactAuditError(f"Unexpected PNG properties: {n}")
                    dimensions.add((w, h))
                    digest = hashlib.sha256(header)
                    while block := entry.read(1 << 20):
                        digest.update(block)
                    # zipfile.ZipExtFile also verifies ZIP CRC after full read.
                if digest.hexdigest() != digest_expected:
                    raise ArtifactAuditError(f"Page SHA-256 mismatch: {n}")
            if len(dimensions) != 1:
                raise ArtifactAuditError("Inconsistent page dimensions")
            width, height = next(iter(dimensions))
            return {"status": "TECHNICAL_PASS_UNAPPROVED",
                    "review_required": True,
                    "page_count": expected_count,
                    "source_sha256": OFFICIAL_SOURCE_SHA256,
                    "width": width, "height": height,
                    "all_page_digests_match": True,
                    "unique_page_hashes": len(hashes),
                    "archive_sha256": sha256_file(zip_path)}
    except (OSError, zipfile.BadZipFile, zipfile.LargeZipFile, KeyError, TypeError, ValueError) as exc:
        if isinstance(exc, ArtifactAuditError):
            raise
        raise ArtifactAuditError(f"Malformed review archive: {exc}") from exc


def sha256_file(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as stream:
        for b in iter(lambda: stream.read(1 << 20), b""):
            h.update(b)
    return h.hexdigest()


if __name__ == "__main__":
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--archive", type=Path, required=True)
    p.add_argument("--output", type=Path)
    args = p.parse_args()
    result = audit(args.archive)
    payload = json.dumps(result, ensure_ascii=False, indent=2)
    if args.output:
        args.output.write_text(payload + "\n", encoding="utf-8")
    print(payload)
