package com.learnwithdip.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
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
}
