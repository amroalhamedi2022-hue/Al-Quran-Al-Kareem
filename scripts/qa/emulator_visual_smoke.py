#!/usr/bin/env python3
"""Real Android emulator UI smoke-test using original 604-page publisher artwork.

Never fabricate Quran text and never call OCR. Compare captured on-device pixels
to the untouched source PNG page using feature matching and a robust homography.
Save all screenshots and comparison panels for actual visual review. No release
approval/certification claim is made by a computer-vision score.
"""
import json
import os
import subprocess
import time
from pathlib import Path

import cv2
import numpy as np
from PIL import Image, ImageDraw, ImageOps

ROOT=Path(__file__).resolve().parents[2]
SOURCE=Path(os.environ.get("QURAN_QA_SOURCE","app/src/main/assets/quran"))
OUT=Path(os.environ.get("QURAN_QA_OUTPUT","qa-visual-evidence"))
PKG="app.alquran.kareem"
OUT.mkdir(parents=True,exist_ok=True)

def adb(*args,timeout=45):
    command=["adb",*map(str,args)]
    r=subprocess.run(command,stdout=subprocess.PIPE,stderr=subprocess.PIPE,timeout=timeout)
    if r.returncode:
        raise RuntimeError(f"{' '.join(command)} failed: {r.stderr.decode('utf-8','replace')[-1200:]}")
    return r.stdout

def phone_settings():
    adb("shell","wm","size","1080x2400")
    adb("shell","wm","density","440")
    adb("shell","settings","put","system","accelerometer_rotation","0")
    adb("shell","settings","put","system","user_rotation","0")

def tablet_settings():
    adb("shell","wm","size","2560x1600")
    adb("shell","wm","density","240")
    adb("shell","settings","put","system","accelerometer_rotation","0")
    # A landscape-sized virtual display must be verified from an actual capture
    # rather than guessed from density or simulated screen width.
    adb("shell","settings","put","system","user_rotation","0")

def start_reader(timeout_seconds=540):
    adb("shell","am","force-stop",PKG)
    adb("shell","am","start","-n",PKG+"/.MainActivity")
    deadline=time.monotonic()+timeout_seconds
    while time.monotonic()<deadline:
        adb("shell","uiautomator","dump","/sdcard/quran-qa-view.xml",timeout=40)
        xml=adb("shell","cat","/sdcard/quran-qa-view.xml").decode("utf8","replace")
        if "التالي" in xml and "السابق" in xml:
            return xml
        if "ملفات المصحف المعتمدة غير مكتملة" in xml:
            raise RuntimeError("App rejected a supposedly valid 604-page source pack")
        time.sleep(8)
    raise RuntimeError("Real 604-page integrity check never displayed readable Quran toolbar")

def screenshot(label):
    path=OUT/(label+".png")
    path.write_bytes(adb("exec-out","screencap","-p",timeout=60))
    with Image.open(path) as test:
        test.verify()
    return path

def source_image(page):
    path=SOURCE/f"{page:03d}.png"
    with Image.open(path) as rgba:
        img=Image.new("RGBA",rgba.size,(255,255,255,255))
        img.alpha_composite(rgba.convert("RGBA"))
        return img.convert("RGB")

