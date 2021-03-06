package com.google.firebase.iid;

import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.tasks.TaskCompletionSource;

abstract class zzaj<T> {
    final int what;
    final int zzcc;
    final TaskCompletionSource<T> zzcd = new TaskCompletionSource();
    final Bundle zzce;

    zzaj(int i, int i2, Bundle bundle) {
        this.zzcc = i;
        this.what = i2;
        this.zzce = bundle;
    }

    abstract boolean zzaa();

    abstract void zzb(Bundle bundle);

    final void finish(T t) {
        if (Log.isLoggable("MessengerIpcClient", 3)) {
            String valueOf = String.valueOf(this);
            String valueOf2 = String.valueOf(t);
            Log.d("MessengerIpcClient", new StringBuilder((String.valueOf(valueOf).length() + 16) + String.valueOf(valueOf2).length()).append("Finishing ").append(valueOf).append(" with ").append(valueOf2).toString());
        }
        this.zzcd.setResult(t);
    }

    final void zza(zzak zzak) {
        if (Log.isLoggable("MessengerIpcClient", 3)) {
            String valueOf = String.valueOf(this);
            String valueOf2 = String.valueOf(zzak);
            Log.d("MessengerIpcClient", new StringBuilder((String.valueOf(valueOf).length() + 14) + String.valueOf(valueOf2).length()).append("Failing ").append(valueOf).append(" with ").append(valueOf2).toString());
        }
        this.zzcd.setException(zzak);
    }

    public String toString() {
        int i = this.what;
        int i2 = this.zzcc;
        return "Request { what=" + i + " id=" + i2 + " oneWay=" + zzaa() + "}";
    }
}
