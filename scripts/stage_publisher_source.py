#!/usr/bin/env python3
"""Stage original publisher-source PNGs for a SIGNED offline Android release.

This is Publisher-source byte integrity, NOT institutional certification.
The official AI archive SHA was independently verified upstream by the pinned
source CI run; here we re-check every reported PNG hash, dimensions, CRC,
page order and unapproved status. Files are COPIED UNCHANGED (no OCR or edits).
"""
import argparse
import hashlib
import json
import re
import shutil
import tempfile
from pathlib import Path
from validate_png import PngValidationError, validate_png

PIN = "280c5d71ca16aaeeb3a343be1b92c76fa6df71d0671e202c026fde12402d9eef"
SIDECARS = {"REVIEW_OFFLINE_UNAPPROVED.html", "TECHNICAL_AUDIT_ONLY.json",
            "REVIEW_LEDGER_604_UNAPPROVED.csv"}
REPORT = "UNAPPROVED_RENDER_REPORT.json"
SOURCE_MANIFEST = "source_provenance_manifest.json"

class PublisherSourceError(ValueError):
    pass

def sha256(path: Path):
    h = hashlib.sha256()
    with path.open("rb") as stream:
        for data in iter(lambda: stream.read(1024*1024), b""):
            h.update(data)
    return h.hexdigest()

def stage_publisher_source(source: Path, destination: Path, *, count: int=604, dimensions=(1600,2287)):
    if not source.is_dir() or source.is_symlink():
        raise PublisherSourceError("Original source artifact folder missing")
    if destination.exists() and any(destination.iterdir()):
        raise PublisherSourceError("Preview destination must be empty; refuse overwrite")
    report_path = source / REPORT
    try:
        raw = report_path.read_bytes()
        report = json.loads(raw)
    except (OSError, ValueError) as exc:
        raise PublisherSourceError("Missing or invalid original conversion report") from exc
    if report.get("status") != "UNAPPROVED_RENDER_ONLY" or report.get("source_sha256") != PIN:
        raise PublisherSourceError("Source is not the pinned published official archive")
    pages = report.get("pages")
    if report.get("page_count") != count or not isinstance(pages,list) or len(pages)!=count:
        raise PublisherSourceError("Expected full official source page count")
    expected = {REPORT} | {f"{n:03d}.png" for n in range(1,count+1)}
    actual = {p.name for p in source.iterdir()}
    if not expected.issubset(actual) or actual - expected - SIDECARS:
        raise PublisherSourceError("Unknown, missing, or extra source artwork")
    hashes = set()
    for number, row in enumerate(pages,1):
        name=f"{number:03d}.png"
        if (not isinstance(row,dict) or row.get("page") != number or row.get("file") != name
            or row.get("review_status") != "PENDING"
            or (row.get("width"),row.get("height")) != dimensions
            or not isinstance(row.get("sha256"),str)
            or not re.fullmatch("[0-9a-f]{64}",row["sha256"])
            or row["sha256"] in hashes):
            raise PublisherSourceError(f"Untrusted page metadata: {number}")
        hashes.add(row["sha256"])
        image=source/name
        if image.is_symlink() or not image.is_file():
            raise PublisherSourceError(f"Missing or linked original page {number}")
        try:
            w,h=validate_png(image)
        except (OSError,PngValidationError) as exc:
            raise PublisherSourceError(f"Invalid source PNG at {number}") from exc
        if (w,h) != dimensions or sha256(image)!=row["sha256"]:
            raise PublisherSourceError(f"Source image mismatch: page {number}")
    destination.parent.mkdir(parents=True, exist_ok=True)
    # Never create partly valid app assets on hash failure.
    with tempfile.TemporaryDirectory(prefix="quran-source-stage-",dir=destination.parent) as tmp:
        stage=Path(tmp)
        for number in range(1,count+1):
            name=f"{number:03d}.png"
            target=stage/name
            shutil.copyfile(source/name,target)
            if sha256(target)!=pages[number-1]["sha256"]:
                raise PublisherSourceError(f"Original bytes changed while copying page {number}")
        (stage/SOURCE_MANIFEST).write_bytes(raw)
        destination.mkdir(exist_ok=True)
        try:
            for entry in stage.iterdir():
                entry.replace(destination/entry.name)
        except Exception:
            for entry in destination.iterdir():
                if entry.is_file():
                    entry.unlink()
            raise
    return {"status":"PUBLISHER_SOURCE_BYTES_VERIFIED",
            "source_sha256":PIN,"pages":count,
            "source_integrity_verified":True,"institutional_certification":False,"bytes_modified":False}

if __name__=="__main__":
    p=argparse.ArgumentParser()
    p.add_argument("--source",type=Path,required=True)
    p.add_argument("--destination",type=Path,required=True)
    a=p.parse_args()
    print(json.dumps(stage_publisher_source(a.source,a.destination),indent=2))
