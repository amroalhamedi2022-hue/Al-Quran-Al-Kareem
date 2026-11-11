#!/usr/bin/env python3
"""Technical audit of converted Mushaf PNGs. NEVER approves Quran content."""
import argparse
import hashlib
import io
import json
import re
from pathlib import Path
from zipfile import ZipFile
from PIL import Image, UnidentifiedImageError

PIN = "280c5d71ca16aaeeb3a343be1b92c76fa6df71d0671e202c026fde12402d9eef"

class RasterAuditError(ValueError):
    pass

def audit(read_bytes, listed_names, *, count=604, expected_dimensions=(1600, 2287)):
    expected = {f"{n:03d}.png" for n in range(1, count + 1)} | {"UNAPPROVED_RENDER_REPORT.json"}
    # Human-review artifacts can contain these explicit non-Quran sidecars.
    # The full 604 images and their source report remain strictly mandatory.
    non_quran_sidecars = {"REVIEW_OFFLINE_UNAPPROVED.html", "TECHNICAL_AUDIT_ONLY.json",
                          "REVIEW_LEDGER_604_UNAPPROVED.csv"}
    names = set(listed_names)
    if (not expected.issubset(names) or names - expected - non_quran_sidecars
            or len(listed_names) != len(names)):
        raise RasterAuditError("Incorrect/missing/extra PNG files or duplicated archive entries")
    try:
        report = json.loads(read_bytes("UNAPPROVED_RENDER_REPORT.json"))
    except (OSError, ValueError) as exc:
        raise RasterAuditError("Unreadable technical source report") from exc
    if report.get("status") != "UNAPPROVED_RENDER_ONLY" or report.get("source_sha256") != PIN:
        raise RasterAuditError("Wrong source provenance or falsely approved report")
    if report.get("page_count") != count or not isinstance(report.get("pages"), list) or len(report["pages"]) != count:
        raise RasterAuditError("Missing or unexpected Quran pages in render report")
    observed_hashes = set()
    stats = []
    for n in range(1, count + 1):
        filename = f"{n:03d}.png"
        row = report["pages"][n - 1]
        if row.get("page") != n or row.get("file") != filename or row.get("review_status") != "PENDING":
            raise RasterAuditError(f"Unreviewed page mapping/order changed at {n}")
        expected_hash = row.get("sha256")
        if not isinstance(expected_hash, str) or not re.fullmatch("[a-f0-9]{64}", expected_hash):
            raise RasterAuditError(f"Invalid expected hash for {n}")
        data = read_bytes(filename)
        actual = hashlib.sha256(data).hexdigest()
        if actual != expected_hash or actual in observed_hashes:
            raise RasterAuditError(f"Content hash mismatch or duplicate for {n}")
        observed_hashes.add(actual)
        try:
            with Image.open(io.BytesIO(data)) as image:
                image.load()
                if image.format != "PNG" or image.mode != "RGBA" or image.size != expected_dimensions:
                    raise RasterAuditError(f"Wrong image format/mode/dimensions on {n}")
                bounds = image.getchannel("A").getbbox()
        except (OSError, UnidentifiedImageError) as exc:
            raise RasterAuditError(f"PNG not decodable on {n}") from exc
        if bounds is None or bounds[0] <= 0 or bounds[1] <= 0 or bounds[2] >= expected_dimensions[0] or bounds[3] >= expected_dimensions[1]:
            raise RasterAuditError(f"Image blank or content touches/crops at border on {n}")
        if n > 2 and (bounds[2] - bounds[0] < expected_dimensions[0] * .55 or bounds[3] - bounds[1] < expected_dimensions[1] * .70):
            raise RasterAuditError(f"Suspiciously small rendered content on {n}")
        stats.append({"page": n, "alpha_content_bbox": list(bounds), "png_bytes": len(data), "sha256": actual})
    return {"status": "UNAPPROVED_TECHNICAL_AUDIT_ONLY",
            "origin_sha256": PIN, "pages_examined": len(stats),
            "all_files_hash_matched_and_pixel_decoded": True,
            "warning": "No scholarly/content verification. NOT authorized for app release.",
            "pages": stats}

def audit_source(source: Path, *, count=604, expected_dimensions=(1600, 2287)):
    if source.is_dir():
        names = [p.name for p in source.iterdir()]
        if any(p.is_symlink() for p in source.iterdir()):
            raise RasterAuditError("Symlink in image pack")
        return audit(lambda name: (source / name).read_bytes(), names, count=count, expected_dimensions=expected_dimensions)
    with ZipFile(source) as archive:
        return audit(archive.read, archive.namelist(), count=count, expected_dimensions=expected_dimensions)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--source", type=Path, required=True)
    parser.add_argument("--out", type=Path, required=True)
    args = parser.parse_args()
    result = audit_source(args.source)
    args.out.write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    print("PASS TECHNICAL ONLY: full 604 PNG decode, source digest, pixel coverage and page mapping; NOT human content approval")
