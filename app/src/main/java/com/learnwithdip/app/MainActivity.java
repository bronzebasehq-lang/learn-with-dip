package com.learnwithdip.app;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import java.util.Calendar;

public class MainActivity extends Activity {
    private Button bLearn, bQuiz, bProfile;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        // Fresh start v4.2: lần đầu mở bản mới → reset toàn bộ tiến độ học.
        new Store(this).ensureFreshStart();
        if (!new Store(this).isOnboarded()) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        bLearn = findViewById(R.id.nav_learn);
        bQuiz = findViewById(R.id.nav_quiz);
        bProfile = findViewById(R.id.nav_profile);
        bLearn.setOnClickListener(v -> show(new LearnFragment(), bLearn));
        bQuiz.setOnClickListener(v -> show(new QuizFragment(), bQuiz));
        bProfile.setOnClickListener(v -> show(new ProfileFragment(), bProfile));
        show(new LearnFragment(), bLearn);

        // Quyền thông báo (Android 13+) + lên lịch nhắc 8h sáng
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 2001);
            }
        }
        scheduleDaily(this);
    }

    /** Lên lịch thông báo "Bài học Day N đã sẵn sàng" lúc 8h sáng mỗi ngày. */
    public static void scheduleDaily(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        Intent i = new Intent(c, DailyReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(c, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pi);
    }

    private void show(Fragment f, Button active) {
        getFragmentManager().beginTransaction().replace(R.id.container, f).commit();
        for (Button b : new Button[]{bLearn, bQuiz, bProfile}) {
            b.setSelected(b == active);
            b.setTextColor(b == active ? 0xFFFFFFFF : 0xFF9CA3AF);
        }
    }

    public void goQuiz() { show(new QuizFragment(), bQuiz); }
    public void goLearn() { show(new LearnFragment(), bLearn); }

    /** Mở lại bài cũ từ Hồ sơ ("Xem lại bài cũ"). */
    public void goLearnDay(int day) { show(LearnFragment.forDay(day), bLearn); }

    @Override
    protected void onResume() {
        super.onResume();
        WidgetUpdater.update(this);
    }
}
