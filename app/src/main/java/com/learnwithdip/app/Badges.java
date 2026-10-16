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
        "Thuộc 20+ từ",
        "Thuộc 45+ từ",
        "Thuộc 70+ từ",
        "Thuộc 95+ từ",
        "Thuộc hết 120 từ",
        "120 từ + streak 7+ ngày"
    };
    private static final int[] WORD_THRESH = {0, 20, 45, 70, 95, 120};

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
