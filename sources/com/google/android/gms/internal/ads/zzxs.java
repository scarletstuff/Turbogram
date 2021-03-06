package com.google.android.gms.internal.ads;

import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import com.google.android.gms.dynamic.IObjectWrapper;
import com.google.android.gms.dynamic.IObjectWrapper.Stub;
import java.util.List;

public final class zzxs extends zzej implements zzxq {
    zzxs(IBinder iBinder) {
        super(iBinder, "com.google.android.gms.ads.internal.mediation.client.IMediationAdapter");
    }

    public final void destroy() throws RemoteException {
        transactAndReadExceptionReturnVoid(5, obtainAndWriteInterfaceToken());
    }

    public final Bundle getInterstitialAdapterInfo() throws RemoteException {
        Parcel transactAndReadException = transactAndReadException(18, obtainAndWriteInterfaceToken());
        Bundle bundle = (Bundle) zzel.zza(transactAndReadException, Bundle.CREATOR);
        transactAndReadException.recycle();
        return bundle;
    }

    public final zzlo getVideoController() throws RemoteException {
        Parcel transactAndReadException = transactAndReadException(26, obtainAndWriteInterfaceToken());
        zzlo zze = zzlp.zze(transactAndReadException.readStrongBinder());
        transactAndReadException.recycle();
        return zze;
    }

    public final IObjectWrapper getView() throws RemoteException {
        Parcel transactAndReadException = transactAndReadException(2, obtainAndWriteInterfaceToken());
        IObjectWrapper asInterface = Stub.asInterface(transactAndReadException.readStrongBinder());
        transactAndReadException.recycle();
        return asInterface;
    }

    public final boolean isInitialized() throws RemoteException {
        Parcel transactAndReadException = transactAndReadException(13, obtainAndWriteInterfaceToken());
        boolean zza = zzel.zza(transactAndReadException);
        transactAndReadException.recycle();
        return zza;
    }

    public final void pause() throws RemoteException {
        transactAndReadExceptionReturnVoid(8, obtainAndWriteInterfaceToken());
    }

    public final void resume() throws RemoteException {
        transactAndReadExceptionReturnVoid(9, obtainAndWriteInterfaceToken());
    }

    public final void setImmersiveMode(boolean z) throws RemoteException {
        Parcel obtainAndWriteInterfaceToken = obtainAndWriteInterfaceToken();
        zzel.zza(obtainAndWriteInterfaceToken, z);
        transactAndReadExceptionReturnVoid(25, obtainAndWriteInterfaceToken);
    }

    public final void showInterstitial() throws RemoteException {
        transactAndReadExceptionReturnVoid(4, obtainAndWriteInterfaceToken());
    }

    public final void showVideo() throws RemoteException {
        transactAndReadExceptionReturnVoid(12, obtainAndWriteInterfaceToken());
    }

    public final void zza(IObjectWrapper iObjectWrapper, zzaic zzaic, List<String> list) throws RemoteException {
        Parcel obtainAndWriteInterfaceToken = obtainAndWriteInterfaceToken();
        zzel.zza(obtainAndWriteInterfaceToken, (IInterface) iObjectWrapper);
        zzel.zza(obtainAndWriteInterfaceToken, (IInterface) zzaic);
        obtainAndWriteInterfaceToken.writeStringList(list);
        transactAndReadExceptionReturnVoid(23, obtainAndWriteInterfaceToken);
    }

    public final void zza(IObjectWrapper iObjectWrapper, zzjj zzjj, String str, zzaic zzaic, String str2) throws RemoteException {
        Parcel obtainAndWriteInterfaceToken = obtainAndWriteInterfaceToken();
        zzel.zza(obtainAndWriteInterfaceToken, (IInterface) iObjectWrapper);
        zzel.zza(obtainAndWriteInterfaceToken, (Parcelable) zzjj);
        obtainAndWriteInterfaceToken.writeString(str);
        zzel.zza(obtainAndWriteInterfaceToken, (IInterface) zzaic);
        obtainAndWriteInterfaceToken.writeString(str2);
        transactAndReadExceptionReturnVoid(10, obtainAndWriteInterfaceToken);
    }

