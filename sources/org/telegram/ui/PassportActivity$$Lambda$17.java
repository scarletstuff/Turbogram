package org.telegram.ui;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

final /* synthetic */ class PassportActivity$$Lambda$17 implements OnTouchListener {
    private final PassportActivity arg$1;

    PassportActivity$$Lambda$17(PassportActivity passportActivity) {
        this.arg$1 = passportActivity;
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        return this.arg$1.lambda$createPhoneInterface$29$PassportActivity(view, motionEvent);
    }
}
