package com.google.android.gms.internal.firebase_messaging;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

final class zzd extends WeakReference<Throwable> {
    private final int zzf;

    public zzd(Throwable th, ReferenceQueue<Throwable> referenceQueue) {
        super(th, referenceQueue);
        if (th == null) {
            throw new NullPointerException("The referent cannot be null");
        }
        this.zzf = System.identityHashCode(th);
    }

    public final int hashCode() {
        return this.zzf;
    }

    public final boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        zzd zzd = (zzd) obj;
        if (this.zzf == zzd.zzf && get() == zzd.get()) {
            return true;
        }
        return false;
    }
}
