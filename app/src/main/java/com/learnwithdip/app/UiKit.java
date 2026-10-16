package com.learnwithdip.app;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
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

        // Dòng 1: từ + nút sao (từ khó) + nút loa
        LinearLayout row = new LinearLayout(c);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView tvWord = text(c, t.word, 22, 0xFFFFFFFF, true);
        LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tvWord.setLayoutParams(wp);
        row.addView(tvWord);
        final Store store = new Store(c);
        Button bStar = new Button(c);
        bStar.setText(store.isStarred(t.word) ? "★" : "☆");
        bStar.setTextSize(18);
        bStar.setBackgroundResource(R.drawable.btn_ghost);
        bStar.setOnClickListener(v -> {
            boolean now = store.toggleStarred(t.word);
            bStar.setText(now ? "★" : "☆");
        });
        row.addView(bStar);
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

        // 3 câu ví dụ (v4.2): công việc / đời sống / tech, mỗi câu kèm dịch.
        // Fallback: dữ liệu cũ chỉ có 1 ví dụ.
        java.util.List<String> exs = t.examples.isEmpty()
                ? java.util.Collections.singletonList(t.example) : t.examples;
        java.util.List<String> trs = t.exampleTranslations.isEmpty()
                ? java.util.Collections.singletonList(t.translation) : t.exampleTranslations;
        for (int i = 0; i < exs.size(); i++) {
            TextView tvEx = text(c, "Ví dụ " + (i + 1) + ": \"" + exs.get(i) + "\"",
                    15, 0xFF9CA3AF, false);
            tvEx.setTypeface(tvEx.getTypeface(), Typeface.ITALIC);
            tvEx.setLayoutParams(lp(dp(c, 6)));
            card.addView(tvEx);
            String tr = i < trs.size() ? trs.get(i) : "";
            if (!tr.isEmpty()) {
                TextView tvTr = text(c, "Dịch: " + tr, 15, 0xFF9CA3AF, false);
                tvTr.setLayoutParams(lp(dp(c, 2)));
                card.addView(tvTr);
            }
        }

        return card;
    }

    /**
     * Nút mũi tên tròn (mockup v4.2): đặt góc phải, kèm nhãn NEXT bên dưới.
     * color: 0 = tím, 1 = xanh (đúng), 2 = đỏ (sai).
     */
    public static LinearLayout nextArrow(Context c, String label, int color,
                                          View.OnClickListener onClick) {
        LinearLayout wrap = new LinearLayout(c);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams wlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wlp.topMargin = dp(c, 16);
        wlp.bottomMargin = dp(c, 8);
        wrap.setLayoutParams(wlp);

        Button b = new Button(c);
        b.setText("→");
        b.setTextSize(24);
        b.setTextColor(0xFFFFFFFF);
        b.setBackgroundResource(color == 1 ? R.drawable.btn_circle_green
                : color == 2 ? R.drawable.btn_circle_red : R.drawable.btn_circle);
        int sz = dp(c, 60);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(sz, sz);
        blp.gravity = Gravity.END;
        b.setLayoutParams(blp);
        b.setOnClickListener(onClick);
        wrap.addView(b);

        TextView tv = text(c, label, 12, 0xFF9CA3AF, true);
        tv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tlp.gravity = Gravity.END;
        tv.setLayoutParams(tlp);
        wrap.addView(tv);
        return wrap;
    }

    public static TextView sectionHeader(Context c, String s) {
        TextView tv = text(c, s, 19, 0xFFF59E0B, true);
        tv.setLayoutParams(lp(dp(c, 20)));
        return tv;
    }

    public static int dp(Context c, int dp) {
        return (int) (dp * c.getResources().getDisplayMetrics().density + 0.5f);
    }
}
