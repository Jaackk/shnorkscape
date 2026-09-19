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
      {0x25550,0x1c4,"4da9e9691d5aac059f9c1c61b17545450daf077267dc9c111707a6f4c7fc74ae"}};
    for(auto s:spans){std::vector<unsigned char> b(s.n);if(!copyRead(base+s.rva,b.data(),s.n)||digest(b.data(),b.size())!=s.hash)return false;}
    uintptr_t target=0;return read(base+0xc61ce0,target)&&target==base+PROBE_THUNK_RVA;
}
struct Sample {uint64_t bucketCount=0,elements=0;uintptr_t node=0;int value=0;};
static bool lookup(uintptr_t domain,Sample& s) {
    uintptr_t buckets=0,node=0;
    if(!read(domain+0x18,buckets)||!read(domain+0x20,s.bucketCount)||!read(domain+0x28,s.elements)||
       !s.bucketCount||s.bucketCount>32768||!s.elements||s.elements>32768||buckets%8)return false;
    if(!read(buckets+(3296%s.bucketCount)*8,node))return false;
    uintptr_t seen[512];size_t used=0;
    while(node&&used<512&&used<s.elements) {
        if(node%8)return false;
        for(size_t j=0;j<used;j++)if(seen[j]==node)return false;
        seen[used++]=node;int id=0;if(!read(node,id))return false;
        if(id==3296){s.node=node;return read(node+8,s.value);}
        if(!read(node+0x28,node))return false;
    }
    return false;
}
static void report(const char* status,const Sample& a,bool stable) {
    char path[MAX_PATH];sprintf_s(path,"C:\\Games\\950OpenSource\\logs\\workspace-static-%lu.json",GetCurrentProcessId());
    HANDLE f=CreateFileA(path,GENERIC_WRITE,FILE_SHARE_READ,nullptr,CREATE_NEW,FILE_ATTRIBUTE_NORMAL,nullptr);
    if(f==INVALID_HANDLE_VALUE)return;
    char line[1024];int n=sprintf_s(line,"{\"status\":\"%s\",\"pid\":%lu,\"thread\":%lu,\"id\":3296,\"bucketCount\":%llu,\"elementCount\":%llu,\"found\":%s,\"stable\":%s,\"int32Projection\":%d,\"nativeWrites\":false}\n",
      status,GetCurrentProcessId(),GetCurrentThreadId(),a.bucketCount,a.elements,a.node?"true":"false",stable?"true":"false",a.value);
    DWORD written=0;if(n>0)WriteFile(f,line,(DWORD)n,&written,nullptr);CloseHandle(f);
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
        Sample a{},b{};
        if(!checked){if(!preflight()){report("preflight-refused",a,false);state.store(2);return;}checked=true;}
        uintptr_t anchor=0,secondary=0,vt=0,manager=0,stats=0,statHead=0;int mode=0;
        if(!read(base+0xfb8258,anchor)||!read(base+0xdb6088,secondary)||anchor!=(uintptr_t)client||anchor!=secondary||
           !read(anchor,vt)||vt!=base+0xc61bf0){report("client-refused",a,false);state.store(2);return;}
        // Exact-950 MainLogicManager update checks this state for world ownership.
        if(!read(anchor+0x19fa0,mode)){report("state-unreadable",a,false);state.store(2);return;}
        if(mode!=30){state.store(0);return;}
        if(!read(anchor+0x19920,manager)||!manager||manager%8||!read(manager+0x7618,stats)||!stats||!read(stats,statHead)) {
            report("manager-refused",a,false);state.store(2);return;
        }
        bool ok=lookup(manager+0x7620,a)&&lookup(manager+0x7620,b)&&a.node==b.node&&a.value==b.value&&a.bucketCount==b.bucketCount&&a.elements==b.elements;
        report(ok?"3296-read-passed":"3296-read-failed",a,ok);state.store(2);
    }catch(...){Sample a{};report("diagnostic-exception",a,false);state.store(2);}
}
#ifdef PROBE_TEST
int main() {
    base=(uintptr_t)GetModuleHandleW(nullptr);
    if(preflight())return 1;
    alignas(8) unsigned char domain[128]{},node[48]{};uintptr_t buckets[2]{};
    buckets[0]=(uintptr_t)node;*(uintptr_t*)(domain+0x18)=(uintptr_t)buckets;
    *(uint64_t*)(domain+0x20)=1;*(uint64_t*)(domain+0x28)=1;
    *(int*)node=3296;*(int*)(node+8)=1234567;Sample s{};
    if(!lookup((uintptr_t)domain,s)||s.value!=1234567||s.node!=(uintptr_t)node)return 2;
    *(uint64_t*)(domain+0x20)=0;if(lookup((uintptr_t)domain,s))return 3;
    *(uint64_t*)(domain+0x20)=32769;if(lookup((uintptr_t)domain,s))return 4;
    *(uint64_t*)(domain+0x20)=1;*(uint64_t*)(domain+0x28)=2;
    *(int*)node=1;*(uintptr_t*)(node+0x28)=(uintptr_t)node;if(lookup((uintptr_t)domain,s))return 5;
    buckets[0]=1;if(lookup((uintptr_t)domain,s))return 6;
    puts("PASS: wrong-image refusal, 3296 projection, bounds, cycle and invalid-pointer refusal");return 0;
}
#endif
