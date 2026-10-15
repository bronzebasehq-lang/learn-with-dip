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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Tab Ôn tập: flashcard đầy đủ format + quiz từ đã trả lời sai. */
public class QuizFragment extends Fragment {

    private LinearLayout box;
    private Store store;

    // Flashcard state
    private List<Lessons.Term> deck = new ArrayList<>();
    private int pos = 0;

    // Quiz state
    private List<Lessons.Term> qTerms = new ArrayList<>();
    private int qi = 0, score = 0;
    private boolean answered = false;
    private Button[] opts = new Button[4];
    private int correctOpt = 0;
    private boolean quizMode = false;

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
        quizMode = false;
        box.removeAllViews();
        deck.clear();
        for (Lessons.Day d : Lessons.all(getActivity()))
            for (Lessons.Term t : d.terms)
                if (!store.getLearned().contains(t.word)) deck.add(t);
        Collections.shuffle(deck);
        pos = 0;

        TextView tvTitle = UiKit.text(getActivity(), "\uD83D\uDCDD Ôn tập flashcard",
                20, 0xFFFFFFFF, true);
        box.addView(tvTitle);

        // Container riêng cho thẻ (để nút quiz từ sai không bị xóa khi lật thẻ)
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

        // Nút quiz từ sai
        int wrongN = store.getWrong().size();
        if (wrongN > 0) {
            Button bWrong = new Button(getActivity());
            bWrong.setText("📝 Kiểm tra " + wrongN + " từ đã sai");
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

    private void renderCard() {
        renderCard(cardBox());
    }

    private void renderCard(LinearLayout cardBox) {
        cardBox.removeAllViews();

        if (pos >= deck.size()) {
            TextView tvDone = UiKit.text(getActivity(),
                    "Xong! Bạn đã ôn " + deck.size() + " từ \uD83C\uDF89",
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

        LinearLayout row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlp.topMargin = UiKit.dp(getActivity(), 12);
        rlp.bottomMargin = UiKit.dp(getActivity(), 16);
        row.setLayoutParams(rlp);

        Button bUnknown = new Button(getActivity());
        bUnknown.setText("Chưa thuộc");
        bUnknown.setBackgroundResource(R.drawable.btn_ghost);
        bUnknown.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        blp.rightMargin = UiKit.dp(getActivity(), 8);
        bUnknown.setLayoutParams(blp);
        bUnknown.setOnClickListener(x -> { pos++; renderCard(); });

        Button bKnown = new Button(getActivity());
        bKnown.setText("Đã thuộc ✓");
        bKnown.setBackgroundResource(R.drawable.btn_primary);
        bKnown.setTextColor(0xFFFFFFFF);
        bKnown.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        bKnown.setOnClickListener(x -> {
            store.addLearned(t.word);
            store.scheduleNew(t.word);
            store.markStudied();
            WidgetUpdater.update(getActivity());
            pos++;
            renderCard();
        });

        row.addView(bUnknown);
        row.addView(bKnown);
        cardBox.addView(row);
    }

    // ---------- Chế độ quiz từ sai ----------
    private void showQuiz() {
        quizMode = true;
        box.removeAllViews();
        qTerms.clear();
        for (String w : store.getWrong()) {
            Lessons.Term t = Lessons.find(getActivity(), w);
            if (t != null) qTerms.add(t);
        }
        if (qTerms.isEmpty()) { showFlashcards(); return; }
        Collections.shuffle(qTerms);
        if (qTerms.size() > 5) qTerms = qTerms.subList(0, 5);
        qi = 0; score = 0;

        TextView tvTitle = UiKit.text(getActivity(), "📝 Ôn từ đã sai",
                20, 0xFFFFFFFF, true);
        box.addView(tvTitle);
        renderQuestion();
    }

    private void renderQuestion() {
        while (box.getChildCount() > 1) box.removeViewAt(1);
        if (qi >= qTerms.size()) { showQuizResult(); return; }
        answered = false;
        Lessons.Term t = qTerms.get(qi);

        TextView tvP = UiKit.text(getActivity(),
                "Câu " + (qi + 1) + "/" + qTerms.size(), 14, 0xFF9CA3AF, false);
        box.addView(tvP);
        TextView tvW = UiKit.text(getActivity(), t.word, 24, 0xFFFFFFFF, true);
        tvW.setGravity(Gravity.CENTER);
        box.addView(tvW);
        TextView tvIpa = UiKit.text(getActivity(), t.ipa + " · " + t.pos, 14, 0xFFC4B5FD, false);
        tvIpa.setGravity(Gravity.CENTER);
        box.addView(tvIpa);

        // 4 đáp án: nghĩa đúng + 3 nghĩa gây nhiễu
        List<Lessons.Term> pool = new ArrayList<>();
        for (Lessons.Day d : Lessons.all(getActivity()))
            for (Lessons.Term x : d.terms)
                if (!x.word.equals(t.word)) pool.add(x);
        Collections.shuffle(pool);
        List<Lessons.Term> options = new ArrayList<>();
        options.add(t);
        for (int i = 0; i < 3 && i < pool.size(); i++) options.add(pool.get(i));
        Collections.shuffle(options);

        for (int i = 0; i < options.size(); i++) {
            final int idx = i;
            Button b = new Button(getActivity());
            b.setText((char) ('A' + i) + ". " + options.get(i).meaning);
            b.setBackgroundResource(R.drawable.btn_ghost);
            b.setTextColor(0xFFFFFFFF);
            b.setAllCaps(false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = UiKit.dp(getActivity(), 8);
            b.setLayoutParams(lp);
            b.setOnClickListener(x -> answer(idx));
            opts[i] = b;
            box.addView(b);
            if (options.get(i).word.equals(t.word)) correctOpt = i;
        }
    }

    private void answer(int idx) {
        if (answered) return;
        answered = true;
        Lessons.Term t = qTerms.get(qi);
        for (Button b : opts) if (b != null) b.setEnabled(false);
        opts[correctOpt].setBackgroundResource(R.drawable.btn_primary);
        if (idx == correctOpt) {
            score++;
            store.removeWrong(t.word);
        } else {
            opts[idx].setBackgroundColor(0xFFEF4444);
        }
        store.markStudied();

        Button bNext = new Button(getActivity());
        bNext.setText(qi + 1 >= qTerms.size() ? "Xem kết quả" : "Câu tiếp →");
        bNext.setBackgroundResource(R.drawable.btn_primary);
        bNext.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = UiKit.dp(getActivity(), 12);
        lp.bottomMargin = UiKit.dp(getActivity(), 24);
        bNext.setLayoutParams(lp);
        bNext.setOnClickListener(x -> { qi++; renderQuestion(); });
        box.addView(bNext);
    }

    private void showQuizResult() {
        while (box.getChildCount() > 1) box.removeViewAt(1);
        store.incQuiz();
        WidgetUpdater.update(getActivity());

        int learned = store.learnedCount();
        int streak = store.getStreak();
        int bi = Badges.index(learned, streak);

        TextView tvScore = UiKit.text(getActivity(),
                "\uD83C\uDF89 Điểm: " + score + "/" + qTerms.size(), 22, 0xFFFFFFFF, true);
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

        TextView tvNudge = UiKit.text(getActivity(),
                "\uD83E\uDD16 Dip: \"" + UiKit.randomNudge() + "\"",
                15, 0xFF9CA3AF, false);
        tvNudge.setGravity(Gravity.CENTER);
        box.addView(tvNudge);

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
