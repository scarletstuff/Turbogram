package com.google.android.gms.internal.ads;

import android.support.media.ExifInterface;
import java.lang.reflect.InvocationTargetException;

public final class zzdn extends zzei {
    private static volatile String zztm = null;
    private static final Object zztn = new Object();

    public zzdn(zzcz zzcz, String str, String str2, zzba zzba, int i, int i2) {
        super(zzcz, str, str2, zzba, i, 29);
    }

    protected final void zzar() throws IllegalAccessException, InvocationTargetException {
        this.zztq.zzdx = ExifInterface.LONGITUDE_EAST;
        if (zztm == null) {
            synchronized (zztn) {
                if (zztm == null) {
                    zztm = (String) this.zztz.invoke(null, new Object[]{this.zzps.getContext()});
                }
            }
        }
        synchronized (this.zztq) {
            this.zztq.zzdx = zzbi.zza(zztm.getBytes(), true);
        }
    }
}
