package com.rs.game.player.client;

import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** Opt-in local diagnostic timeline. It is deliberately outside every gameplay decision. */
final class Native950BugTest {
    private static final ThreadPoolExecutor WRITER = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(1024), daemon("native950-bugtest"), new ThreadPoolExecutor.DiscardPolicy());
    private static final Map<Player, Session> SESSIONS = new IdentityHashMap<Player, Session>();
    private Native950BugTest() { }

    static synchronized boolean toggle(Player player) {
        Session prior = SESSIONS.remove(player);
        if (prior != null) { prior.event("session", "disabled", "reason", "command"); prior.close(); return false; }
        Session next = new Session(player);
        SESSIONS.put(player, next);
        next.event("session", "enabled", "player", player.getUsername(), "tile", tile(player));
        return true;
    }

    static synchronized void close(Player player, String reason) {
        Session session = SESSIONS.remove(player);
        if (session == null) return;
        session.event("session", "closed", "reason", reason, "state", state(player));
        session.close();
    }

    static void marker(Player player, String description) {
        Session session = session(player);
        if (session == null) return;
        String stamp = stamp();
        String image = "bug-" + stamp + ".png";
        session.event("marker", "bug", "description", description == null || description.trim().isEmpty() ? "(no description)" : description.trim(),
                "screenshot", image, "state", state(player));
        capture(session, image);
    }

    static void command(Player player, String command, String argument) {
        Session session = session(player);
        if (session != null) session.event("command", "development", "command", command,
                "argument", argument == null ? "" : argument, "state", state(player));
    }

    static void action(Player player, Native950Actions.Action action) {
        Session session = session(player);
        if (session == null) return;
        if (action instanceof Native950Actions.InterfaceAction) {
            Native950Actions.InterfaceAction a=(Native950Actions.InterfaceAction)action;
            session.event("input", "interface", "interface", a.interfaceId(), "component", a.componentId(), "slot", a.slot(), "option", a.option(), "item", a.itemId());
        } else if (action instanceof Native950Actions.DragAction) {
            Native950Actions.DragAction a=(Native950Actions.DragAction)action;
            session.event("input", "drag", "source", component(a.sourceInterfaceId(),a.sourceComponentId()), "sourceSlot", a.sourceSlot(),
                    "sourceItem", a.sourceItemId(), "target", component(a.targetInterfaceId(),a.targetComponentId()), "targetSlot", a.targetSlot(), "targetItem", a.targetItemId());
        } else if (action instanceof Native950Actions.NpcAction) {
            Native950Actions.NpcAction a=(Native950Actions.NpcAction)action;
            session.event("input", "npc", "index", a.index(), "option", a.option());
        } else if (action instanceof Native950Actions.CloseModalAction) {
            session.event("interface", "close-modal");
        }
    }

    static void event(Player player, String category, String name, Object... fields) {
        Session session = session(player);
        if (session != null) session.event(category, name, fields);
    }

    private static synchronized Session session(Player player) { return SESSIONS.get(player); }
    private static String component(int face,int child) { return face+":"+child; }
    private static String tile(Player p) { return p.getX()+","+p.getY()+","+p.getPlane(); }
    private static String state(Player p) {
        return "tile="+tile(p)+";bar="+(p.getNative950ActionBar().activeBar()+1)+";revolution="+p.getNative950ActionBar().isRevolutionEnabled()
                +";prayer="+(p.getPrayer()==null?-1:p.getPrayer().getPrayerpoints())+";adrenaline="+p.getCombatDefinitions().getSpecialAttackPercentage();
    }
    private static String stamp() { return new SimpleDateFormat("yyyyMMdd-HHmmss-SSS", Locale.ROOT).format(new Date()); }
    private static ThreadFactory daemon(final String name) { return new ThreadFactory() { public Thread newThread(Runnable run) { Thread thread=new Thread(run,name);thread.setDaemon(true);return thread; } }; }

    private static void capture(final Session session, final String image) {
        WRITER.execute(new Runnable() { public void run() {
            File output=new File(session.directory,image);
            String executable=powershell();
            String script="$ErrorActionPreference='Stop'; $p=Get-Process -Name rs2client-vulkan -ErrorAction Stop | Where-Object {$_.MainWindowHandle -ne 0} | Select-Object -First 1; if($null -eq $p){throw 'RuneTek window was not found'}; Add-Type -AssemblyName System.Drawing; Add-Type @'\nusing System; using System.Runtime.InteropServices; public static class BugTestWindow { [DllImport(\"user32.dll\")] public static extern bool GetWindowRect(IntPtr h,out RECT r); public struct RECT { public int Left,Top,Right,Bottom; } }\n'@; $r=New-Object BugTestWindow+RECT; if(-not [BugTestWindow]::GetWindowRect($p.MainWindowHandle,[ref]$r)){throw 'GetWindowRect failed'}; $w=$r.Right-$r.Left; $h=$r.Bottom-$r.Top; if($w -le 0 -or $h -le 0){throw 'window is not visible'}; Write-Output ('windowHandle=0x{0:X};bounds={1},{2},{3},{4}' -f $p.MainWindowHandle,$r.Left,$r.Top,$r.Right,$r.Bottom); $b=New-Object System.Drawing.Bitmap $w,$h; $g=[System.Drawing.Graphics]::FromImage($b); try {$g.CopyFromScreen($r.Left,$r.Top,0,0,$b.Size); $b.Save('"+ps(output.getAbsolutePath())+"',[System.Drawing.Imaging.ImageFormat]::Png)} finally {$g.Dispose(); $b.Dispose()}";
            try {
                String encoded=java.util.Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE));
                Process process=new ProcessBuilder(executable,"-NoProfile","-NonInteractive","-ExecutionPolicy","Bypass","-EncodedCommand",encoded).redirectErrorStream(true).start();
                int exit=process.waitFor();
                String outputText=read(process.getInputStream());
                session.event("screenshot",exit==0&&output.isFile()&&output.length()>0?"saved":"failed","file",image,"exit",exit,
                        "helper",executable,"output",outputText,"bytes",output.isFile()?output.length():0);
            } catch(Throwable failure) { session.event("screenshot","failed","file",image,"helper",executable,
                    "error",failure.getClass().getSimpleName(),"message",safeMessage(failure)); }
        }});
    }
    private static String powershell() {
        String root=System.getenv("SystemRoot"); if(root==null||root.trim().isEmpty())root="C:\\Windows";
        return new File(root,"System32\\WindowsPowerShell\\v1.0\\powershell.exe").getAbsolutePath();
    }
    private static String read(InputStream input) throws java.io.IOException {
        byte[] buffer=new byte[1024]; int total=0,read; StringBuilder out=new StringBuilder();
        while(total<8192&&(read=input.read(buffer,0,Math.min(buffer.length,8192-total)))>=0){out.append(new String(buffer,0,read,StandardCharsets.UTF_8));total+=read;}
        return out.toString().trim();
    }
    private static String safeMessage(Throwable failure) { String value=failure.getMessage(); return value==null?"":value.length()>1024?value.substring(0,1024):value; }
    private static String ps(String path) { return path.replace("'", "''"); }

    private static final class Session {
        final File directory, log;
        Session(Player player) {
            directory=new File(new File(new File(System.getProperty("user.dir")).getParentFile(),"logs"),"bugtest"+File.separator+"session-"+stamp()+"-"+safe(player.getUsername()));
            directory.mkdirs(); log=new File(directory,"timeline.jsonl");
        }
        void event(final String category, final String name, final Object... fields) {
            final String line=json(category,name,fields);
            WRITER.execute(new Runnable() { public void run() { try {
                BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(log,true),StandardCharsets.UTF_8));
                try { writer.write(line); writer.newLine(); } finally { writer.close(); }
            } catch(Throwable ignored) { } }});
        }
        void close() { }
        private static String json(String category,String name,Object... fields) {
            StringBuilder out=new StringBuilder("{\"time\":\"").append(escape(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX",Locale.ROOT).format(new Date()))).append("\",\"category\":\"").append(escape(category)).append("\",\"event\":\"").append(escape(name)).append('"');
            for(int i=0;i+1<fields.length;i+=2)out.append(",\"").append(escape(String.valueOf(fields[i]))).append("\":\"").append(escape(String.valueOf(fields[i+1]))).append('"');
            return out.append('}').toString();
        }
        private static String escape(String value) { return value.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r"); }
        private static String safe(String value) { return value==null?"player":value.replaceAll("[^A-Za-z0-9._-]","_"); }
    }
}
