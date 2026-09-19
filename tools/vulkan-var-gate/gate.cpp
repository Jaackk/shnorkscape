#include <windows.h>
#include <tlhelp32.h>
#include <bcrypt.h>
#include <stdint.h>
#include <stddef.h>
#include <stdio.h>
#include <string.h>
#include <atomic>
#include <vector>
#include <string>
extern "C" {
#include "config.h"
#include "funchook_internal.h"
}

// Diagnostic only. No variable setters, network endpoints or arbitrary read API.
static constexpr char imageHash[] = "36c45c1cf6eed0c6cb0b789ca1672d685c1746def9d65d6f18d72638f0087bc9";
static const unsigned char entryBytes[] = {0x48,0x89,0x5c,0x24,0x08,0x48,0x89,0x6c,0x24,0x10,0x48,0x89,0x74,0x24,0x18};
using MainLogic = void(*)(void*, unsigned char);
static MainLogic original;
static uintptr_t base;
static std::atomic<long> active{0}, result{0};
static DWORD owner;
static int observed;
static uintptr_t clientSeen, managerSeen;
static uint64_t bucketsSeen, elementsSeen;

static bool copyRead(uintptr_t p, void* out, size_t n) {
    if (p < 0x10000 || n > 65536 || p + n < p) return false;
    MEMORY_BASIC_INFORMATION mbi{};
    if (!VirtualQuery((void*)p, &mbi, sizeof(mbi)) || mbi.State != MEM_COMMIT ||
        (mbi.Protect & (PAGE_GUARD | PAGE_NOACCESS)) ||
        p+n > (uintptr_t)mbi.BaseAddress+mbi.RegionSize) return false;
    __try { memcpy(out, (void*)p, n); return true; }
    __except(EXCEPTION_EXECUTE_HANDLER) { return false; }
}
template<class T> static bool read(uintptr_t p, T& value) { return copyRead(p, &value, sizeof(value)); }

static std::string digest(const unsigned char* data, size_t size) {
    BCRYPT_ALG_HANDLE alg{}; BCRYPT_HASH_HANDLE hash{};
    unsigned char bytes[32]{}; char hex[65]{};
    if (BCryptOpenAlgorithmProvider(&alg, BCRYPT_SHA256_ALGORITHM, nullptr, 0)<0) return {};
    bool ok = BCryptCreateHash(alg, &hash, nullptr, 0, nullptr, 0, 0)>=0;
    if (ok) ok = size<=0xffffffffULL && BCryptHashData(hash, const_cast<PUCHAR>(data), (ULONG)size, 0)>=0 && BCryptFinishHash(hash, bytes, 32, 0)>=0;
    if (hash) BCryptDestroyHash(hash);
    BCryptCloseAlgorithmProvider(alg,0);
    if (!ok) return {};
    for (int i=0;i<32;i++) sprintf_s(hex+i*2,3,"%02x",bytes[i]);
    return hex;
}
static bool preflight() {
    wchar_t path[MAX_PATH]; if (!GetModuleFileNameW(nullptr,path,MAX_PATH)) return false;
    HANDLE f=CreateFileW(path,GENERIC_READ,FILE_SHARE_READ,nullptr,OPEN_EXISTING,0,nullptr);
    if(f==INVALID_HANDLE_VALUE) return false;
    DWORD size=GetFileSize(f,nullptr), got=0;
    std::vector<unsigned char> data(size==15089416?size:0);
    bool ok=!data.empty() && ReadFile(f,data.data(),size,&got,nullptr) && got==size;
    CloseHandle(f); if(!ok || digest(data.data(),data.size())!=imageHash) return false;
    struct Span { uintptr_t start; size_t size; const char* hash; };
    const Span spans[]={
        {0x1c830,0x8b1,"6c051fb5fb9cb2c2b1fc9a38c34ac9ae62a25bfbb5396047c14defd77f0fc4a6"},
        {0x1b070,0x1ab,"e07682c0fa905789eb7a9867be0a73d823a7ed2aaf5312b80c7014f27131991b"},
        {0x21bc0,0x2412,"abf6cdbedc671e86acd0dddd2ae0a8a24f7b79b35f31657ddf8dc8260e0ea01d"},
        {0x1471e0,0x5dc,"b0318cf9af6e400158fd23f9ae8329b9a7b305b429d8b0adebcb40fdc086245e"},
        {0x147c40,0x94,"ffd0f52280cbe972b26b619d94611e6f4509573873b7af225c7a95907986c8c7"},
        {0x25550,0x1c4,"4da9e9691d5aac059f9c1c61b17545450daf077267dc9c111707a6f4c7fc74ae"}};
    for(auto s:spans) {
        std::vector<unsigned char> code(s.size);
        if(!copyRead(base+s.start,code.data(),s.size) || digest(code.data(),code.size())!=s.hash) return false;
    }
    uintptr_t fn=0;
    return read(base+0xc61ce0,fn) && fn==base+0x25550;
}

