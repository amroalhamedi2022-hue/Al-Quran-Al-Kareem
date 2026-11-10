#!/usr/bin/env python3
"""Stage (NOT certify) original KFGQPC Hafs 604-page AI artwork.

Never generates or rewrites Quran. NEVER creates approved_manifest.json.
"""
import argparse
import hashlib
import json
import re
import stat
from datetime import datetime, timezone
from pathlib import Path, PurePosixPath
from zipfile import ZipFile, BadZipFile

EXPECTED_SHA256 = "280c5d71ca16aaeeb3a343be1b92c76fa6df71d0671e202c026fde12402d9eef"
SOURCE_PAGE = "https://dm.qurancomplex.gov.sa/"
ARCHIVE_URL = "https://dm.qurancomplex.gov.sa/Download/1441-AI-hafs.zip"
PAGE_NAME = re.compile(r"^(\d{3})___Hafs\d+__DM\.ai$", re.IGNORECASE)
MAX_TOTAL_BYTES = 6 * 1024**3
MAX_FILE_BYTES = 512 * 1024**2

class StagingError(ValueError):
    pass

def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()

def examine_archive(path: Path, expected_sha: str = EXPECTED_SHA256):
    if not path.is_file():
        raise StagingError("Official archive is unavailable")
    if not re.fullmatch(r"[a-f0-9]{64}", expected_sha):
        raise StagingError("Invalid pinned source hash")
    observed = sha256_file(path)
    if observed != expected_sha:
        raise StagingError(f"Official upstream SHA256 mismatch: {observed}")
    result = {}
    total_bytes = 0
    try:
        with ZipFile(path) as bundle:
            for entry in bundle.infolist():
                raw = entry.filename.replace("\\", "/")
                rel = PurePosixPath(raw)
                if (raw.startswith("/") or ".." in rel.parts or ":" in raw or not raw.strip()):
                    raise StagingError("Unsafe ZIP path")
                if stat.S_ISLNK(entry.external_attr >> 16):
                    raise StagingError("ZIP symlinks are forbidden")
                if entry.is_dir():
                    continue
                total_bytes += entry.file_size
                if total_bytes > MAX_TOTAL_BYTES or entry.file_size > MAX_FILE_BYTES:
                    raise StagingError("Unreasonably large source archive")
                m = PAGE_NAME.fullmatch(rel.name)
                if not m:
                    if rel.name.lower().endswith(".ai"):
                        raise StagingError(f"Unknown AI artwork entry: {rel.name}")
                    continue
                page = int(m.group(1))
                if not 1 <= page <= 604 or page in result:
                    raise StagingError(f"Duplicate or invalid Quran page: {page}")
                if entry.file_size == 0:
                    raise StagingError(f"Empty AI artwork for page {page}")
                result[page] = entry
            missing = sorted(set(range(1,605)) - set(result))
            if missing:
                raise StagingError(f"Missing Quran artwork pages: {missing[:12]} (total {len(missing)})")
            if len(result) != 604:
                raise StagingError(f"Expected exactly 604 pages, got {len(result)}")
            for page in range(1,605):
                with bundle.open(result[page]) as handle:
                    while handle.read(1024 * 1024):
                        pass
    except (BadZipFile, OSError, RuntimeError) as exc:
        raise StagingError(f"Invalid or truncated source archive: {exc}") from exc
    return result, observed

def stage(archive: Path, destination: Path, expected_sha: str = EXPECTED_SHA256):
    pages, observed = examine_archive(archive, expected_sha)
    dest = destination.resolve()
    dest.mkdir(parents=True, exist_ok=True)
    if any(dest.iterdir()):
        raise StagingError("Staging destination must be empty")
    rows = []
    try:
        with ZipFile(archive) as bundle:
            for n in range(1,605):
                new_name = f"{n:03d}.ai"
                output = dest / new_name
                digest = hashlib.sha256()
                with bundle.open(pages[n]) as source, output.open("xb") as target:
                    for block in iter(lambda: source.read(1024 * 1024), b""):
                        target.write(block)
                        digest.update(block)
                rows.append({"page": n, "original_zip_path": pages[n].filename,
                             "staged_file": new_name, "original_ai_sha256": digest.hexdigest()})
        report = {
            "status": "UNAPPROVED_SOURCE_STAGE_ONLY",
            "warning": "NOT for release: page-by-page human approval still required",
            "publisher": "King Fahd Glorious Quran Printing Complex",
            "source_page": SOURCE_PAGE, "archive_url": ARCHIVE_URL,
            "official_archive_sha256": EXPECTED_SHA256,
            "observed_archive_sha256": observed,
            "page_count": len(rows), "pages": rows,
            "staged_utc": datetime.now(timezone.utc).isoformat()
        }
        (dest / "SOURCE_STAGE_ONLY.json").write_text(
            json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8"
        )
        return report
    except Exception:
        for item in dest.iterdir():
            if item.is_file():
                item.unlink()
        raise

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--archive", type=Path, required=True)
    parser.add_argument("--out-dir", type=Path)
    args = parser.parse_args()
    if args.out_dir:
        result = stage(args.archive, args.out_dir)
        print(f"UNAPPROVED: staged {result['page_count']} original AI files; verified {result['observed_archive_sha256']}")
    else:
        pages, digest = examine_archive(args.archive)
        print(f"PASS: official SHA256 and ZIP CRC; {len(pages)} original AI pages; NOT approved for app")
