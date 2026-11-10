#!/usr/bin/env python3
"""Create a blank, offline 604-page human review ledger, never approval data."""
import argparse
import csv
import json
import re
from pathlib import Path

EXPECTED_SOURCE = "280c5d71ca16aaeeb3a343be1b92c76fa6df71d0671e202c026fde12402d9eef"
COLUMNS = ("page", "png_file", "png_sha256", "reviewer_1", "reviewer_1_status",
           "reviewer_2", "reviewer_2_status", "notes")

class ReviewLedgerError(ValueError):
    pass


def generate(render_report: Path, csv_path: Path, expected_count: int = 604) -> int:
    """Fail closed: none of the produced ledger rows are an approval."""
    try:
        src = json.loads(render_report.read_text(encoding="utf-8"))
    except (OSError, ValueError) as exc:
        raise ReviewLedgerError("Missing or malformed render report") from exc
    if src.get("status") != "UNAPPROVED_RENDER_ONLY" or src.get("source_sha256") != EXPECTED_SOURCE:
        raise ReviewLedgerError("Unverified source provenance or misleading render status")
    pages = src.get("pages")
    if not isinstance(pages, list) or len(pages) != expected_count or src.get("page_count") != expected_count:
        raise ReviewLedgerError("Incomplete page report")
    records = []
    for n, row in enumerate(pages, start=1):
        filename = f"{n:03d}.png"
        if (not isinstance(row, dict) or row.get("page") != n
            or row.get("file") != filename
            or not isinstance(row.get("sha256"), str)
            or not re.fullmatch(r"[0-9a-f]{64}", row["sha256"])
            or row.get("review_status") != "PENDING"):
            raise ReviewLedgerError(f"Invalid or pre-approved page {n}")
        records.append({"page": n, "png_file": filename, "png_sha256": row["sha256"],
                        "reviewer_1": "", "reviewer_1_status": "PENDING",
                        "reviewer_2": "", "reviewer_2_status": "PENDING", "notes": ""})
    if csv_path.exists():
        raise ReviewLedgerError("Review ledger already exists; refusing to overwrite human annotations")
    csv_path.parent.mkdir(parents=True, exist_ok=True)
    with csv_path.open("x", encoding="utf-8-sig", newline="") as out:
        writer = csv.DictWriter(out, fieldnames=COLUMNS)
        writer.writeheader()
        writer.writerows(records)
    return len(records)


if __name__ == "__main__":
    p = argparse.ArgumentParser()
    p.add_argument("--render-report", required=True, type=Path)
    p.add_argument("--output", required=True, type=Path)
    a = p.parse_args()
    print(f"Created {generate(a.render_report, a.output)} PENDING rows; NOT an approved Quran review")