// Fully bounded sparse lookup; no missing-key default is accepted for the gate.
static bool lookup(uintptr_t domain, int& value, uintptr_t& found) {
    uintptr_t buckets=0,node=0; uint64_t count=0,elements=0;
    if(!read(domain+0x18,buckets)||!read(domain+0x20,count)||!read(domain+0x28,elements) ||
       !count||count>32768||!elements||elements>32768 || buckets%8) return false;
    if(!read(buckets+(3296%count)*8,node)) return false;
    uintptr_t visited[512]; size_t used=0;
    while(node && used<512 && used<elements) {
        if(node%8) return false;
        for(size_t i=0;i<used;i++) if(visited[i]==node) return false;
        visited[used++]=node;
        int id=0; if(!read(node,id)) return false;
        if(id==3296) {
            found=node; bucketsSeen=count; elementsSeen=elements;
            return read(node+8,value);
        }
        if(!read(node+0x28,node)) return false;
    }
    return false;
}
static void diagnostic(void* client, unsigned char argument) {
    active.fetch_add(1);
    original(client,argument); // Preserve the caller's argument, unlike the reference's forced 1.
    if(result.load()==0) {
        uintptr_t anchor=0,secondary=0,vtable=0,manager=0,node1=0,node2=0;
        int a=0,b=0;
        bool ok=read(base+0xfb8258,anchor)&&read(base+0xdb6088,secondary)&&
          anchor==(uintptr_t)client && anchor==secondary && read(anchor,vtable)&&vtable==base+0xc61bf0 &&
          read(anchor+0x19920,manager)&&manager && !(manager%8) &&
          lookup(manager+0x7620,a,node1)&&lookup(manager+0x7620,b,node2)&&a==b&&node1==node2;
        owner=GetCurrentThreadId(); clientSeen=anchor; managerSeen=manager; observed=a;
        result.store(ok?1:-1);
    }
    active.fetch_sub(1);
}

