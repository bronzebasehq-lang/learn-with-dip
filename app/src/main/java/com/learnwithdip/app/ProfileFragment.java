package com.learnwithdip.app;

import android.app.Fragment;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/** Tab Hồ sơ: chào cá nhân hóa, huy hiệu, streak, nút ghim widget. */
public class ProfileFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup parent, Bundle s) {
        View v = inf.inflate(R.layout.fragment_profile, parent, false);
        Store store = new Store(getActivity());

        int learned = store.learnedCount();
        int streak = store.getStreak();
        int bi = Badges.index(learned, streak);

        ((TextView) v.findViewById(R.id.tv_hello)).setText("Chào " + store.getName() + "!");

        // Mascot 120dp: ngủ nếu chưa học hôm nay, ăn mừng nếu streak >= 7,
        // cổ vũ nếu streak >= 3, vẫy tay chào còn lại.
        MascotView.Mood mood;
        String bubble;
        if (!store.studiedToday()) {
            mood = MascotView.Mood.SLEEPING;
            bubble = "Dip đang ngủ… học để đánh thức Dip! • Wake Dip up!";
        } else if (streak >= 7) {
            mood = MascotView.Mood.CELEBRATING;
            bubble = "Streak 7+! Đỉnh! • Amazing streak!";
        } else if (streak >= 3) {
            mood = MascotView.Mood.CHEERING;
            bubble = "Streak đang cháy! • Streak on fire!";
        } else {
            mood = MascotView.Mood.WAVING;
            bubble = "Chào bro! • Hey there!";
        }
        ((android.widget.LinearLayout) v.findViewById(R.id.mascot_slot))
                .addView(MascotView.create(getActivity(), mood, 120, bubble));
        ((TextView) v.findViewById(R.id.tv_badge)).setText(Badges.ICONS[bi] + " " + Badges.NAMES[bi]);
        ((TextView) v.findViewById(R.id.tv_badge_desc)).setText(Badges.DESCS[bi]);
        ((TextView) v.findViewById(R.id.tv_next_goal)).setText(Badges.nextGoal(learned, streak));
        ((TextView) v.findViewById(R.id.tv_streak)).setText("\uD83D\uDD25 Streak: " + streak + " ngày");
        ((TextView) v.findViewById(R.id.tv_stats)).setText(
                "Từ đã thuộc: " + learned + "/" + Lessons.totalTerms(getActivity()) + "\n" +
                "Quiz đã làm: " + store.getQuizCount() + " lần\n" +
                "Từ cần ôn: " + store.getWrong().size());

        Button bPin = v.findViewById(R.id.btn_pin_widget);
        bPin.setOnClickListener(x -> {
            AppWidgetManager mgr = (AppWidgetManager) getActivity()
                    .getSystemService(Context.APPWIDGET_SERVICE);
            ComponentName cn = new ComponentName(getActivity(), LoopWidgetProvider.class);
            if (mgr.isRequestPinAppWidgetSupported()) {
                mgr.requestPinAppWidget(cn, null, null);
            } else {
                Toast.makeText(getActivity(), "Máy này không hỗ trợ ghim widget",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // (Nút làm lại onboarding đã bỏ theo yêu cầu)

        return v;
    }
}
