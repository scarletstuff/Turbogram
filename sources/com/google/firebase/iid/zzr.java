package com.google.firebase.iid;

import android.os.Bundle;
import com.google.android.gms.tasks.TaskCompletionSource;

final /* synthetic */ class zzr implements Runnable {
    private final zzq zzbh;
    private final Bundle zzbi;
    private final TaskCompletionSource zzbj;

    zzr(zzq zzq, Bundle bundle, TaskCompletionSource taskCompletionSource) {
        this.zzbh = zzq;
        this.zzbi = bundle;
        this.zzbj = taskCompletionSource;
    }

    public final void run() {
        this.zzbh.zza(this.zzbi, this.zzbj);
    }
}
