# Developer Console fresh-open readiness correction

## Live failure and boundary

The 13:46-13:48 Bug Test (`session-20260924-134638-444-jaxa`) records
`awaiting-native-ready` after opening1448 but no `native-ready` or ready string
packet. The screenshot shows the untouched Customisations frame and blank body.
The previous startup successor is **LIVE FAIL**, including after `;;god`.

The later `;;cs 8286` probe closed the console because the command dispatcher
closes developer interfaces before non-passive commands. It cannot establish
whether the native callback fired. Do not repeat this probe as a lifecycle test.

## Concrete contract defect and correction

The bridge stored its marker on1477:713, a type0 container, then used IF_GETTEXT.
In the exact950 executable (SHA256 fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36),
IF_GETTEXT handler1401f3230 reaches1401f21a0; the text-capability lookup1401f1ce0
rejects container types and the getter returns an empty string. The previous
unit test incorrectly modeled every component as accepting readable text.
Additionally, server IF_SETTEXT uses deferred changes (parser140107690), so
sending that packet before a varc change is not a synchronous marker contract.

CS21130 now selects hidden native type4 text1448:14 with IF_FIND and writes it
synchronously with CC_SETTEXT before the server publishes varc2911. CS8286's
scoped guard reads that text only if the component exists. Close and placement
clear it through the same script before unmounting. The loading-panel builder
CS9525 creates its spinner children without changing the static text child.

All original20 instructions of8286 reconstruct byte-for-byte to its original
5bd6296b... digest; only the return boundary is extended with the guarded ready
notification. No delay, forced early render, diagnostic redraw requirement or
backend rewrite was added. Both Settings and console verification pins change
together. Existing title/tab takeover runs only after the ready round trip.

## Verification and limitations

Focused Java settings/console tests and six Python script/pin checks pass.
The production JS5 reader validates seven replacements and new helper21130,
including lengths, CRCs, versions and unchanged unrelated references. Full
startup preflight passes with verification enabled against the rebuilt jar and
an isolated cache overlay containing the successor; live cache is untouched.
Final regression and installer results are in the validation report/handoff.

**AUTOMATED VERIFIED; LIVE TEST PENDING.** This fixes a concrete broken marker
contract, but physical Vulkan fresh-open rendering has not yet been accepted.
Prior Heal/search/placement acceptance applies only after the earlier diagnostic
redraw. Existing native800x600/pagination/metadata-preview visual limitations
remain; the full mockup is not claimed complete. The three user-deferred bugs
(cracker Pull, bar drag-off, equipment binding selection) remain deferred.

## Live check after applying

1. Fresh login, `;;dev`: Developer Console title/content immediately visible,
   Customisations tabs hidden, Heal -> Execute and search clickable.
2. Close/reopen twice, then NPC search/select -> Place -> chosen tile.
3. Close, open bank/items, verify ability keybinds still work.

No extra command or native tab click should be required. Bug Test should show
`awaiting-native-ready` followed by `native-ready` on each fresh open. Do not
restart or install automatically; the user applies through Apply Staged Update.cmd.
