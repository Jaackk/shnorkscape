These lists were exported directly from the paired950 cache.

items.txt, npcs.txt and objects.txt contain ID, name and optional variant details, separated by tabs. Open them in any text editor and search by name. Every definition is included in numeric ID order, including unnamed entries.

Item notes and other template variants inherit their names from the matching cache definition. Their variant/base ID is shown in Details. NPC and object base names do not resolve quest/variable-dependent transforms. A listed ID is a cache entry, not a promise that all of its gameplay has been ported.

<unnamed> means the cache has no display name. Decode/name-resolution failures, if any, are explicit and detailed in export-report.json. That report also contains counts and SHA-256 fingerprints.

To regenerate from the project folder:
  .\Export-950CacheNames.ps1

The exporter reads the cache without writing to it, uses the packaged950 definition decoders, and does not start or modify the game server.
