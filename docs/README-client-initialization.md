# Portable client initialization

Place `Initialize-950Client.ps1` in the bundle root. It requires only Windows
PowerShell 5.1 (included with Windows) or PowerShell 7. It installs nothing,
downloads nothing, and never launches a client.

From the bundle folder:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\Initialize-950Client.ps1
```

By default it initializes both renderers. Select one with `-Vulkan` or `-OpenGL`,
or supply another bundle location using `-Root 'C:\Games\950OpenSource'`.

The required, unisolated RSA-patched seeds are:

- `OpenNXT\data\clients\950\win64\patched\rs2client.exe`
- `OpenNXT\data\clients\950\bt10\patched\rs2client.exe`

The generated outputs are:

- `client\rs2client.exe` and `logs\client-isolation.json` (OpenGL)
- `client\rs2client-vulkan.exe` and `logs\client-isolation-vulkan.json` (Vulkan)

Run the initializer again after moving or renaming the bundle. It derives the
current absolute `client-state` path, rebuilds from the unchanged seeds, and
updates the report hashes and paths. Repeated runs at the same location preserve
client and report timestamps when contents already match. The root's `client`,
`client-state`, `logs`, and `temp` directories are created if needed.

A launcher can run the initializer before reading the isolation report:

```powershell
& (Join-Path $root 'Initialize-950Client.ps1') -Root $root -Vulkan
```

Pass `-OpenGL` instead when that renderer is selected. The report fields
`storage_root` and `isolated_sha256` retain the existing launcher contract.

Keep the extracted bundle path reasonably short. The full storage path,
including `\client-state`, can contain at most 91 UTF-16 code units for Vulkan
or 203 for OpenGL with these audited seeds. Equivalently, an ordinary bundle
directory path can contain at most 78 or 190 code units respectively. A path
that does not fit fails before any requested client or report is written; move
the bundle to a shorter location and retry. Windows' separate 259-code-unit
limit for the API's path buffer is also checked. Spaces and Unicode work.

If a client needs regeneration while it is running, close that client before
retrying. Output and report replacement uses temporary files in their own
directories and atomic file replacement. After an interruption, rerunning the
initializer repairs any missing or outdated output or report.

## Verification and maintenance

The initializer verifies the complete SHA-256 of each source, parses its AMD64
PE headers and import table, derives the `SHGetFolderPathW` import and its call
sites, and checks the four audited call offsets and their original bytes. It
places the existing 35-byte redirect stub and UTF-16 path in verified zero
padding, checks section headroom, and extends only the necessary section
virtual sizes. It never uses discardable sections for the path. Both requested
clients are constructed and validated in memory before any output is written.

The audited seed hashes are:

| Renderer | SHA-256 |
| --- | --- |
| OpenGL | `8cd36d48440a82e7aebb162ef1e2911c8bf2b6366c0324d96dbb44c4f10bed89` |
| Vulkan | `ad8b881cebd1d0b087fd069513993cbd7da830794f6bd3d0c7c843c80361733d` |

Do not replace these hashes to accept a different executable without auditing
that executable and its patch sites.

`Test-950ClientInitializer.ps1` validates against a reference directory that
contains both seeds and known-good isolated executables. It only reads that
reference; all created files and the relocated fixture remain under its new
work directory. Choose a short work directory so the Vulkan path fits:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\Test-950ClientInitializer.ps1 `
  -ReferenceRoot 'C:\Users\developer\Desktop\950RevTest' `
  -WorkDirectory 'C:\Temp\950-client-check'
```

Validation covers exact byte equality at the reference storage path, source and
output hashes, repeated-run timestamps, folder relocation with spaces and
Unicode, embedded UTF-16 paths, changed seed rejection, unchanged outputs after
rejection, API buffer limits, and available section capacity. No client runs
during validation. A successful run writes `validation-results.json` in the
work directory.

The delivered version passed all 28 checks under Windows PowerShell 5.1 and
PowerShell 7.6.5. At the original storage path, its output reproduces the known
clients with zero differing bytes:

| Renderer | Known-good isolated SHA-256 |
| --- | --- |
| OpenGL | `9ce771772b86d3bd1524a6c79bc7cdbecbe9d2c52b8be62a73d2d00255617696` |
| Vulkan | `a03942a9be4825ff499a834f159a285400b271c9a3cab5f51b8c4df7bd5322df` |
