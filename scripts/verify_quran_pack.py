#!/usr/bin/env python3
"""Only proves bytes match an independently reviewed manifest; not Quran correctness."""
import argparse
import hashlib
import json
import re
from pathlib import Path
from validate_png import validate_png, PngValidationError

class IntegrityError(ValueError):
    pass

def verify_pages(assets: Path, manifest: Path, count: int = 604):
    if not assets.is_dir():
        raise IntegrityError("Approved Quran page directory is missing")
    try:
        data = json.loads(manifest.read_text(encoding="utf-8"))
    except (OSError, ValueError) as exc:
        raise IntegrityError("Approved manifest absent or malformed") from exc
    if data.get("rights_reviewed") is not True:
        raise IntegrityError("Source usage rights must be reviewed")
    if not data.get("source_name") or not data.get("source_url"):
        raise IntegrityError("Unidentified source")
    if data.get("riwaya") != "Hafs 'an Asim" or data.get("layout") != "Madinah 604-page":
        raise IntegrityError("Unexpected edition")
    reviewers = [x.strip() for x in data.get("approved_by", []) if isinstance(x,str) and x.strip()]
    if len(set(reviewers)) < 2:
        raise IntegrityError("Two independent recorded reviewers required")
    pages = data.get("pages")
    if not isinstance(pages, list) or len(pages) != count:
        raise IntegrityError(f"Expected {count} pages")
    names = set()
    hashes = set()
    for n, record in enumerate(pages, start=1):
        name = f"{n:03d}.png"
        if not isinstance(record, dict) or record.get("page") != n or record.get("file") != name:
            raise IntegrityError(f"Invalid page ordering at {n}")
        wanted = record.get("sha256")
        if not isinstance(wanted,str) or not re.fullmatch("[0-9a-f]{64}",wanted):
            raise IntegrityError(f"Invalid SHA-256 at page {n}")
        if wanted in hashes:
            raise IntegrityError(f"Duplicate page data at {n}")
        names.add(name)
        hashes.add(wanted)
        path = assets / name
        if not path.is_file() or path.is_symlink():
            raise IntegrityError(f"Missing or symlinked page {name}")
        try:
            validate_png(path)
        except (PngValidationError, OSError) as exc:
            raise IntegrityError(f"Invalid PNG structure {name}: {exc}") from exc
        with path.open("rb") as stream:
            digest = hashlib.file_digest(stream, "sha256").hexdigest()
        if digest != wanted:
            raise IntegrityError(f"Hash mismatch {name}")
    if {x.name for x in assets.iterdir()} != names:
        raise IntegrityError("Unexpected or missing pages in directory")
    return f"{count} files verified (independent scholarly approval still required)"

if __name__ == "__main__":
    p=argparse.ArgumentParser()
    p.add_argument("--pages",type=Path,required=True)
    p.add_argument("--manifest",type=Path,required=True)
    a=p.parse_args()
    print(verify_pages(a.pages,a.manifest))
