package org.telegram.ui.Adapters;

import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC$TL_error;
import org.telegram.ui.Adapters.MentionsAdapter.C10174;

final /* synthetic */ class MentionsAdapter$4$$Lambda$1 implements Runnable {
    private final C10174 arg$1;
    private final String arg$2;
    private final TLRPC$TL_error arg$3;
    private final TLObject arg$4;
    private final MessagesController arg$5;
    private final MessagesStorage arg$6;

    MentionsAdapter$4$$Lambda$1(C10174 c10174, String str, TLRPC$TL_error tLRPC$TL_error, TLObject tLObject, MessagesController messagesController, MessagesStorage messagesStorage) {
        this.arg$1 = c10174;
        this.arg$2 = str;
        this.arg$3 = tLRPC$TL_error;
        this.arg$4 = tLObject;
        this.arg$5 = messagesController;
        this.arg$6 = messagesStorage;
    }

    public void run() {
        this.arg$1.lambda$null$0$MentionsAdapter$4(this.arg$2, this.arg$3, this.arg$4, this.arg$5, this.arg$6);
    }
}
