package org.telegram.messenger;

import org.telegram.tgnet.TLRPC$messages_Dialogs;

final /* synthetic */ class MessagesController$$Lambda$208 implements Runnable {
    private final MessagesController arg$1;
    private final TLRPC$messages_Dialogs arg$2;
    private final int arg$3;

    MessagesController$$Lambda$208(MessagesController messagesController, TLRPC$messages_Dialogs tLRPC$messages_Dialogs, int i) {
        this.arg$1 = messagesController;
        this.arg$2 = tLRPC$messages_Dialogs;
        this.arg$3 = i;
    }

    public void run() {
        this.arg$1.lambda$null$103$MessagesController(this.arg$2, this.arg$3);
    }
}
