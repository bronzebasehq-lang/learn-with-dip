package com.learnwithdip.app;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/** Nạp 7 ngày bài học từ assets/lessons.json (offline 100%). */
public class Lessons {

    public static class Term {
        public String word, ipa, pos, meaning, example, translation, audio;
        /** 3 câu ví dụ (v4.2): ngữ cảnh công việc / đời sống / tech + dịch từng câu. */
        public List<String> examples = new ArrayList<>();
        public List<String> exampleTranslations = new ArrayList<>();
    }

    public static class QuizQ {
        public String q;
        public String[] options;
        public int answer;
    }

    public static class Day {
        public int day;
        public String topic;
        public List<Term> terms = new ArrayList<>();
        public String reading, readingVi;
        public List<QuizQ> quiz = new ArrayList<>();
        /** Đoạn đọc hiểu thứ 2 (v4.2): ôn lại từ đã học + quiz riêng (2 câu). */
        public String reading2 = "", reading2Vi = "";
        public List<QuizQ> quiz2 = new ArrayList<>();
    }

    private static List<Day> cache;

    public static List<Day> all(Context c) {
        if (cache != null) return cache;
        cache = new ArrayList<>();
        try {
            InputStream is = c.getAssets().open("lessons.json");
            byte[] buf = new byte[is.available()];
            int n = 0, r;
            while ((r = is.read(buf, n, buf.length - n)) > 0) n += r;
            is.close();
            String json = new String(buf, 0, n, "UTF-8");
            JSONArray days = new JSONObject(json).getJSONArray("days");
            for (int i = 0; i < days.length(); i++) {
                JSONObject d = days.getJSONObject(i);
                Day day = new Day();
                day.day = d.getInt("day");
                day.topic = d.getString("topic");
                JSONArray terms = d.getJSONArray("terms");
                for (int j = 0; j < terms.length(); j++) {
                    JSONObject t = terms.getJSONObject(j);
                    Term term = new Term();
                    term.word = t.getString("word");
                    term.ipa = t.getString("ipa");
                    term.pos = t.getString("pos");
                    term.meaning = t.getString("meaning");
                    term.example = t.getString("example");
                    term.translation = t.getString("translation");
                    term.audio = t.getString("audio");
                    if (t.has("examples")) {
                        JSONArray exs = t.getJSONArray("examples");
                        for (int k = 0; k < exs.length(); k++) term.examples.add(exs.getString(k));
                    }
                    if (t.has("example_translations")) {
                        JSONArray trs = t.getJSONArray("example_translations");
                        for (int k = 0; k < trs.length(); k++) term.exampleTranslations.add(trs.getString(k));
                    }
                    day.terms.add(term);
                }
                day.reading = d.getString("reading");
                day.readingVi = d.getString("reading_vi");
                JSONArray quiz = d.getJSONArray("quiz");
                for (int j = 0; j < quiz.length(); j++) {
                    JSONObject q = quiz.getJSONObject(j);
                    QuizQ qq = new QuizQ();
                    qq.q = q.getString("q");
                    JSONArray opts = q.getJSONArray("options");
                    qq.options = new String[opts.length()];
                    for (int k = 0; k < opts.length(); k++) qq.options[k] = opts.getString(k);
                    qq.answer = q.getInt("answer");
                    day.quiz.add(qq);
                }
                if (d.has("reading2")) day.reading2 = d.getString("reading2");
                if (d.has("reading2_vi")) day.reading2Vi = d.getString("reading2_vi");
                if (d.has("quiz2")) {
                    JSONArray quiz2 = d.getJSONArray("quiz2");
                    for (int j = 0; j < quiz2.length(); j++) {
                        JSONObject q = quiz2.getJSONObject(j);
                        QuizQ qq = new QuizQ();
                        qq.q = q.getString("q");
                        JSONArray opts = q.getJSONArray("options");
                        qq.options = new String[opts.length()];
                        for (int k = 0; k < opts.length(); k++) qq.options[k] = opts.getString(k);
                        qq.answer = q.getInt("answer");
                        day.quiz2.add(qq);
                    }
                }
                cache.add(day);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cache;
    }

    /** Số ngày học hiện có (đọc từ JSON, không hardcode). */
    public static int dayCount(Context c) {
        return all(c).size();
    }

    /** Day theo chỉ số 1..N (tự xoay vòng). */
    public static Day get(Context c, int dayIndex1) {
        List<Day> days = all(c);
        if (days.isEmpty()) return null;
        int i = (dayIndex1 - 1) % days.size();
        if (i < 0) i = 0;
        return days.get(i);
    }

    public static int totalTerms(Context c) {
        int n = 0;
        for (Day d : all(c)) n += d.terms.size();
        return n;
    }

    /** Tìm term theo tên (để map với từ đã thuộc / từ sai). */
    public static Term find(Context c, String word) {
        for (Day d : all(c))
            for (Term t : d.terms)
                if (t.word.equals(word)) return t;
        return null;
    }
}
