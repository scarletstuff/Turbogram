package com.google.android.gms.internal.ads;

import android.content.res.Resources;
import android.os.Bundle;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.gms.ads.impl.C0371R;
import com.google.android.gms.ads.internal.zzbv;
import com.google.firebase.analytics.FirebaseAnalytics.Param;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.json.JSONException;
import org.json.JSONObject;

@zzadh
public final class zzacn implements zzacd<zzoo> {
    private final boolean zzbto;
    private final boolean zzcbk;
    private final boolean zzcbl;

    public zzacn(boolean z, boolean z2, boolean z3) {
        this.zzcbk = z;
        this.zzcbl = z2;
        this.zzbto = z3;
    }

    public final /* synthetic */ zzpb zza(zzabv zzabv, JSONObject jSONObject) throws JSONException, InterruptedException, ExecutionException {
        String string;
        List<zzanz> zza = zzabv.zza(jSONObject, "images", false, this.zzcbk, this.zzcbl);
        Future zza2 = zzabv.zza(jSONObject, "app_icon", true, this.zzcbk);
        zzanz zzc = zzabv.zzc(jSONObject, MimeTypes.BASE_TYPE_VIDEO);
        Future zzg = zzabv.zzg(jSONObject);
        List arrayList = new ArrayList();
        for (zzanz zzanz : zza) {
            arrayList.add((zzon) zzanz.get());
        }
        zzaqw zzc2 = zzabv.zzc(zzc);
        String string2 = jSONObject.getString("headline");
        if (this.zzbto) {
            if (((Boolean) zzkb.zzik().zzd(zznk.zzbfr)).booleanValue()) {
                Resources resources = zzbv.zzeo().getResources();
                string = resources != null ? resources.getString(C0371R.string.s7) : "Test Ad";
                if (string2 != null) {
                    string = new StringBuilder((String.valueOf(string).length() + 3) + String.valueOf(string2).length()).append(string).append(" : ").append(string2).toString();
                }
                return new zzoo(string, arrayList, jSONObject.getString(TtmlNode.TAG_BODY), (zzpw) zza2.get(), jSONObject.getString("call_to_action"), jSONObject.optDouble("rating", -1.0d), jSONObject.optString("store"), jSONObject.optString(Param.PRICE), (zzoj) zzg.get(), new Bundle(), zzc2 == null ? zzc2.zztm() : null, zzc2 == null ? zzc2.getView() : null, null, null);
            }
        }
        string = string2;
        if (zzc2 == null) {
        }
        if (zzc2 == null) {
        }
        return new zzoo(string, arrayList, jSONObject.getString(TtmlNode.TAG_BODY), (zzpw) zza2.get(), jSONObject.getString("call_to_action"), jSONObject.optDouble("rating", -1.0d), jSONObject.optString("store"), jSONObject.optString(Param.PRICE), (zzoj) zzg.get(), new Bundle(), zzc2 == null ? zzc2.zztm() : null, zzc2 == null ? zzc2.getView() : null, null, null);
    }
}
