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
import java.util.Collections;
import java.util.List;

/**
 * Tab Ôn tập (v4.2):
 * - Flashcard: 1 từ / màn hình (thẻ đầy đủ: sao + loa + 3 ví dụ) + nút
 *   "Đã thuộc ✓" + nút mũi tên tròn → (bỏ qua). Từ khó (★) xuất hiện 2 lần
 *   và xếp trước để ôn nhiều hơn.
 * - Quiz từ đã sai + từ khó: 1 câu / màn hình, 3 dạng câu hỏi trộn lẫn:
 *   (1) chọn nghĩa, (2) điền từ vào câu, (3) chọn câu dùng từ đúng.
 *   Chọn đáp án → Next → âm thanh đúng/sai + phản hồi → câu tiếp.
 * KHÔNG còn mascot trong app (v4.2).
 */
public class QuizFragment extends Fragment {

    private LinearLayout box;
    private Store store;

    // Flashcard state
    private List<Lessons.Term> deck = new ArrayList<>();
    private int pos = 0;

    // Quiz state
    private List<QItem> questions = new ArrayList<>();
    private int qi = 0, score = 0;

    /** 1 câu hỏi ôn tập: dạng + từ mục tiêu + câu hỏi + đáp án + đáp án đúng. */
    private static class QItem {
        static final int MEANING = 0, FILL_BLANK = 1, PICK_USAGE = 2;
        int type;
        Lessons.Term term;
        String prompt;      // câu hỏi / câu điền chỗ trống / tiêu đề
        String[] options;   // 4 đáp án
        int answer;
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup parent, Bundle s) {
        View v = inf.inflate(R.layout.fragment_quiz, parent, false);
        box = v.findViewById(R.id.review_box);
        store = new Store(getActivity());
        showFlashcards();
        return v;
    }

    // ---------- Chế độ flashcard ----------
    private void showFlashcards() {
        box.removeAllViews();
        deck.clear();
        List<Lessons.Term> starred = new ArrayList<>();
        List<Lessons.Term> rest = new ArrayList<>();
        for (Lessons.Day d : Lessons.all(getActivity()))
            for (Lessons.Term t : d.terms)
                if (!store.getLearned().contains(t.word)) {
                    if (store.isStarred(t.word)) starred.add(t);
                    else rest.add(t);
                }
        Collections.shuffle(rest);
        // Từ khó xếp trước + xuất hiện 2 lần (ôn nhiều hơn).
        deck.addAll(starred);
        deck.addAll(starred);
        deck.addAll(rest);
        pos = 0;

        TextView tvTitle = UiKit.text(getActivity(), "\uD83D\uDCDD Ôn tập flashcard",
                20, 0xFFFFFFFF, true);
        box.addView(tvTitle);

        LinearLayout cardBox = new LinearLayout(getActivity());
        cardBox.setOrientation(LinearLayout.VERTICAL);
        cardBox.setId(View.generateViewId());
        box.addView(cardBox);

        if (deck.isEmpty()) {
            TextView tvDone = UiKit.text(getActivity(),
                    "Bạn đã thuộc hết " + Lessons.totalTerms(getActivity()) + " từ! \uD83C\uDF89",
                    16, 0xFF9CA3AF, false);
            tvDone.setGravity(Gravity.CENTER);
            cardBox.addView(tvDone);
        } else {
            renderCard(cardBox);
        }

        // Nút quiz từ sai + từ khó
        int wrongN = store.getWrong().size();
        int starN = store.getStarred().size();
        if (wrongN > 0 || starN > 0) {
            Button bWrong = new Button(getActivity());
            bWrong.setText("📝 Kiểm tra " + (wrongN + starN) + " từ (đã sai + từ khó)");
            bWrong.setBackgroundResource(R.drawable.btn_primary);
            bWrong.setTextColor(0xFFFFFFFF);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = UiKit.dp(getActivity(), 16);
            lp.bottomMargin = UiKit.dp(getActivity(), 24);
            bWrong.setLayoutParams(lp);
            bWrong.setOnClickListener(x -> showQuiz());
            box.addView(bWrong);
        }
    }

