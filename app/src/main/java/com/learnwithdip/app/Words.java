package com.learnwithdip.app;

/** 60 từ tiếng Anh chuyên ngành AI/Tech/Code: từ + nghĩa Việt + ví dụ Anh. */
public class Words {
    public static final int N = 60;

    public static final String[] WORDS = {
        "deploy","repository","commit","merge","bug",
        "debug","framework","library","API","endpoint",
        "latency","cache","refactor","legacy","async",
        "callback","promise","thread","container","image",
        "branch","pull request","rebase","stash","webhook",
        "middleware","migration","schema","query","index",
        "token","encryption","hash","rate limit","debounce",
        "recursion","closure","scope","inheritance","polymorphism",
        "encapsulation","abstraction","interface","generic","lambda",
        "tensor","embedding","inference","overfitting","prompt",
        "agent","pipeline","artifact","rollback","hotfix",
        "deprecate","changelog","boilerplate","snippet","sandbox"
    };

    public static final String[] MEANINGS = {
        "triển khai","kho mã nguồn","lưu thay đổi","hợp nhất","lỗi phần mềm",
        "gỡ lỗi","khung phát triển","thư viện","giao diện lập trình","điểm cuối API",
        "độ trễ","bộ nhớ đệm","tái cấu trúc code","cũ, lỗi thời","bất đồng bộ",
        "hàm gọi lại","đối tượng bất đồng bộ","luồng","bộ chứa (container)","ảnh container",
        "nhánh","yêu cầu hợp nhất","dựng lại nhánh","cất tạm thay đổi","móc web",
        "phần mềm trung gian","di trú dữ liệu","lược đồ dữ liệu","truy vấn","chỉ mục",
        "mã thông báo","mã hóa","băm","giới hạn tốc độ","chống dồn",
        "đệ quy","bao đóng","phạm vi","kế thừa","đa hình",
        "đóng gói","trừu tượng hóa","giao diện","kiểu tổng quát","hàm ẩn danh",
        "ten-xơ","véc-tơ nhúng","suy luận (chạy model)","quá khớp","câu lệnh (cho AI)",
        "tác tử AI","đường ống xử lý","sản phẩm build","quay lui","bản vá nóng",
        "ngừng hỗ trợ","nhật ký thay đổi","code mẫu lặp lại","đoạn code ngắn","hộp cát (môi trường thử)"
    };

    public static final String[] EXAMPLES = {
        "We deploy the new version every Friday.",
        "Clone the repository to your local machine.",
        "Commit your changes before leaving.",
        "Merge the feature branch into main.",
        "QA found a critical bug in login.",
        "I spent an hour to debug this crash.",
        "React is a popular frontend framework.",
        "This library handles date formatting.",
        "The API returns JSON data.",
        "The /users endpoint requires auth.",
        "High latency ruins video calls.",
        "Clear the cache if the page looks broken.",
        "We should refactor this messy function.",
        "The legacy system still runs on Java 6.",
        "Use async calls to avoid blocking.",
        "Pass a callback to handle the result.",
        "The promise resolves with user data.",
        "Each request runs on its own thread.",
        "The app runs inside a Docker container.",
        "Pull the latest image from the registry.",
        "Create a new branch for this feature.",
        "Open a pull request when ready.",
        "Rebase your branch onto main.",
        "Stash your changes before switching branches.",
        "The webhook notifies us on every push.",
        "Auth middleware checks the token.",
        "Run the migration to update the schema.",
        "The schema defines three tables.",
        "This query fetches active users.",
        "Add an index to speed up search.",
        "Store the token securely.",
        "Encryption protects data in transit.",
        "Passwords are stored as a hash.",
        "The API has a strict rate limit.",
        "Debounce the search input by 300ms.",
        "Recursion solves the tree traversal.",
        "The closure captures the outer variable.",
        "This variable is out of scope here.",
        "Inheritance reuses parent behavior.",
        "Polymorphism lets objects share an interface.",
        "Encapsulation hides internal details.",
        "Good abstraction simplifies complexity.",
        "Implement the Storage interface.",
        "Use a generic list for any type.",
        "Sort with a short lambda.",
        "The model takes a tensor as input.",
        "Embeddings turn words into vectors.",
        "Inference runs on the GPU.",
        "Overfitting happens with too little data.",
        "Write a clear prompt for the model.",
        "The agent books flights by itself.",
        "The CI pipeline runs tests automatically.",
        "Download the build artifact here.",
        "Rollback to the previous release.",
        "Ship a hotfix for the crash.",
        "This API will be deprecated soon.",
        "Check the changelog for breaking changes.",
        "The template removes boilerplate.",
        "Save this snippet for later.",
        "Test the code in a sandbox first."
    };

    public static int indexOf(String word) {
        for (int i = 0; i < N; i++) if (WORDS[i].equals(word)) return i;
        return -1;
    }
}
