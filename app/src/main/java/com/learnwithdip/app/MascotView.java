package com.learnwithdip.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Mascot Dip (v4.1, mockup 4-5-6): tái dùng đúng 4 file art mascot hiện có
 * (mascot / mascot_happy / mascot_sleepy / mascot_party), KHÔNG vẽ art mới.
 * 8 biểu cảm / hành động được thể hiện bằng animation + hiệu ứng phụ
 * (vệt chuyển động, bóng, dấu ?, !, Z z, sao, confetti) trên art đó.
 *
 * - create(): khối mascot hoàn chỉnh = đĩa glow tím + art + hiệu ứng + bong bóng text.
 * - update(): đổi biểu cảm/bong bóng của khối đã tạo (không cần build lại view).
 * - animate(): chạy animation biểu cảm lên 1 ImageView mascot có sẵn (vd onboarding).
 * - playRunAcross(): hiệu ứng chuyển cảnh mockup 6 — Dip chạy ngang màn hình,
 *   kéo nội dung mới vào; atMidpoint chạy đúng lúc Dip ở giữa màn hình.
 */
public class MascotView {

    public enum Mood {
        RUNNING,    // Đang chạy — vệt chuyển động + nghiêng người
        JUMPING,    // Đang nhảy — nảy lên xuống + bóng dưới chân
        CELEBRATING,// Ăn mừng — lắc + confetti (art party)
        THINKING,   // Đang suy nghĩ — nghiêng đầu + dấu ?
        SLEEPING,   // Đang ngủ — thở chậm + "Z z" (art sleepy)
        WAVING,     // Vẫy tay chào — lắc qua lại + 👋 (art happy)
        SURPRISED,  // Bất ngờ — phình to + dấu !
        CHEERING    // Cổ vũ — nhảy + lắc + sao ★ (art party)
    }

    // ---- Art có sẵn cho từng biểu cảm (không art mới) ----
    public static int drawableFor(Mood m) {
        switch (m) {
            case JUMPING:
            case WAVING: return R.drawable.mascot_happy;
            case CELEBRATING:
            case CHEERING: return R.drawable.mascot_party;
            case SLEEPING: return R.drawable.mascot_sleepy;
            default: return R.drawable.mascot; // RUNNING, THINKING, SURPRISED: mặt chính
        }
    }

    /** Ký hiệu hiệu ứng góc phải-trên (theo mockup 5). */
    public static String effectFor(Mood m) {
        switch (m) {
            case THINKING: return "?";
            case SLEEPING: return "Z z";
            case WAVING: return "\uD83D\uDC4B";
            case SURPRISED: return "!";
            case CELEBRATING: return "\u2726";
            case CHEERING: return "\u2605";
            default: return ""; // RUNNING: vệt chuyển động; JUMPING: bóng — không cần ký hiệu
        }
    }

    /**
     * Khối mascot hoàn chỉnh: đĩa glow tím + art + hiệu ứng + bong bóng text.
     * imageDp: kích thước art (trang Học = 168dp = 3x widget 56dp, mockup 4).
     * bubble: null/"" = không bong bóng.
     */
    public static View create(Context c, Mood mood, int imageDp, String bubble) {
        LinearLayout root = new LinearLayout(c);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.setTag("mascot_root");

        int frameSize = UiKit.dp(c, imageDp + 56);
        FrameLayout frame = new FrameLayout(c);
        LinearLayout.LayoutParams frameLp = new LinearLayout.LayoutParams(frameSize, frameSize);
        frameLp.gravity = Gravity.CENTER_HORIZONTAL;
        frame.setLayoutParams(frameLp);
        root.addView(frame);

        // Đĩa glow tím sau lưng (mockup 4)
        View glow = new View(c);
        glow.setBackgroundResource(R.drawable.mascot_glow);
        glow.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        frame.addView(glow);

        // Art mascot
        ImageView img = new ImageView(c);
        int imgPx = UiKit.dp(c, imageDp);
        FrameLayout.LayoutParams imgLp = new FrameLayout.LayoutParams(imgPx, imgPx);
        imgLp.gravity = Gravity.CENTER;
        img.setLayoutParams(imgLp);
        img.setImageResource(drawableFor(mood));
        img.setTag("mascot_img");
        frame.addView(img);

        // Vệt chuyển động (chỉ RUNNING): 3 vạch tím bên trái art
        LinearLayout dashes = new LinearLayout(c);
        dashes.setOrientation(LinearLayout.VERTICAL);
        dashes.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams dashLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dashLp.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
        dashLp.leftMargin = UiKit.dp(c, 2);
        dashes.setLayoutParams(dashLp);
        dashes.setTag("mascot_dashes");
        int[] dashW = {28, 20, 14};
        for (int w : dashW) {
            View d = new View(c);
            LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                    UiKit.dp(c, w), UiKit.dp(c, 4));
            dlp.topMargin = UiKit.dp(c, 4);
            dlp.bottomMargin = UiKit.dp(c, 4);
            d.setLayoutParams(dlp);
            d.setBackgroundColor(0xFF8B5CF6);
            dashes.addView(d);
        }
        frame.addView(dashes);

