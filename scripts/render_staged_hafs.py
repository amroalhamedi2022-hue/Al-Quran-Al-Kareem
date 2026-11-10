#!/usr/bin/env python3
"""Render SHA-verified original AI stage for HUMAN REVIEW ONLY, not release.

Retains original AI artwork. Never creates approved_manifest.json.
"""
import argparse
import json
import shutil
import subprocess
from pathlib import Path
from validate_png import validate_png, PngValidationError
from stage_official_hafs import EXPECTED_SHA256, sha256_file

class RenderError(ValueError):
    pass

def render(stage_dir: Path, out_dir: Path, width: int = 1600, renderer: str = "inkscape"):
    if width < 800 or width > 5000:
        raise RenderError("Invalid raster width")
    try:
        metadata = json.loads((stage_dir / "SOURCE_STAGE_ONLY.json").read_text("utf-8"))
    except (OSError, ValueError) as exc:
        raise RenderError("Missing official staging report") from exc
    if (metadata.get("status") != "UNAPPROVED_SOURCE_STAGE_ONLY"
        or metadata.get("official_archive_sha256") != EXPECTED_SHA256
        or metadata.get("observed_archive_sha256") != EXPECTED_SHA256):
        raise RenderError("Stage has not matched pinned official archive")
    rows = metadata.get("pages")
    if not isinstance(rows, list) or len(rows) != 604:
        raise RenderError("Source stage is not exactly 604 pages")
    if shutil.which(renderer) is None:
        raise RenderError("Vector renderer unavailable")
    dest = out_dir.resolve()
    if dest.exists() and any(dest.iterdir()):
        raise RenderError("Output folder must be empty")
    dest.mkdir(parents=True, exist_ok=True)
    results = []
    try:
        for n, row in enumerate(rows, 1):
            name = f"{n:03d}"
            if row.get("page") != n or row.get("staged_file") != f"{name}.ai":
                raise RenderError(f"Stage page mismatch at {n}")
            source = stage_dir / f"{name}.ai"
            if not source.is_file() or source.is_symlink():
                raise RenderError(f"AI source missing at {n}")
            if sha256_file(source) != row.get("original_ai_sha256"):
                raise RenderError(f"AI source changed after staging at {n}")
            target = dest / f"{name}.png"
            proc = subprocess.run([renderer, str(source), f"--export-filename={target}",
                                   f"--export-width={width}"], timeout=120,
                                  capture_output=True, text=True)
            if proc.returncode != 0:
                raise RenderError(f"Render error at page {n}: {proc.stderr[-250:]}")
            try:
                w, h = validate_png(target)
            except (OSError, PngValidationError) as exc:
                raise RenderError(f"Invalid output PNG at page {n}: {exc}") from exc
            if w != width or h < 200:
                raise RenderError(f"Unexpected PNG dimensions for page {n}: {w}x{h}")
            results.append({"page": n, "file": target.name, "width": w, "height": h,
                            "sha256": sha256_file(target), "review_status": "PENDING"})
        report = {"status": "UNAPPROVED_RENDER_ONLY",
                  "source_sha256": EXPECTED_SHA256, "renderer": renderer,
                  "raster_width": width, "page_count": 604,
                  "warning": "NOT Quran certification. Independent visual review required for EACH page.",
                  "pages": results}
        (dest / "UNAPPROVED_RENDER_REPORT.json").write_text(
            json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
        return report
    except Exception:
        for file in dest.iterdir():
            if file.is_file():
                file.unlink()
        raise

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--stage-dir", type=Path, required=True)
    parser.add_argument("--out-dir", type=Path, required=True)
    parser.add_argument("--width", type=int, default=1600)
    args = parser.parse_args()
    report = render(args.stage_dir, args.out_dir, args.width)
    print(f"{report['page_count']} PNG pages staged only; ALL require independent review")