    public final void zza(IObjectWrapper iObjectWrapper, zzjj zzjj, String str, zzxt zzxt) throws RemoteException {
        Parcel obtainAndWriteInterfaceToken = obtainAndWriteInterfaceToken();
        zzel.zza(obtainAndWriteInterfaceToken, (IInterface) iObjectWrapper);
        zzel.zza(obtainAndWriteInterfaceToken, (Parcelable) zzjj);
        obtainAndWriteInterfaceToken.writeString(str);
        zzel.zza(obtainAndWriteInterfaceToken, (IInterface) zzxt);
        transactAndReadExceptionReturnVoid(3, obtainAndWriteInterfaceToken);
    }

    public final void zza(IObjectWrapper iObjectWrapper, zzjj zzjj, String str, String str2, zzxt zzxt) throws RemoteException {
        Parcel obtainAndWriteInterfaceToken = obtainAndWriteInterfaceToken();
        zzel.zza(obtainAndWriteInterfaceToken, (IInterface) iObjectWrapper);
        zzel.zza(obtainAndWriteInterfaceToken, (Parcelable) zzjj);
        obtainAndWriteInterfaceToken.writeString(str);
        obtainAndWriteInterfaceToken.writeString(str2);
        zzel.zza(obtainAndWriteInterfaceToken, (IInterface) zzxt);
        transactAndReadExceptionReturnVoid(7, obtainAndWriteInterfaceToken);
    }

    public final void zza(IObjectWrapper iObjectWrapper, zzjj zzjj, String str, String str2, zzxt zzxt, zzpl zzpl, List<String> list) throws RemoteException {
        Parcel obtainAndWriteInterfaceToken = obtainAndWriteInterfaceToken();
        zzel.zza(obtainAndWriteInterfaceToken, (IInterface) iObjectWrapper);
        zzel.zza(obtainAndWriteInterfaceToken, (Parcelable) zzjj);
        obtainAndWriteInterfaceToken.writeString(str);
        obtainAndWriteInterfaceToken.writeString(str2);
        zzel.zza(obtainAndWriteInterfaceToken, (IInterface) zzxt);
        zzel.zza(obtainAndWriteInterfaceToken, (Parcelable) zzpl);
        obtainAndWriteInterfaceToken.writeStringList(list);
        transactAndReadExceptionReturnVoid(14, obtainAndWriteInterfaceToken);
    }

    public final void zza(IObjectWrapper iObjectWrapper, zzjn zzjn, zzjj zzjj, String str, zzxt zzxt) throws RemoteException {
        Parcel obtainAndWriteInterfaceToken = obtainAndWriteInterfaceToken();
        zzel.zza(obtainAndWriteInterfaceToken, (IInterface) iObjectWrapper);
        zzel.zza(obtainAndWriteInterfaceToken, (Parcelable) zzjn);
        zzel.zza(obtainAndWriteInterfaceToken, (Parcelable) zzjj);
        obtainAndWriteInterfaceToken.writeString(str);
        zzel.zza(obtainAndWriteInterfaceToken, (IInterface) zzxt);
        transactAndReadExceptionReturnVoid(1, obtainAndWriteInterfaceToken);
    }

    public final void zza(IObjectWrapper iObjectWrapper, zzjn zzjn, zzjj zzjj, String str, String str2, zzxt zzxt) throws RemoteException {
        Parcel obtainAndWriteInterfaceToken = obtainAndWriteInterfaceToken();
        zzel.zza(obtainAndWriteInterfaceToken, (IInterface) iObjectWrapper);
        zzel.zza(obtainAndWriteInterfaceToken, (Parcelable) zzjn);
        zzel.zza(obtainAndWriteInterfaceToken, (Parcelable) zzjj);
        obtainAndWriteInterfaceToken.writeString(str);
        obtainAndWriteInterfaceToken.writeString(str2);
        zzel.zza(obtainAndWriteInterfaceToken, (IInterface) zzxt);
        transactAndReadExceptionReturnVoid(6, obtainAndWriteInterfaceToken);
    }

    public final void zza(zzjj zzjj, String str, String str2) throws RemoteException {
        Parcel obtainAndWriteInterfaceToken = obtainAndWriteInterfaceToken();
        zzel.zza(obtainAndWriteInterfaceToken, (Parcelable) zzjj);
        obtainAndWriteInterfaceToken.writeString(str);
        obtainAndWriteInterfaceToken.writeString(str2);
        transactAndReadExceptionReturnVoid(20, obtainAndWriteInterfaceToken);
    }

