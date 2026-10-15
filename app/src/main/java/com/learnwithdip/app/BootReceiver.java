package com.learnwithdip.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Lên lịch lại thông báo 8h sáng sau khi máy khởi động. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            MainActivity.scheduleDaily(c);
        }
    }
}
