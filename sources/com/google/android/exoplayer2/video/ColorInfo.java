package com.google.android.exoplayer2.video;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.util.Util;
import java.util.Arrays;

public final class ColorInfo implements Parcelable {
    public static final Creator<ColorInfo> CREATOR = new C03601();
    public final int colorRange;
    public final int colorSpace;
    public final int colorTransfer;
    private int hashCode;
    @Nullable
    public final byte[] hdrStaticInfo;

    /* renamed from: com.google.android.exoplayer2.video.ColorInfo$1 */
    static class C03601 implements Creator<ColorInfo> {
        C03601() {
        }

        public ColorInfo createFromParcel(Parcel in) {
            return new ColorInfo(in);
        }

        public ColorInfo[] newArray(int size) {
            return new ColorInfo[0];
        }
    }

    public ColorInfo(int colorSpace, int colorRange, int colorTransfer, @Nullable byte[] hdrStaticInfo) {
        this.colorSpace = colorSpace;
        this.colorRange = colorRange;
        this.colorTransfer = colorTransfer;
        this.hdrStaticInfo = hdrStaticInfo;
    }

    ColorInfo(Parcel in) {
        this.colorSpace = in.readInt();
        this.colorRange = in.readInt();
        this.colorTransfer = in.readInt();
        this.hdrStaticInfo = Util.readBoolean(in) ? in.createByteArray() : null;
    }

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ColorInfo other = (ColorInfo) obj;
        if (this.colorSpace == other.colorSpace && this.colorRange == other.colorRange && this.colorTransfer == other.colorTransfer && Arrays.equals(this.hdrStaticInfo, other.hdrStaticInfo)) {
            return true;
        }
        return false;
    }

    public String toString() {
        return "ColorInfo(" + this.colorSpace + ", " + this.colorRange + ", " + this.colorTransfer + ", " + (this.hdrStaticInfo != null) + ")";
    }

    public int hashCode() {
        if (this.hashCode == 0) {
            this.hashCode = ((((((this.colorSpace + 527) * 31) + this.colorRange) * 31) + this.colorTransfer) * 31) + Arrays.hashCode(this.hdrStaticInfo);
        }
        return this.hashCode;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.colorSpace);
        dest.writeInt(this.colorRange);
        dest.writeInt(this.colorTransfer);
        Util.writeBoolean(dest, this.hdrStaticInfo != null);
        if (this.hdrStaticInfo != null) {
            dest.writeByteArray(this.hdrStaticInfo);
        }
    }
}
