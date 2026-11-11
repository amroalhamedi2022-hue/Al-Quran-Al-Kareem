#!/usr/bin/env python3
"""Audit the signed release APK's embedded verified original publisher artwork.

Matches PNG bytes and conversion report against the staged SHA-checked source.
Never grants human certification or final-release permission.
"""
import argparse
import hashlib
import json
from pathlib import Path
from zipfile import ZipFile, BadZipFile

class PublisherApkError(ValueError):
    pass

def audit_publisher_apk(apk: Path, source: Path, count=604):
    try:
        report_raw = (source / "source_provenance_manifest.json").read_bytes()
        manifest = json.loads(report_raw)
        pages = manifest["pages"]
        if manifest.get("status") != "UNAPPROVED_RENDER_ONLY" or manifest.get("page_count") != count or len(pages) != count:
            raise PublisherApkError("Missing or falsely approved development source report")
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
                if row.get("page")!=n or row.get("file")!=filename or row.get("review_status")!="PENDING":
                    raise PublisherApkError(f"Untrusted source page mapping: {n}")
                sha=row.get("sha256")
                if not isinstance(sha,str) or sha in seen:
                    raise PublisherApkError(f"Missing/duplicate source digest at {n}")
                seen.add(sha)
                raw=zipfile.read(f"assets/quran/{filename}")
                if hashlib.sha256(raw).hexdigest()!=sha:
                    raise PublisherApkError(f"Unexpected APK pixel bytes at {n}")
                if hashlib.sha256((source/filename).read_bytes()).hexdigest()!=sha:
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
