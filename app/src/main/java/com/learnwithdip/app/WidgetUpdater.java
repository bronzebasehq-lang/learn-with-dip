package com.learnwithdip.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import java.util.Calendar;

/**
 * Widget v4 (Minh 15/10, duyệt theo mockup 1): NHỎ lại như bản cũ (180x80dp,
 * mascot 56dp ôm bong bóng text) + TIẾNG ANH hoàn toàn.
 * Caption: câu nudge tiếng Anh ngắn, đổi mỗi ngày, TỐI ĐA 15 TỪ (words, không phải ký tự),
 * KHÔNG chi tiết từng ngày (không Day N / chủ đề / số từ).
 * Mascot 4 biểu cảm: sleepy (chưa học) / happy / main / party (streak >= 7).
 * Mascot TO 3x + mascot mọi trang là phần khác, KHÔNG build ở đây.
 */
public class WidgetUpdater {
    /** Giới hạn cứng: tối đa 15 từ, thừa thì cắt + "…". */
    static final int MAX_WORDS = 15;

    static String capWords(String s, int max) {
        if (s == null) return "";
        String[] words = s.trim().split("\\s+");
        if (words.length <= max) return s.trim();
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < max; i++) {
            if (i > 0) b.append(' ');
            b.append(words[i]);
        }
        return b.append("…").toString();
    }

    /** Caption chưa học — tiếng Anh, xoay theo ngày trong năm, mỗi câu <= 6 từ. */
    static final String[] TODO = {
        "Time to learn!",
        "10 new words await!",
        "Dip is waiting for you!",
        "Learn 10 new words today!",
        "Don't keep Dip waiting!",
        "Open the app, let's learn!",
        "A new lesson is ready!"
    };

    /** Caption đã học xong — tiếng Anh, xoay theo ngày trong năm, mỗi câu <= 6 từ. */
    static final String[] DONE = {
        "Done today, great job!",
        "Lesson complete, well done!",
        "See you tomorrow!",
        "Streak is on fire, keep it up!"
    };

    public static void update(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        int[] ids = m.getAppWidgetIds(new ComponentName(c, LoopWidgetProvider.class));
        if (ids.length == 0) return;
        Store s = new Store(c);
        int streak = s.getStreak();
        boolean studied = s.studiedToday();

        // Mascot theo trạng thái: 4 biểu cảm khác nhau
        int mascot;
        if (!studied) {
            mascot = R.drawable.mascot_sleepy;
        } else if (streak >= 7) {
            mascot = R.drawable.mascot_party;
        } else if (streak >= 3) {
            mascot = R.drawable.mascot; // mặt chính, bình thản
        } else {
            mascot = R.drawable.mascot_happy;
        }

        // Caption: nudge ngắn, đổi mỗi ngày, không chi tiết từng ngày
        int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        String caption = studied
                ? DONE[dayOfYear % DONE.length]
                : TODO[dayOfYear % TODO.length];
        caption = capWords(caption, MAX_WORDS);

        for (int id : ids) {
            RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget_loop);
            rv.setImageViewResource(R.id.w_mascot, mascot);
            rv.setTextViewText(R.id.w_streak, "\uD83D\uDD25 STREAK " + streak);
            rv.setTextViewText(R.id.w_caption, caption);
            // Nhấn widget -> mở thẳng bài học hôm nay
            Intent i = new Intent(c, MainActivity.class);
            i.putExtra("openLesson", true);
            PendingIntent pi = PendingIntent.getActivity(c, 0, i,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            rv.setOnClickPendingIntent(R.id.w_root, pi);
            m.updateAppWidget(id, rv);
        }
    }
}
