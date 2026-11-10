#!/usr/bin/env python3
"""Build an OFFLINE human review gallery; NEVER certifies Quranic correctness.

Uses converted PNGs untouched, including their original alpha channel.
The HTML does not contact remote servers or produce an approved Quran manifest.
"""
import argparse
import json
import re
from pathlib import Path

OFFICIAL_SHA = "280c5d71ca16aaeeb3a343be1b92c76fa6df71d0671e202c026fde12402d9eef"

def make_gallery(directory: Path, count: int = 604) -> Path:
    manifest = directory / "UNAPPROVED_RENDER_REPORT.json"
    if not manifest.is_file():
        raise ValueError("Missing provenance report")
    data = json.loads(manifest.read_text("utf-8"))
    rows = data.get("pages", [])
    if data.get("status") != "UNAPPROVED_RENDER_ONLY" or data.get("source_sha256") != OFFICIAL_SHA:
        raise ValueError("Unapproved publisher source report not verified")
    if data.get("page_count") != count or len(rows) != count:
        raise ValueError("Cannot prepare an incomplete page review")
    page_info = []
    for n, row in enumerate(rows, 1):
        filename = f"{n:03d}.png"
        if (row.get("page") != n or row.get("file") != filename
                or row.get("review_status") != "PENDING"
                or not re.fullmatch("[0-9a-f]{64}", row.get("sha256", ""))):
            raise ValueError(f"Bad unapproved page at {n}")
        if not (directory / filename).is_file():
            raise ValueError(f"Page {n} is missing")
        page_info.append({"page": n, "filename": filename, "sha256": row["sha256"]})
    payload = json.dumps(page_info, ensure_ascii=False, separators=(",", ":")).replace("<", "\\u003c")
    result = directory / "REVIEW_OFFLINE_UNAPPROVED.html"
    result.write_text(HTML.replace("__REVIEW_PAGE_METADATA__", payload), encoding="utf-8")
    return result