    public final void zzc(zzjj zzjj, String str) throws RemoteException {
        Parcel obtainAndWriteInterfaceToken = obtainAndWriteInterfaceToken();
        zzel.zza(obtainAndWriteInterfaceToken, (Parcelable) zzjj);
        obtainAndWriteInterfaceToken.writeString(str);
        transactAndReadExceptionReturnVoid(11, obtainAndWriteInterfaceToken);
    }

    public final void zzi(IObjectWrapper iObjectWrapper) throws RemoteException {
        Parcel obtainAndWriteInterfaceToken = obtainAndWriteInterfaceToken();
        zzel.zza(obtainAndWriteInterfaceToken, (IInterface) iObjectWrapper);
        transactAndReadExceptionReturnVoid(21, obtainAndWriteInterfaceToken);
    }

    public final zzxz zzmo() throws RemoteException {
        zzxz zzxz;
        Parcel transactAndReadException = transactAndReadException(15, obtainAndWriteInterfaceToken());
        IBinder readStrongBinder = transactAndReadException.readStrongBinder();
        if (readStrongBinder == null) {
            zzxz = null;
        } else {
            IInterface queryLocalInterface = readStrongBinder.queryLocalInterface("com.google.android.gms.ads.internal.mediation.client.INativeAppInstallAdMapper");
            zzxz = queryLocalInterface instanceof zzxz ? (zzxz) queryLocalInterface : new zzyb(readStrongBinder);
        }
        transactAndReadException.recycle();
        return zzxz;
    }

    public final zzyc zzmp() throws RemoteException {
        zzyc zzyc;
        Parcel transactAndReadException = transactAndReadException(16, obtainAndWriteInterfaceToken());
        IBinder readStrongBinder = transactAndReadException.readStrongBinder();
        if (readStrongBinder == null) {
            zzyc = null;
        } else {
            IInterface queryLocalInterface = readStrongBinder.queryLocalInterface("com.google.android.gms.ads.internal.mediation.client.INativeContentAdMapper");
            zzyc = queryLocalInterface instanceof zzyc ? (zzyc) queryLocalInterface : new zzye(readStrongBinder);
        }
        transactAndReadException.recycle();
        return zzyc;
    }

    public final Bundle zzmq() throws RemoteException {
        Parcel transactAndReadException = transactAndReadException(17, obtainAndWriteInterfaceToken());
        Bundle bundle = (Bundle) zzel.zza(transactAndReadException, Bundle.CREATOR);
        transactAndReadException.recycle();
        return bundle;
    }

    public final Bundle zzmr() throws RemoteException {
        Parcel transactAndReadException = transactAndReadException(19, obtainAndWriteInterfaceToken());
        Bundle bundle = (Bundle) zzel.zza(transactAndReadException, Bundle.CREATOR);
        transactAndReadException.recycle();
        return bundle;
    }

    public final boolean zzms() throws RemoteException {
        Parcel transactAndReadException = transactAndReadException(22, obtainAndWriteInterfaceToken());
        boolean zza = zzel.zza(transactAndReadException);
        transactAndReadException.recycle();
        return zza;
    }

    public final zzqs zzmt() throws RemoteException {
        Parcel transactAndReadException = transactAndReadException(24, obtainAndWriteInterfaceToken());
        zzqs zzk = zzqt.zzk(transactAndReadException.readStrongBinder());
        transactAndReadException.recycle();
        return zzk;
    }

    public final zzyf zzmu() throws RemoteException {
        zzyf zzyf;
        Parcel transactAndReadException = transactAndReadException(27, obtainAndWriteInterfaceToken());
        IBinder readStrongBinder = transactAndReadException.readStrongBinder();
        if (readStrongBinder == null) {
            zzyf = null;
        } else {
            IInterface queryLocalInterface = readStrongBinder.queryLocalInterface("com.google.android.gms.ads.internal.mediation.client.IUnifiedNativeAdMapper");
            zzyf = queryLocalInterface instanceof zzyf ? (zzyf) queryLocalInterface : new zzyh(readStrongBinder);
        }
        transactAndReadException.recycle();
        return zzyf;
    }
}
