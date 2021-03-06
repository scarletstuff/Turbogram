package com.google.android.gms.internal.ads;

import android.support.annotation.Nullable;
import com.google.android.gms.ads.internal.zzbv;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;

@zzadh
@ParametersAreNonnullByDefault
public final class zzgf {
    private final Object mLock = new Object();
    private int zzahm;
    private List<zzge> zzahn = new LinkedList();

    public final boolean zza(zzge zzge) {
        boolean z;
        synchronized (this.mLock) {
            if (this.zzahn.contains(zzge)) {
                z = true;
            } else {
                z = false;
            }
        }
        return z;
    }

    public final boolean zzb(zzge zzge) {
        synchronized (this.mLock) {
            Iterator it = this.zzahn.iterator();
            while (it.hasNext()) {
                zzge zzge2 = (zzge) it.next();
                if (!((Boolean) zzkb.zzik().zzd(zznk.zzawq)).booleanValue() || zzbv.zzeo().zzqh().zzqu()) {
                    if (((Boolean) zzkb.zzik().zzd(zznk.zzaws)).booleanValue() && !zzbv.zzeo().zzqh().zzqw() && zzge != zzge2 && zzge2.zzgp().equals(zzge.zzgp())) {
                        it.remove();
                        return true;
                    }
                } else if (zzge != zzge2 && zzge2.getSignature().equals(zzge.getSignature())) {
                    it.remove();
                    return true;
                }
            }
            return false;
        }
    }

    public final void zzc(zzge zzge) {
        synchronized (this.mLock) {
            if (this.zzahn.size() >= 10) {
                zzane.zzck("Queue is full, current size = " + this.zzahn.size());
                this.zzahn.remove(0);
            }
            int i = this.zzahm;
            this.zzahm = i + 1;
            zzge.zzo(i);
            this.zzahn.add(zzge);
        }
    }

    @Nullable
    public final zzge zzgv() {
        zzge zzge = null;
        synchronized (this.mLock) {
            if (this.zzahn.size() == 0) {
                zzane.zzck("Queue empty");
                return null;
            } else if (this.zzahn.size() >= 2) {
                int i = Integer.MIN_VALUE;
                int i2 = 0;
                int i3 = 0;
                for (zzge zzge2 : this.zzahn) {
                    int score = zzge2.getScore();
                    if (score > i) {
                        i2 = i3;
                    } else {
                        score = i;
                        zzge2 = zzge;
                    }
                    i3++;
                    i = score;
                    zzge = zzge2;
                }
                this.zzahn.remove(i2);
                return zzge;
            } else {
                zzge2 = (zzge) this.zzahn.get(0);
                zzge2.zzgq();
                return zzge2;
            }
        }
    }
}