def compare_source_pixels(page,actual_screenshot,label):
    src=np.asarray(source_image(page).convert("L"))
    screenshot=np.asarray(Image.open(actual_screenshot).convert("L"))
    # Feature points survive normal Android Image(ContentScale.Fit) raster scale,
    # and allow detecting the exact source art within phone or tablet displays.
    orb=cv2.ORB_create(nfeatures=6500,fastThreshold=6,edgeThreshold=12)
    ref_kp,ref_desc=orb.detectAndCompute(src,None)
    rendered_kp,rendered_desc=orb.detectAndCompute(screenshot,None)
    if ref_desc is None or rendered_desc is None:
        raise AssertionError(f"{label}: cannot detect matching Quran page artwork")
    matches=cv2.BFMatcher(cv2.NORM_HAMMING).knnMatch(ref_desc,rendered_desc,k=2)
    good=[a for a,b in matches if a.distance < .83*b.distance]
    inliers=0
    if len(good)>=8:
        p=np.float32([ref_kp[m.queryIdx].pt for m in good]).reshape(-1,1,2)
        q=np.float32([rendered_kp[m.trainIdx].pt for m in good]).reshape(-1,1,2)
        h,mask=cv2.findHomography(p,q,cv2.RANSAC,5.0)
        if h is not None and mask is not None:
            inliers=int(mask.sum())
    info={"scenario":label,"quran_source_page":page,
          "matched_feature_pairs":len(good),"inlier_source_features":inliers,
          "screenshot_size":list(Image.open(actual_screenshot).size)}
    # A strict positive visual-source evidence gate, NOT a per-haraka audit.
    if len(good)<15 or inliers<10:
        raise AssertionError(f"Visual source comparison failed: {json.dumps(info)}")
    return info

def panel(reference_page,screenshot_path,name):
    ref=source_image(reference_page)
    shot=Image.open(screenshot_path).convert("RGB")
    target_h=850
    ref.thumbnail((760,target_h))
    shot.thumbnail((1100,target_h))
    canvas=Image.new("RGB",(ref.width+shot.width+48,max(ref.height,shot.height)+90),"#f4f4f4")
    draw=ImageDraw.Draw(canvas)
    draw.text((15,15),f"OFFICIAL ORIGINAL PAGE {reference_page:03d}",fill="black")
    draw.text((ref.width+34,15),f"ACTUAL EMULATOR: {name}",fill="black")
    canvas.paste(ref,(12,55))
    canvas.paste(shot,(ref.width+30,55))
    canvas.save(OUT/(name+"-source-vs-emulator.jpg"),quality=90)

def run():
    results=[]
    phone_settings()
    start_reader()
    time.sleep(3)
    shot=screenshot("phone-portrait-first-page")
    with Image.open(shot) as image:
        assert image.height>image.width, "Phone screenshot is not portrait"
    results.append(compare_source_pixels(1,shot,"phone-portrait-original-page-1"))
    panel(1,shot,"phone-portrait-page-1")

    # RTL right edge = NEXT. Let the real page-turn animation settle.
    adb("shell","input","tap","1035","900")
    time.sleep(6)
    shot=screenshot("phone-portrait-second-page")
    results.append(compare_source_pixels(2,shot,"phone-rtl-next-page-2"))
    panel(2,shot,"phone-rtl-next-page-2")

    tablet_settings()
    start_reader()
    time.sleep(3)
    shot=screenshot("tablet-landscape-two-page-spread")
    with Image.open(shot) as image:
        assert image.width>image.height, "Tablet emulator screenshot unexpectedly portrait"
    results.append(compare_source_pixels(1,shot,"tablet-two-page-right-page-1"))
    results.append(compare_source_pixels(2,shot,"tablet-two-page-left-page-2"))
    panel(2,shot,"tablet-two-page-spread")

    # Compare first and second page visual previews without new fake graphics.
    result={
        "test_status":"EMULATOR_SOURCE_PIXEL_MATCH_CHECKS_PASS",
        "app_id":PKG,
        "checked_cases":results,
        "notes":[
          "Real Android emulator screen captures from ORIGINAL publisher PNG assets",
          "OpenCV feature-homography comparison establishes printed-page art is present",
          "No OCR; no Quran verse text generated or changed",
          "This computer-vision smoke-test cannot certify every letter, vowel mark, or full curved page animation",
          "No user-installable debug APK is uploaded"
        ]
    }
    (OUT/"visual-comparison-report.json").write_text(
        json.dumps(result,ensure_ascii=False,indent=2),encoding="utf-8")
    print(json.dumps(result,indent=2))

if __name__=="__main__":
    try:run()
    except Exception:
        import traceback
        (OUT/"failure.txt").write_text(traceback.format_exc(),encoding="utf-8")
        raise
