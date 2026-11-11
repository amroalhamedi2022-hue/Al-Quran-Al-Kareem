# NON-NEGOTIABLE: Quran preservation & release protection

لا يجوز تغيير كلمة، حرف، حركة، علامة، رقم آية، موضع أو ترتيب في نص القرآن الكريم.

## Trust boundaries
- Source of truth: a verified, identified official Hafs/Madinah 604-page edition with documented redistribution terms.
- Original vendor assets are unchanged and hashed externally; Android uses full byte-for-byte verified source PNGs, never AI/OCR/retyped Quran.
- The source-provenance manifest lists 001.png through 604.png and every PNG SHA-256; the upstream ZIP must match the published publisher SHA-256. Packaging and runtime re-hash all 604 PNGs. Do not describe this metadata as a scholarly approval.
- The published upstream archive checksum and complete PNG checks are non-negotiable. External institutional certification has been paused at the owner's request. Byte matching proves fidelity to those exported files, not infallibility of their graphical conversion.
- All 604 pages are machine-audited for source/order/hash/borders, but NOT officially certified for letter-by-letter correctness; do not promise zero graphic defects. No gradients, negative filters or recoloring of Quran ink in idle reading.
- The page-curl backside is the appropriate source page, not mirrored Arabic text.
- Self-reveal: 15-step non-semantic paper mask hides/shows original bitmap without edits. Exact verse-by-verse mode remains disabled until reliable independently verified verse coordinates exist.
- Hash check at startup is defense-in-depth; original archive SHA-256 and a stable Android release signature provide technical provenance (not a religious certificate).
- Final signed release must verify *actual APK contents*, not just source files. No partial Quran accepted; same package ID and signer required for every update.
- Fail closed: if any file is missing/tampered, show explicit "المصحف غير متاح" state. Do not silently replace with any text.

## Manual signoff checklist
For page 001–604: no cropped text, no wrong verse, no altered diacritics, no missing page/line/marker, correct layout and page numbering; two reviewers document differences; reviewer may not sign their own transformation as sole evidence.

## Out of scope
The app never generates Quran verses from prompts or writes new Quran word strings in code; future interpretation requires new explicit scope after v1.
