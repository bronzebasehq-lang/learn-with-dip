package com.learnwithdip.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/** Vẽ lại widget: mascot theo trạng thái + streak + câu nudge cá nhân hóa. */
public class WidgetUpdater {
    public static void update(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        int[] ids = m.getAppWidgetIds(new ComponentName(c, LoopWidgetProvider.class));
        if (ids.length == 0) return;
        Store s = new Store(c);
        int streak = s.getStreak();
        boolean studied = s.studiedToday();
        String name = s.getName();

        int mascot;
        String nudge;
        if (!studied) {
            mascot = R.drawable.mascot_sleepy;
            nudge = "Chào " + name + ", tới giờ học rồi!";
        } else if (streak >= 7) {
            mascot = R.drawable.mascot_party;
            nudge = "Tuyệt vời " + name + "! Streak " + streak + " ngày \uD83C\uDF89";
        } else {
            mascot = R.drawable.mascot_happy;
            nudge = "Xong hôm nay rồi " + name + "! Mai tiếp nhé \uD83D\uDCAA";
        }

        for (int id : ids) {
            RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget_loop);
            rv.setImageViewResource(R.id.w_mascot, mascot);
            rv.setTextViewText(R.id.w_streak, "\uD83D\uDD25 " + streak);
            rv.setTextViewText(R.id.w_nudge, nudge);
            Intent i = new Intent(c, MainActivity.class);
            PendingIntent pi = PendingIntent.getActivity(c, 0, i,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            rv.setOnClickPendingIntent(R.id.w_root, pi);
            m.updateAppWidget(id, rv);
        }
    }
}