    private LinearLayout cardBox() {
        return (LinearLayout) box.getChildAt(1);
    }

    private void renderCard(LinearLayout cardBox) {
        cardBox.removeAllViews();

        if (pos >= deck.size()) {
            TextView tvDone = UiKit.text(getActivity(),
                    "Xong! Bạn đã ôn " + deck.size() + " thẻ \uD83C\uDF89",
                    16, 0xFF9CA3AF, false);
            tvDone.setGravity(Gravity.CENTER);
            cardBox.addView(tvDone);
            return;
        }

        Lessons.Term t = deck.get(pos);
        TextView tvProgress = UiKit.text(getActivity(),
                "Thẻ " + (pos + 1) + "/" + deck.size(), 14, 0xFF9CA3AF, false);
        tvProgress.setGravity(Gravity.CENTER);
        cardBox.addView(tvProgress);
        cardBox.addView(UiKit.termCard(getActivity(), t));

        Button bKnown = new Button(getActivity());
        bKnown.setText("Đã thuộc ✓");
        bKnown.setBackgroundResource(R.drawable.btn_primary);
        bKnown.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams klp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        klp.topMargin = UiKit.dp(getActivity(), 12);
        bKnown.setLayoutParams(klp);
        bKnown.setOnClickListener(x -> {
            store.addLearned(t.word);
            store.scheduleNew(t.word);
            store.markStudied();
            WidgetUpdater.update(getActivity());
            pos++;
            renderCard(cardBox);
        });
        cardBox.addView(bKnown);

        // Mũi tên → = bỏ qua thẻ này (chưa thuộc), sang thẻ tiếp theo.
        cardBox.addView(UiKit.nextArrow(getActivity(), "BỎ QUA", 0, x -> {
            pos++;
            renderCard(cardBox);
        }));
    }

    // ---------- Chế độ quiz: 3 dạng câu hỏi ----------
    private void showQuiz() {
        box.removeAllViews();
        questions.clear();
        List<Lessons.Term> pool = new ArrayList<>();
        for (String w : store.getWrong()) {
            Lessons.Term t = Lessons.find(getActivity(), w);
            if (t != null) pool.add(t);
        }
        for (String w : store.getStarred()) {
            Lessons.Term t = Lessons.find(getActivity(), w);
            if (t != null && !pool.contains(t)) pool.add(t);
        }
        Collections.shuffle(pool);
        if (pool.size() > 8) pool = pool.subList(0, 8);
        if (pool.isEmpty()) { showFlashcards(); return; }

        // 1 từ = 1 câu hỏi, dạng câu hỏi xoay vòng theo thứ tự ngẫu nhiên.
        List<Integer> types = new ArrayList<>();
        types.add(QItem.MEANING);
        types.add(QItem.FILL_BLANK);
        types.add(QItem.PICK_USAGE);
        Collections.shuffle(types);
        for (int i = 0; i < pool.size(); i++) {
            QItem q = buildQuestion(pool.get(i), types.get(i % types.size()));
            if (q != null) questions.add(q);
        }
        if (questions.isEmpty()) { showFlashcards(); return; }
        qi = 0; score = 0;

        TextView tvTitle = UiKit.text(getActivity(), "📝 Ôn từ đã sai + từ khó",
                20, 0xFFFFFFFF, true);
        box.addView(tvTitle);
        renderQuestion();
    }

