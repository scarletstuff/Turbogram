package com.google.android.gms.ads.internal;

import com.google.android.gms.internal.ads.zzane;
import com.google.android.gms.internal.ads.zzoo;

final class zzt implements Runnable {
    private final /* synthetic */ zzq zzwt;
    private final /* synthetic */ zzoo zzwv;

    zzt(zzq zzq, zzoo zzoo) {
        this.zzwt = zzq;
        this.zzwv = zzoo;
    }

    public final void run() {
        try {
            if (this.zzwt.zzvw.zzade != null) {
                this.zzwt.zzvw.zzade.zza(this.zzwv);
                this.zzwt.zzb(this.zzwv.zzka());
            }
        } catch (Throwable e) {
            zzane.zzd("#007 Could not call remote method.", e);
        }
    }
}
