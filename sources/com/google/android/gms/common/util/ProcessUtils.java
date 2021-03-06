package com.google.android.gms.common.util;

import android.os.Binder;
import android.os.Process;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;
import javax.annotation.Nullable;

public class ProcessUtils {
    private static String zzaai = null;
    private static int zzaaj = 0;

    public static class SystemGroupsNotAvailableException extends Exception {
        SystemGroupsNotAvailableException(String str) {
            super(str);
        }

        SystemGroupsNotAvailableException(String str, Throwable th) {
            super(str, th);
        }
    }

    private ProcessUtils() {
    }

    @Nullable
    public static String getCallingProcessName() {
        int callingPid = Binder.getCallingPid();
        return callingPid == zzde() ? getMyProcessName() : zzl(callingPid);
    }

    @Nullable
    public static String getMyProcessName() {
        if (zzaai == null) {
            zzaai = zzl(zzde());
        }
        return zzaai;
    }

    public static boolean hasSystemGroups() throws SystemGroupsNotAvailableException {
        Closeable closeable = null;
        try {
            closeable = zzm("/proc/" + zzde() + "/status");
            boolean zzk = zzk(closeable);
            IOUtils.closeQuietly(closeable);
            return zzk;
        } catch (Throwable e) {
            throw new SystemGroupsNotAvailableException("Unable to access /proc/pid/status.", e);
        } catch (Throwable th) {
            IOUtils.closeQuietly(closeable);
        }
    }

    private static int zzde() {
        if (zzaaj == 0) {
            zzaaj = Process.myPid();
        }
        return zzaaj;
    }

    private static boolean zzk(BufferedReader bufferedReader) throws IOException, SystemGroupsNotAvailableException {
        String readLine = bufferedReader.readLine();
        while (readLine != null) {
            readLine = readLine.trim();
            if (readLine.startsWith("Groups:")) {
                for (String parseLong : readLine.substring(7).trim().split("\\s", -1)) {
                    try {
                        long parseLong2 = Long.parseLong(parseLong);
                        if (parseLong2 >= 1000 && parseLong2 < AdaptiveTrackSelection.DEFAULT_MIN_TIME_BETWEEN_BUFFER_REEVALUTATION_MS) {
                            return true;
                        }
                    } catch (NumberFormatException e) {
                    }
                }
                return false;
            }
            readLine = bufferedReader.readLine();
        }
        throw new SystemGroupsNotAvailableException("Missing Groups entry from proc/pid/status.");
    }

    @Nullable
    private static String zzl(int i) {
        Throwable th;
        Closeable closeable;
        String str = null;
        if (i > 0) {
            Closeable zzm;
            try {
                zzm = zzm("/proc/" + i + "/cmdline");
                try {
                    str = zzm.readLine().trim();
                    IOUtils.closeQuietly(zzm);
                } catch (IOException e) {
                    IOUtils.closeQuietly(zzm);
                    return str;
                } catch (Throwable th2) {
                    th = th2;
                    closeable = zzm;
                    IOUtils.closeQuietly(closeable);
                    throw th;
                }
            } catch (IOException e2) {
                zzm = str;
                IOUtils.closeQuietly(zzm);
                return str;
            } catch (Throwable th3) {
                th = th3;
                closeable = str;
                IOUtils.closeQuietly(closeable);
                throw th;
            }
        }
        return str;
    }

    private static BufferedReader zzm(String str) throws IOException {
        ThreadPolicy allowThreadDiskReads = StrictMode.allowThreadDiskReads();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(str));
            return bufferedReader;
        } finally {
            StrictMode.setThreadPolicy(allowThreadDiskReads);
        }
    }
}
