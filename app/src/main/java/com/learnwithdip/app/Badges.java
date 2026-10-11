package com.learnwithdip.app;

/** Huy hiệu dev thay cho thanh tiến trình: theo số từ đã thuộc + streak. */
public class Badges {
    public static final String[] NAMES = {
        "Newbie", "Script", "Debugger", "Builder", "Architect", "Wizard", "Legend"
    };
    public static final String[] ICONS = {
        "\uD83C\uDF31", "\uD83D\uDCDC", "\uD83D\uDD0D", "\uD83D\uDD28", "\uD83C\uDFD7\uFE0F", "\uD83E\uDDD9", "\uD83D\uDC51"
    };
    public static final String[] DESCS = {
        "Bắt đầu hành trình",
        "Thuộc 10+ từ",
        "Thuộc 25+ từ",
        "Thuộc 40+ từ",
        "Thuộc 50+ từ",
        "Thuộc hết 60 từ",
        "60 từ + streak 7+ ngày"
    };
    private static final int[] WORD_THRESH = {0, 10, 25, 40, 50, 60};

    /** Trả về index huy hiệu hiện tại (0..6). */
    public static int index(int learnedCount, int streak) {
        int i = 0;
        for (int t = 0; t < WORD_THRESH.length; t++)
            if (learnedCount >= WORD_THRESH[t]) i = t;
        if (i >= 5 && streak >= 7) i = 6; // Wizard + streak 7 -> Legend
        return i;
    }

    /** Mô tả huy hiệu kế tiếp để user có mục tiêu. */
    public static String nextGoal(int learnedCount, int streak) {
        int i = index(learnedCount, streak);
        if (i >= 6) return "Đã max! Giữ streak mỗi ngày \uD83D\uDD25";
        if (i == 5) return "Streak 7 ngày để lên Legend (còn " + (7 - streak) + " ngày)";
        int need = WORD_THRESH[i + 1] - learnedCount;
        return "Còn " + need + " từ nữa lên " + NAMES[i + 1];
    }
}
