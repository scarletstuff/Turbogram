package com.google.android.gms.ads.internal.gmsg;

import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import com.google.android.gms.ads.internal.zzbv;
import com.google.android.gms.internal.ads.zzakk;
import com.google.android.gms.internal.ads.zzane;
import com.google.android.gms.internal.ads.zzaqw;
import java.util.HashMap;
import java.util.Map;

final class zzm implements zzv<zzaqw> {
    zzm() {
    }

    public final /* synthetic */ void zza(Object obj, Map map) {
        zzaqw zzaqw = (zzaqw) obj;
        WindowManager windowManager = (WindowManager) zzaqw.getContext().getSystemService("window");
        zzbv.zzek();
        View view = (View) zzaqw;
        DisplayMetrics zza = zzakk.zza(windowManager);
        int i = zza.widthPixels;
        int i2 = zza.heightPixels;
        int[] iArr = new int[2];
        Map hashMap = new HashMap();
        view.getLocationInWindow(iArr);
        hashMap.put("xInPixels", Integer.valueOf(iArr[0]));
        hashMap.put("yInPixels", Integer.valueOf(iArr[1]));
        hashMap.put("windowWidthInPixels", Integer.valueOf(i));
        hashMap.put("windowHeightInPixels", Integer.valueOf(i2));
        zzaqw.zza("locationReady", hashMap);
        zzane.zzdk("GET LOCATION COMPILED");
    }
}
