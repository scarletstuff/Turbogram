package org.telegram.ui;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.ui.ActionBar.AlertDialog;

final /* synthetic */ class LaunchActivity$$Lambda$14 implements RequestDelegate {
    private final LaunchActivity arg$1;
    private final AlertDialog arg$2;

    LaunchActivity$$Lambda$14(LaunchActivity launchActivity, AlertDialog alertDialog) {
        this.arg$1 = launchActivity;
        this.arg$2 = alertDialog;
    }

    public void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        this.arg$1.lambda$runLinkRequest$25$LaunchActivity(this.arg$2, tLObject, tLRPC$TL_error);
    }
}
