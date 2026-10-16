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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Tab Hồ sơ (v4.2): chào cá nhân hóa, huy hiệu, streak, nút ghim widget,
 * nút bật/tắt âm thanh đúng-sai, danh sách "Xem lại bài cũ".
 * KHÔNG còn mascot trong app (v4.2).
 */
public class ProfileFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup parent, Bundle s) {
        View v = inf.inflate(R.layout.fragment_profile, parent, false);
        Store store = new Store(getActivity());

        int learned = store.learnedCount();
        int streak = store.getStreak();
        int bi = Badges.index(learned, streak);

        ((TextView) v.findViewById(R.id.tv_hello)).setText("Chào " + store.getName() + "!");

        ((TextView) v.findViewById(R.id.tv_badge)).setText(Badges.ICONS[bi] + " " + Badges.NAMES[bi]);
        ((TextView) v.findViewById(R.id.tv_badge_desc)).setText(Badges.DESCS[bi]);
        ((TextView) v.findViewById(R.id.tv_next_goal)).setText(Badges.nextGoal(learned, streak));
        ((TextView) v.findViewById(R.id.tv_streak)).setText("\uD83D\uDD25 Streak: " + streak + " ngày");
        ((TextView) v.findViewById(R.id.tv_stats)).setText(
                "Từ đã thuộc: " + learned + "/" + Lessons.totalTerms(getActivity()) + "\n" +
                "Quiz đã làm: " + store.getQuizCount() + " lần\n" +
                "Từ cần ôn: " + store.getWrong().size() + "\n" +
                "Từ khó (★): " + store.getStarred().size());

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

        // Nút bật/tắt âm thanh đúng-sai (v4.2, mặc định BẬT).
        Button bSound = v.findViewById(R.id.btn_sound);
        Runnable refreshSound = () -> bSound.setText(store.isSoundOn()
                ? "🔊 Âm thanh: Bật (nhấn để tắt)"
                : "🔇 Âm thanh: Tắt (nhấn để bật)");
        refreshSound.run();
        bSound.setOnClickListener(x -> {
            store.setSoundOn(!store.isSoundOn());
            refreshSound.run();
        });

        // Danh sách "Xem lại bài cũ" (v4.2): mở lại bất kỳ ngày nào đã có.
        LinearLayout pastBox = v.findViewById(R.id.past_lessons);
        for (Lessons.Day d : Lessons.all(getActivity())) {
            Button b = new Button(getActivity());
            b.setText("Day " + d.day + ": " + d.topic);
            b.setBackgroundResource(R.drawable.btn_ghost);
            b.setTextColor(0xFFFFFFFF);
            b.setAllCaps(false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = UiKit.dp(getActivity(), 6);
            b.setLayoutParams(lp);
            final int day = d.day;
            b.setOnClickListener(x -> ((MainActivity) getActivity()).goLearnDay(day));
            pastBox.addView(b);
        }

        return v;
    }
}
