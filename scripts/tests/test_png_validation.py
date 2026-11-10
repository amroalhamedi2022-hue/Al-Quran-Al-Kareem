import struct
import sys
import tempfile
import unittest
import zlib
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from validate_png import validate_png, PngValidationError

def chunk(kind, data):
    return struct.pack(">I",len(data))+kind+data+struct.pack(">I",zlib.crc32(kind+data)&0xffffffff)

def sample():
    header=struct.pack(">IIBBBBB",1,1,8,2,0,0,0)
    return b"\x89PNG\r\n\x1a\n"+chunk(b"IHDR",header)+chunk(b"IDAT",zlib.compress(b"\x00\xff\xff\xff"))+chunk(b"IEND",b"")

class PngGuards(unittest.TestCase):
    def check(self, data):
        with tempfile.TemporaryDirectory() as directory:
            path=Path(directory)/"test.png"
            path.write_bytes(data)
            return validate_png(path)
    def test_valid_png(self):
        self.assertEqual(self.check(sample()),(1,1))
    def test_fake_png(self):
        with self.assertRaises(PngValidationError):
            self.check(b"\x89PNG\r\n\x1a\nFAKE")
    def test_crc_corruption(self):
        blob=bytearray(sample()); blob[22]^=1
        with self.assertRaisesRegex(PngValidationError,"CRC"):
            self.check(bytes(blob))
    def test_truncated(self):
        with self.assertRaises(PngValidationError):
            self.check(sample()[:-8])
    def test_trailing_bytes(self):
        with self.assertRaisesRegex(PngValidationError,"Trailing"):
            self.check(sample()+b"extra")
    def test_animation_rejected(self):
        blob=sample()
        with self.assertRaisesRegex(PngValidationError,"Animated"):
            self.check(blob[:33]+chunk(b"acTL",struct.pack(">II",2,0))+blob[33:])

if __name__=="__main__":
    unittest.main()
