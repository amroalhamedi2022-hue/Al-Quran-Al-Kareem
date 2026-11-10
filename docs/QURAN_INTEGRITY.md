# NON-NEGOTIABLE: Quran preservation & release protection

لا يجوز تغيير كلمة، حرف، حركة، علامة، رقم آية، موضع أو ترتيب في نص القرآن الكريم.

## Trust boundaries
- Source of truth: a verified, identified official Hafs/Madinah 604-page edition with documented redistribution terms.
- Original vendor assets are stored unchanged and hashed externally. Rendering uses only independently reviewed page exports, never AI text/OCR/retyped Quran.
- The *page pack's* approved manifest lists 001.png through 604.png, source URL, riwaya, layout, rights review, two distinct human approvers and SHA-256 for each PNG.
- A generated manifest alone is NOT an attestation. Independent human check + separately pinned source checksum + signoff needed.
- All 604 pages reviewed before release; no gradients, negative filters or recoloring ink in baseline. Glass/mesh are applied outside the glyph content except geometric movement during turn.
- The page-curl backside is the appropriate source page, not mirrored Arabic text.
- Self-reveal: approved verse coordinates only; overlay hides/shows but never edits original pixels. Disable if unavailable.
- Hash check at startup is defense-in-depth; APK signature and independent reference provide provenance.
- Final release verifies *actual APK contents*, not just source files. No partial Quran accepted.
- Fail closed: if any file is missing/tampered, show explicit "المصحف غير متاح" state. Do not silently replace with any text.

## Manual signoff checklist
For page 001–604: no cropped text, no wrong verse, no altered diacritics, no missing page/line/marker, correct layout and page numbering; two reviewers document differences; reviewer may not sign their own transformation as sole evidence.

## Out of scope
The app never generates Quran verses from prompts or writes new Quran word strings in code; future interpretation requires new explicit scope after v1.
