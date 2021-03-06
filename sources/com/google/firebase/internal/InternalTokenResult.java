package com.google.firebase.internal;

import android.support.annotation.Nullable;
import com.google.android.gms.common.annotation.KeepForSdk;
import com.google.android.gms.common.internal.Objects;
import net.hockeyapp.android.FeedbackActivity;

@KeepForSdk
/* compiled from: com.google.firebase:firebase-common@@16.0.1 */
public class InternalTokenResult {
    private String zza;

    @KeepForSdk
    public InternalTokenResult(@Nullable String token) {
        this.zza = token;
    }

    @Nullable
    @KeepForSdk
    public String getToken() {
        return this.zza;
    }

    public int hashCode() {
        return Objects.hashCode(this.zza);
    }

    public boolean equals(Object o) {
        if (!(o instanceof InternalTokenResult)) {
            return false;
        }
        return Objects.equal(this.zza, ((InternalTokenResult) o).zza);
    }

    public String toString() {
        return Objects.toStringHelper(this).add(FeedbackActivity.EXTRA_TOKEN, this.zza).toString();
    }
}
