package org.telegram.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.TimeZone;

class LocaleController$TimeZoneChangedReceiver extends BroadcastReceiver {
    final /* synthetic */ LocaleController this$0;

    private LocaleController$TimeZoneChangedReceiver(LocaleController localeController) {
        this.this$0 = localeController;
    }

    public void onReceive(Context context, Intent intent) {
        ApplicationLoader.applicationHandler.post(new LocaleController$TimeZoneChangedReceiver$$Lambda$0(this));
    }

    final /* synthetic */ void lambda$onReceive$0$LocaleController$TimeZoneChangedReceiver() {
        if (!this.this$0.formatterMonth.getTimeZone().equals(TimeZone.getDefault())) {
            LocaleController.getInstance().recreateFormatters();
        }
    }
}