    /** Dựng 1 câu hỏi theo dạng. Trả null nếu thiếu dữ liệu. */
    private QItem buildQuestion(Lessons.Term t, int type) {
        List<Lessons.Term> all = new ArrayList<>();
        for (Lessons.Day d : Lessons.all(getActivity()))
            for (Lessons.Term x : d.terms)
                if (!x.word.equals(t.word)) all.add(x);
        Collections.shuffle(all);
        if (all.size() < 3) return null;

        QItem q = new QItem();
        q.type = type;
        q.term = t;
        if (type == QItem.MEANING) {
            q.prompt = t.word;
            List<String> opts = new ArrayList<>();
            opts.add(t.meaning);
            for (int i = 0; i < 3; i++) opts.add(all.get(i).meaning);
            Collections.shuffle(opts);
            q.options = opts.toArray(new String[0]);
            for (int i = 0; i < q.options.length; i++)
                if (q.options[i].equals(t.meaning)) q.answer = i;
        } else if (type == QItem.FILL_BLANK) {
            String ex = pickExample(t);
            if (ex == null) return null;
            q.prompt = blankWord(ex, t.word);
            if (q.prompt == null) return null;
            List<String> opts = new ArrayList<>();
            opts.add(t.word);
            for (int i = 0; i < 3; i++) opts.add(all.get(i).word);
            Collections.shuffle(opts);
            q.options = opts.toArray(new String[0]);
            for (int i = 0; i < q.options.length; i++)
                if (q.options[i].equals(t.word)) q.answer = i;
        } else { // PICK_USAGE
            String good = pickExample(t);
            if (good == null) return null;
            q.prompt = "Câu nào dùng từ \"" + t.word + "\" ĐÚNG?";
            List<String> opts = new ArrayList<>();
            opts.add(good);
            for (int i = 0; i < 3; i++) {
                String bad = pickExample(all.get(i));
                if (bad != null) opts.add(bad);
            }
            if (opts.size() < 4) return null;
            Collections.shuffle(opts);
            q.options = opts.toArray(new String[0]);
            for (int i = 0; i < q.options.length; i++)
                if (q.options[i].equals(good)) q.answer = i;
        }
        return q;
    }

    /** Lấy 1 câu ví dụ của từ (ưu tiên examples[], fallback example). */
    private String pickExample(Lessons.Term t) {
        if (t.examples != null && !t.examples.isEmpty())
            return t.examples.get((int) (Math.random() * t.examples.size()));
        return t.example;
    }

    /** Thay lần xuất hiện đầu tiên của từ mục tiêu trong câu bằng "___". */
    private String blankWord(String sentence, String word) {
        int idx = sentence.toLowerCase().indexOf(word.toLowerCase());
        if (idx < 0) return null;
        return sentence.substring(0, idx) + "___" + sentence.substring(idx + word.length());
    }

