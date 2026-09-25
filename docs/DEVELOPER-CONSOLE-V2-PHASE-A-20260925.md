# Developer Console V2 — Phase A

This is the requested primitive-validation milestone, **not the finished V2 visual redesign**. Native Vulkan acceptance is required before expanding the composition into the supplied nine-panel design guide. The proven 1448 readiness handshake and world-editor backend remain in use.

## Implemented for physical verification

- NPC results: at most 100 rows in a native clipped viewport, with CS7791/31 scrollbar. Every row's text, border and operation actor belongs to the same container. Earlier/More select another bounded window; the full catalogue is never instantiated as UI children.
- Clicking a row highlights it locally. The server updates only the selected inspector/model, preserving the list and its scroll state. Row nonces use a separate namespace from the native button child indices, so a hundred results cannot create invalid holes in the inspector's component children.
- Full-body NPC model via the exact active-component counterpart of Beasts' NPC setter. Native render-animation definitions supply idle sequences; existing validated combat profiles supply attacks. Idle/Attack callbacks play locally and cannot attack or spawn an NPC. Unverified sequences have no operation. Drag rotation uses the native CS8479/8480 model controls.
- Boss-specific framing uses exact Beasts struct params 1347/3040/3041 only when the NPC identity matches. Other NPCs use explicit provisional footprint-based framing. Both small and large models require physical acceptance; metadata validation does not prove that they fit or face correctly.
- In-console search edits locally with the native text-edit routine and context 11. Enter submits the query; Escape restores the previous query without submission. Close/navigation release the field's keyboard hook and context. Name/ID/alias matching stays in the existing indexed backend. This milestone submits on Enter; it does not claim debounced search-as-you-type.
- Native NPC context operations: Select, Spawn near me, Place in world, Inspect. Commands no longer duplicate Open Details. Server-owned actions retain permissions, account ownership, stale-render rejection and existing placement validation.

No interface-definition archive, client executable, combat implementation, character, real bank or Equipment Library is modified. Only staged server code and narrowly scoped client scripts change. The existing loading-marker/readiness callback is preserved.

## Evidence and limitations

Contracts: `docs/DEV-UI-NATIVE-CAPABILITIES-950.md`, CS3503/3869/11613 (models), CS11619/8479/8480 (rotation), CS11074/7791/31 (scroll), CS9833/13121/7170/1553/8841 (text input). Project helpers occupy 21131–21143; 21124–21130 retain existing responsibilities.

The read-only paired-cache probe resolves:

| NPC | Idle | Verified attack | Framing |
| --- | --- | --- | --- |
| Man, 1 | 808 | 422 | Provisional ordinary-NPC framing |
| General Graardor, 6260 | 17387 | 17389 | Native Beasts zoom 1325 / height 240 |
| Vorago, 17161 | 20333 | Unavailable | Provisional large-NPC framing |

Status: **AUTOMATED VERIFIED / LIVE TEST PENDING** for the new primitives. World mutations continue through the existing handlers. No new attack binding is invented for unsupported bosses.

The final Home/dashboard, rebuilt Items/Objects/Bosses/Combat pages, preview framing polish, Animation Lab and travel composition are subsequent phases. Object placement remains the existing browser. No complete object model preview, arbitrary FX preview, or floating-object fix is claimed here. Existing detailed NPC controls remain accessible via More controls; the Phase-A compact inspector is not the final configuration layout.

## One live checklist

After manually applying the staged candidate and launching normally:

1. Fresh `;;dev`: title and controls appear without another command. Heal still executes.
2. NPCs: click the integrated search field, type `Man`, use Backspace, press Enter. Search `Graardor`, then `Vorago`. Escape from an edited query should cancel it.
3. Clear search; wheel-scroll and drag the scrollbar. Click partially clipped and fully visible rows; the selected name/model must match, and selection must not jump the list back to its top.
4. Select Man and Graardor: full models, Idle/Attack playback and drag rotation. Check Vorago fits; its unverified Attack operation must be unavailable.
5. Right-click a row: Inspect and Spawn near me. Then Place in world and click a valid tile. Confirm the selected NPC appears and the console returns normally.
6. Close/reopen, try `;;bank` / `;;items`, then action-bar keybinds. Report any stuck focus or unexpected chat typing with `;;bug dev-v2-phase-a`.

This checkpoint is deliberately narrow: physical results determine the next composition changes instead of multiplying unproven controls throughout V2.
