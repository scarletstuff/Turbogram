package com.google.android.gms.internal.ads;

import com.google.android.gms.ads.internal.gmsg.zzv;
import java.util.Map;

final class zzabz implements zzv<Object> {
    private final /* synthetic */ zzabv zzcal;
    private final /* synthetic */ zzos zzcam;

    zzabz(zzabv zzabv, zzos zzos) {
        this.zzcal = zzabv;
        this.zzcam = zzos;
    }

    public final void zza(Object obj, Map<String, String> map) {
        this.zzcal.zzc(this.zzcam, (String) map.get("asset"));
    }
}
