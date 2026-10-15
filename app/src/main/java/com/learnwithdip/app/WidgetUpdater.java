package com.learnwithdip.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import java.util.Calendar;

/**
 * Widget v3 (Minh 15/10): mascot TO ôm bong bóng text + caption to rõ.
 * Caption: câu nudge ngắn, đổi mỗi ngày, TỐI ĐA 15 TỪ (words, không phải ký tự),
 * KHÔNG chi tiết từng ngày (không Day N / chủ đề / số từ).
 * Mascot 4 biểu cảm: sleepy (chưa học) / happy / main / party (streak >= 7).
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

    /** Caption chưa học — xoay theo ngày trong năm, mỗi câu <= 6 từ. */
    static final String[] TODO = {
        "Tới giờ học rồi!",
        "Học 10 từ mới thôi!",
        "Dip đợi bạn học bài!",
        "Hôm nay học gì nào?",
        "10 từ mới, vào học thôi!",
        "Đừng để Dip đợi lâu!",
        "Mở app, học bài nào!"
    };

    /** Caption đã học xong — xoay theo ngày trong năm, mỗi câu <= 6 từ. */
    static final String[] DONE = {
        "Đã học xong, giỏi lắm!",
        "Xong bài rồi, giỏi quá!",
        "Xong rồi, mai gặp nhé!",
        "Chuỗi vẫn cháy, giữ nhé!"
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
            rv.setTextViewText(R.id.w_streak, "\uD83D\uDD25 " + streak + " ngày");
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
