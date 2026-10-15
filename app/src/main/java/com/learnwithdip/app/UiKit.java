package com.learnwithdip.app;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Dựng thẻ từ vựng đầy đủ: từ + loa + IPA + loại từ + Nghĩa + Ví dụ + Dịch. */
public class UiKit {

    private static LinearLayout.LayoutParams lp(int top) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.topMargin = top;
        return p;
    }

    public static TextView text(Context c, String s, int sizeSp, int color, boolean bold) {
        TextView tv = new TextView(c);
        tv.setText(s);
        tv.setTextSize(sizeSp);
        tv.setTextColor(color);
        if (bold) tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        return tv;
    }

    /** Thẻ từ vựng đầy đủ theo format Minh duyệt. */
    public static LinearLayout termCard(Context c, Lessons.Term t) {
        LinearLayout card = new LinearLayout(c);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.card_bg);
        int pad = dp(c, 16);
        card.setPadding(pad, pad, pad, pad);
        card.setLayoutParams(lp(dp(c, 12)));

        // Dòng 1: từ + nút loa
        LinearLayout row = new LinearLayout(c);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView tvWord = text(c, t.word, 22, 0xFFFFFFFF, true);
        LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tvWord.setLayoutParams(wp);
        row.addView(tvWord);
        Button bSpeak = new Button(c);
        bSpeak.setText("\uD83D\uDD0A");
        bSpeak.setTextSize(18);
        bSpeak.setBackgroundResource(R.drawable.btn_ghost);
        bSpeak.setOnClickListener(v -> AudioPlayer.play(c, t.audio, t.word));
        row.addView(bSpeak);
        card.addView(row);

        // Dòng 2: IPA + loại từ
        TextView tvIpa = text(c, t.ipa + "  ·  " + t.pos, 14, 0xFFC4B5FD, false);
        tvIpa.setTypeface(tvIpa.getTypeface(), Typeface.ITALIC);
        card.addView(tvIpa);

        // Nghĩa
        TextView tvMeaning = text(c, "Nghĩa: " + t.meaning, 16, 0xFFF59E0B, false);
        LinearLayout.LayoutParams mp2 = lp(dp(c, 8));
        tvMeaning.setLayoutParams(mp2);
        card.addView(tvMeaning);

        // Ví dụ
        TextView tvEx = text(c, "Ví dụ: \"" + t.example + "\"", 15, 0xFF9CA3AF, false);
        tvEx.setTypeface(tvEx.getTypeface(), Typeface.ITALIC);
        tvEx.setLayoutParams(lp(dp(c, 6)));
        card.addView(tvEx);

        // Dịch
        TextView tvTr = text(c, "Dịch: " + t.translation, 15, 0xFF9CA3AF, false);
        tvTr.setLayoutParams(lp(dp(c, 4)));
        card.addView(tvTr);

        return card;
    }

    public static TextView sectionHeader(Context c, String s) {
        TextView tv = text(c, s, 19, 0xFFF59E0B, true);
        tv.setLayoutParams(lp(dp(c, 20)));
        return tv;
    }

    public static int dp(Context c, int dp) {
        return (int) (dp * c.getResources().getDisplayMetrics().density + 0.5f);
    }

    /** Câu nudge ngẫu nhiên của mascot Dip. */
    private static final String[] NUDGES = {
            "Dip tin bro làm được! Mai tiếp nhé \uD83D\uDCAA",
            "10 từ hôm nay, mai 10 từ nữa. Cứ thế mà lên! \uD83D\uDE80",
            "Streak đang cháy, đừng để tắt! \uD83D\uDD25",
            "Học đều mỗi ngày mới là bí kíp, không phải học dồn một lúc \uD83D\uDC22",
            "Mấy từ này mai mốt đọc paper khỏi cần dịch nữa \uD83D\uDE0E",
            "Chậm mà chắc. Dip đi cùng bro từng ngày \uD83E\uDD16"
    };

    public static String randomNudge() {
        return NUDGES[(int) (Math.random() * NUDGES.length)];
    }
}
