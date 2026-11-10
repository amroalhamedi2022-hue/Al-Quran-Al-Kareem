#!/usr/bin/env python3
"""Static source-manifest guard. Also inspect merged manifest and final APK later."""
import argparse
from pathlib import Path
from xml.etree import ElementTree as ET

ANDROID = "{http://schemas.android.com/apk/res/android}"
BANNED = ("INTERNET", "ACCESS_NETWORK_STATE", "CHANGE_NETWORK_STATE",
          "ACCESS_WIFI_STATE", "CHANGE_WIFI_STATE", "NEARBY_WIFI_DEVICES")
SOURCE_BANNED = ("android.permission.INTERNET","OkHttpClient","Retrofit.Builder",
                 "HttpURLConnection","java.net.Socket","FirebaseAnalytics",
                 "FirebaseCrashlytics","com.google.android.gms.ads","ktor.client")

class OfflineError(ValueError):
    pass

def check_manifest(path: Path):
    root=ET.parse(path).getroot()
    if root.tag != "manifest":
        raise OfflineError("Not an Android manifest")
    for elem in root.iter():
        tag=elem.tag.split("}")[-1]
        if tag.startswith("uses-permission"):
            permission=elem.get(ANDROID+"name","")
            if any(x in permission.upper() for x in BANNED):
                raise OfflineError(f"Forbidden permission: {permission}")
        if tag=="application" and elem.get(ANDROID+"usesCleartextTraffic")=="true":
            raise OfflineError("Cleartext network traffic enabled")
    return True

def check_sources(root: Path):
    count=0
    for file in root.rglob("*"):
        if file.suffix.lower() not in (".kt",".java",".kts") or "/build/" in str(file):
            continue
        text=file.read_text(encoding="utf-8")
        count += 1
        for pattern in SOURCE_BANNED:
            if pattern in text:
                raise OfflineError(f"Forbidden network identifier {pattern} in {file}")
    if not count:
        raise OfflineError("No source files checked")
    return count

if __name__ == "__main__":
    p=argparse.ArgumentParser()
    p.add_argument("--manifest",type=Path,required=True)
    p.add_argument("--sources",type=Path,required=True)
    a=p.parse_args()
    check_manifest(a.manifest)
    print("No banned network permission; scanned",check_sources(a.sources),"source files")
