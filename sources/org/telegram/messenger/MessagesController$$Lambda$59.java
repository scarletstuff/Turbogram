package org.telegram.messenger;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;

final /* synthetic */ class MessagesController$$Lambda$59 implements RequestDelegate {
    private final MessagesController arg$1;
    private final int arg$2;
    private final long arg$3;

    MessagesController$$Lambda$59(MessagesController messagesController, int i, long j) {
        this.arg$1 = messagesController;
        this.arg$2 = i;
        this.arg$3 = j;
    }

    public void run(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
        this.arg$1.lambda$sendTyping$83$MessagesController(this.arg$2, this.arg$3, tLObject, tLRPC$TL_error);
    }
}
