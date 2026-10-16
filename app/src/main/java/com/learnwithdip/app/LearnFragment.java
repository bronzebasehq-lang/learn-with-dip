package com.learnwithdip.app;

import android.app.Fragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/**
 * Tab Học (v4.2):
 * Phase A — 1 TỪ / MÀN HÌNH: thẻ từ (từ + sao + loa + IPA + nghĩa + 3 ví dụ)
 *              + nút mũi tên tròn → sang từ tiếp theo.
 * Phase B — Trang ôn tập: danh sách từ vừa học + nút START QUIZ.
 * Phase C — ĐỌC HIỂU (phần quan trọng nhất, giữ format cũ + thêm đoạn 2):
 *   Đoạn 1 (1 màn hình) → Quiz 1: 1 câu / màn hình (chọn đáp án → Next →
 *   âm thanh đúng/sai + phản hồi → câu tiếp) → Đoạn 2 (ôn từ đã học) →
 *   Quiz 2 (2 câu, cùng luồng) → Kết quả.
 * Trên cùng: khối "Ôn tập đến hạn" (spaced repetition 1/3/7 ngày) nếu có từ đến hạn.
 * KHÔNG còn mascot trong app (v4.2).
 */
public class LearnFragment extends Fragment {

    private Lessons.Day lesson;
    private Store store;
    private LinearLayout box;
    private LinearLayout phaseBox;
    private LinearLayout resultBox;
    /** Day mở lại từ "Xem lại bài cũ" (Hồ sơ). -1 = bài hôm nay. */
    private int overrideDay = -1;

    // Flashcard state
    private int cardIndex = 0;

    // Spaced-repetition due review state
    private List<Lessons.Term> dueTerms = new ArrayList<>();
    private int duePos = 0;
    private LinearLayout dueBlock;

    // Phase C state
    private int score = 0;
    private int totalQuestions = 0;

    public static LearnFragment forDay(int day) {
        LearnFragment f = new LearnFragment();
        Bundle b = new Bundle();
        b.putInt("day", day);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup parent, Bundle s) {
        View v = inf.inflate(R.layout.fragment_learn, parent, false);
        box = v.findViewById(R.id.lesson_box);
        store = new Store(getActivity());
        if (getArguments() != null) overrideDay = getArguments().getInt("day", -1);
        int dayIdx = overrideDay > 0 ? overrideDay : store.getDayIndex();
        lesson = Lessons.get(getActivity(), dayIdx);
        if (lesson == null) return v;

        // Tiêu đề Day N
        TextView tvTitle = UiKit.text(getActivity(),
                "Day " + lesson.day + ": " + lesson.topic, 22, 0xFFFFFFFF, true);
        box.addView(tvTitle);
        if (overrideDay > 0) {
            TextView tvRe = UiKit.text(getActivity(),
                    "Đang xem lại bài cũ — học lại để ôn, không tính vào streak hôm nay.",
                    14, 0xFFF59E0B, false);
            box.addView(tvRe);
        } else {
            TextView tvSub = UiKit.text(getActivity(),
                    "Chào " + store.getName() + "! Bài học hôm nay có 10 thuật ngữ mới \uD83C\uDF93",
                    14, 0xFF9CA3AF, false);
            box.addView(tvSub);
        }

        // Khối ôn tập đến hạn (spaced repetition) — chỉ hiện khi có từ đến hạn
        List<String> dueWords = store.getDueReviews();
        dueTerms.clear();
        for (String w : dueWords) {
            Lessons.Term t = Lessons.find(getActivity(), w);
            if (t != null) dueTerms.add(t);
        }
        if (!dueTerms.isEmpty() && overrideDay <= 0) {
            dueBlock = new LinearLayout(getActivity());
            dueBlock.setOrientation(LinearLayout.VERTICAL);
            dueBlock.setBackgroundResource(R.drawable.card_bg);
            int pad = UiKit.dp(getActivity(), 16);
            dueBlock.setPadding(pad, pad, pad, pad);
            LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dlp.topMargin = UiKit.dp(getActivity(), 12);
            dueBlock.setLayoutParams(dlp);

            TextView tvDue = UiKit.text(getActivity(),
                    "\uD83D\uDD01 Ôn tập đến hạn: " + dueTerms.size() + " từ",
                    17, 0xFFF59E0B, true);
            dueBlock.addView(tvDue);
            TextView tvDueSub = UiKit.text(getActivity(),
                    "Những từ này đến lịch ôn lại — ôn nhanh rồi học bài mới.",
                    14, 0xFF9CA3AF, false);
            tvDueSub.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            dueBlock.addView(tvDueSub);

            Button bDue = new Button(getActivity());
            bDue.setText("ÔN TẬP NGAY \u2192");
            bDue.setBackgroundResource(R.drawable.btn_primary);
            bDue.setTextColor(0xFFFFFFFF);
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            blp.topMargin = UiKit.dp(getActivity(), 10);
            bDue.setLayoutParams(blp);
            bDue.setOnClickListener(x -> showDueReview());
            dueBlock.addView(bDue);

            box.addView(dueBlock);
        }

        // Khối phase: flashcard -> review -> reading + quiz
        phaseBox = new LinearLayout(getActivity());
        phaseBox.setOrientation(LinearLayout.VERTICAL);
        box.addView(phaseBox);

        showFlashcard();
        return v;
    }

