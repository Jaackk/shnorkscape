# One live checklist - combat polish 24 September

After applying the staged update, enable `;;bugtest`. These checks distinguish new candidate behaviour from already accepted features. Conjure attack/expiry and Living Death remain unresolved, so report them honestly rather than expecting a guaranteed fix.

1. Press an ability with a long cooldown: ring stays visible, Revolution continues, then it executes automatically. Replace/cancel once and confirm the old ring clears.
2. Drink Overload: Necromancy rises (120 to141 for normal; supreme to143), then restores on expiry.
3. Watch Skeleton/Zombie/Ghost attacks: report which actors visibly animate versus only produce damage.
4. Right-click **Examine** a conjure: its description appears.
5. Let a conjure expire/dismiss: report whether it still disappears without an effect (unresolved).
6. Build souls, keep fighting including Skulls in flight, then fully stop: souls persist during combat and count/world visuals expire together afterward.
7. Test **Soul Strike and Volley separately**: Strike's impact/AoE/stun and Volley's simultaneous soul hits after travel.
8. Run `;;comp`: inspect previously locked abilities and Undead Army's four selected conjures; note any remaining legitimate lock by name.
9. Activate Living Death: report visible animation/effect (unresolved; new Bug Test boundary capture).
10. Activate Berserk with melee equipment: check its newly bound pose and existing buff/cooldown.
11. Quickly check bank/items close -> keybind, chosen-tile Dive with conjures, bouncing Skulls and Revolution.
12. Jaxa + Nooby: independent queues, souls, conjures/stacks, damage and cleanup when one moves/dies/logs out; test Phantom command and Valour reset.
