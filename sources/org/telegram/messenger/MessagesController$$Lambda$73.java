package org.telegram.messenger;

import java.util.ArrayList;

final /* synthetic */ class MessagesController$$Lambda$73 implements Runnable {
    private final MessagesController arg$1;
    private final ArrayList arg$2;

    MessagesController$$Lambda$73(MessagesController messagesController, ArrayList arrayList) {
        this.arg$1 = messagesController;
        this.arg$2 = arrayList;
    }

    public void run() {
        this.arg$1.lambda$reloadMentionsCountForChannels$111$MessagesController(this.arg$2);
    }
}
