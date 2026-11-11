#!/usr/bin/env python3
"""Audit signed APK Quran page bytes against source-staged publisher artwork.

Technical byte integrity only; not human scholarly certification or release approval.
"""
import argparse
import hashlib
import json
import re
from pathlib import Path
from zipfile import ZipFile, BadZipFile

OFFICIAL_HAFS_ARCHIVE_SHA256 = "280c5d71ca16aaeeb3a343be1b92c76fa6df71d0671e202c026fde12402d9eef"
PNG_SHA256 = re.compile(r"[0-9a-f]{64}\Z")

class PublisherApkError(ValueError):
    pass

def audit_publisher_apk(apk: Path, source: Path, count=604):
    try:
        report_raw = (source / "source_provenance_manifest.json").read_bytes()
        manifest = json.loads(report_raw)
        if not isinstance(manifest, dict):
            raise PublisherApkError("Invalid source manifest structure")
        pages = manifest.get("pages")
        if (manifest.get("status") != "UNAPPROVED_RENDER_ONLY"
                or manifest.get("source_sha256") != OFFICIAL_HAFS_ARCHIVE_SHA256
                or manifest.get("page_count") != count
                or not isinstance(pages, list) or len(pages) != count):
            raise PublisherApkError("Missing, unpinned or falsely approved source report")
        with ZipFile(apk) as zipfile:
            actual = zipfile.namelist()
            embedded = {x for x in actual if x.startswith("assets/quran/") and not x.endswith("/")}
            expected = {f"assets/quran/{n:03d}.png" for n in range(1,count+1)}
            expected.add("assets/quran/source_provenance_manifest.json")
            if embedded != expected or len(actual) != len(set(actual)):
                raise PublisherApkError("Missing/extra/duplicated source assets inside APK")
            if zipfile.read("assets/quran/source_provenance_manifest.json") != report_raw:
                raise PublisherApkError("Conversion manifest bytes modified inside APK")
            seen=set()
            for n,row in enumerate(pages,1):
                filename=f"{n:03d}.png"
                if (not isinstance(row, dict)
                        or row.get("page") != n or row.get("file") != filename
                        or row.get("review_status") != "PENDING"
                        or (row.get("width"), row.get("height")) != (1600, 2287)):
                    raise PublisherApkError(f"Untrusted source page mapping/dimensions: {n}")
                sha=row.get("sha256")
                if not isinstance(sha,str) or not PNG_SHA256.fullmatch(sha) or sha in seen:
                    raise PublisherApkError(f"Invalid/duplicate source digest at {n}")
                seen.add(sha)
                raw=zipfile.read(f"assets/quran/{filename}")
                if hashlib.sha256(raw).hexdigest()!=sha:
                    raise PublisherApkError(f"Unexpected APK pixel bytes at {n}")
                original = source / filename
                if original.is_symlink() or not original.is_file():
                    raise PublisherApkError(f"Source page missing or symlinked: {n}")
                with original.open("rb") as stream:
                    if hashlib.file_digest(stream, "sha256").hexdigest()!=sha:
                        raise PublisherApkError(f"Unexpected staged page at {n}")
        return {"status":"PUBLISHER_ORIGINAL_BYTES_EXACT_APK",
                "pages_checked":count,"source_integrity_verified":True,
                "institutional_certification":False}
    except (OSError,BadZipFile,KeyError,ValueError,TypeError) as exc:
        if isinstance(exc,PublisherApkError): raise
        raise PublisherApkError(f"Invalid publisher-source APK or artwork: {exc}") from exc

if __name__=="__main__":
    cli=argparse.ArgumentParser()
    cli.add_argument("--apk",type=Path,required=True)
    cli.add_argument("--source",type=Path,required=True)
    opts=cli.parse_args()
    print(json.dumps(audit_publisher_apk(opts.apk,opts.source),indent=2))
