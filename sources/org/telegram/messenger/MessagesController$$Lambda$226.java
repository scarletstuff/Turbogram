package org.telegram.messenger;

import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;

final /* synthetic */ class MessagesController$$Lambda$226 implements Runnable {
    private final MessagesController arg$1;
    private final TLRPC$TL_error arg$2;
    private final TLObject arg$3;
    private final Integer arg$4;

    MessagesController$$Lambda$226(MessagesController messagesController, TLRPC$TL_error tLRPC$TL_error, TLObject tLObject, Integer num) {
        this.arg$1 = messagesController;
        this.arg$2 = tLRPC$TL_error;
        this.arg$3 = tLObject;
        this.arg$4 = num;
    }

    public void run() {
        this.arg$1.lambda$null$62$MessagesController(this.arg$2, this.arg$3, this.arg$4);
    }
}
