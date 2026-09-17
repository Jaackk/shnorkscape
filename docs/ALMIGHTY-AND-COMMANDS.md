# Almighty and in-game command directory

`;;almighty` enables all six implemented testing modes: damage immunity, infinite prayer, infinite adrenaline/energy, infinite run energy, free native combat rune costs, and infinite ammunition. It restores vitals/energy once when enabled; consumption is guarded at the existing APIs, without a per-tick refill loop.

When all six modes are enabled, `;;almighty` disables all six. If only some are enabled, it enables the complete set. Individual commands remain available: `;;god`, `;;infprayer`, `;;infadren`, `;;infrun`, `;;infrunes`, `;;infammo`. `;;devstatus` shows all six states. All flags are session-only and cleared on logout. Saved skills/XP are not changed; `;;max` remains separate.

Rune suppression applies to the implemented native combat spell costs. Ammunition must still be compatible and equipped (at least one arrow/bolt or thrown weapon); infinite ammo preserves that supply. This does not add new spells, abilities, cooldown mechanics, item charges or unlimited crafting ingredients. Existing native necromancy attacks have no rune consumption in their current profile. War's Retreat and ability/action-bar work are deferred to the next task.

`;;commands` displays a coloured command directory with one description per line:

- `;;commands 1`: combat and resources.
- `;;commands 2`: gear and items.
- `;;commands 3`: NPCs and travel.
- `;;commands 4`: development/status tools.

Each page includes the next-page command. These use the existing native game-message packets; no new interface IDs or client scripts are guessed. All new commands retain local/native/dev-mode and explicit account/admin permission gates. Jaxa already has the local account grant.

Changes are limited to native admin command handling, transient Player flags/run-energy consumption, native combat supply checks, logout cleanup, tests and this documentation. No client engine, map, action-bar or boss changes are included.

## Verification and deployment

44 focused tests passed (admin commands, combat styles and melee combat), with no failures or skips. The isolated real-cache Native950SuppliesProbe passed: arrows are preserved while enabled, consumed after disabling, and incompatible/missing ammunition remains refused. No manual in-game verification was performed.

Engine and lobby overrides were rebuilt and deployed. Server PID 30168 is listening on 127.0.0.2 ports 80, 8950 and 43650. Pre-deployment saves, engine JAR and overrides are backed up at `backups/almighty-20260917-005646`. Jaxa's saved-character SHA-256 matches that backup after startup.
