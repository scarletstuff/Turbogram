package org.telegram.messenger;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

final /* synthetic */ class MessagesController$$Lambda$147 implements OnClickListener {
    private final MessagesController arg$1;
    private final int arg$2;

    MessagesController$$Lambda$147(MessagesController messagesController, int i) {
        this.arg$1 = messagesController;
        this.arg$2 = i;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        this.arg$1.lambda$null$239$MessagesController(this.arg$2, dialogInterface, i);
    }
}