        // Bóng dưới chân (chỉ JUMPING)
        View shadow = new View(c);
        GradientDrawable shadowBg = new GradientDrawable();
        shadowBg.setShape(GradientDrawable.OVAL);
        shadowBg.setColor(0x55000000);
        shadow.setBackground(shadowBg);
        FrameLayout.LayoutParams shLp = new FrameLayout.LayoutParams(
                UiKit.dp(c, (int) (imageDp * 0.6)), UiKit.dp(c, 10));
        shLp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        shLp.bottomMargin = UiKit.dp(c, 6);
        shadow.setLayoutParams(shLp);
        shadow.setTag("mascot_shadow");
        frame.addView(shadow);

        // Confetti (chỉ CELEBRATING / CHEERING): chấm màu quanh art, đặt trực tiếp lên frame
        int[] confettiColors = {0xFFF59E0B, 0xFFEC4899, 0xFF22C55E, 0xFF8B5CF6, 0xFFF59E0B};
        int[][] confettiPos = {{8, 6}, {86, 10}, {4, 78}, {88, 74}, {50, 2}};
        for (int i = 0; i < confettiColors.length; i++) {
            View dot = new View(c);
            GradientDrawable dotBg = new GradientDrawable();
            dotBg.setShape(GradientDrawable.OVAL);
            dotBg.setColor(confettiColors[i]);
            dot.setBackground(dotBg);
            FrameLayout.LayoutParams dotLp = new FrameLayout.LayoutParams(
                    UiKit.dp(c, 9), UiKit.dp(c, 9));
            dotLp.gravity = Gravity.TOP | Gravity.START;
            dotLp.leftMargin = UiKit.dp(c, confettiPos[i][0] * (imageDp + 56) / 100);
            dotLp.topMargin = UiKit.dp(c, confettiPos[i][1] * (imageDp + 56) / 100);
            dot.setLayoutParams(dotLp);
            frame.addView(dot);
            dot.setTag("mascot_dot_" + i);
        }
        // (confetti là các dot riêng lẻ, không gom layout — để frame quản lý vị trí)
        // Ký hiệu hiệu ứng góc phải-trên (?, !, Z z, 👋, ✦, ★)
        TextView effect = new TextView(c);
        effect.setText(effectFor(mood));
        effect.setTextSize(26);
        effect.setTypeface(effect.getTypeface(), android.graphics.Typeface.BOLD);
        effect.setTextColor(0xFFF59E0B);
        FrameLayout.LayoutParams effLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        effLp.gravity = Gravity.TOP | Gravity.END;
        effLp.topMargin = UiKit.dp(c, 4);
        effLp.rightMargin = UiKit.dp(c, 10);
        effect.setLayoutParams(effLp);
        effect.setTag("mascot_effect");
        frame.addView(effect);

