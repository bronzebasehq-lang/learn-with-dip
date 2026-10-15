package com.learnwithdip.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/** Thông báo 8h sáng mỗi ngày: "Bài học Day N đã sẵn sàng". */
public class DailyReceiver extends BroadcastReceiver {
    private static final String CH = "daily_lesson";

    @Override
    public void onReceive(Context c, Intent intent) {
        Store s = new Store(c);
        if (!s.isOnboarded()) return;
        int dayIdx = s.getDayIndex();
        Lessons.Day lesson = Lessons.get(c, dayIdx);
        String title = "Learn With Dip \uD83C\uDF93";
        String text = lesson != null
                ? "Bài học Day " + lesson.day + ": " + lesson.topic + " đã sẵn sàng!"
                : "Bài học hôm nay đã sẵn sàng!";

        NotificationManager nm =
                (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CH, "Bài học hàng ngày",
                    NotificationManager.IMPORTANCE_DEFAULT);
            ch.setDescription("Nhắc bài học tiếng Anh mỗi sáng");
            nm.createNotificationChannel(ch);
        }

        Intent i = new Intent(c, MainActivity.class);
        i.putExtra("openLesson", true);
        PendingIntent pi = PendingIntent.getActivity(c, 1, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder b = (Build.VERSION.SDK_INT >= 26)
                ? new Notification.Builder(c, CH)
                : new Notification.Builder(c);
        b.setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.mascot)
                .setContentIntent(pi)
                .setAutoCancel(true);
        nm.notify(1001, b.build());

        // Cập nhật widget theo ngày mới
        WidgetUpdater.update(c);
    }
}
