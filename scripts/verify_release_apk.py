#!/usr/bin/env python3
"""Verify Quran page bytes actually embedded in Android APK match an independently
approved manifest. This does not substitute for aapt2 permission audit or human
Quran verification.
"""
import argparse
import hashlib
import json
from pathlib import Path
from zipfile import ZipFile, BadZipFile

class ApkIntegrityError(ValueError):
    pass

def verify_apk_assets(apk: Path, independent_manifest: Path, count: int = 604):
    try:
        original = independent_manifest.read_bytes()
        data = json.loads(original)
        if data.get("rights_reviewed") is not True:
            raise ApkIntegrityError("Source rights not reviewed")
        if data.get("riwaya") != "Hafs 'an Asim" or data.get("layout") != "Madinah 604-page":
            raise ApkIntegrityError("Incorrect approved edition")
        if not data.get("source_name") or not data.get("source_url"):
            raise ApkIntegrityError("Source provenance missing")
        reviewers = [v.strip() for v in data.get("approved_by",[]) if isinstance(v,str) and v.strip()]
        if len(set(reviewers)) < 2:
            raise ApkIntegrityError("No two independent reviewers")
        pages = data.get("pages")
        if not isinstance(pages,list) or len(pages)!=count:
            raise ApkIntegrityError(f"Expected {count} approved pages")
        with ZipFile(apk) as package:
            members=package.namelist()
            quran_names=[n for n in members if n.startswith("assets/quran/") and not n.endswith("/")]
            expected={"assets/quran/approved_manifest.json"}
            expected.update(f"assets/quran/{i:03d}.png" for i in range(1,count+1))
            if len(quran_names)!=len(set(quran_names)) or set(quran_names)!=expected:
                raise ApkIntegrityError("APK page assets missing, duplicated or unexpected")
            if package.read("assets/quran/approved_manifest.json") != original:
                raise ApkIntegrityError("Embedded manifest differs from independently approved manifest")
            for n,row in enumerate(pages,1):
                name=f"{n:03d}.png"
                if not isinstance(row,dict) or row.get("page")!=n or row.get("file")!=name:
                    raise ApkIntegrityError(f"Page index mismatch at {n}")
                sha=row.get("sha256","")
                if not isinstance(sha,str) or len(sha)!=64 or any(c not in "0123456789abcdef" for c in sha):
                    raise ApkIntegrityError(f"Bad approved hash at {n}")
                h=hashlib.sha256()
                with package.open(f"assets/quran/{name}") as stream:
                    for chunk in iter(lambda: stream.read(1024*1024),b""):
                        h.update(chunk)
                if h.hexdigest()!=sha:
                    raise ApkIntegrityError(f"APK page mismatch at {n}")
    except (OSError, ValueError, BadZipFile, KeyError) as exc:
        if isinstance(exc,ApkIntegrityError):
            raise
        raise ApkIntegrityError(f"Unable to validate final APK pages: {exc}") from exc
    return f"Verified {count} APK Quran page blobs against independent reviewed manifest"

if __name__ == "__main__":
    p=argparse.ArgumentParser()
    p.add_argument("--apk",type=Path,required=True)
    p.add_argument("--approved-manifest",type=Path,required=True)
    a=p.parse_args()
    print(verify_apk_assets(a.apk,a.approved_manifest))
