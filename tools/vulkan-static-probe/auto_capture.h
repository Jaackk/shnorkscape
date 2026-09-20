// V5 only. Fixed 912-ID snapshot transport; no native setters or arbitrary read API.
#include <iphlpapi.h>
static std::atomic<uint64_t> autoCommand{0},autoResult{0};
static std::atomic<bool> autoDisabled{false};
static std::vector<unsigned char> autoBody;
static unsigned autoStatus=0;
static constexpr uint32_t autoMagic=0x57534335;
static void put32(std::vector<unsigned char>& b,uint32_t v){for(int i=3;i>=0;--i)b.push_back((unsigned char)(v>>(i*8)));}
static void put64(std::vector<unsigned char>& b,uint64_t v){put32(b,(uint32_t)(v>>32));put32(b,(uint32_t)v);}
static uint32_t get32(const unsigned char* b){return ((uint32_t)b[0]<<24)|((uint32_t)b[1]<<16)|((uint32_t)b[2]<<8)|b[3];}
static void autoLog(const char* status) {
    static unsigned lines=0;if(++lines>256)return;
    char path[MAX_PATH],line[160];sprintf_s(path,"C:\\Games\\950OpenSource\\logs\\workspace-auto-v5-%lu.log",GetCurrentProcessId());
    HANDLE f=CreateFileA(path,FILE_APPEND_DATA,FILE_SHARE_READ,nullptr,OPEN_ALWAYS,FILE_ATTRIBUTE_NORMAL,nullptr);
    if(f==INVALID_HANDLE_VALUE)return;
    int n=sprintf_s(line,"tick=%llu status=%s nativeWrites=false\r\n",GetTickCount64(),status);DWORD written=0;
    WriteFile(f,line,(DWORD)n,&written,nullptr);CloseHandle(f);
}
static bool autoServer(HANDLE pipe) {
    ULONG pid=0;if(!GetNamedPipeServerProcessId(pipe,&pid)||!pid)return false;
    DWORD size=0;if(GetExtendedTcpTable(nullptr,&size,FALSE,AF_INET,TCP_TABLE_OWNER_PID_ALL,0)!=ERROR_INSUFFICIENT_BUFFER||size>1048576||size<4)return false;
    std::vector<unsigned char> bytes(size);
    if(GetExtendedTcpTable(bytes.data(),&size,FALSE,AF_INET,TCP_TABLE_OWNER_PID_ALL,0)!=NO_ERROR)return false;
    auto table=(MIB_TCPTABLE_OWNER_PID*)bytes.data();
    if(table->dwNumEntries>16384||4ULL+24ULL*table->dwNumEntries>size)return false;
    for(DWORD i=0;i<table->dwNumEntries;i++) {
        const auto& client=table->table[i];
        if(client.dwOwningPid!=GetCurrentProcessId()||client.dwState!=MIB_TCP_STATE_ESTAB||ntohs((u_short)client.dwRemotePort)!=43650||
           (client.dwLocalAddr&255)!=127||(client.dwRemoteAddr&255)!=127)continue;
        for(DWORD j=0;j<table->dwNumEntries;j++) {
            const auto& server=table->table[j];
            if(server.dwOwningPid==pid&&server.dwState==MIB_TCP_STATE_ESTAB&&server.dwLocalAddr==client.dwRemoteAddr&&
               server.dwRemoteAddr==client.dwLocalAddr&&server.dwLocalPort==client.dwRemotePort&&server.dwRemotePort==client.dwLocalPort)return true;
        }
    }return false;
}
static bool autoRead(HANDLE f,unsigned char* b,DWORD count) {
    DWORD at=0;while(at<count&&!autoDisabled.load()) {
        DWORD available=0;if(!PeekNamedPipe(f,nullptr,0,nullptr,&available,nullptr))return false;
        if(!available){Sleep(20);continue;}
        DWORD got=0;DWORD wanted=count-at<available?count-at:available;
        if(!ReadFile(f,b+at,wanted,&got,nullptr)||!got)return false;at+=got;
    }return at==count;
}
static DWORD WINAPI autoWorker(void*) {
    uint64_t generation=0;
    while(!autoDisabled.load()) {
        HANDLE pipe=CreateFileW(L"\\\\.\\pipe\\shnork-workspace-disposable-v5",GENERIC_READ|GENERIC_WRITE,0,nullptr,OPEN_EXISTING,0,nullptr);
        if(pipe==INVALID_HANDLE_VALUE){Sleep(500);continue;}
        DWORD mode=PIPE_NOWAIT;
        if(!SetNamedPipeHandleState(pipe,&mode,nullptr,nullptr)||!autoServer(pipe)){CloseHandle(pipe);autoLog("peer-refused");Sleep(1000);continue;}
        autoLog("paired-to-game-server");
        unsigned char header[32];
        unsigned char nonce[16]{};uint64_t last=0;
        while(autoRead(pipe,header,32)) {
            if(get32(header)!=autoMagic)break;
            uint64_t sequence=((uint64_t)get32(header+20)<<32)|get32(header+24);
            if(!last)memcpy(nonce,header+4,16);
            if(memcmp(nonce,header+4,16))break;
            uint32_t kind=get32(header+28);
            if(kind==2){if(sequence!=last)break;autoLog("durable-receipt");continue;}
            if(kind!=1||sequence<=last)break;
            last=sequence;
            uint64_t request=++generation;
            autoCommand.store(request,std::memory_order_release);
            ULONGLONG deadline=GetTickCount64()+120000;
            while(autoResult.load(std::memory_order_acquire)!=request&&!autoDisabled.load()&&GetTickCount64()<deadline)Sleep(10);
            bool ready=autoResult.load(std::memory_order_acquire)==request;
            std::vector<unsigned char> frame(header,header+28);
            put32(frame,ready?autoStatus:2);put32(frame,ready?(uint32_t)autoBody.size():0);
            if(ready)frame.insert(frame.end(),autoBody.begin(),autoBody.end());
            autoCommand.store(0,std::memory_order_release);
            DWORD written=0;
            if(frame.size()>8192||!WriteFile(pipe,frame.data(),(DWORD)frame.size(),&written,nullptr)||written!=frame.size())break;
            autoLog(ready&&autoStatus==1?"native-exit-snapshot-sent":"native-exit-snapshot-refused");
        }
        autoCommand.store(0,std::memory_order_release);CloseHandle(pipe);Sleep(500);
    }return 0;
}
static void autoStart() {
    static bool started=false;if(started)return;started=true;
    HANDLE worker=CreateThread(nullptr,0,autoWorker,nullptr,0,nullptr);
    if(worker)CloseHandle(worker);else autoDisabled.store(true);
}
static void autoTick(uintptr_t domain) {
    uint64_t request=autoCommand.load(std::memory_order_acquire);
    static uint64_t seen=0,entered=0;
    if(!request||autoResult.load(std::memory_order_relaxed)==request)return;
    if(request!=seen){seen=request;entered=0;}
    Sample edit{},again{};
    if(!lookup(domain,3477,edit)||!lookup(domain,3477,again)||!equalSample(edit,again)||!edit.found)return;
    if(edit.value==1){if(!entered)entered=GetTickCount64();return;}
    if(edit.value!=0||!entered)return;
    uint64_t exited=GetTickCount64();
    std::vector<Sample> a(expectedCount),b(expectedCount);bool valid=true;
    for(int i=0;i<expectedCount;i++)if(!lookup(domain,workspaceIds[i],a[i]))valid=false;
    for(int i=0;i<expectedCount;i++)if(!lookup(domain,workspaceIds[i],b[i])||!equalSample(a[i],b[i])||a[i].bucketCount!=a[0].bucketCount||a[i].elements!=a[0].elements)valid=false;
    for(int i=0;i<6;i++){Sample x{},y{};if(!lookup(domain,controls[i].id,x)||!lookup(domain,controls[i].id,y)||!x.found||!equalSample(x,y))valid=false;}
    std::vector<unsigned char> body;
    if(valid) {
        put64(body,entered);put64(body,exited);put64(body,GetTickCount64());
        body.push_back((unsigned char)(expectedCount>>8));body.push_back((unsigned char)(expectedCount&255));
        for(int i=0;i<expectedCount;i++) {
            body.push_back((unsigned char)(workspaceIds[i]>>8));body.push_back((unsigned char)workspaceIds[i]);
            body.push_back(a[i].found?1:0);if(a[i].found)put32(body,(uint32_t)a[i].value);
        }
    }
    autoBody=std::move(body);autoStatus=valid?1:2;
    autoResult.store(request,std::memory_order_release);
}
