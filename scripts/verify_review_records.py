#!/usr/bin/env python3
"""Check two full reviewer record files against an immutable render report.

Records are self-declarations only. This DOES NOT verify a reviewer's identity,
qualifications, actual visual work or Quran text; NEVER grants publication rights.
Never creates approved_manifest.json or changes any Quran page pixels.
"""
import argparse
import json
import re
from pathlib import Path

PIN = "280c5d71ca16aaeeb3a343be1b92c76fa6df71d0671e202c026fde12402d9eef"
REVIEW_STATUS = "HUMAN_REVIEW_RECORD_ONLY_NOT_APPROVAL"

class ReviewRecordError(ValueError):
    pass

def _load(path: Path):
    try:
        return json.loads(path.read_text("utf-8"))
    except (OSError, ValueError) as exc:
        raise ReviewRecordError(f"Cannot read review data: {path.name}") from exc

def check_records(report: Path, reviewer_files: list[Path], count: int = 604):
    if len(reviewer_files) != 2 or reviewer_files[0].resolve() == reviewer_files[1].resolve():
        raise ReviewRecordError("Require two separate independent reviewer files")
    source = _load(report)
    originals = source.get("pages")
    if source.get("status") != "UNAPPROVED_RENDER_ONLY" or source.get("source_sha256") != PIN:
        raise ReviewRecordError("Source render report unverified")
    if source.get("page_count") != count or not isinstance(originals, list) or len(originals) != count:
        raise ReviewRecordError("Incorrect source page count")
    for n, page in enumerate(originals, 1):
        if (page.get("page") != n or page.get("file") != f"{n:03d}.png"
                or not re.fullmatch("[0-9a-f]{64}", str(page.get("sha256", "")))
                or page.get("review_status") != "PENDING"):
            raise ReviewRecordError(f"Invalid original page record at {n}")
    reviewers = []
    ids = set()
    for reviewer_file in reviewer_files:
        record = _load(reviewer_file)
        if (record.get("status") != REVIEW_STATUS or record.get("source_sha256") != PIN
                or record.get("expectedPages") != count):
            raise ReviewRecordError(f"Wrong review report edition/status in {reviewer_file.name}")
        name = record.get("reviewer")
        if not isinstance(name, str) or len(name.strip()) < 3 or len(name.strip()) > 150:
            raise ReviewRecordError("Each reviewer must identify themselves")
        unique = name.strip().casefold()
        if unique in ids:
            raise ReviewRecordError("Duplicate reviewer identity in files")
        ids.add(unique)
        pages = record.get("records")
        if not isinstance(pages, list) or len(pages) != count:
            raise ReviewRecordError(f"Incomplete review: {name}")
        for n, page in enumerate(pages, 1):
            expected = originals[n-1]
            if page.get("page") != n or page.get("sha256") != expected["sha256"]:
                raise ReviewRecordError(f"Reviewed page hash/order mismatch: {name}, page {n}")
            if page.get("checked") is not True or page.get("flagged") is not False:
                raise ReviewRecordError(f"Page not confirmed or still flagged: {name}, page {n}")
            if not isinstance(page.get("lastUpdatedUtc"), str) or not page["lastUpdatedUtc"].endswith("Z"):
                raise ReviewRecordError(f"Missing individual page review timestamp: {name}, page {n}")
        reviewers.append({"name": name.strip(), "review_file": reviewer_file.name,
                          "reviewed_pages": count, "exportedUtc": record.get("exportedUtc")})
    return {
        "status": "TWO_COMPLETE_SELF_REPORTED_REVIEWS_NOT_CONTENT_APPROVAL",
        "source_sha256": PIN, "reviewed_page_count": count,
        "recorded_reviewers": reviewers,
        "page_hashes_all_match_render_source": True,
        "human_identity_and_actual_visual_comparison_not_verified": True,
        "automatic_quran_approval": False, "release_allowed": False,
        "next_step": "Manually verify qualified reviewers, independent source comparison, resolve findings and sign acceptance."
    }

if __name__ == "__main__":
    cli = argparse.ArgumentParser()
    cli.add_argument("--render-report", type=Path, required=True)
    cli.add_argument("--review", type=Path, action="append", required=True)
    cli.add_argument("--out", type=Path, required=True)
    args = cli.parse_args()
    result = check_records(args.render_report, args.review)
    args.out.write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    print("PASS record consistency: 604 pages x 2 self-report files; NO Quran text certification or release permission")
