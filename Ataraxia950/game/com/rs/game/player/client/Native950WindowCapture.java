package com.rs.game.player.client;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/** Windows-only, fail-open capture of the visible RuneTek Vulkan window. */
final class Native950WindowCapture {
    private static final int OUTPUT_LIMIT = 8192;

    static final class Result {
        final boolean saved;
        final int exitCode;
        final String helper, command, output, errorType, errorMessage;
        final long bytes;

        Result(boolean saved, int exitCode, String helper, String command, String output,
               String errorType, String errorMessage, long bytes) {
            this.saved = saved;
            this.exitCode = exitCode;
            this.helper = helper;
            this.command = command;
            this.output = output;
            this.errorType = errorType;
            this.errorMessage = errorMessage;
            this.bytes = bytes;
        }
    }

    private Native950WindowCapture() { }

    static Result capture(File output) {
        String helper = powershell();
        String command = helper + " -NoProfile -NonInteractive -ExecutionPolicy Bypass -EncodedCommand <redacted>";
        String script = "$ErrorActionPreference='Stop'; "
                + "$p=Get-Process -Name rs2client-vulkan -ErrorAction Stop | Where-Object {$_.MainWindowHandle -ne 0} | Select-Object -First 1; "
                + "if($null -eq $p){throw 'RuneTek window was not found'}; "
                + "Add-Type -AssemblyName System.Drawing; Add-Type @'\n"
                + "using System; using System.Runtime.InteropServices; public static class BugTestWindow { "
                + "[DllImport(\"user32.dll\")] public static extern bool GetWindowRect(IntPtr h,out RECT r); "
                + "public struct RECT { public int Left,Top,Right,Bottom; } }\n'@; "
                + "$r=New-Object BugTestWindow+RECT; "
                + "if(-not [BugTestWindow]::GetWindowRect($p.MainWindowHandle,[ref]$r)){throw 'GetWindowRect failed'}; "
                + "$w=$r.Right-$r.Left; $h=$r.Bottom-$r.Top; if($w -le 0 -or $h -le 0){throw 'window is not visible'}; "
                + "Write-Output ('windowHandle=0x{0:X};bounds={1},{2},{3},{4}' -f $p.MainWindowHandle,$r.Left,$r.Top,$r.Right,$r.Bottom); "
                + "$b=New-Object System.Drawing.Bitmap $w,$h; $g=[System.Drawing.Graphics]::FromImage($b); "
                + "try {$g.CopyFromScreen($r.Left,$r.Top,0,0,$b.Size); $b.Save('" + ps(output.getAbsolutePath())
                + "',[System.Drawing.Imaging.ImageFormat]::Png)} finally {$g.Dispose(); $b.Dispose()}";
        Process process = null;
        try {
            String encoded = Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE));
            process = new ProcessBuilder(helper, "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass",
                    "-EncodedCommand", encoded).redirectErrorStream(true).start();
            if (!process.waitFor(15, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return new Result(false, -1, helper, command, "", "TimeoutException",
                        "Screenshot helper exceeded 15 seconds", output.isFile() ? output.length() : 0L);
            }
            int exit = process.exitValue();
            String text = read(process.getInputStream());
            boolean saved = exit == 0 && output.isFile() && output.length() > 0L;
            return new Result(saved, exit, helper, command, text, "", "",
                    output.isFile() ? output.length() : 0L);
        } catch (Throwable failure) {
            return new Result(false, -1, helper, command, "", failure.getClass().getName(),
                    safe(failure.getMessage()), output.isFile() ? output.length() : 0L);
        } finally {
            if (process != null && process.isAlive()) process.destroyForcibly();
        }
    }

    private static String powershell() {
        String root = System.getenv("SystemRoot");
        if (root == null || root.trim().isEmpty()) root = "C:\\Windows";
        return new File(root, "System32\\WindowsPowerShell\\v1.0\\powershell.exe").getAbsolutePath();
    }

    private static String read(InputStream input) throws java.io.IOException {
        byte[] buffer = new byte[1024];
        int total = 0, count;
        StringBuilder out = new StringBuilder();
        while (total < OUTPUT_LIMIT
                && (count = input.read(buffer, 0, Math.min(buffer.length, OUTPUT_LIMIT - total))) >= 0) {
            out.append(new String(buffer, 0, count, StandardCharsets.UTF_8));
            total += count;
        }
        return out.toString().trim();
    }

    private static String safe(String value) {
        if (value == null) return "";
        return value.length() > 1024 ? value.substring(0, 1024) : value;
    }

    private static String ps(String path) { return path.replace("'", "''"); }
}