// No thread contexts are modified. Refuse patching while any thread is inside
// the entry span or a diagnostic invocation; resume every acquired thread.
static bool toggle(funchook_t* hook, bool install) {
    HANDLE handles[1024]; DWORD ids[1024]; size_t used=0; bool safe=true;
    HANDLE snap=CreateToolhelp32Snapshot(TH32CS_SNAPTHREAD,0);
    if(snap==INVALID_HANDLE_VALUE) return false;
    THREADENTRY32 e{sizeof(e)};
    if(Thread32First(snap,&e)) do {
        if(e.th32OwnerProcessID!=GetCurrentProcessId()||e.th32ThreadID==GetCurrentThreadId()) continue;
        if(used==1024) {safe=false;break;}
        HANDLE h=OpenThread(THREAD_SUSPEND_RESUME|THREAD_GET_CONTEXT,FALSE,e.th32ThreadID);
        if(!h) {safe=false;break;}
        if(SuspendThread(h)==(DWORD)-1) {CloseHandle(h);safe=false;break;}
        ids[used]=e.th32ThreadID; handles[used++]=h;
        CONTEXT c{}; c.ContextFlags=CONTEXT_CONTROL;
        if(!GetThreadContext(h,&c)||(c.Rip>=base+0x25550&&c.Rip<base+0x25555)) {safe=false;break;}
    } while(Thread32Next(snap,&e));
    CloseHandle(snap);
    // Refuse if a thread appeared while the first enumeration was suspended.
    snap=CreateToolhelp32Snapshot(TH32CS_SNAPTHREAD,0);
    if(snap==INVALID_HANDLE_VALUE) safe=false;
    else {
        e.dwSize=sizeof(e);
        if(Thread32First(snap,&e)) do {
            if(e.th32OwnerProcessID!=GetCurrentProcessId()||e.th32ThreadID==GetCurrentThreadId()) continue;
            bool seen=false; for(size_t i=0;i<used;i++) if(ids[i]==e.th32ThreadID)seen=true;
            if(!seen)safe=false;
        } while(Thread32Next(snap,&e));
        CloseHandle(snap);
    }
    safe=safe && active.load()==0;
    int rc=-1;
    if(safe) rc=install?funchook_install(hook,0):funchook_uninstall(hook,0);
    while(used) {HANDLE h=handles[--used];ResumeThread(h);CloseHandle(h);}
    return safe && rc==0;
}
static void report(const char* status, bool restored) {
    FILE* f=nullptr;
    fopen_s(&f,"C:\\Games\\950OpenSource\\logs\\vulkan-var-gate.txt","a");
    if(f) {
        fprintf(f,"pid=%lu status=%s restored=%d ownerThread=%lu id=3296 resolved=%d bucketCount=%llu elementCount=%llu\n",
          GetCurrentProcessId(),status,restored,owner,result.load()==1,bucketsSeen,elementsSeen);
        fclose(f);
    }
}
static DWORD WINAPI worker(void*) {
    base=(uintptr_t)GetModuleHandleW(nullptr);
    if(!preflight()) {report("preflight-refused",true);return 0;}
    auto h=funchook_create(); if(!h) {report("allocation-refused",true);return 0;}
    original=(MainLogic)(base+0x25550);
    if(funchook_prepare(h,(void**)&original,(void*)diagnostic)!=0) {
        report("prepare-refused",true);funchook_destroy(h);return 0;
    }
    // Exact pinned Funchook structure: inspect what prepare actually produced.
    auto e=(funchook_entry_t*)((uintptr_t)original - offsetof(funchook_entry_t,trampoline));
    if(e->patch_code_size!=5 || e->target_func!=(void*)(base+0x25550) ||
       memcmp(e->old_code,entryBytes,5)||memcmp((void*)original,entryBytes,5)||
       memcmp((void*)(base+0x25550),entryBytes,sizeof(entryBytes))) {
        report("stolen-span-refused",true);funchook_destroy(h);return 0;
    }
    if(!toggle(h,true)) {
        bool restored=memcmp((void*)(base+0x25550),entryBytes,sizeof(entryBytes))==0;
        if(!restored) restored=toggle(h,false);
        report("activation-refused",restored);return 0;
    }
    for(int i=0;i<300 && result.load()==0;i++) Sleep(100);
    if(result.load()==0) result.store(-2);
    bool restored=false;
    for(int i=0;i<100&&!restored;i++) {restored=toggle(h,false);if(!restored)Sleep(20);}
    restored=restored && memcmp((void*)(base+0x25550),entryBytes,sizeof(entryBytes))==0;
    report(result.load()==1?"gate-passed":result.load()==-2?"no-mainlogic-callback":"gate-failed",restored);
    // Keep the inert module/trampoline allocated until process exit, avoiding
    // unloading code beneath a thread that previously reached the detour.
    return 0;
}
#ifndef GATE_TEST
BOOL WINAPI DllMain(HINSTANCE module,DWORD reason,LPVOID) {
    if(reason==DLL_PROCESS_ATTACH) {
        DisableThreadLibraryCalls(module);
        HANDLE t=CreateThread(nullptr,0,worker,nullptr,0,nullptr);
        if(t)CloseHandle(t);
    }
    return TRUE;
}
#else
int main() {
    if(preflight()) return 1; // The test executable must never be accepted as the client.
    alignas(8) unsigned char domain[128]{};
    alignas(8) unsigned char node[48]{};
    uintptr_t buckets[2]{}; buckets[0]=(uintptr_t)node;
    *(uintptr_t*)(domain+0x18)=(uintptr_t)buckets;
    *(uint64_t*)(domain+0x20)=1; *(uint64_t*)(domain+0x28)=1;
    *(int*)node=3296; *(int*)(node+8)=1234567;
    int value=0; uintptr_t found=0;
    if(!lookup((uintptr_t)domain,value,found)||value!=1234567||found!=(uintptr_t)node)return 2;
    *(uint64_t*)(domain+0x20)=0; if(lookup((uintptr_t)domain,value,found))return 3;
    *(uint64_t*)(domain+0x20)=32769; if(lookup((uintptr_t)domain,value,found))return 4;
    *(uint64_t*)(domain+0x20)=1; *(int*)node=999; *(uintptr_t*)(node+0x28)=(uintptr_t)node;
    *(uint64_t*)(domain+0x28)=2; if(lookup((uintptr_t)domain,value,found))return 5;
    buckets[0]=1; if(lookup((uintptr_t)domain,value,found))return 6;
    // Prepare a synthetic function only, never executable client memory.
    auto code=(unsigned char*)VirtualAlloc(nullptr,4096,MEM_COMMIT|MEM_RESERVE,PAGE_EXECUTE_READWRITE);
    memcpy(code,entryBytes,sizeof(entryBytes)); code[15]=0xc3;
    void* trampoline=code; auto h=funchook_create();
    if(!h||funchook_prepare(h,&trampoline,(void*)diagnostic))return 7;
    auto e=(funchook_entry_t*)((uintptr_t)trampoline - offsetof(funchook_entry_t,trampoline));
    if(e->patch_code_size!=5||memcmp(e->old_code,entryBytes,5)||memcmp(trampoline,entryBytes,5))return 8;
    if(memcmp(code,entryBytes,15))return 9;
    funchook_destroy(h); VirtualFree(code,0,MEM_RELEASE);
    puts("PASS: wrong-image rejection, bounded sparse lookup, invalid/cyclic pointers, five-byte prepared span, prepare leaves target unchanged");
    return 0;
}
#endif
