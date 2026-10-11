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
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** Tab Ôn tập: quiz trắc nghiệm 4 đáp án, lưu từ sai để ôn lại. */
public class QuizFragment extends Fragment {
    private List<Integer> questions = new ArrayList<>();
    private int qi = 0, score = 0;
    private List<Integer> wrongThisRound = new ArrayList<>();
    private TextView tvQProgress, tvQWord, tvQResult;
    private Button[] opts = new Button[4];
    private Button btnNext, btnWrongMode;
    private LinearLayout quizBox, resultBox;
    private int correctOpt = 0;
    private boolean answered = false;

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup parent, Bundle s) {
        View v = inf.inflate(R.layout.fragment_quiz, parent, false);
        tvQProgress = v.findViewById(R.id.tv_qprogress);
        tvQWord = v.findViewById(R.id.tv_qword);
        tvQResult = v.findViewById(R.id.tv_qresult);
        opts[0] = v.findViewById(R.id.opt0);
        opts[1] = v.findViewById(R.id.opt1);
        opts[2] = v.findViewById(R.id.opt2);
        opts[3] = v.findViewById(R.id.opt3);
        btnNext = v.findViewById(R.id.btn_qnext);
        btnWrongMode = v.findViewById(R.id.btn_wrongmode);
        quizBox = v.findViewById(R.id.quiz_box);
        resultBox = v.findViewById(R.id.result_box);

        Store store = new Store(getActivity());
        if (!store.getWrong().isEmpty()) {
            btnWrongMode.setVisibility(View.VISIBLE);
            btnWrongMode.setText("Ôn " + store.getWrong().size() + " từ đã sai");
            btnWrongMode.setOnClickListener(x -> startQuiz(wrongPool(store), store));
        } else {
            btnWrongMode.setVisibility(View.GONE);
        }

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            opts[i].setOnClickListener(x -> answer(idx));
        }
        btnNext.setOnClickListener(x -> {
            qi++;
            if (qi >= questions.size()) showResult(store);
            else showQuestion();
        });
        v.findViewById(R.id.btn_qretry).setOnClickListener(x -> startQuiz(learnedPool(store), store));
        v.findViewById(R.id.btn_qlearn).setOnClickListener(x -> ((MainActivity) getActivity()).goLearn());

        startQuiz(learnedPool(store), store);
        return v;
    }

    private List<Integer> learnedPool(Store store) {
        List<Integer> pool = new ArrayList<>();
        for (String w : store.getLearned()) {
            int i = Words.indexOf(w);
            if (i >= 0) pool.add(i);
        }
        if (pool.size() < 4) { // chưa học đủ -> dùng cả 60 từ
            pool.clear();
            for (int i = 0; i < Words.N; i++) pool.add(i);
        }
        return pool;
    }

    private List<Integer> wrongPool(Store store) {
        List<Integer> pool = new ArrayList<>();
        for (String w : store.getWrong()) {
            int i = Words.indexOf(w);
            if (i >= 0) pool.add(i);
        }
        return pool;
    }

    private void startQuiz(List<Integer> pool, Store store) {
        if (pool.isEmpty()) return;
        Collections.shuffle(pool);
        questions = new ArrayList<>(pool.subList(0, Math.min(5, pool.size())));
        qi = 0; score = 0; wrongThisRound.clear();
        quizBox.setVisibility(View.VISIBLE);
        resultBox.setVisibility(View.GONE);
        showQuestion();
    }

    private void showQuestion() {
        answered = false;
        int w = questions.get(qi);
        tvQProgress.setText("Câu " + (qi + 1) + "/" + questions.size());
        tvQWord.setText(Words.WORDS[w]);

        List<Integer> distract = new ArrayList<>();
        for (int i = 0; i < Words.N; i++) if (i != w) distract.add(i);
        Collections.shuffle(distract);
        List<Integer> options = new ArrayList<>();
        options.add(w);
        options.add(distract.get(0)); options.add(distract.get(1)); options.add(distract.get(2));
        Collections.shuffle(options);

        for (int i = 0; i < 4; i++) {
            int oi = options.get(i);
            opts[i].setText(Words.MEANINGS[oi]);
            opts[i].setBackgroundResource(R.drawable.btn_ghost);
            opts[i].setEnabled(true);
            if (oi == w) correctOpt = i;
        }
        btnNext.setVisibility(View.GONE);
    }

    private void answer(int idx) {
        if (answered) return;
        answered = true;
        Store store = new Store(getActivity());
        int w = questions.get(qi);
        for (Button b : opts) b.setEnabled(false);
        opts[correctOpt].setBackgroundResource(R.drawable.btn_primary);
        if (idx == correctOpt) {
            score++;
            store.removeWrong(Words.WORDS[w]); // trả lời đúng -> xóa khỏi list từ sai
        } else {
            opts[idx].setBackgroundColor(0xFFEF4444);
            store.addWrong(Words.WORDS[w]);
            wrongThisRound.add(w);
        }
        store.markStudied();
        btnNext.setVisibility(View.VISIBLE);
        btnNext.setText(qi + 1 >= questions.size() ? "Xem kết quả" : "Câu tiếp");
    }

    private void showResult(Store store) {
        store.incQuiz();
        WidgetUpdater.update(getActivity());
        quizBox.setVisibility(View.GONE);
        resultBox.setVisibility(View.VISIBLE);
        StringBuilder sb = new StringBuilder();
        sb.append("Điểm: ").append(score).append("/").append(questions.size());
        if (wrongThisRound.isEmpty()) {
            sb.append("\nHoàn hảo! Không sai câu nào \uD83C\uDF89");
        } else {
            sb.append("\nTừ cần ôn lại:\n");
            for (int w : wrongThisRound)
                sb.append("• ").append(Words.WORDS[w]).append(" — ").append(Words.MEANINGS[w]).append("\n");
        }
        tvQResult.setText(sb.toString().trim());
    }
}