        // Bong bóng text dưới mascot
        TextView bubbleTv = new TextView(c);
        bubbleTv.setText(bubble == null ? "" : bubble);
        bubbleTv.setTextSize(15);
        bubbleTv.setTextColor(0xFFFFFFFF);
        bubbleTv.setGravity(Gravity.CENTER);
        bubbleTv.setBackgroundResource(R.drawable.widget_bubble);
        int bPadH = UiKit.dp(c, 16);
        int bPadV = UiKit.dp(c, 8);
        bubbleTv.setPadding(bPadH, bPadV, bPadH, bPadV);
        LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bLp.topMargin = UiKit.dp(c, 8);
        bLp.bottomMargin = UiKit.dp(c, 4);
        bubbleTv.setLayoutParams(bLp);
        bubbleTv.setTag("mascot_bubble");
        root.addView(bubbleTv);

        applyMood(root, mood);
        return root;
    }

    /** Đổi biểu cảm/bong bóng của khối mascot đã tạo bằng create(). */
    public static void update(View root, Mood mood, String bubble) {
        if (root == null) return;
        if (bubble != null) {
            TextView bubbleTv = (TextView) root.findViewWithTag("mascot_bubble");
            if (bubbleTv != null) bubbleTv.setText(bubble);
        }
        applyMood(root, mood);
    }

    private static void applyMood(View root, Mood mood) {
        ImageView img = (ImageView) root.findViewWithTag("mascot_img");
        if (img != null) {
            img.setImageResource(drawableFor(mood));
            animate(img, mood);
        }
        TextView effect = (TextView) root.findViewWithTag("mascot_effect");
        if (effect != null) effect.setText(effectFor(mood));
        View dashes = root.findViewWithTag("mascot_dashes");
        if (dashes != null) dashes.setVisibility(mood == Mood.RUNNING ? View.VISIBLE : View.GONE);
        View shadow = root.findViewWithTag("mascot_shadow");
        if (shadow != null) shadow.setVisibility(mood == Mood.JUMPING ? View.VISIBLE : View.GONE);
        boolean confetti = mood == Mood.CELEBRATING || mood == Mood.CHEERING;
        for (int i = 0; i < 5; i++) {
            View dot = root.findViewWithTag("mascot_dot_" + i);
            if (dot != null) dot.setVisibility(confetti ? View.VISIBLE : View.GONE);
        }
    }

    /** Chạy animation biểu cảm lên 1 ImageView mascot có sẵn (vd onboarding). */
    public static void animate(View v, Mood mood) {
        if (v == null) return;
        v.clearAnimation();
        Context c = v.getContext();
        Animation anim;
        switch (mood) {
            case RUNNING: {
                AnimationSet set = new AnimationSet(true);
                RotateAnimation rot = new RotateAnimation(-7, 7,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rot.setDuration(220);
                rot.setRepeatCount(Animation.INFINITE);
                rot.setRepeatMode(Animation.REVERSE);
                TranslateAnimation tr = new TranslateAnimation(0, 0,
                        0, UiKit.dp(c, 3));
                tr.setDuration(220);
                tr.setRepeatCount(Animation.INFINITE);
                tr.setRepeatMode(Animation.REVERSE);
                set.addAnimation(rot);
                set.addAnimation(tr);
                set.setRepeatCount(Animation.INFINITE);
                set.setRepeatMode(Animation.REVERSE);
                anim = set;
                break;
            }
            case JUMPING: {
                TranslateAnimation tr = new TranslateAnimation(0, 0,
                        0, -UiKit.dp(c, 26));
                tr.setDuration(650);
                tr.setRepeatCount(Animation.INFINITE);
                tr.setRepeatMode(Animation.REVERSE);
                anim = tr;
                break;
            }
            case CELEBRATING: {
                AnimationSet set = new AnimationSet(true);
                RotateAnimation rot = new RotateAnimation(-10, 10,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rot.setDuration(280);
                rot.setRepeatCount(Animation.INFINITE);
                rot.setRepeatMode(Animation.REVERSE);
                ScaleAnimation sc = new ScaleAnimation(1f, 1.07f, 1f, 1.07f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                sc.setDuration(280);
                sc.setRepeatCount(Animation.INFINITE);
                sc.setRepeatMode(Animation.REVERSE);
                set.addAnimation(rot);
                set.addAnimation(sc);
                set.setRepeatCount(Animation.INFINITE);
                set.setRepeatMode(Animation.REVERSE);
                anim = set;
                break;
            }
            case THINKING: {
                RotateAnimation rot = new RotateAnimation(-6, 6,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rot.setDuration(1100);
                rot.setRepeatCount(Animation.INFINITE);
                rot.setRepeatMode(Animation.REVERSE);
                anim = rot;
                break;
            }
            case SLEEPING: {
                ScaleAnimation sc = new ScaleAnimation(1f, 1.05f, 1f, 1.05f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                sc.setDuration(1600);
                sc.setRepeatCount(Animation.INFINITE);
                sc.setRepeatMode(Animation.REVERSE);
                anim = sc;
                break;
            }
            case WAVING: {
                RotateAnimation rot = new RotateAnimation(-14, 14,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.15f);
                rot.setDuration(320);
                rot.setRepeatCount(Animation.INFINITE);
                rot.setRepeatMode(Animation.REVERSE);
                anim = rot;
                break;
            }
            case SURPRISED: {
                ScaleAnimation sc = new ScaleAnimation(1f, 1.14f, 1f, 1.14f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                sc.setDuration(700);
                sc.setRepeatCount(Animation.INFINITE);
                sc.setRepeatMode(Animation.REVERSE);
                anim = sc;
                break;
            }
            case CHEERING:
            default: {
                AnimationSet set = new AnimationSet(true);
                TranslateAnimation tr = new TranslateAnimation(0, 0,
                        0, -UiKit.dp(c, 18));
                tr.setDuration(500);
                tr.setRepeatCount(Animation.INFINITE);
                tr.setRepeatMode(Animation.REVERSE);
                RotateAnimation rot = new RotateAnimation(-8, 8,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rot.setDuration(300);
                rot.setRepeatCount(Animation.INFINITE);
                rot.setRepeatMode(Animation.REVERSE);
                set.addAnimation(tr);
                set.addAnimation(rot);
                set.setRepeatCount(Animation.INFINITE);
                set.setRepeatMode(Animation.REVERSE);
                anim = set;
                break;
            }
        }
        v.startAnimation(anim);
    }

    /**
     * Hiệu ứng chuyển cảnh (mockup 6): Dip chạy từ trái sang phải màn hình,
     * kéo theo 3 vệt chuyển động; atMidpoint chạy đúng lúc Dip ở giữa màn hình
     * (caller đổi nội dung ở đó để trông như Dip kéo nội dung mới vào).
     */
    public static void playRunAcross(Activity a, Runnable atMidpoint) {
        if (a == null || a.isFinishing()) {
            if (atMidpoint != null) atMidpoint.run();
            return;
        }
        Context c = a;
        ViewGroup decor = (ViewGroup) a.getWindow().getDecorView();

        LinearLayout container = new LinearLayout(c);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        int[] dashW = {28, 20, 14};
        for (int w : dashW) {
            View d = new View(c);
            LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                    UiKit.dp(c, w), UiKit.dp(c, 4));
            dlp.rightMargin = UiKit.dp(c, 4);
            d.setLayoutParams(dlp);
            d.setBackgroundColor(0xFF8B5CF6);
            container.addView(d);
        }
        ImageView img = new ImageView(c);
        int imgPx = UiKit.dp(c, 76);
        img.setLayoutParams(new LinearLayout.LayoutParams(imgPx, imgPx));
        img.setImageResource(R.drawable.mascot);
        container.addView(img);
        animate(img, Mood.RUNNING);

        int screenH = c.getResources().getDisplayMetrics().heightPixels;
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.topMargin = (int) (screenH * 0.62);
        decor.addView(container, lp);

        container.post(() -> {
            int w = container.getWidth();
            int screenW = c.getResources().getDisplayMetrics().widthPixels;
            ObjectAnimator anim = ObjectAnimator.ofFloat(
                    container, "translationX", -w - 20, screenW + 20);
            anim.setDuration(650);
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    decor.removeView(container);
                }
            });
            anim.start();
        });
        // Đổi nội dung ở giữa hành trình (~300ms) = Dip "kéo" nội dung mới vào.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (atMidpoint != null) atMidpoint.run();
        }, 300);
    }
}
