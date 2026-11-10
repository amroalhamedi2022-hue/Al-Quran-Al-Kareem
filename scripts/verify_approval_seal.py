#!/usr/bin/env python3
"""Independent manifest seal gate: fail release when unreviewed/unpinned.

This checks metadata provenance, not Quranic text. Human page-by-page checks
are still mandatory. Pin SHA separately in Android source after review.
"""
import argparse
import hashlib
import json
import re
from pathlib import Path

SEAL = re.compile(r'private\s+const\s+val\s+INDEPENDENTLY_APPROVED_MANIFEST_SHA256\s*=\s*"([^"]*)"')

class ApprovalError(ValueError):
    pass

def verify_seal(kotlin_source: Path, manifest: Path):
    if not kotlin_source.is_file() or not manifest.is_file():
        raise ApprovalError("Approval source or manifest absent")
    found = SEAL.search(kotlin_source.read_text(encoding="utf-8"))
    if not found:
        raise ApprovalError("No approval manifest pin in Kotlin source")
    pinned = found.group(1)
    if not re.fullmatch(r"[a-f0-9]{64}", pinned):
        raise ApprovalError("Independent approval hash not set")
    raw = manifest.read_bytes()
    actual = hashlib.sha256(raw).hexdigest()
    if actual != pinned:
        raise ApprovalError("Approved manifest is not identical to independent pinned reference")
    try:
        metadata = json.loads(raw)
    except ValueError as exc:
        raise ApprovalError("Invalid manifest JSON") from exc
    people = metadata.get("approved_by", [])
    if not isinstance(people, list) or len(set(str(x).strip() for x in people if x)) < 2:
        raise ApprovalError("No two independent reviewers recorded")
    if metadata.get("rights_reviewed") is not True:
        raise ApprovalError("Rights not reviewed")
    pages = metadata.get("pages")
    if not isinstance(pages, list) or len(pages) != 604:
        raise ApprovalError("Must approve exactly 604 pages")
    return actual

if __name__ == "__main__":
    p = argparse.ArgumentParser()
    p.add_argument("--kotlin", type=Path, required=True)
    p.add_argument("--manifest", type=Path, required=True)
    args = p.parse_args()
    print("Approval seal validated", verify_seal(args.kotlin, args.manifest))
