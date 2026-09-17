# Inventory menus and actions, revision 950

## Diagnosis

The paired 950 cache already contains the expected inventory labels. Strict item decoding succeeds for representative ordinary items; this repair does not change the ObjType decoder. Native950CacheItems discarded the labels and Native950ItemCatalog kept only a limited selection of implemented actions. They now retain the current cache's labels, including notes' own menus, with equipment eligibility still separately verified.

The backpack event mask omitted source target bits and the component target bit needed for Use. Native CS2 authoring remains responsible for captions. The corrected mask enables item, object and NPC selection alongside ordinary operations and drag behavior.

The paired cache scripts route ordinary cache options 1/2/3/4/5 to interface operations 1/2/3/7/8. Drop therefore normally arrives as interface operation 8 (inbound opcode 66), not option 5 or the original 910 option 7. Specialized item menus are gated by the actual script2833 ID, category and parameter branches before ordinary operation translation.

## Implemented scope

- Current-cache inventory labels, ordinary menu translation and Use permissions.
- Exact clicked-slot Drop, including complete stacks and notes, using the existing floor-item ownership, lifetime and pickup infrastructure. Controller vetoes and special item state remain enforced; Destroy/Discard do not fall through to Drop.
- Ordinary eating from the original Food table, validated against 950 identities and animation records. Portions and containers remain in the clicked slot, with original healing/cooldowns. Special-effect foods remain explicit unsupported actions.
- Existing item-on-item crafting, production and tinderbox/log workflows, with the controller check restored.
- Verified 950 item-on-object input: selected raw food on cooking stations, logs on fires, and essence on supported altars. Approach waits for the final movement frame before starting. Selection, object presence, collision and controller permission are checked again on arrival.
- Verified item-on-NPC input provides an explicit unsupported response for unported workflows. No arbitrary 910 item handlers are enabled based solely on menu text.

This is an inventory interaction repair, not complete item-content parity. Tool-belt actions, specialized pouches/books/containers, special food effects and NPC item workflows still need their own ports. Unsupported actions explain that limitation rather than silently disappearing.

## Verification

Installed and running: 1083 unit tests passed, two existing skips, zero failures/errors; 18 launcher checks. Actual-cache Drop/pickup and selected Use workflows passed, together with existing NPC skills, UI and cache-preflight checks. Detailed counts and logs are in validation-inventory-actions-2026-09-12.json. Visible menus and client interaction are awaiting the user playtest. See inventory-menus-950-evidence.json for the menu/packet derivation. All changes are contained in 950RevTest; the 910 and 947 projects are reference-only. Backups are under implementation-backup/2026-09-12-inventory-actions.