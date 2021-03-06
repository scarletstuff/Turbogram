package com.google.android.gms.internal.ads;

import android.os.Bundle;
import android.support.annotation.Nullable;
import com.google.android.gms.common.internal.BaseGmsClient.BaseConnectionCallbacks;

final class zzhg implements BaseConnectionCallbacks {
    private final /* synthetic */ zzhd zzajt;

    zzhg(zzhd zzhd) {
        this.zzajt = zzhd;
    }

    public final void onConnected(@Nullable Bundle bundle) {
        synchronized (this.zzajt.mLock) {
            try {
                if (this.zzajt.zzajr != null) {
                    this.zzajt.zzajs = this.zzajt.zzajr.zzhl();
                }
            } catch (Throwable e) {
                zzane.zzb("Unable to obtain a cache service instance.", e);
                this.zzajt.disconnect();
            }
            this.zzajt.mLock.notifyAll();
        }
    }

    public final void onConnectionSuspended(int i) {
        synchronized (this.zzajt.mLock) {
            this.zzajt.zzajs = null;
            this.zzajt.mLock.notifyAll();
        }
    }
}
