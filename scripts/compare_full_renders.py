#!/usr/bin/env python3
"""Compare two separately rendered official Hafs page packs byte-for-byte.

Technical reproducibility check only. NEVER certifies Quranic accuracy or
human approval; does not rewrite, export, or alter any Quran images.
"""
import argparse
import hashlib
import json
import re
from pathlib import Path

SOURCE_SHA = "280c5d71ca16aaeeb3a343be1b92c76fa6df71d0671e202c026fde12402d9eef"
REPORT_FILE = "UNAPPROVED_RENDER_REPORT.json"

class RenderComparisonError(ValueError):
    pass

def checksum(path: Path):
    h = hashlib.sha256()
    with path.open("rb") as f:
        for block in iter(lambda: f.read(1024*1024), b""):
            h.update(block)
    return h.hexdigest()

def read_pack(folder: Path, count: int):
    try:
        report = json.loads((folder / REPORT_FILE).read_text("utf-8"))
    except (OSError, ValueError) as exc:
        raise RenderComparisonError(f"Missing or corrupt render report: {folder}") from exc
    rows = report.get("pages")
    if report.get("status") != "UNAPPROVED_RENDER_ONLY" or report.get("source_sha256") != SOURCE_SHA:
        raise RenderComparisonError(f"Not an independently pinned official-source rendering: {folder}")
    if report.get("page_count") != count or not isinstance(rows, list) or len(rows) != count:
        raise RenderComparisonError(f"Incomplete render pack: {folder}")
    actual_files = {entry.name for entry in folder.iterdir() if entry.is_file()}
    expected_files = {f"{n:03d}.png" for n in range(1,count+1)} | {REPORT_FILE}
    # The newer artifacts can also contain local review HTML, pending ledger,
    # and unapproved technical audit; those are non-Quran sidecars.
    allowed_sidecars = {"REVIEW_OFFLINE_UNAPPROVED.html",
                       "TECHNICAL_AUDIT_ONLY.json",
                       "REVIEW_LEDGER_604_UNAPPROVED.csv"}
    if not expected_files.issubset(actual_files) or actual_files - expected_files - allowed_sidecars:
        raise RenderComparisonError(f"Wrong page file set in {folder}")
    found = []
    for n, row in enumerate(rows, 1):
        name=f"{n:03d}.png"
        if row.get("page") != n or row.get("file") != name or row.get("review_status") != "PENDING":
            raise RenderComparisonError(f"Wrong page order/status at {n} in {folder}")
        sha=row.get("sha256")
        if not isinstance(sha,str) or not re.fullmatch("[a-f0-9]{64}",sha):
            raise RenderComparisonError(f"Invalid page SHA at {n} in {folder}")
        image=folder/name
        if image.is_symlink() or not image.is_file():
            raise RenderComparisonError(f"Missing or linked page {n} in {folder}")
        if checksum(image)!=sha:
            raise RenderComparisonError(f"Altered PNG page {n} in {folder}")
        found.append((sha,row.get("width"),row.get("height")))
    return found

def compare_folders(left: Path, right: Path, count: int=604):
    if left.resolve()==right.resolve():
        raise RenderComparisonError("Two independent render archives are required")
    first=read_pack(left,count)
    second=read_pack(right,count)
    mismatches=[n for n,(a,b) in enumerate(zip(first,second),1) if a!=b]
    if mismatches:
        raise RenderComparisonError(f"Non-reproducible output: {len(mismatches)} pages differ, first: {mismatches[:20]}")
    return {
        "status":"REPRODUCIBLE_PNG_BYTES_UNAPPROVED",
        "source_sha256":SOURCE_SHA,
        "same_bytes_for_all_pages":True,
        "pages_compared":count,
        "independent_human_quran_review":False,
        "release_authorized":False
    }

if __name__=="__main__":
    p=argparse.ArgumentParser()
    p.add_argument("--left",type=Path,required=True)
    p.add_argument("--right",type=Path,required=True)
    p.add_argument("--out",type=Path,required=True)
    args=p.parse_args()
    result=compare_folders(args.left,args.right)
    args.out.write_text(json.dumps(result,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
    print(f"TECHNICAL REPRODUCIBILITY PASS: {result['pages_compared']}/{result['pages_compared']} identical PNG bytes (not Quran approval)")
