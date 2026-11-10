#!/usr/bin/env python3
"""Check *unchanged official upstream Hafs vector archive* against KFGQPC hash.
Does not approve any derivative page files or constitute scholarly review.
"""
import argparse
import hashlib
from pathlib import Path

OFFICIAL_SHA256 = "280c5d71ca16aaeeb3a343be1b92c76fa6df71d0671e202c026fde12402d9eef"

class UpstreamError(ValueError):
    pass

def verify_official_hafs_archive(path: Path):
    if not path.is_file() or path.is_symlink():
        raise UpstreamError("Official source archive not found or symlinked")
    sha = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(4 * 1024 * 1024), b""):
            sha.update(chunk)
    digest = sha.hexdigest()
    if digest != OFFICIAL_SHA256:
        raise UpstreamError(f"Untrusted source SHA-256: {digest}")
    return digest

if __name__ == "__main__":
    p=argparse.ArgumentParser(description="Verify KFGQPC official source archive (Hafs)")
    p.add_argument("archive",type=Path)
    a=p.parse_args()
    print("PASS:",verify_official_hafs_archive(a.archive))