    // ---------- Phase A: 1 từ / màn hình ----------
    private void showFlashcard() {
        phaseBox.removeAllViews();
        if (cardIndex >= lesson.terms.size()) {
            showReview();
            return;
        }
        Lessons.Term t = lesson.terms.get(cardIndex);

        TextView tvProgress = UiKit.text(getActivity(),
                "Word " + (cardIndex + 1) + " of " + lesson.terms.size(),
                16, 0xFFC4B5FD, true);
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        plp.topMargin = UiKit.dp(getActivity(), 16);
        tvProgress.setLayoutParams(plp);
        phaseBox.addView(tvProgress);

        LinearLayout bar = new LinearLayout(getActivity());
        bar.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UiKit.dp(getActivity(), 8));
        barLp.topMargin = UiKit.dp(getActivity(), 8);
        bar.setLayoutParams(barLp);
        View fill = new View(getActivity());
        LinearLayout.LayoutParams fillLp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, cardIndex + 1);
        fill.setBackgroundColor(0xFF8B5CF6);
        fill.setLayoutParams(fillLp);
        bar.addView(fill);
        View rest = new View(getActivity());
        LinearLayout.LayoutParams restLp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Math.max(lesson.terms.size() - cardIndex - 1, 0));
        rest.setBackgroundColor(0xFF2A2752);
        rest.setLayoutParams(restLp);
        bar.addView(rest);
        phaseBox.addView(bar);

        // Thẻ từ duy nhất (từ + sao + loa + IPA + loại từ + Nghĩa + 3 Ví dụ + Dịch)
        phaseBox.addView(UiKit.termCard(getActivity(), t));

        // Nút mũi tên tròn → sang từ tiếp theo (từ cuối → sang trang ôn tập)
        boolean last = cardIndex == lesson.terms.size() - 1;
        phaseBox.addView(UiKit.nextArrow(getActivity(),
                last ? "ÔN TẬP" : "NEXT", 0, x -> {
                    cardIndex++;
                    showFlashcard();
                }));
    }

    // ---------- Phase B: Trang ôn tập ----------
    private void showReview() {
        phaseBox.removeAllViews();

        TextView tvHead = UiKit.text(getActivity(), "REVIEW", 16, 0xFFC4B5FD, true);
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hlp.topMargin = UiKit.dp(getActivity(), 16);
        tvHead.setLayoutParams(hlp);
        phaseBox.addView(tvHead);

        TextView tvTitle = UiKit.text(getActivity(),
                "Day " + lesson.day + "  •  " + lesson.topic, 20, 0xFFFFFFFF, true);
        phaseBox.addView(tvTitle);

        TextView tvSub = UiKit.text(getActivity(),
                lesson.terms.size() + " words you just learned — English first, "
                        + "giải thích bằng tiếng Việt kèm theo.",
                14, 0xFF9CA3AF, false);
        phaseBox.addView(tvSub);

        for (int i = 0; i < lesson.terms.size(); i++) {
            Lessons.Term t = lesson.terms.get(i);
            LinearLayout row = new LinearLayout(getActivity());
            row.setOrientation(LinearLayout.VERTICAL);
            row.setBackgroundResource(R.drawable.card_bg);
            int pad = UiKit.dp(getActivity(), 12);
            row.setPadding(pad, pad, pad, pad);
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rlp.topMargin = UiKit.dp(getActivity(), 8);
            row.setLayoutParams(rlp);

            TextView tvWord = UiKit.text(getActivity(),
                    (i + 1) + ".  " + t.word, 17, 0xFFFFFFFF, true);
            row.addView(tvWord);
            TextView tvLine = UiKit.text(getActivity(),
                    t.ipa + "  •  " + t.meaning, 14, 0xFF9CA3AF, false);
            row.addView(tvLine);
            phaseBox.addView(row);
        }

        Button bQuiz = new Button(getActivity());
        bQuiz.setText("START QUIZ \u2192");
        bQuiz.setBackgroundResource(R.drawable.btn_primary);
        bQuiz.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams qlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        qlp.topMargin = UiKit.dp(getActivity(), 16);
        qlp.bottomMargin = UiKit.dp(getActivity(), 24);
        bQuiz.setLayoutParams(qlp);
        bQuiz.setOnClickListener(x -> {
            score = 0;
            totalQuestions = lesson.quiz.size() + lesson.quiz2.size();
            showReading(lesson.reading, lesson.readingVi, "Đoạn đọc hiểu 1",
                    () -> showQuestionFlow(lesson.quiz, 0,
                            () -> showReading(lesson.reading2, lesson.reading2Vi,
                                    "Đoạn đọc hiểu 2 — Ôn lại từ đã học",
                                    () -> showQuestionFlow(lesson.quiz2, 0, this::showResult))));
        });
        phaseBox.addView(bQuiz);
    }

    // ---------- Phase C: Đọc hiểu — đoạn văn 1 màn hình riêng ----------
    private void showReading(String passage, String translation, String title, Runnable next) {
        phaseBox.removeAllViews();
        phaseBox.addView(UiKit.sectionHeader(getActivity(), "\uD83D\uDCD6 " + title));
        TextView tvReading = UiKit.text(getActivity(), passage, 16, 0xFFFFFFFF, false);
        tvReading.setLineSpacing(UiKit.dp(getActivity(), 4), 1f);
        phaseBox.addView(tvReading);

        phaseBox.addView(UiKit.sectionHeader(getActivity(), "\uD83C\uDDFB\uD83C\uDDF3 Bản dịch"));
        TextView tvReadingVi = UiKit.text(getActivity(), translation, 15, 0xFF9CA3AF, false);
        tvReadingVi.setLineSpacing(UiKit.dp(getActivity(), 4), 1f);
        phaseBox.addView(tvReadingVi);

        phaseBox.addView(UiKit.nextArrow(getActivity(), "LÀM BÀI", 0, x -> next.run()));
    }

    // ---------- Phase C: Quiz — 1 câu / màn hình, Next → âm thanh + phản hồi ----------
    private void showQuestionFlow(List<Lessons.QuizQ> questions, int idx, Runnable onDone) {
        phaseBox.removeAllViews();
        if (idx >= questions.size()) {
            onDone.run();
            return;
        }
        Lessons.QuizQ q = questions.get(idx);

        TextView tvP = UiKit.text(getActivity(),
                "Câu " + (idx + 1) + " / " + questions.size(), 14, 0xFFC4B5FD, true);
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        plp.topMargin = UiKit.dp(getActivity(), 16);
        tvP.setLayoutParams(plp);
        phaseBox.addView(tvP);

        TextView tvQ = UiKit.text(getActivity(), q.q, 17, 0xFFFFFFFF, true);
        LinearLayout.LayoutParams qlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        qlp.topMargin = UiKit.dp(getActivity(), 10);
        tvQ.setLayoutParams(qlp);
        phaseBox.addView(tvQ);

        final Button[] opts = new Button[q.options.length];
        final int[] picked = {-1};
        final boolean[] answered = {false};
        final LinearLayout feedbackBox = new LinearLayout(getActivity());
        feedbackBox.setOrientation(LinearLayout.VERTICAL);
        final LinearLayout arrowSlot = new LinearLayout(getActivity());
        arrowSlot.setOrientation(LinearLayout.VERTICAL);

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
                if (answered[0]) return;
                picked[0] = oIdx;
                for (int k = 0; k < opts.length; k++)
                    opts[k].setBackgroundResource(k == oIdx
                            ? R.drawable.btn_primary : R.drawable.btn_ghost);
            });
            opts[oi] = b;
            phaseBox.addView(b);
        }

        phaseBox.addView(feedbackBox);
        phaseBox.addView(arrowSlot);
        arrowSlot.addView(UiKit.nextArrow(getActivity(), "TRẢ LỜI", 0, x -> {
            if (answered[0]) { // đã có phản hồi → sang câu tiếp
                showQuestionFlow(questions, idx + 1, onDone);
                return;
            }
            if (picked[0] < 0) {
                Toast.makeText(getActivity(), "Chọn đáp án đã nhé",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            answered[0] = true;
            boolean correct = picked[0] == q.answer;
            if (correct) {
                score++;
                SoundFx.correct(getActivity());
            } else {
                SoundFx.wrong(getActivity());
            }
            for (Button b : opts) b.setEnabled(false);
            opts[q.answer].setBackgroundResource(R.drawable.btn_primary);
            if (!correct) opts[picked[0]].setBackgroundColor(0xFFEF4444);

            feedbackBox.removeAllViews();
            TextView tvFb = UiKit.text(getActivity(),
                    correct ? "✓ Chính xác!" : "✗ Chưa đúng! Đáp án đúng: "
                            + (char) ('A' + q.answer) + ". " + q.options[q.answer],
                    17, correct ? 0xFF4ADE80 : 0xFFF87171, true);
            tvFb.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            flp.topMargin = UiKit.dp(getActivity(), 14);
            tvFb.setLayoutParams(flp);
            feedbackBox.addView(tvFb);

            arrowSlot.removeAllViews();
            arrowSlot.addView(UiKit.nextArrow(getActivity(),
                    idx + 1 >= questions.size() ? "TIẾP TỤC" : "CÂU TIẾP",
                    correct ? 1 : 2, y -> showQuestionFlow(questions, idx + 1, onDone)));
        }));
    }

    // ---------- Phase C: Kết quả ----------
    private void showResult() {
        phaseBox.removeAllViews();

        // Ghi nhận học + thuộc 10 từ hôm nay + vào hàng đợi ôn ngắt quãng
        if (overrideDay <= 0) store.markStudied();
        for (Lessons.Term t : lesson.terms) {
            store.addLearned(t.word);
            store.scheduleNew(t.word);
        }
        store.incQuiz();
        WidgetUpdater.update(getActivity());

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
        phaseBox.addView(resultBox);

        int learned = store.learnedCount();
        int streak = store.getStreak();
        int bi = Badges.index(learned, streak);

        TextView tvScore = UiKit.text(getActivity(),
                "\uD83C\uDF89 Điểm: " + score + "/" + totalQuestions, 22, 0xFFFFFFFF, true);
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
    }

    // ---------- Ôn tập đến hạn (spaced repetition) ----------
    private void showDueReview() {
        duePos = 0;
        renderDueCard();
    }

    private void renderDueCard() {
        phaseBox.removeAllViews();
        if (duePos >= dueTerms.size()) {
            TextView tvDone = UiKit.text(getActivity(),
                    "Xong! Đã ôn " + dueTerms.size() + " từ \uD83D\uDD01",
                    17, 0xFFFFFFFF, true);
            tvDone.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dlp.topMargin = UiKit.dp(getActivity(), 16);
            tvDone.setLayoutParams(dlp);
            phaseBox.addView(tvDone);

            Button bBack = new Button(getActivity());
            bBack.setText("VỀ BÀI HỌC \u2192");
            bBack.setBackgroundResource(R.drawable.btn_primary);
            bBack.setTextColor(0xFFFFFFFF);
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            blp.topMargin = UiKit.dp(getActivity(), 12);
            bBack.setLayoutParams(blp);
            bBack.setOnClickListener(x -> {
                if (dueBlock != null) box.removeView(dueBlock);
                showFlashcard();
            });
            phaseBox.addView(bBack);
            return;
        }

        Lessons.Term t = dueTerms.get(duePos);
        TextView tvProgress = UiKit.text(getActivity(),
                "Ôn tập " + (duePos + 1) + " / " + dueTerms.size(),
                16, 0xFFC4B5FD, true);
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        plp.topMargin = UiKit.dp(getActivity(), 16);
        tvProgress.setLayoutParams(plp);
        phaseBox.addView(tvProgress);

        phaseBox.addView(UiKit.termCard(getActivity(), t));

        LinearLayout row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlp.topMargin = UiKit.dp(getActivity(), 12);
        rlp.bottomMargin = UiKit.dp(getActivity(), 24);
        row.setLayoutParams(rlp);

        Button bForgot = new Button(getActivity());
        bForgot.setText("Chưa nhớ");
        bForgot.setBackgroundResource(R.drawable.btn_ghost);
        bForgot.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        blp.rightMargin = UiKit.dp(getActivity(), 8);
        bForgot.setLayoutParams(blp);
        bForgot.setOnClickListener(x -> {
            store.reviewWord(t.word, false);
            store.markStudied();
            duePos++;
            renderDueCard();
        });

        Button bRemembered = new Button(getActivity());
        bRemembered.setText("Nhớ rồi ✓");
        bRemembered.setBackgroundResource(R.drawable.btn_primary);
        bRemembered.setTextColor(0xFFFFFFFF);
        bRemembered.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        bRemembered.setOnClickListener(x -> {
            store.reviewWord(t.word, true);
            store.markStudied();
            duePos++;
            renderDueCard();
        });

        row.addView(bForgot);
        row.addView(bRemembered);
        phaseBox.addView(row);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AudioPlayer.stop();
    }
}
