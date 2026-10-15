package com.learnwithdip.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Mọi dữ liệu local của app: onboarding, từ đã thuộc, từ sai, streak. */
public class Store {
    private static final String P = "loop_prefs";
    private final SharedPreferences sp;

    public Store(Context c) {
        sp = c.getSharedPreferences(P, Context.MODE_PRIVATE);
    }

    // ---- Onboarding ----
    public boolean isOnboarded() { return sp.getBoolean("onboarded", false); }

    public void saveOnboarding(String name, int age, String goal, int minutes) {
        sp.edit().putBoolean("onboarded", true)
                .putString("name", name)
                .putInt("age", age)
                .putString("goal", goal)
                .putInt("minutes", minutes)
                .putString("onboardDate", LocalDate.now().toString())
                .apply();
    }

    /** Ngày học hiện tại (1..7, tự xoay vòng sau 7 ngày). */
    public int getDayIndex() {
        String start = sp.getString("onboardDate", "");
        if (start.isEmpty()) {
            start = LocalDate.now().toString();
            sp.edit().putString("onboardDate", start).apply();
        }
        long diff;
        try {
            diff = ChronoUnit.DAYS.between(LocalDate.parse(start), LocalDate.now());
        } catch (Exception e) {
            diff = 0;
        }
        if (diff < 0) diff = 0;
        return (int) (diff % 7) + 1;
    }

    public String getName() { return sp.getString("name", "bạn"); }
    public int getMinutes() { return sp.getInt("minutes", 10); }

    public void resetAll() { sp.edit().clear().apply(); }

    // ---- Từ đã thuộc ----
    public Set<String> getLearned() {
        return new HashSet<>(sp.getStringSet("learned", new HashSet<String>()));
    }

    public void addLearned(String w) {
        Set<String> s = getLearned();
        if (s.add(w)) sp.edit().putStringSet("learned", s).apply();
    }

    public int learnedCount() { return getLearned().size(); }

    // ---- Từ trả lời sai (để ôn lại) ----
    public Set<String> getWrong() {
        return new HashSet<>(sp.getStringSet("wrong", new HashSet<String>()));
    }

    public void addWrong(String w) {
        Set<String> s = getWrong();
        if (s.add(w)) sp.edit().putStringSet("wrong", s).apply();
    }

    public void removeWrong(String w) {
        Set<String> s = getWrong();
        if (s.remove(w)) sp.edit().putStringSet("wrong", s).apply();
    }

    // ---- Streak ----
    public int getStreak() { return sp.getInt("streak", 0); }

    public boolean studiedToday() {
        return sp.getString("lastDate", "").equals(LocalDate.now().toString());
    }

    /** Gọi mỗi khi user học/quiz. Tự tính streak theo ngày. */
    public void markStudied() {
        String today = LocalDate.now().toString();
        String last = sp.getString("lastDate", "");
        if (last.equals(today)) return;
        int st = last.equals(LocalDate.now().minusDays(1).toString())
                ? sp.getInt("streak", 0) + 1 : 1;
        sp.edit().putInt("streak", st).putString("lastDate", today).apply();
    }

    // ---- Quiz ----
    public int getQuizCount() { return sp.getInt("quiz", 0); }
    public void incQuiz() { sp.edit().putInt("quiz", getQuizCount() + 1).apply(); }

    // ---- Ôn tập ngắt quãng (spaced repetition, Minh chốt 15/10) ----
    // Mỗi từ đã học vào hàng đợi ôn: hiện lại sau 1 / 3 / 7 ngày.
    // Lưu trong StringSet "sr", mỗi entry "word|stage|nextDate" (nextDate ISO yyyy-MM-dd).
    // stage 0 -> ôn sau 1 ngày; nhớ rồi -> stage 1 -> ôn sau 3 ngày;
    // nhớ rồi -> stage 2 -> ôn sau 7 ngày; nhớ rồi -> thuộc chắc, rời hàng đợi.
    // Quên -> về stage 0, ôn lại ngày mai.
    private static final int[] SR_INTERVALS = {1, 3, 7};

    private Set<String> getSrRaw() {
        return new HashSet<>(sp.getStringSet("sr", new HashSet<String>()));
    }

    private void putSrRaw(Set<String> s) {
        sp.edit().putStringSet("sr", s).apply();
    }

    /** Lên lịch ôn cho từ mới học: stage 0, ôn lại ngày mai. */
    public void scheduleNew(String word) {
        Set<String> s = getSrRaw();
        for (String e : s) if (e.startsWith(word + "|")) return;
        s.add(word + "|0|" + LocalDate.now().plusDays(1));
        putSrRaw(s);
    }

    /** Ghi kết quả ôn 1 từ: remembered=true -> lên stage; false -> về stage 0. */
    public void reviewWord(String word, boolean remembered) {
        Set<String> s = getSrRaw();
        String found = null;
        for (String e : s) if (e.startsWith(word + "|")) { found = e; break; }
        if (found == null) return;
        s.remove(found);
        if (remembered) {
            int stage = Integer.parseInt(found.split("\\|")[1]) + 1;
            if (stage < SR_INTERVALS.length) {
                s.add(word + "|" + stage + "|"
                        + LocalDate.now().plusDays(SR_INTERVALS[stage]));
            }
            // stage >= 3: thuộc chắc -> rời hàng đợi ôn.
        } else {
            s.add(word + "|0|" + LocalDate.now().plusDays(1));
        }
        putSrRaw(s);
    }

    /** Từ đến hạn ôn hôm nay (nextDate <= hôm nay), xếp theo thứ tự từ điển. */
    public List<String> getDueReviews() {
        migrateSrOnce();
        List<String> due = new ArrayList<>();
        for (String e : getSrRaw()) {
            String[] p = e.split("\\|");
            if (p.length < 3) continue;
            try {
                if (!LocalDate.parse(p[2]).isAfter(LocalDate.now())) due.add(p[0]);
            } catch (Exception ignored) { }
        }
        Collections.sort(due);
        return due;
    }

    /** Migrate 1 lần: từ đã thuộc trước bản SR cũng vào hàng đợi (ôn ngày mai). */
    private void migrateSrOnce() {
        if (sp.getBoolean("sr_migrated", false)) return;
        Set<String> s = getSrRaw();
        for (String w : getLearned()) {
            boolean has = false;
            for (String e : s) if (e.startsWith(w + "|")) { has = true; break; }
            if (!has) s.add(w + "|0|" + LocalDate.now().plusDays(1));
        }
        sp.edit().putStringSet("sr", s).putBoolean("sr_migrated", true).apply();
    }
}
