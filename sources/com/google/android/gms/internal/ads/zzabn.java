package com.google.android.gms.internal.ads;

import android.content.Context;

@zzadh
public class zzabn extends zzabf {
    zzabn(Context context, zzaji zzaji, zzaqw zzaqw, zzabm zzabm) {
        super(context, zzaji, zzaqw, zzabm);
    }

    protected final void zzns() {
        if (this.zzbzf.errorCode == -2) {
            this.zzbnd.zzuf().zza((zzasd) this);
            zznu();
            zzane.zzck("Loading HTML in WebView.");
            this.zzbnd.zzc(this.zzbzf.zzbyq, this.zzbzf.zzceo, null);
        }
    }

    protected void zznu() {
    }
}
