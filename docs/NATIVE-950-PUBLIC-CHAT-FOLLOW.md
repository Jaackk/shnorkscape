# Native public chat and Follow (2026-09-22)

Baseline: e7d5f03. Ordinary decoded public text was intentionally discarded in
Native950Interactions. PlayerAction was likewise unhandled. These were missing
server routes, not guest authentication or LAN packet loss.

Public text now uses the existing verified PLAYER_INFO force-talk mask 0x400000,
flags bit 0, for overhead plus chatbox type 2. Script/NPC overhead text keeps flag
0. Undercut's world processChat/update-mask architecture supports this approach;
the exact 950 mask parser/encoder is authority. Messages are bounded, sanitized,
mute checked and limited to one accepted message per world tick. Viewer ignore
and public filters suppress both text destinations. A filtered recipient gets an
empty overhead-only block so the shared declared mask remains structurally valid.
No raw public text is logged by this path. Colour/effect prefixes are not rendered
by this plain-text path.

SET_PLAYER_OP is independently verified against exact 950 parser 0x14010f730
(original rs2client.exe SHA256 fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36),
not just the candidate opcode table. Opcode 99 / VarByte consumes priority
128-minus-byte, NUL CP1252 label, negated one-based slot, BE short-add cursor.
The parser accepts slots 1..8 and stores the priority via a zero comparison.
Undercut rev950 SetPlayerOp matches these transforms. Follow uses wire slot 4
(reference zero-based slot 3), decoded player action 4. It reuses PlayerFollow's
collision-aware route and action lifecycle after validation/cancelling prior activity.
Walking replaces Follow; finished/dead/different-plane/distant targets stop it.
Follow cannot start while the exit/editor modal owns world input.

Trade is NOT enabled: its 910 interface and exchange packet path remain unported.
Slot 5 is hidden, and stale slot-5 clicks receive an unavailable response without
touching containers. A safe two-party trade is separate work, not a menu-only fix.

Live acceptance required: two nearby players send plain messages both ways and
see overhead/chatbox once; Follow tracks movement, then stops on a ground click
or target logout. No workspace, authentication, saves or local launcher changes.

Validation: full engine suite 1,442 tests, zero failures/errors, two optional
evidence-table skips. Candidate SHA256:
473527e4491d55ebac30bf8bbd6707dbb50d078cf47f389efbdbd3f57759c3dd.
At the user's request, the running server is NOT restarted or hot-replaced.
The candidate is staged in dist/chat-follow-update. After closing clients and
running Stop.cmd, run Apply Chat and Follow Update.cmd, then normal Play.cmd.
The installer hash-checks, refuses a running Java/client process, snapshots the
old engine and player/workspace files, and changes only the engine JAR.
