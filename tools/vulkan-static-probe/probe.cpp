#define WIN32_LEAN_AND_MEAN
#define NOMINMAX
#include <windows.h>
#include <bcrypt.h>
#include <intrin.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <atomic>
#include <string>
#include <vector>
#ifdef PROBE_FULL_WORKSPACE
#include "snapshot-schema-v4.h"
static constexpr int probeVersion=4, expectedCount=912;
#else
#include "snapshot-schema.h"
static constexpr int probeVersion=3, expectedCount=907;
#endif
#ifndef PROBE_IMAGE_HASH
#error Require the deterministic diagnostic image hash
#endif
#ifndef PROBE_THUNK_RVA
#error Require the diagnostic wrapper RVA
#endif

// A normal, statically imported DLL. No DllMain worker, hooks, setters or IPC.
static std::atomic<int> state{0};
static uintptr_t base;
static bool copyRead(uintptr_t p,void* out,size_t n) {
    if(p<0x10000||p%4||n>65536||p+n<p)return false;
    MEMORY_BASIC_INFORMATION m{};
    if(!VirtualQuery((void*)p,&m,sizeof(m))||m.State!=MEM_COMMIT||
       (m.Protect&(PAGE_NOACCESS|PAGE_GUARD))||p+n>(uintptr_t)m.BaseAddress+m.RegionSize)return false;
    __try {memcpy(out,(void*)p,n);return true;}
    __except(EXCEPTION_EXECUTE_HANDLER){return false;}
}
template<class T>static bool read(uintptr_t p,T& v){return copyRead(p,&v,sizeof(v));}
static std::string digest(const unsigned char* p,size_t n) {
    BCRYPT_ALG_HANDLE a{};BCRYPT_HASH_HANDLE h{};unsigned char bytes[32]{};char hex[65]{};
    if(BCryptOpenAlgorithmProvider(&a,BCRYPT_SHA256_ALGORITHM,nullptr,0)<0)return {};
    bool ok=BCryptCreateHash(a,&h,nullptr,0,nullptr,0,0)>=0;
    if(ok)ok=n<=0xffffffffULL&&BCryptHashData(h,const_cast<PUCHAR>(p),(ULONG)n,0)>=0&&BCryptFinishHash(h,bytes,32,0)>=0;
    if(h)BCryptDestroyHash(h);BCryptCloseAlgorithmProvider(a,0);
    if(!ok)return {};
    for(int i=0;i<32;i++)sprintf_s(hex+2*i,3,"%02x",bytes[i]);
    return hex;
}
static bool preflight() {
    wchar_t path[MAX_PATH]{};if(!GetModuleFileNameW(nullptr,path,MAX_PATH))return false;
    HANDLE f=CreateFileW(path,GENERIC_READ,FILE_SHARE_READ,nullptr,OPEN_EXISTING,0,nullptr);
    if(f==INVALID_HANDLE_VALUE)return false;
    DWORD n=GetFileSize(f,nullptr),got=0;std::vector<unsigned char> bytes(n>=15089416&&n<=17000000?n:0);
    bool ok=!bytes.empty()&&ReadFile(f,bytes.data(),n,&got,nullptr)&&got==n;
    CloseHandle(f);if(!ok||digest(bytes.data(),bytes.size())!=PROBE_IMAGE_HASH)return false;
    struct Span{uintptr_t rva;size_t n;const char* hash;};
    const Span spans[]={
      {0x1c830,0x8b1,"6c051fb5fb9cb2c2b1fc9a38c34ac9ae62a25bfbb5396047c14defd77f0fc4a6"},
      {0x1b070,0x1ab,"e07682c0fa905789eb7a9867be0a73d823a7ed2aaf5312b80c7014f27131991b"},
      {0x21bc0,0x2412,"abf6cdbedc671e86acd0dddd2ae0a8a24f7b79b35f31657ddf8dc8260e0ea01d"},
      {0x1471e0,0x5dc,"b0318cf9af6e400158fd23f9ae8329b9a7b305b429d8b0adebcb40fdc086245e"},
      {0x147c40,0x94,"ffd0f52280cbe972b26b619d94611e6f4509573873b7af225c7a95907986c8c7"},
      {0x25550,0x1c4,"4da9e9691d5aac059f9c1c61b17545450daf077267dc9c111707a6f4c7fc74ae"},
      {0x2eb480,0x8f,"fd3bbc7aa8c049def62da0d0738a95ea9cdaaf1bc65a18eada8f16c5a64c509b"},
      {0x1501d0,0x43,"8312f51437d3248a22845ec728983d767b6ca16c7df3713e21aabbc3ec080e15"},
      {0x10dff0,0x42,"0f794d5143bea4b5f54a839b5b8c0096862842bc9e0861b0d3fc6524bb6f3931"}};
    for(auto s:spans){std::vector<unsigned char> b(s.n);if(!copyRead(base+s.rva,b.data(),s.n)||digest(b.data(),b.size())!=s.hash)return false;}
    uintptr_t target=0;return read(base+0xc61ce0,target)&&target==base+PROBE_THUNK_RVA;
}
struct Sample {
    uint64_t bucketCount=0,elements=0;uintptr_t node=0;
    int value=0;unsigned char tag=255;unsigned visited=0;
    const char* outcome="invalid-table";bool valid=false,found=false;
};
static bool lookup(uintptr_t domain,int id,Sample& s) {
    uintptr_t buckets=0,node=0,sentinel=0;
    if(!read(domain+0x18,buckets)||!read(domain+0x20,s.bucketCount)||!read(domain+0x28,s.elements)||
       !s.bucketCount||s.bucketCount>32768||!s.elements||s.elements>32768||buckets%8)return false;
    if(!read(buckets+s.bucketCount*8,sentinel)||!read(buckets+(id%s.bucketCount)*8,node))return false;
    uintptr_t seen[512];size_t used=0;
    while(node&&node!=sentinel&&used<512&&used<s.elements) {
        if(node%8)return false;
        for(size_t j=0;j<used;j++)if(seen[j]==node){s.outcome="cycle";return false;}
        seen[used++]=node;s.visited=(unsigned)used;
        int key=0;if(!read(node,key))return false;
        // Identity hash for positive descriptor IDs, confirmed by Vulkan getter.
        if(key<0||(uint64_t)key%s.bucketCount!=(uint64_t)id%s.bucketCount){s.outcome="bucket-key-mismatch";return false;}
        if(key==id){
            s.node=node;s.found=true;
            // Exact Vulkan integer accessor requires variant tag 0 at value+0x18.
            if(!read(node+0x20,s.tag)||s.tag!=0){s.outcome="non-integer-tag";return false;}
            if(!read(node+8,s.value))return false;
            s.outcome="found-int32";s.valid=true;return true;
        }
        if(!read(node+0x28,node))return false;
    }
    if(!node||node==sentinel){s.outcome=node?"absent-sentinel":"absent-null";s.valid=true;return true;}
    s.outcome="traversal-bound";
    return false;
}
struct Control {int id;int bootstrap;};
static const Control controls[]={{2852,319951120},{2912,32},{3721,100992003},{4955,16780678},{5139,-2146664148},{6458,8390656},{3296,0}};
struct Results {Sample items[7]{};bool stable[7]{};};
static void report(const char* status,const char* phase,const Results& r) {
    char path[MAX_PATH];sprintf_s(path,"C:\\Games\\950OpenSource\\logs\\workspace-static-v%d-%lu-%s-controls.json",probeVersion,GetCurrentProcessId(),phase);
    HANDLE f=CreateFileA(path,GENERIC_WRITE,FILE_SHARE_READ,nullptr,CREATE_NEW,FILE_ATTRIBUTE_NORMAL,nullptr);
    if(f==INVALID_HANDLE_VALUE)return;
    char line[8192];int n=sprintf_s(line,"{\"status\":\"%s\",\"phase\":\"%s\",\"pid\":%lu,\"thread\":%lu,\"nativeWrites\":false,\"controls\":[",status,phase,GetCurrentProcessId(),GetCurrentThreadId());
    for(int i=0;i<7;i++) {
        const auto& a=r.items[i];
        n+=sprintf_s(line+n,sizeof(line)-n,"%s{\"id\":%d,\"bootstrapMember\":%s,\"bucketCount\":%llu,\"elementCount\":%llu,\"outcome\":\"%s\",\"found\":%s,\"stable\":%s,\"variantTag\":%u,\"int32\":%d,\"matchesBootstrap\":%s,\"visited\":%u}",
          i?",":"",controls[i].id,i<6?"true":"false",a.bucketCount,a.elements,a.outcome,a.found?"true":"false",r.stable[i]?"true":"false",a.tag,a.value,(i<6&&a.found&&a.value==controls[i].bootstrap)?"true":"false",a.visited);
    }
    n+=sprintf_s(line+n,sizeof(line)-n,"]}\n");
    DWORD written=0;if(n>0)WriteFile(f,line,(DWORD)n,&written,nullptr);CloseHandle(f);
}
static bool equalSample(const Sample& a,const Sample& b) {
    return a.valid&&b.valid&&a.node==b.node&&a.found==b.found&&a.value==b.value&&a.tag==b.tag&&
        a.bucketCount==b.bucketCount&&a.elements==b.elements&&strcmp(a.outcome,b.outcome)==0;
}
static bool snapshot(uintptr_t domain,const char* phase) {
    constexpr size_t count=sizeof(workspaceIds)/sizeof(workspaceIds[0]);
    static_assert(count==expectedCount,"Unexpected snapshot scope");
    std::vector<Sample> first(count),second(count);
    bool valid=true;
    // Two complete passes on the native main-logic thread, never native getters/setters.
    for(size_t i=0;i<count;i++)if(!lookup(domain,workspaceIds[i],first[i]))valid=false;
    for(size_t i=0;i<count;i++)if(!lookup(domain,workspaceIds[i],second[i]))valid=false;
    for(size_t i=0;i<count;i++)
        if(!equalSample(first[i],second[i])||first[i].bucketCount!=first[0].bucketCount||first[i].elements!=first[0].elements)valid=false;
    char line[1024];
    sprintf_s(line,"{\"version\":%d,\"status\":\"%s\",\"phase\":\"%s\",\"schemaSha256\":\"%s\",\"imageSha256\":\"%s\",\"pid\":%lu,\"thread\":%lu,\"tick\":%llu,\"nativeWrites\":false,\"bucketCount\":%llu,\"elementCount\":%llu,\"items\":[",
        probeVersion,valid?"snapshot-stable":"snapshot-refused",phase,schemaHash,PROBE_IMAGE_HASH,GetCurrentProcessId(),GetCurrentThreadId(),GetTickCount64(),first[0].bucketCount,first[0].elements);
    std::string json=line;
    for(size_t i=0;i<count;i++) {
        const auto& a=first[i];char value[32],tag[16];
        if(a.found&&a.valid)sprintf_s(value,"%d",a.value);else strcpy_s(value,"null");
        if(a.found)sprintf_s(tag,"%u",a.tag);else strcpy_s(tag,"null");
        sprintf_s(line,"%s{\"id\":%d,\"found\":%s,\"stable\":%s,\"variantTag\":%s,\"int32\":%s,\"outcome\":\"%s\"}",
            i?",":"",workspaceIds[i],a.found?"true":"false",equalSample(a,second[i])?"true":"false",tag,value,a.outcome);
        json+=line;
    }
    json+="]}\n";
    char path[MAX_PATH];sprintf_s(path,"C:\\Games\\950OpenSource\\logs\\workspace-static-v%d-%lu-%s.json",probeVersion,GetCurrentProcessId(),phase);
    HANDLE f=CreateFileA(path,GENERIC_WRITE,FILE_SHARE_READ,nullptr,CREATE_NEW,FILE_ATTRIBUTE_NORMAL,nullptr);
    if(f==INVALID_HANDLE_VALUE)return false;
    DWORD written=0;bool saved=WriteFile(f,json.data(),(DWORD)json.size(),&written,nullptr)&&written==json.size();
    CloseHandle(f);return valid&&saved;
}
extern "C" __declspec(dllexport) void WorkspaceProbe(void* client) noexcept {
    // Only the image wrapper calls this export. A different caller is refused.
    uintptr_t caller=(uintptr_t)_ReturnAddress(),image=(uintptr_t)GetModuleHandleW(nullptr);
    if(caller<image+PROBE_THUNK_RVA||caller>=image+PROBE_THUNK_RVA+512)return;
    if(state.load()!=0)return;
    int expected=0;if(!state.compare_exchange_strong(expected,1))return;
    base=image;
    try {
        static bool checked=false;
        static unsigned shots=0;static bool held=false;static DWORD owner=0;
        const char* phases[]={"A","B","C"};
        Results results{};const char* phase=phases[shots];
        if(!checked){if(!preflight()){report("preflight-refused",phase,results);state.store(2);return;}checked=true;}
        uintptr_t anchor=0,secondary=0,vt=0,manager=0,stats=0,statHead=0;int mode=0;
        if(!read(base+0xfb8258,anchor)||!read(base+0xdb6088,secondary)||anchor!=(uintptr_t)client||anchor!=secondary||
           !read(anchor,vt)||vt!=base+0xc61bf0){report("client-refused",phase,results);state.store(2);return;}
        // Exact-950 MainLogicManager update checks this state for world ownership.
        if(!read(anchor+0x19fa0,mode)){report("state-unreadable",phase,results);state.store(2);return;}
        DWORD foregroundPid=0;GetWindowThreadProcessId(GetForegroundWindow(),&foregroundPid);
        bool pressed=foregroundPid==GetCurrentProcessId()&&(GetAsyncKeyState(VK_CONTROL)&0x8000)&&
            (GetAsyncKeyState(VK_SHIFT)&0x8000)&&(GetAsyncKeyState(VK_F9)&0x8000);
        bool trigger=pressed&&!held;held=pressed;
        if(mode!=30||!trigger){state.store(0);return;}
        if(owner&&owner!=GetCurrentThreadId()){report("thread-changed",phase,results);state.store(2);return;}
        owner=GetCurrentThreadId();
        if(!read(anchor+0x19920,manager)||!manager||manager%8||!read(manager+0x7618,stats)||!stats||!read(stats,statHead)) {
            report("manager-refused",phase,results);state.store(2);return;
        }
        uintptr_t domain=manager+0x7620,dvt=0,basevt=0;
        if(!read(domain,dvt)||dvt!=base+0xc6dbb8||!read(domain+8,basevt)||basevt!=base+0xc6d818){report("domain-vtable-refused",phase,results);state.store(2);return;}
        bool valid=true,controlsFound=true;
        for(int i=0;i<7;i++) {
            auto& a=results.items[i];Sample b{};
            bool ok=lookup(domain,controls[i].id,a)&&lookup(domain,controls[i].id,b);
            results.stable[i]=ok&&a.node==b.node&&a.found==b.found&&a.value==b.value&&a.tag==b.tag&&a.bucketCount==b.bucketCount&&a.elements==b.elements&&strcmp(a.outcome,b.outcome)==0;
            valid=valid&&results.stable[i];if(i<6)controlsFound=controlsFound&&a.found;
        }
        const char* status=!valid?"reader-invariant-failed":!controlsFound?"bootstrap-controls-missing":results.items[6].found?"controls-and-3296-resolved":"controls-resolved-3296-absent";
        report(status,phase,results);
        if(!valid||!controlsFound||!snapshot(domain,phase)){state.store(2);return;}
        ++shots;state.store(shots==3?2:0);
    }catch(...){Results r{};report("diagnostic-exception","exception",r);state.store(2);}
}
#ifdef PROBE_TEST
int main() {
    base=(uintptr_t)GetModuleHandleW(nullptr);
    if(preflight())return 1;
    alignas(8) unsigned char domain[128]{},node[48]{};uintptr_t buckets[2]{};
    buckets[0]=(uintptr_t)node;*(uintptr_t*)(domain+0x18)=(uintptr_t)buckets;
    *(uint64_t*)(domain+0x20)=1;*(uint64_t*)(domain+0x28)=1;
    *(int*)node=3296;*(int*)(node+8)=1234567;Sample s{};
    if(!lookup((uintptr_t)domain,3296,s)||s.value!=1234567||s.node!=(uintptr_t)node||!s.found)return 2;
    *(uint64_t*)(domain+0x20)=0;if(lookup((uintptr_t)domain,3296,s))return 3;
    *(uint64_t*)(domain+0x20)=32769;if(lookup((uintptr_t)domain,3296,s))return 4;
    *(uint64_t*)(domain+0x20)=1;*(uint64_t*)(domain+0x28)=2;
    *(int*)node=1;*(uintptr_t*)(node+0x28)=(uintptr_t)node;if(lookup((uintptr_t)domain,3296,s))return 5;
    buckets[0]=1;if(lookup((uintptr_t)domain,3296,s))return 6;
    buckets[0]=0;s={};if(!lookup((uintptr_t)domain,3296,s)||s.found||!s.valid)return 7;
    buckets[0]=(uintptr_t)node;buckets[1]=(uintptr_t)node;s={};if(!lookup((uintptr_t)domain,3296,s)||s.found)return 8;
    buckets[1]=0;*(int*)node=3296;node[0x20]=2;s={};if(lookup((uintptr_t)domain,3296,s))return 9;
    node[0x20]=0;*(int*)(node+8)=0;s={};if(!lookup((uintptr_t)domain,3296,s)||!s.found||s.value!=0)return 10;
    puts("PASS: typed direct-key lookup, zero versus absent, sentinel, bounds, cycles and wrong-image refusal");return 0;
}
#endif
