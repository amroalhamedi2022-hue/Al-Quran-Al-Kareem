#!/usr/bin/env python3
"""Audit the actual DEBUG APK's embedded unapproved original publisher pages.

Matches PNG bytes and conversion report against the staged SHA-checked source.
Never grants human certification or final-release permission.
"""
import argparse
import hashlib
import json
from pathlib import Path
from zipfile import ZipFile, BadZipFile

class PreviewApkError(ValueError):
    pass

def audit_preview_apk(apk: Path, source: Path, count=604):
    try:
        report_raw = (source / "source_preview_manifest.json").read_bytes()
        manifest = json.loads(report_raw)
        pages = manifest["pages"]
        if manifest.get("status") != "UNAPPROVED_RENDER_ONLY" or manifest.get("page_count") != count or len(pages) != count:
            raise PreviewApkError("Missing or falsely approved development source report")
        with ZipFile(apk) as zipfile:
            actual = zipfile.namelist()
            embedded = {x for x in actual if x.startswith("assets/quran/") and not x.endswith("/")}
            expected = {f"assets/quran/{n:03d}.png" for n in range(1,count+1)}
            expected.add("assets/quran/source_preview_manifest.json")
            if embedded != expected or len(actual) != len(set(actual)):
                raise PreviewApkError("Missing/extra/duplicated source assets inside APK")
            if zipfile.read("assets/quran/source_preview_manifest.json") != report_raw:
                raise PreviewApkError("Conversion manifest bytes modified inside APK")
            seen=set()
            for n,row in enumerate(pages,1):
                filename=f"{n:03d}.png"
                if row.get("page")!=n or row.get("file")!=filename or row.get("review_status")!="PENDING":
                    raise PreviewApkError(f"Untrusted source page mapping: {n}")
                sha=row.get("sha256")
                if not isinstance(sha,str) or sha in seen:
                    raise PreviewApkError(f"Missing/duplicate source digest at {n}")
                seen.add(sha)
                raw=zipfile.read(f"assets/quran/{filename}")
                if hashlib.sha256(raw).hexdigest()!=sha:
                    raise PreviewApkError(f"Unexpected APK pixel bytes at {n}")
                if hashlib.sha256((source/filename).read_bytes()).hexdigest()!=sha:
                    raise PreviewApkError(f"Unexpected staged page at {n}")
        return {"status":"PREVIEW_ORIGINAL_BYTES_EXACT_APK",
                "pages_checked":count,"release_allowed":False,
                "human_quran_approval":False}
    except (OSError,BadZipFile,KeyError,ValueError,TypeError) as exc:
        if isinstance(exc,PreviewApkError): raise
        raise PreviewApkError(f"Invalid development APK or source: {exc}") from exc

if __name__=="__main__":
    cli=argparse.ArgumentParser()
    cli.add_argument("--apk",type=Path,required=True)
    cli.add_argument("--source",type=Path,required=True)
    opts=cli.parse_args()
    print(json.dumps(audit_preview_apk(opts.apk,opts.source),indent=2))