HTML = r"""<!doctype html>
<html lang="ar" dir="rtl">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta http-equiv="Content-Security-Policy" content="default-src 'none'; img-src 'self' data: file:; script-src 'unsafe-inline'; style-src 'unsafe-inline';">
<title>مراجعة المصحف — غير معتمد للنشر</title>
<style>
:root{font-family:system-ui,"Noto Naskh Arabic",sans-serif;color:#262b26;background:#ebeae3}
*{box-sizing:border-box}body{margin:0}header{background:#26362c;color:#fff;padding:10px 20px;display:flex;gap:16px;align-items:center;justify-content:space-between;flex-wrap:wrap}
header strong{font-size:18px}header small{color:#dfebdc}.danger{background:#fff0df;border:2px solid #b75b17;padding:10px 16px;margin:12px auto;max-width:1500px;font-weight:bold;text-align:center}
main{display:grid;grid-template-columns:minmax(230px,320px) minmax(0,1fr);gap:14px;max-width:1500px;margin:auto;padding:0 12px 16px}
aside{background:#fff;border:1px solid #c6cec2;border-radius:10px;padding:18px;height:max-content;display:grid;gap:12px}
label{font-weight:600;font-size:14px}input,textarea,button{font:inherit}input[type=text],input[type=number],textarea{border:1px solid #abbbaa;border-radius:8px;padding:8px;width:100%}textarea{min-height:100px;resize:vertical}
button{background:#234c3e;color:white;border:0;border-radius:8px;padding:10px;cursor:pointer}button:hover{background:#1a382e}button:focus-visible,input:focus-visible,textarea:focus-visible{outline:3px solid #c48b2a}
.secondary{background:#5c655e}.actions{display:flex;flex-wrap:wrap;gap:8px}.actions button{flex:1}
.pageView{min-height:70vh;display:flex;justify-content:center;align-items:flex-start;background:#d1d0c9;padding:14px;border-radius:10px;overflow:auto}
.pageView img{display:block;max-width:100%;width:auto;max-height:88vh;object-fit:contain;background:white;box-shadow:0 8px 22px #0002;user-select:none}
.pageView.zoomed img{max-height:none;max-width:none;width:1600px}
.warning{color:#8b3016;font-size:13px}.progress{font-weight:700;font-variant-numeric:tabular-nums}.flag{display:flex;gap:10px;align-items:center}
@media(max-width:850px){main{grid-template-columns:1fr}.pageView img{max-height:75vh}.pageView{min-height:45vh}}
</style>
</head>
<body>
<header><strong>القرآن الكريم | أداة مراجعة الصفحات</strong><small>Offline • صور المصدر دون تعديل • لا تمنح اعتمادًا تلقائيًا</small></header>
<div class="danger">⚠️ هذه صور قيد المراجعة، وليست إصدارًا معتمدًا. التأكد من كل حرف وحركة وعلامة وموضع مقارنة بالأصل شرط قبل أي نشر.</div>
<main>
<aside>
<label for="reviewer">اسم المراجع (للإقرار الشخصي فقط، لا للتحقق من الهوية)</label>
<input id="reviewer" type="text" autocomplete="off" placeholder="اكتب اسمك">
<div class="progress" id="progress">0 / 604 صفحة تمت مراجعتها</div>
<div class="actions">
<button id="previous">الصفحة السابقة ←</button>
<button id="next">→ الصفحة التالية</button>
</div>
<label for="jump">انتقل إلى رقم صفحة</label><input id="jump" type="number" min="1" max="604" value="1">
<button id="zoom" class="secondary">تكبير / تصغير</button>
<div class="flag"><input id="checked" type="checkbox"><label for="checked">قارنتُ هذه الصفحة مع الأصل وتحققتُ من كل عناصرها</label></div>
<div class="flag"><input id="flagged" type="checkbox"><label for="flagged">توجد مشكلة يجب إصلاحها</label></div>
<label for="notes">ملاحظات الصفحة</label><textarea id="notes" placeholder="سجّل أي فرق أو قصّ أو حركة أو علامة أو عيب في العرض…"></textarea>
<p class="warning">العلامة ✓ تعني إفادة هذا المراجع فقط، ولا تعني تصديقًا شرعيًا أو إداريًا. يجب وجود مراجعتين مستقلتين قبل إعداد أي بيان اعتماد.</p>
<div class="actions"><button id="export">تصدير تقدم المراجع JSON</button><button class="secondary" id="importButton">استيراد تقرير سابق</button></div>
<input id="importFile" type="file" accept=".json,application/json" hidden>
<small id="saveStatus">البيانات تحفظ محليًا قدر الإمكان؛ صدّر تقريرًا دوريًا حتى لا تفقد عملك.</small>
</aside>
<section class="pageView" id="pageView"><img id="pageImage" src="001.png" alt="الصفحة 1 للمراجعة"></section>
</main>
<script>
"use strict";
const PAGES=__REVIEW_PAGE_METADATA__;
const ID="quran-604-review:"+PAGES[0].sha256+":"+PAGES[PAGES.length-1].sha256;
const $=id=>document.getElementById(id);
let position=1,entries={},reviewer="";
function loadLocal(){try{const raw=localStorage.getItem(ID);if(raw){const s=JSON.parse(raw);reviewer=s.reviewer||"";entries=s.entries||{};}}catch(e){$("saveStatus").textContent="التخزين المحلي غير متاح؛ احفظ تقرير JSON يدويًا.";}}
function saveLocal(){try{localStorage.setItem(ID,JSON.stringify({reviewer,entries}));}catch(e){$("saveStatus").textContent="لم يُحفظ محليًا؛ استخدم زر التصدير.";}}
function key(n){return String(n);}
function persist(){
 reviewer=$("reviewer").value.trim();
 entries[key(position)]={checked:$("checked").checked,flagged:$("flagged").checked,notes:$("notes").value,
   lastUpdatedUtc:new Date().toISOString(),sha256:PAGES[position-1].sha256};
 saveLocal();updateProgress();
}
function updateProgress(){
 const confirmed=Object.values(entries).filter(e=>e.checked===true && e.flagged!==true).length;
 const flagged=Object.values(entries).filter(e=>e.flagged===true).length;
 $("progress").textContent=confirmed+" / "+PAGES.length+" صفحة تمت مراجعتها؛ "+flagged+" صفحة عليها ملاحظات";
}
function navigate(n){
 position=Math.min(PAGES.length,Math.max(1,Number(n)||1));
 const r=entries[key(position)]||{};
 $("jump").value=position;$("checked").checked=!!r.checked;$("flagged").checked=!!r.flagged;
 $("notes").value=r.notes||"";$("pageImage").src=PAGES[position-1].filename;
 $("pageImage").alt="الصفحة "+position+" من المصحف — قيد المراجعة";
 updateProgress();
}
$("pageImage").addEventListener("error",()=>{$("saveStatus").textContent="خطأ: ملف الصفحة غير موجود في المجلد.";});
$("reviewer").addEventListener("change",persist);
["checked","flagged"].forEach(id=>$(id).addEventListener("change",persist));
$("notes").addEventListener("input",persist);
$("previous").addEventListener("click",()=>navigate(position-1));
$("next").addEventListener("click",()=>navigate(position+1));
$("jump").addEventListener("change",()=>navigate($("jump").value));
$("zoom").addEventListener("click",()=>$("pageView").classList.toggle("zoomed"));
document.addEventListener("keydown",event=>{
 if(["INPUT","TEXTAREA"].includes(document.activeElement.tagName))return;
 if(event.key==="ArrowLeft"){navigate(position+1);event.preventDefault();}
 if(event.key==="ArrowRight"){navigate(position-1);event.preventDefault();}
});
$("export").addEventListener("click",()=>{
 persist();
 const file={status:"HUMAN_REVIEW_RECORD_ONLY_NOT_APPROVAL",product:"القرآن الكريم",source_edition:"Madinah Hafs 1441H",
   source_sha256:"280c5d71ca16aaeeb3a343be1b92c76fa6df71d0671e202c026fde12402d9eef",
   exportedUtc:new Date().toISOString(),reviewer,expectedPages:604,
   records:PAGES.map(p=>({page:p.page,sha256:p.sha256,...(entries[key(p.page)]||{checked:false,flagged:false,notes:""})}))};
 const blob=new Blob([JSON.stringify(file,null,2)],{type:"application/json"});
 const url=URL.createObjectURL(blob);const link=document.createElement("a");link.href=url;link.download="QURAN_REVIEW_UNAPPROVED_"+Date.now()+".json";link.click();setTimeout(()=>URL.revokeObjectURL(url),2000);
});
$("importButton").addEventListener("click",()=>$("importFile").click());
$("importFile").addEventListener("change",async event=>{
 const file=event.target.files[0];if(!file)return;
 try{
  const imported=JSON.parse(await file.text());
  if(imported.status!=="HUMAN_REVIEW_RECORD_ONLY_NOT_APPROVAL" || imported.expectedPages!==604 ||
     imported.source_sha256!=="280c5d71ca16aaeeb3a343be1b92c76fa6df71d0671e202c026fde12402d9eef" ||
     !Array.isArray(imported.records) || imported.records.length!==PAGES.length)throw Error("بيانات غير مطابقة للمصحف");
  const restored={};
  for(let index=0;index<PAGES.length;index++){
    const record=imported.records[index], page=PAGES[index];
    if(record.page!==page.page||record.sha256!==page.sha256)throw Error("بصمات الصفحات غير مطابقة");
    restored[key(page.page)]={checked:record.checked===true,flagged:record.flagged===true,notes:String(record.notes||"").slice(0,10000),sha256:record.sha256};
  }
  reviewer=String(imported.reviewer||"").slice(0,150);entries=restored;
  $("reviewer").value=reviewer;saveLocal();navigate(position);
 }catch(e){alert("تعذر استيراد تقرير المراجعة: "+e.message);}
 event.target.value="";
});
loadLocal();$("reviewer").value=reviewer;navigate(1);
</script>
</body>
</html>"""

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--pages-dir", type=Path, required=True)
    args = parser.parse_args()
    print("UNAPPROVED OFFLINE HUMAN REVIEW GALLERY:", make_gallery(args.pages_dir))
