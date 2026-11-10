"""Validate PNG chunk framing and CRC before approving Quran page packs.

Structural checks only; this does not attest to Quran text or edition correctness.
"""
from pathlib import Path
import struct
import zlib

SIGNATURE = b"\x89PNG\r\n\x1a\n"

class PngValidationError(ValueError):
    pass

def validate_png(path: Path) -> tuple[int, int]:
    with Path(path).open("rb") as stream:
        if stream.read(8) != SIGNATURE:
            raise PngValidationError("Invalid PNG signature")
        seen_ihdr = seen_idat = False
        width = height = 0
        count = 0
        while True:
            header = stream.read(8)
            if len(header) != 8:
                raise PngValidationError("Missing IEND or truncated header")
            size, kind = struct.unpack(">I4s", header)
            count += 1
            if count > 100000 or size > 256 * 1024 * 1024:
                raise PngValidationError("Unreasonable chunk")
            if not all(65 <= byte <= 90 or 97 <= byte <= 122 for byte in kind):
                raise PngValidationError("Invalid chunk type")
            data = stream.read(size)
            crc = stream.read(4)
            if len(data) != size or len(crc) != 4:
                raise PngValidationError("Truncated chunk")
            if zlib.crc32(kind + data) & 0xffffffff != struct.unpack(">I", crc)[0]:
                raise PngValidationError("PNG CRC mismatch")
            if not seen_ihdr:
                if kind != b"IHDR" or size != 13:
                    raise PngValidationError("IHDR must be first")
                width, height, depth, color, comp, filt, interlace = struct.unpack(">IIBBBBB", data)
                valid_depths = {0:(1,2,4,8,16),2:(8,16),3:(1,2,4,8),4:(8,16),6:(8,16)}
                if not (0 < width <= 20000 and 0 < height <= 20000):
                    raise PngValidationError("Invalid image dimensions")
                if color not in valid_depths or depth not in valid_depths[color]:
                    raise PngValidationError("Invalid PNG color format")
                if comp != 0 or filt != 0 or interlace not in (0,1):
                    raise PngValidationError("Invalid PNG encoding")
                seen_ihdr = True
            elif kind == b"IHDR":
                raise PngValidationError("Duplicate IHDR")
            elif kind == b"acTL":
                raise PngValidationError("Animated PNG prohibited")
            elif kind == b"IDAT":
                seen_idat = True
            elif kind == b"IEND":
                if size != 0 or not seen_idat:
                    raise PngValidationError("Invalid IEND or missing IDAT")
                if stream.read(1):
                    raise PngValidationError("Trailing bytes after IEND")
                return width, height
            elif kind[0] & 0x20 == 0 and kind != b"PLTE":
                raise PngValidationError("Unknown critical chunk")