    private void renderQuestion() {
        while (box.getChildCount() > 1) box.removeViewAt(1);
        if (qi >= questions.size()) { showQuizResult(); return; }
        QItem q = questions.get(qi);

        TextView tvP = UiKit.text(getActivity(),
                "Câu " + (qi + 1) + "/" + questions.size(), 14, 0xFF9CA3AF, false);
        box.addView(tvP);

        if (q.type == QItem.MEANING) {
            TextView tvW = UiKit.text(getActivity(), q.prompt, 24, 0xFFFFFFFF, true);
            tvW.setGravity(Gravity.CENTER);
            box.addView(tvW);
            TextView tvIpa = UiKit.text(getActivity(),
                    q.term.ipa + " · " + q.term.pos, 14, 0xFFC4B5FD, false);
            tvIpa.setGravity(Gravity.CENTER);
            box.addView(tvIpa);
            TextView tvHint = UiKit.text(getActivity(), "Chọn nghĩa đúng:",
                    15, 0xFF9CA3AF, false);
            box.addView(tvHint);
        } else if (q.type == QItem.FILL_BLANK) {
            TextView tvS = UiKit.text(getActivity(), "Điền từ vào chỗ trống:",
                    15, 0xFF9CA3AF, false);
            box.addView(tvS);
            TextView tvSent = UiKit.text(getActivity(), q.prompt, 18, 0xFFFFFFFF, true);
            box.addView(tvSent);
        } else {
            TextView tvS = UiKit.text(getActivity(), q.prompt, 18, 0xFFFFFFFF, true);
            box.addView(tvS);
        }

        final Button[] opts = new Button[q.options.length];
        final int[] picked = {-1};
        final boolean[] answered = {false};
        final LinearLayout feedbackBox = new LinearLayout(getActivity());
        feedbackBox.setOrientation(LinearLayout.VERTICAL);
        final LinearLayout arrowSlot = new LinearLayout(getActivity());
        arrowSlot.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < q.options.length; i++) {
            final int idx = i;
            Button b = new Button(getActivity());
            b.setText((char) ('A' + i) + ". " + q.options[i]);
            b.setBackgroundResource(R.drawable.btn_ghost);
            b.setTextColor(0xFFFFFFFF);
            b.setAllCaps(false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = UiKit.dp(getActivity(), 8);
            b.setLayoutParams(lp);
            b.setOnClickListener(x -> {
                if (answered[0]) return;
                picked[0] = idx;
                for (int k = 0; k < opts.length; k++)
                    opts[k].setBackgroundResource(k == idx
                            ? R.drawable.btn_primary : R.drawable.btn_ghost);
            });
            opts[i] = b;
            box.addView(b);
        }

        box.addView(feedbackBox);
        box.addView(arrowSlot);
        arrowSlot.addView(UiKit.nextArrow(getActivity(), "TRẢ LỜI", 0, x -> {
            if (answered[0]) { // đã có phản hồi → sang câu tiếp
                qi++;
                renderQuestion();
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
                if (q.type == QItem.MEANING) store.removeWrong(q.term.word);
            } else {
                SoundFx.wrong(getActivity());
                if (q.type == QItem.MEANING) store.addWrong(q.term.word);
            }
            store.markStudied();
            for (Button b : opts) b.setEnabled(false);
            opts[q.answer].setBackgroundResource(R.drawable.btn_primary);
            if (!correct) opts[picked[0]].setBackgroundColor(0xFFEF4444);

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
                    qi + 1 >= questions.size() ? "XEM KẾT QUẢ" : "CÂU TIẾP",
                    correct ? 1 : 2, y -> {
                        qi++;
                        renderQuestion();
                    }));
        }));
    }

    private void showQuizResult() {
        while (box.getChildCount() > 1) box.removeViewAt(1);
        store.incQuiz();
        WidgetUpdater.update(getActivity());

        int learned = store.learnedCount();
        int streak = store.getStreak();
        int bi = Badges.index(learned, streak);

        TextView tvScore = UiKit.text(getActivity(),
                "\uD83C\uDF89 Điểm: " + score + "/" + questions.size(), 22, 0xFFFFFFFF, true);
        tvScore.setGravity(Gravity.CENTER);
        box.addView(tvScore);

        TextView tvBadge = UiKit.text(getActivity(),
                Badges.ICONS[bi] + " " + Badges.NAMES[bi] + "  ·  \uD83D\uDD25 Streak: " + streak + " ngày",
                16, 0xFFF59E0B, true);
        tvBadge.setGravity(Gravity.CENTER);
        box.addView(tvBadge);

        TextView tvNext = UiKit.text(getActivity(),
                "Tiếp tục đà này \uD83D\uDE80\n" + Badges.nextGoal(learned, streak),
                15, 0xFFC4B5FD, false);
        tvNext.setGravity(Gravity.CENTER);
        box.addView(tvNext);

        Button bBack = new Button(getActivity());
        bBack.setText("← Về flashcard");
        bBack.setBackgroundResource(R.drawable.btn_ghost);
        bBack.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = UiKit.dp(getActivity(), 12);
        lp.bottomMargin = UiKit.dp(getActivity(), 24);
        bBack.setLayoutParams(lp);
        bBack.setOnClickListener(x -> showFlashcards());
        box.addView(bBack);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AudioPlayer.stop();
    }
}
