package com.learnwithdip.app;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/** Tab Học: bài học theo ngày — 10 thuật ngữ + đoạn đọc hiểu + dịch + quiz kiểm tra. */
public class LearnFragment extends Fragment {

    private Lessons.Day lesson;
    private List<Button[]> optRows = new ArrayList<>();
    private int[] picked;
    private boolean submitted = false;
    private LinearLayout box;
    private LinearLayout resultBox;

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup parent, Bundle s) {
        View v = inf.inflate(R.layout.fragment_learn, parent, false);
        box = v.findViewById(R.id.lesson_box);
        Store store = new Store(getActivity());
        int dayIdx = store.getDayIndex();
        lesson = Lessons.get(getActivity(), dayIdx);
        if (lesson == null) return v;

        // Tiêu đề Day N
        TextView tvTitle = UiKit.text(getActivity(),
                "Day " + lesson.day + ": " + lesson.topic, 22, 0xFFFFFFFF, true);
        box.addView(tvTitle);
        TextView tvSub = UiKit.text(getActivity(),
                "Chào " + store.getName() + "! Bài học hôm nay có 10 thuật ngữ mới \uD83C\uDF93",
                14, 0xFF9CA3AF, false);
        box.addView(tvSub);

        // 10 thuật ngữ
        box.addView(UiKit.sectionHeader(getActivity(), "10 Thuật ngữ hôm nay"));
        for (Lessons.Term t : lesson.terms) {
            box.addView(UiKit.termCard(getActivity(), t));
        }

        // Đoạn đọc hiểu
        box.addView(UiKit.sectionHeader(getActivity(), "\uD83D\uDCD6 Đoạn đọc hiểu"));
        TextView tvReading = UiKit.text(getActivity(), lesson.reading, 16, 0xFFFFFFFF, false);
        tvReading.setLineSpacing(UiKit.dp(getActivity(), 4), 1f);
        box.addView(tvReading);

        box.addView(UiKit.sectionHeader(getActivity(), "\uD83C\uDDFB\uD83C\uDDF3 Bản dịch"));
        TextView tvReadingVi = UiKit.text(getActivity(), lesson.readingVi, 15, 0xFF9CA3AF, false);
        tvReadingVi.setLineSpacing(UiKit.dp(getActivity(), 4), 1f);
        box.addView(tvReadingVi);

        // Quiz kiểm tra đọc hiểu
        box.addView(UiKit.sectionHeader(getActivity(), "✅ Kiểm tra đọc hiểu"));
        picked = new int[lesson.quiz.size()];
        for (int i = 0; i < picked.length; i++) picked[i] = -1;
        for (int qi = 0; qi < lesson.quiz.size(); qi++) {
            Lessons.QuizQ q = lesson.quiz.get(qi);
            TextView tvQ = UiKit.text(getActivity(), (qi + 1) + ". " + q.q, 16, 0xFFFFFFFF, true);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = UiKit.dp(getActivity(), 12);
            tvQ.setLayoutParams(lp);
            box.addView(tvQ);
            Button[] opts = new Button[q.options.length];
            final int qIdx = qi;
            for (int oi = 0; oi < q.options.length; oi++) {
                Button b = new Button(getActivity());
                b.setText((char) ('A' + oi) + ". " + q.options[oi]);
                b.setBackgroundResource(R.drawable.btn_ghost);
                b.setTextColor(0xFFFFFFFF);
                b.setAllCaps(false);
                LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                blp.topMargin = UiKit.dp(getActivity(), 6);
                b.setLayoutParams(blp);
                final int oIdx = oi;
                b.setOnClickListener(x -> {
                    if (submitted) return;
                    picked[qIdx] = oIdx;
                    for (int k = 0; k < opts.length; k++)
                        opts[k].setBackgroundResource(k == oIdx
                                ? R.drawable.btn_primary : R.drawable.btn_ghost);
                });
                opts[oi] = b;
                box.addView(b);
            }
            optRows.add(opts);
        }

        // Nút nộp bài
        Button bSubmit = new Button(getActivity());
        bSubmit.setText("NỘP BÀI");
        bSubmit.setBackgroundResource(R.drawable.btn_primary);
        bSubmit.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.topMargin = UiKit.dp(getActivity(), 20);
        bSubmit.setLayoutParams(slp);
        bSubmit.setOnClickListener(x -> submit(store));
        box.addView(bSubmit);

        // Hộp kết quả + khối kích thích học tiếp
        resultBox = new LinearLayout(getActivity());
        resultBox.setOrientation(LinearLayout.VERTICAL);
        resultBox.setBackgroundResource(R.drawable.card_bg);
        int pad = UiKit.dp(getActivity(), 16);
        resultBox.setPadding(pad, pad, pad, pad);
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlp.topMargin = UiKit.dp(getActivity(), 16);
        rlp.bottomMargin = UiKit.dp(getActivity(), 32);
        resultBox.setLayoutParams(rlp);
        resultBox.setVisibility(View.GONE);
        box.addView(resultBox);

        return v;
    }

    private void submit(Store store) {
        if (submitted) return;
        for (int p : picked) {
            if (p < 0) {
                android.widget.Toast.makeText(getActivity(),
                        "Bạn chưa trả lời hết các câu hỏi", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
        }
        submitted = true;
        int score = 0;
        for (int qi = 0; qi < lesson.quiz.size(); qi++) {
            Lessons.QuizQ q = lesson.quiz.get(qi);
            Button[] opts = optRows.get(qi);
            for (Button b : opts) b.setEnabled(false);
            opts[q.answer].setBackgroundResource(R.drawable.btn_primary);
            if (picked[qi] == q.answer) {
                score++;
            } else {
                opts[picked[qi]].setBackgroundColor(0xFFEF4444);
            }
        }

        // Ghi nhận học + thuộc 10 từ hôm nay
        store.markStudied();
        for (Lessons.Term t : lesson.terms) store.addLearned(t.word);
        store.incQuiz();
        WidgetUpdater.update(getActivity());

        // Khối kết quả + kích thích học tiếp
        resultBox.removeAllViews();
        int learned = store.learnedCount();
        int streak = store.getStreak();
        int bi = Badges.index(learned, streak);

        TextView tvScore = UiKit.text(getActivity(),
                "\uD83C\uDF89 Điểm: " + score + "/" + lesson.quiz.size(), 22, 0xFFFFFFFF, true);
        tvScore.setGravity(android.view.Gravity.CENTER);
        resultBox.addView(tvScore);

        TextView tvBadge = UiKit.text(getActivity(),
                Badges.ICONS[bi] + " " + Badges.NAMES[bi] + "  ·  \uD83D\uDD25 Streak: " + streak + " ngày",
                16, 0xFFF59E0B, true);
        tvBadge.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        blp.topMargin = UiKit.dp(getActivity(), 8);
        tvBadge.setLayoutParams(blp);
        resultBox.addView(tvBadge);

        TextView tvNext = UiKit.text(getActivity(),
                "Tiếp tục đà này \uD83D\uDE80\n" + Badges.nextGoal(learned, streak),
                15, 0xFFC4B5FD, false);
        tvNext.setGravity(android.view.Gravity.CENTER);
        tvNext.setLayoutParams(blp);
        resultBox.addView(tvNext);

        TextView tvNudge = UiKit.text(getActivity(),
                "\uD83E\uDD16 Dip: \"" + UiKit.randomNudge() + "\"",
                15, 0xFF9CA3AF, false);
        tvNudge.setGravity(android.view.Gravity.CENTER);
        tvNudge.setLayoutParams(blp);
        resultBox.addView(tvNudge);

        Button bMore = new Button(getActivity());
        bMore.setText("HỌC TIẾP \uD83D\uDCDD");
        bMore.setBackgroundResource(R.drawable.btn_primary);
        bMore.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mlp.topMargin = UiKit.dp(getActivity(), 12);
        bMore.setLayoutParams(mlp);
        bMore.setOnClickListener(x -> ((MainActivity) getActivity()).goQuiz());
        resultBox.addView(bMore);

        resultBox.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AudioPlayer.stop();
    }
}
