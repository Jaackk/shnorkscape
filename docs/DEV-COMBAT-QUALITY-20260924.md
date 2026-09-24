# Developer combat quality pass - 24 September 2026

First checkpoint: read-only `;;abilityinfo <slot 1-14>` reports server cooldown/GCD/channel ticks, queue, effective ability, target, runtime refusal, equipment/level/resource gates and Revolution filters. The action bar retains its latest CS6570 arguments per structure. These are transport publication observations, **not Vulkan acknowledgements**. The snapshot does not publish packets or select abilities.

`;;resetcooldowns` is an explicit developer action: clears only the requesting player's ability/GCD timers and queue; refuses during a channel. `;;npcinfo <ID, partial name or audited symbol>` reports real production admission/refusal, authored/cache profile sources, timings, presentation and drop-row presence. NPC browser has an Inspect Combat Profile action. Existing Best loadouts, QA and disengage actions are discoverable under Combat.

Gameval search includes provenance with strict type tokens. Exact command/alias matches rank before incidental description substrings (for example DM versus admission).

Focused combat, console and Gameval tests pass, including snapshot no-publication and second-player cooldown isolation. Native UI remains LIVE TEST PENDING. No deployment or restart.

The previous `dist/combat-live-successor-world-20260924` stage is preserved. Pre-edit source verified on GitHub at eead39b; runtime/saves and installer metadata backed up under backups/pre-edit-20260924-174830-246.
