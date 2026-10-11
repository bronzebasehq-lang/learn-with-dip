package com.learnwithdip.app;

import android.app.AlertDialog;
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

/** Tab Học: card từ vựng, chạm để lật nghĩa, Đã thuộc / Chưa thuộc. */
public class LearnFragment extends Fragment {
    private List<Integer> queue = new ArrayList<>();
    private int pos = 0;
    private boolean revealed = false;
    private TextView tvProgress, tvWord, tvMeaning, tvExample, tvHint;
    private LinearLayout card;

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup parent, Bundle s) {
        View v = inf.inflate(R.layout.fragment_learn, parent, false);
        Store store = new Store(getActivity());

        Set<String> learned = store.getLearned();
        List<Integer> rest = new ArrayList<>();
        for (int i = 0; i < Words.N; i++)
            if (!learned.contains(Words.WORDS[i])) rest.add(i);
        Collections.shuffle(rest);
        int size = Math.min(rest.size(), store.getMinutes());
        queue = rest.subList(0, size);

        tvProgress = v.findViewById(R.id.tv_progress);
        tvWord = v.findViewById(R.id.tv_word);
        tvMeaning = v.findViewById(R.id.tv_meaning);
        tvExample = v.findViewById(R.id.tv_example);
        tvHint = v.findViewById(R.id.tv_hint);
        card = v.findViewById(R.id.card);
        Button bKnown = v.findViewById(R.id.btn_known);
        Button bUnknown = v.findViewById(R.id.btn_unknown);
        TextView tvDone = v.findViewById(R.id.tv_alldone);

        if (queue.isEmpty()) {
            card.setVisibility(View.GONE);
            bKnown.setVisibility(View.GONE);
            bUnknown.setVisibility(View.GONE);
            tvProgress.setVisibility(View.GONE);
            tvDone.setVisibility(View.VISIBLE);
            tvDone.setText("Bạn đã thuộc hết " + Words.N + " từ! \uD83C\uDF89\nQua tab Ôn tập để giữ phong độ nhé.");
            return v;
        }

        card.setOnClickListener(x -> reveal());
        bKnown.setOnClickListener(x -> {
            store.addLearned(Words.WORDS[queue.get(pos)]);
            store.markStudied();
            next();
        });
        bUnknown.setOnClickListener(x -> next());

        showCard();
        return v;
    }

    private void showCard() {
        revealed = false;
        int i = queue.get(pos);
        tvProgress.setText((pos + 1) + "/" + queue.size());
        tvWord.setText(Words.WORDS[i]);
        tvMeaning.setText(Words.MEANINGS[i]);
        tvExample.setText("\"" + Words.EXAMPLES[i] + "\"");
        tvMeaning.setVisibility(View.GONE);
        tvExample.setVisibility(View.GONE);
        tvHint.setVisibility(View.VISIBLE);
    }

    private void reveal() {
        if (revealed) return;
        revealed = true;
        tvMeaning.setVisibility(View.VISIBLE);
        tvExample.setVisibility(View.VISIBLE);
        tvHint.setVisibility(View.GONE);
    }

    private void next() {
        pos++;
        if (pos >= queue.size()) done();
        else showCard();
    }

    private void done() {
        WidgetUpdater.update(getActivity());
        new AlertDialog.Builder(getActivity())
                .setTitle("Xong buổi học! \uD83C\uDF89")
                .setMessage("Bạn đã học " + queue.size() + " từ. Làm quiz 5 câu kiểm tra lại nhé?")
                .setPositiveButton("Làm quiz", (d, w) -> ((MainActivity) getActivity()).goQuiz())
                .setNegativeButton("Để sau", null)
                .show();
    }
}
