#requires -Version 5.1
<#
.SYNOPSIS
Builds storage-isolated 950 clients from the bundled, verified RSA-patched seeds.
.DESCRIPTION
Run after extracting or moving the bundle. With no renderer flag, both clients
are initialized. Requires only Windows PowerShell 5.1 or PowerShell 7 on Windows.
No client is launched. Original seed executables are never modified.
#>
[CmdletBinding()]
param(
    [string]$Root,
    [switch]$Vulkan,
    [switch]$OpenGL
)
$ErrorActionPreference = 'Stop'
if ($Vulkan -and $OpenGL) { throw 'Choose -Vulkan, -OpenGL, or neither to initialize both clients.' }
if (-not $PSBoundParameters.ContainsKey('Root')) { $Root = $PSScriptRoot }
if ([string]::IsNullOrWhiteSpace($Root)) { throw 'A bundle root directory is required.' }
$bundleRoot = [IO.Path]::GetFullPath($Root).TrimEnd([IO.Path]::DirectorySeparatorChar)
if ($bundleRoot.Length -eq 2 -and $bundleRoot[1] -eq ':') { $bundleRoot += '\' }
if (-not [IO.Directory]::Exists($bundleRoot)) { throw "Bundle root does not exist: $bundleRoot" }
$storageRoot = [IO.Path]::GetFullPath((Join-Path $bundleRoot 'client-state'))

if (-not ('Bundle950.ClientStoragePatcherV1' -as [type])) {
    Add-Type -TypeDefinition @'
using System;
using System.Collections.Generic;
using System.IO;
using System.Security.Cryptography;
using System.Text;

namespace Bundle950 {
    public sealed class PatchResult {
        public byte[] Bytes;
        public string InputHash;
        public string OutputHash;
        public string[] CallSites;
        public string IatRva;
        public string StubRva;
        public string StubFile;
        public string PathFile;
        public string Layout;
        public int PayloadLength;
        public Dictionary<string,string> GrownHeaders;
    }

    public static class ClientStoragePatcherV1 {
        private sealed class Section {
            public string Name;
            public int Header;
            public int VirtualSize;
            public int Rva;
            public int RawSize;
            public int Raw;
            public uint Flags;
        }
        private sealed class Slack {
            public int Start;
            public int Length;
            public int Headroom;
        }
        private static Exception Invalid(string message) {
            return new InvalidDataException("950 client initialization: " + message);
        }
        private static void Range(byte[] bytes, long offset, long size) {
            if (offset < 0 || size < 0 || offset > bytes.Length - size)
                throw Invalid("PE data is outside the source file.");
        }
        private static ushort U16(byte[] b, int p) { Range(b,p,2); return BitConverter.ToUInt16(b,p); }
        private static uint U32(byte[] b, int p) { Range(b,p,4); return BitConverter.ToUInt32(b,p); }
        private static int I32(byte[] b, int p) { Range(b,p,4); return BitConverter.ToInt32(b,p); }
        private static ulong U64(byte[] b, int p) { Range(b,p,8); return BitConverter.ToUInt64(b,p); }
        private static int CheckedInt(uint n) {
            if (n > Int32.MaxValue) throw Invalid("PE offset is too large.");
            return (int)n;
        }
        private static void Put32(byte[] b, int p, int n) {
            Range(b,p,4); Buffer.BlockCopy(BitConverter.GetBytes(n),0,b,p,4);
        }
        private static string Hex(int n) { return "0x" + n.ToString("x"); }
        public static string Hash(byte[] bytes) {
            using (SHA256 sha = SHA256.Create()) {
                return BitConverter.ToString(sha.ComputeHash(bytes)).Replace("-", "").ToLowerInvariant();
            }
        }
        private static byte[] FromHex(string text) {
            byte[] b = new byte[text.Length / 2];
            for (int i=0; i<b.Length; i++) b[i] = Convert.ToByte(text.Substring(i*2,2),16);
            return b;
        }
        private static int Offset(List<Section> sections, int rva, int size) {
            foreach (Section s in sections) {
                long relative = (long)rva - s.Rva;
                if (relative >= 0 && relative + size <= s.RawSize)
                    return checked(s.Raw + (int)relative);
            }
            throw Invalid("PE RVA " + Hex(rva) + " has no file-backed section.");
        }
        private static string Ascii(byte[] b, int p) {
            Range(b,p,1);
            int end=p;
            while (end<b.Length && end-p<1024 && b[end]!=0) end++;
            if (end==b.Length || end-p==1024) throw Invalid("Unterminated PE import name.");
            return Encoding.ASCII.GetString(b,p,end-p);
        }
        private static Slack FindSlack(byte[] b, List<Section> sections, Section section) {
            int length = section.RawSize - section.VirtualSize;
            if (length <= 0) return null;
            int start=checked(section.Raw + section.VirtualSize);
            Range(b,start,length);
            for (int i=start; i<start+length; i++) if (b[i]!=0) return null;
            int index=sections.IndexOf(section);
            int headroom = index+1<sections.Count
                ? checked(sections[index+1].Rva - section.Rva - section.VirtualSize)
                : length;
            return new Slack {Start=start, Length=length, Headroom=headroom};
        }
        private static int Relative(int target, int origin) {
            long rel=(long)target-origin;
            if (rel<Int32.MinValue || rel>Int32.MaxValue) throw Invalid("Patch displacement exceeds signed 32-bit range.");
            return (int)rel;
        }

        public static PatchResult Build(byte[] source, string storageRoot, bool vulkan) {
            // SHGetFolderPathW receives a MAX_PATH (260 UTF-16 code unit) buffer.
            if (String.IsNullOrEmpty(storageRoot) || !Path.IsPathRooted(storageRoot) || storageRoot.IndexOf('\0')>=0)
                throw Invalid("Storage root must be an absolute Windows path without NUL characters.");
            if (storageRoot.Length >= 260)
                throw Invalid("Storage path exceeds SHGetFolderPathW's 259-character limit. Move the bundle to a shorter path.");
            string expectedHash=vulkan
                ? "ad8b881cebd1d0b087fd069513993cbd7da830794f6bd3d0c7c843c80361733d"
                : "8cd36d48440a82e7aebb162ef1e2911c8bf2b6366c0324d96dbb44c4f10bed89";
            string inputHash=Hash(source);
            if (!String.Equals(inputHash,expectedHash,StringComparison.Ordinal))
                throw Invalid("Unexpected " + (vulkan ? "Vulkan" : "OpenGL") + " seed SHA-256. Expected " + expectedHash + ", received " + inputHash + ". Restore the bundled patched seed.");
            byte[] raw=(byte[])source.Clone();
            if (U16(raw,0)!=0x5a4d) throw Invalid("Missing MZ header.");
            int pe=I32(raw,0x3c);
            Range(raw,pe,24);
            if (U32(raw,pe)!=0x00004550 || U16(raw,pe+4)!=0x8664) throw Invalid("Expected an AMD64 PE executable.");
            int count=U16(raw,pe+6);
            int optionalSize=U16(raw,pe+20);
            int optional=checked(pe+24);
            Range(raw,optional,optionalSize);
            if (optionalSize<128 || U16(raw,optional)!=0x20b || U32(raw,optional+108)<2)
                throw Invalid("Expected a PE32+ import directory.");
            int importRva=CheckedInt(U32(raw,optional+120));
            int importSize=CheckedInt(U32(raw,optional+124));
            if (importRva==0 || importSize<20) throw Invalid("PE import directory is missing.");
            int sectionTable=checked(optional+optionalSize);
            Range(raw,sectionTable,checked(count*40));
            List<Section> sections=new List<Section>();
            Section text=null;
            for (int i=0; i<count; i++) {
                int h=sectionTable+i*40;
                Section s=new Section {
                    Name=Encoding.ASCII.GetString(raw,h,8).TrimEnd('\0'), Header=h,
                    VirtualSize=CheckedInt(U32(raw,h+8)), Rva=CheckedInt(U32(raw,h+12)),
                    RawSize=CheckedInt(U32(raw,h+16)), Raw=CheckedInt(U32(raw,h+20)), Flags=U32(raw,h+36)
                };
                Range(raw,s.Raw,s.RawSize);
                if (s.Name==".text") {
                    if (text!=null) throw Invalid("Multiple .text sections.");
                    text=s;
                }
                sections.Add(s);
            }
            sections.Sort(delegate(Section a, Section b) {return a.Rva.CompareTo(b.Rva);});
            if (text==null || (text.Flags & 0x20000000)==0) throw Invalid("Executable .text section not found.");
            int iat=-1;
            int importStart=Offset(sections,importRva,importSize);
            Range(raw,importStart,importSize);
            for (int d=importStart; d<=importStart+importSize-20; d+=20) {
                uint namesRva=U32(raw,d), nameRva=U32(raw,d+12), firstThunk=U32(raw,d+16);
                if (namesRva==0 && nameRva==0 && firstThunk==0) break;
                if (nameRva==0) throw Invalid("PE import descriptor has no DLL name.");
                string dll=Ascii(raw,Offset(sections,CheckedInt(nameRva),1));
                if (!String.Equals(dll,"SHELL32.dll",StringComparison.OrdinalIgnoreCase)) continue;
                int thunkRva=CheckedInt(namesRva==0 ? firstThunk : namesRva);
                for (int index=0; index<65536; index++) {
                    int slotRva=checked(thunkRva+index*8);
                    ulong named=U64(raw,Offset(sections,slotRva,8));
                    if (named==0) break;
                    if ((named & 0x8000000000000000UL)!=0) continue;
                    if (named>Int32.MaxValue) throw Invalid("Invalid import name RVA.");
                    string name=Ascii(raw,Offset(sections,(int)named,3)+2);
                    if (name=="SHGetFolderPathW") {
                        if (iat>=0) throw Invalid("Duplicate SHGetFolderPathW import.");
                        iat=checked(CheckedInt(firstThunk)+index*8);
                    }
                }
            }
            int expectedIat=vulkan ? 0x8f3790 : 0x7f5930;
            if (iat!=expectedIat) throw Invalid("SHGetFolderPathW import does not match the audited build.");
            int delta=checked(text.Rva-text.Raw);
            List<int> sites=new List<int>();
            for (int i=text.Raw; i<text.Raw+text.RawSize-6; i++) {
                if (raw[i]==0xff && raw[i+1]==0x15 && (long)i+delta+6+I32(raw,i+2)==iat) sites.Add(i);
            }
            int[] expectedSites=vulkan ? new int[] {0x6de20b,0x6de379,0x6de4ad,0x88dd60}
                : new int[] {0x6dbb8b,0x6dbcf9,0x6dbe2d,0x7962a0};
            string[] signatures=vulkan ? new string[] {"ff157f492100","ff1511482100","ff15dd462100","ff152a4e0600"}
                : new string[] {"ff159f911100","ff1531901100","ff15fd8e1100","ff158aea0500"};
            if (sites.Count!=expectedSites.Length) throw Invalid("Unexpected SHGetFolderPathW call-site count.");
            for (int i=0; i<expectedSites.Length; i++) {
                if (sites[i]!=expectedSites[i]) throw Invalid("SHGetFolderPathW call-site offsets changed.");
                byte[] signature=FromHex(signatures[i]);
                for (int j=0; j<signature.Length; j++)
                    if (raw[sites[i]+j]!=signature[j]) throw Invalid("Call signature changed at " + Hex(sites[i]) + ".");
            }
            byte[] path=Encoding.Unicode.GetBytes(storageRoot+"\0");
            byte[] stub=FromHex("488b442428488d151c00000031c9440fb7044a6644890448ffc1664585c075ee31c0c3");
            Slack execSlack=FindSlack(raw,sections,text);
            if (execSlack==null) throw Invalid(".text slack is not zero padding.");
            int stubRaw=execSlack.Start;
            int stubRva=checked(stubRaw+delta);
            int pathRaw;
            bool contiguous=execSlack.Length>=40+path.Length && execSlack.Headroom>=40+path.Length;
            Dictionary<string,string> grown=new Dictionary<string,string>();
            List<KeyValuePair<int,int>> growth=new List<KeyValuePair<int,int>>();
            if (contiguous) {
                pathRaw=checked(stubRaw+40);
                growth.Add(new KeyValuePair<int,int>(text.Header+8,checked(text.VirtualSize+40+path.Length)));
            } else {
                Section host=null;
                Slack hostSlack=null;
                foreach (Section s in sections) {
                    // Discardable sections (for example .reloc) need not remain
                    // mapped after loading, so they cannot safely host the path.
                    if (s==text || (s.Flags & 0x40000000)==0 || (s.Flags & 0x02000000)!=0) continue;
                    Slack info=FindSlack(raw,sections,s);
                    if (info!=null && info.Length>=path.Length && info.Headroom>=path.Length) {
                        host=s; hostSlack=info; break;
                    }
                }
                if (host==null) throw Invalid("No readable section has enough zero padding for this storage path. Move the bundle to a shorter path.");
                if (execSlack.Length<stub.Length || execSlack.Headroom<stub.Length) throw Invalid(".text padding is too small for the redirect stub.");
                pathRaw=hostSlack.Start;
                int pathRva=checked(pathRaw+host.Rva-host.Raw);
                Put32(stub,8,Relative(pathRva,checked(stubRva+12)));
                growth.Add(new KeyValuePair<int,int>(text.Header+8,checked(text.VirtualSize+stub.Length)));
                growth.Add(new KeyValuePair<int,int>(host.Header+8,checked(host.VirtualSize+path.Length)));
            }
            Range(raw,stubRaw,stub.Length);
            Range(raw,pathRaw,path.Length);
            foreach (KeyValuePair<int,int> change in growth) {
                // Expansion stays inside file-backed slack and the next section (checked above).
                Put32(raw,change.Key,change.Value);
                grown.Add(Hex(change.Key),Hex(change.Value));
            }
            Buffer.BlockCopy(stub,0,raw,stubRaw,stub.Length);
            Buffer.BlockCopy(path,0,raw,pathRaw,path.Length);
            string[] callSites=new string[sites.Count];
            for (int i=0; i<sites.Count; i++) {
                int off=sites[i];
                raw[off]=0xe8;
                Put32(raw,off+1,Relative(stubRva,checked(off+delta+5)));
                raw[off+5]=0x90;
                callSites[i]=Hex(off);
            }
            return new PatchResult {
                Bytes=raw, InputHash=inputHash, OutputHash=Hash(raw), CallSites=callSites,
                IatRva=Hex(iat), StubRva=Hex(stubRva), StubFile=Hex(stubRaw), PathFile=Hex(pathRaw),
                Layout=contiguous ? "contiguous" : "split", PayloadLength=stub.Length+(contiguous ? 5 : 0)+path.Length,
                GrownHeaders=grown
            };
        }
    }
}
'@
}

function Write-950AtomicFile {
    param([string]$Path, [byte[]]$Bytes)
    $temporary = $Path + '.' + [Guid]::NewGuid().ToString('N') + '.tmp'
    try {
        [IO.File]::WriteAllBytes($temporary, $Bytes)
        if ([IO.File]::Exists($Path)) {
            [IO.File]::Replace($temporary, $Path, [NullString]::Value)
        } else {
            [IO.File]::Move($temporary, $Path)
        }
    } catch {
        throw "Could not update '$Path'. Close any client running from this bundle and confirm the folder is writable. $($_.Exception.Message)"
    } finally {
        if ([IO.File]::Exists($temporary)) { [IO.File]::Delete($temporary) }
    }
}

$variants = @()
if (-not $Vulkan) { $variants += [pscustomobject]@{Name='OpenGL'; IsVulkan=$false; Seed='win64'; Exe='rs2client.exe'; Report='client-isolation.json'} }
if (-not $OpenGL) { $variants += [pscustomobject]@{Name='Vulkan'; IsVulkan=$true; Seed='bt10'; Exe='rs2client-vulkan.exe'; Report='client-isolation-vulkan.json'} }
$jobs = @()
# Validate every requested seed and construct complete outputs before creating/writing files.
foreach ($variant in $variants) {
    $seed = Join-Path $bundleRoot ('OpenNXT\data\clients\950\' + $variant.Seed + '\patched\rs2client.exe')
    if (-not [IO.File]::Exists($seed)) { throw "Bundled $($variant.Name) client seed is missing: $seed" }
    $result = [Bundle950.ClientStoragePatcherV1]::Build([IO.File]::ReadAllBytes($seed), $storageRoot, $variant.IsVulkan)
    $output = Join-Path $bundleRoot ('client\' + $variant.Exe)
    $reportPath = Join-Path $bundleRoot ('logs\' + $variant.Report)
    $headers = [ordered]@{}
    foreach ($key in $result.GrownHeaders.Keys) { $headers[$key] = $result.GrownHeaders[$key] }
    $report = [ordered]@{
        source_version = '950-1'
        call_sites = $result.CallSites
        redirected_calls = $result.CallSites
        iat_rva = $result.IatRva
        stub_rva = $result.StubRva
        stub_file = $result.StubFile
        path_file = $result.PathFile
        layout = $result.Layout
        payload_length = $result.PayloadLength
        storage_root = $storageRoot
        grown_headers = $headers
        input = $seed
        input_sha256 = $result.InputHash
        output = $output
        isolated_sha256 = $result.OutputHash
        initializer_version = 1
    }
    $jobs += [pscustomobject]@{Variant=$variant; Result=$result; Output=$output; ReportPath=$reportPath; Report=$report}
}
foreach ($directory in @('client', 'client-state', 'logs', 'temp')) {
    [IO.Directory]::CreateDirectory((Join-Path $bundleRoot $directory)) | Out-Null
}
$utf8 = New-Object Text.UTF8Encoding($false)
foreach ($job in $jobs) {
    $unchanged = [IO.File]::Exists($job.Output) -and
        ((Get-FileHash -LiteralPath $job.Output -Algorithm SHA256).Hash -eq $job.Result.OutputHash)
    if (-not $unchanged) { Write-950AtomicFile -Path $job.Output -Bytes $job.Result.Bytes }
    $reportText = ($job.Report | ConvertTo-Json -Depth 6) + [Environment]::NewLine
    if (-not [IO.File]::Exists($job.ReportPath) -or [IO.File]::ReadAllText($job.ReportPath) -cne $reportText) {
        Write-950AtomicFile -Path $job.ReportPath -Bytes $utf8.GetBytes($reportText)
    }
    if ((Get-FileHash -LiteralPath $job.Output -Algorithm SHA256).Hash -ne $job.Result.OutputHash) {
        throw "Client verification failed after writing: $($job.Output)"
    }
    Write-Host ("950 {0} client {1}: {2}" -f $job.Variant.Name, $(if ($unchanged) {'verified'} else {'initialized'}), $job.Output)
}
