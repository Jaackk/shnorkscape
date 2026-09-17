# Native950 map areas and lodestone loading

The scene area is now derived from the paired cache for every position. The earlier broad rectangles and experimental ID sweep were replaced with all5,197 map squares (332,608 chunks), including307 mixed squares. There are869 unique area identities.

## Why the previous lookup failed

Config index2/group83 opcode2 is a24-bit identity label. Calling it only a colour missed its relationship to the world map membership table. The950 client loads index23/group3/file=(squareX | squareY<<7), expands its RLE into64 labels, and compares those labels with the selected config area's label. A map square can contain several areas at8-tile chunk granularity. Config3/4 records provide overrides/transforms; their bounding rectangle is not authoritative membership.

The exact WIN64 references are recorded in map-areas-950-evidence.json. The loader is0x14032e690; RLE decode is0x14032e900..0x14032ea1d; per-chunk identity comparison is0x1403162dc, with x-major chunk indexing confirmed at0x140316436. Config IDs are uniquely determined by their labels, including City of Um814 (37f44a), Wendlewick857 (11d114), Anachronia762 (19b6cc), Karamja674 (ff6075) and Ashdale4 (fffb11). The other24 ordinary lodestones resolve to474 (638fe6).

## Runtime behavior

Native950MapAreas chooses the8-tile chunk's area. Native950Session sends a rebuild on area changes even when the character remains inside the256-tile scene. World admission uses the resolved entry area for the initial packet but passes the original configured fallback into Session. Logging in at Um can therefore never make814 the mainland fallback. Debug ;;area/;;areascan state belongs to one player session and ;;area off also stops its sweep; it cannot affect another client.

Both complete source groups are SHA-256 pinned and every generated chunk/identity is compared with actual cache bytes before live world admission. The digest is concatenated big-endian fileID, byte length and file contents in ascending ID order. Sixteen map-square files contain unassigned label0 runs; these remain unspecified and use the configured fallback. No coordinates, character saves or cache files were changed. The existing City of Um landing is retained at1084,1768,1 because the ordinary south tile is blocked.

## Regeneration and validation

Run tools/generate_950_map_areas.py with the project's Python environment. It reads the cache and updates only the generated resource and evidence JSON. Native950MapAreasAcceptance independently verifies both complete source groups and all29 lodestones. Its actual-cache run passed on2026-09-12; log: logs/map-areas-cache-950.log.

Native950MapAreasTest covers all29 destinations, non-mainland-to-mainland lookup, mixed8-tile chunks, invalid-coordinate alias prevention and malformed RLE refusal. Native950LodestonesAcceptance additionally runs all29 original HomeTeleport journeys and a fresh City of Um session returning to Lumbridge; it checks collision, walk-off, encrypted input, scene area and local teleport position. Full-suite/combined runtime deployment is owned by the main integration task. Rendering is still a separate manual client check and is not claimed by these cache/packet checks.
