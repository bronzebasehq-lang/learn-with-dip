# Loop (tên làm việc)

App Android học từ vựng tiếng Anh chuyên ngành AI/Tech/Code mỗi ngày.
Local-first: mọi dữ liệu lưu trên máy, không cần mạng, không tài khoản.

## Tính năng (v2)
- Onboarding 4 bước (tên, tuổi, mục tiêu học, số phút/ngày) — chỉ hiện lần đầu
- Tab Học: card từ vựng (chạm để lật nghĩa), Đã thuộc / Chưa thuộc, tiến trình x/y
- Tab Ôn tập: quiz trắc nghiệm 4 đáp án từ pool đã học, lưu từ sai để ôn lại
- Tab Hồ sơ: chào cá nhân hóa, huy hiệu dev (Newbie → Legend), streak, nút ghim widget
- Widget màn hình chính: mascot đổi trạng thái (vui / buồn ngủ / ăn mừng) + streak 🔥 + câu nudge cá nhân hóa
- 60 từ AI/Tech/Code (từ + nghĩa Việt + ví dụ Anh)

## Build
Không dùng Gradle (daemon hỏng trên VM build). Build thủ công:

```bash
./build.sh   # -> build/loop-debug.apk
```

Toolchain: `~/android-sdk` (build-tools 34.0.0, platform android-34), aapt2 + javac + d8 + apksigner.

## Đổi tên app / mascot
- Tên app: 1 dòng trong `app/src/main/res/values/strings.xml` (`app_name`)
- Mascot: thay 3 file trong `app/src/main/res/drawable/`:
  `mascot_happy.xml`, `mascot_sleepy.xml`, `mascot_party.xml` (hiện là placeholder vector)

## Cấu trúc source
```
app/src/main/
  AndroidManifest.xml
  java/com/dailyloop/app/
    MainActivity.java        # bottom nav 3 tab
    OnboardingActivity.java  # 4 bước onboarding
    LearnFragment.java       # tab Học
    QuizFragment.java        # tab Ôn tập
    ProfileFragment.java     # tab Hồ sơ + ghim widget
    Store.java               # SharedPreferences: onboarding, từ đã thuộc/sai, streak
    Words.java               # 60 từ vựng
    Badges.java              # logic huy hiệu
    LoopWidgetProvider.java  # widget
    WidgetUpdater.java       # vẽ lại widget
  res/
    layout/   # 6 layout
    drawable/ # mascot placeholder + bg/nút
    values/   # strings, colors, styles
    xml/      # widget_info
```
