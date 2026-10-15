# Learn With Dip

App Android tự học từ vựng tiếng Anh chuyên ngành **AI / Tech** mỗi ngày.
**100% local, offline** — mọi dữ liệu lưu trên máy, không cần mạng, không tài khoản, không quảng cáo.

Mascot **Dip**: ninja bot đen số 6 + mắt chibi tròn, đổi biểu cảm theo trạng thái học.

## Tính năng (v3)

- **Bài học mỗi ngày**: Day N + chủ đề, **10 thuật ngữ** — mỗi từ có từ + IPA + loại từ + nghĩa tiếng Việt + ví dụ tiếng Anh + dịch tiếng Việt.
- **Đoạn đọc hiểu** dùng đúng từ trong bài + bản dịch tiếng Việt.
- **Quiz trắc nghiệm** đọc hiểu theo bài học trong ngày (không XP, không coin).
- **Flashcard**: chạm để lật nghĩa, nút loa phát âm mọi từ — **70 file MP3 bundle offline** trong app.
- **Widget màn hình chính (v3)**: mascot TO (88dp) ôm bong bóng text; caption to rõ, đổi mỗi ngày, tối đa **15 từ**, không chi tiết từng ngày; nhấn widget mở thẳng bài học hôm nay. Mascot 4 biểu cảm: buồn ngủ (chưa học) / vui / mặt chính / ăn mừng (streak ≥ 7).
- **Streak** (chuỗi ngày học liên tiếp) + **huy hiệu** Newbie → Legend.
- **Onboarding** lần đầu + **Hồ sơ** cá nhân hóa.
- Nhắc học hàng ngày qua receiver (không server, không push từ xa).

## Nội dung

`app/src/main/assets/lessons.json` — **7 ngày × 10 thuật ngữ = 70 từ** AI/Tech, kèm đoạn đọc hiểu + quiz mỗi ngày.
`app/src/main/assets/audio/` — 70 file MP3 phát âm (1 file / từ).

## Cài đặt

### Cách 1 — Cài APK có sẵn

Tải file `build/learn-with-dip-v3.apk` (hoặc từ mục Releases), chép sang điện thoại Android 8.0+ (API 26+), mở file để cài (cho phép "cài đặt từ nguồn không xác định" nếu máy hỏi).

### Cách 2 — Build từ source

Không dùng Gradle (daemon hỏng trên VM build). Build thủ công bằng `build.sh`:

```bash
./build.sh   # -> build/learn-with-dip-v3.apk
```

Toolchain: `~/android-sdk` (build-tools 34.0.0, platform android-34), `aapt2` + `javac` + `d8` + `apksigner`. APK được ký bằng debug keystore tự tạo trong `build/` (chỉ để dev/test, không dùng cho Play Store).

## Widget (v3)

- Layout: `app/src/main/res/layout/widget_loop.xml` — RelativeLayout, mascot 88dp đè lên góc trái bong bóng text (hiệu ứng "ôm text").
- Logic: `app/src/main/java/com/learnwithdip/app/WidgetUpdater.java` — chọn mascot theo trạng thái, caption nudge ngắn xoay theo ngày trong năm, giới hạn cứng `capWords(..., 15)` (15 từ, không phải ký tự).
- Kích thước tối thiểu: `app/src/main/res/xml/widget_info.xml` — 250dp × 120dp.
- Trạng thái mascot: `mascot_sleepy` (chưa học) / `mascot_happy` (đã học, streak < 3) / `mascot` mặt chính (streak 3–6) / `mascot_party` (streak ≥ 7).

## Đổi tên app / mascot

- Tên app: 1 dòng trong `app/src/main/res/values/strings.xml` (`app_name`).
- Mascot: thay 4 file PNG trong `app/src/main/res/drawable/`: `mascot.png`, `mascot_happy.png`, `mascot_sleepy.png`, `mascot_party.png` (512×512, nền trong suốt).

## Cấu trúc source

```
app/src/main/
  AndroidManifest.xml
  java/com/learnwithdip/app/
    MainActivity.java        # bottom nav + mở bài học hôm nay từ widget
    OnboardingActivity.java  # onboarding lần đầu
    LearnFragment.java       # tab Học (flashcard + phát âm)
    QuizFragment.java        # tab Quiz
    ProfileFragment.java     # tab Hồ sơ + huy hiệu + ghim widget
    Store.java               # SharedPreferences: onboarding, từ đã thuộc/sai, streak
    Lessons.java             # nạp lessons.json (7 ngày)
    Badges.java              # logic huy hiệu Newbie → Legend
    AudioPlayer.java         # phát MP3 offline
    LoopWidgetProvider.java  # widget provider
    WidgetUpdater.java       # vẽ lại widget (mascot + caption + streak)
    DailyReceiver.java       # nhắc học hàng ngày
    BootReceiver.java        # khôi phục lịch nhắc sau khi khởi động lại
    UiKit.java               # component UI dùng chung
  res/
    layout/   # activity + fragment + widget_loop
    drawable/ # mascot PNG + bg/nút + widget_bubble
    values/   # strings, colors, styles
    xml/      # widget_info
  assets/
    lessons.json  # 7 ngày bài học
    audio/        # 70 MP3 phát âm
build.sh  # script build thủ công (aapt2 + javac + d8 + apksigner)
```

## Giấy phép

MIT — xem file `LICENSE`.
