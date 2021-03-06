package com.google.android.gms.internal.ads;

import android.support.media.ExifInterface;
import java.util.HashMap;

public final class zzbj extends zzbh<Integer, Object> {
    public String zzcx;
    public String zzcz;
    public String zzda;
    public String zzdb;
    public long zzhx;

    public zzbj() {
        this.zzcx = ExifInterface.LONGITUDE_EAST;
        this.zzhx = -1;
        this.zzcz = ExifInterface.LONGITUDE_EAST;
        this.zzda = ExifInterface.LONGITUDE_EAST;
        this.zzdb = ExifInterface.LONGITUDE_EAST;
    }

    public zzbj(String str) {
        this();
        zzj(str);
    }

    protected final void zzj(String str) {
        HashMap zzk = zzbh.zzk(str);
        if (zzk != null) {
            this.zzcx = zzk.get(Integer.valueOf(0)) == null ? ExifInterface.LONGITUDE_EAST : (String) zzk.get(Integer.valueOf(0));
            this.zzhx = zzk.get(Integer.valueOf(1)) == null ? -1 : ((Long) zzk.get(Integer.valueOf(1))).longValue();
            this.zzcz = zzk.get(Integer.valueOf(2)) == null ? ExifInterface.LONGITUDE_EAST : (String) zzk.get(Integer.valueOf(2));
            this.zzda = zzk.get(Integer.valueOf(3)) == null ? ExifInterface.LONGITUDE_EAST : (String) zzk.get(Integer.valueOf(3));
            this.zzdb = zzk.get(Integer.valueOf(4)) == null ? ExifInterface.LONGITUDE_EAST : (String) zzk.get(Integer.valueOf(4));
        }
    }

    protected final HashMap<Integer, Object> zzu() {
        HashMap<Integer, Object> hashMap = new HashMap();
        hashMap.put(Integer.valueOf(0), this.zzcx);
        hashMap.put(Integer.valueOf(4), this.zzdb);
        hashMap.put(Integer.valueOf(3), this.zzda);
        hashMap.put(Integer.valueOf(2), this.zzcz);
        hashMap.put(Integer.valueOf(1), Long.valueOf(this.zzhx));
        return hashMap;
    }
}
