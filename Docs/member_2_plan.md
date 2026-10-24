# Kế hoạch chi tiết: THÀNH VIÊN 2 (Phân hệ Quản trị viên - Admin)

> **Dự án**: Quản lý giờ làm & lương nhân viên (Android - Kotlin, Room, MVVM, ViewBinding)  
> **Vai trò**: Phụ trách toàn bộ nghiệp vụ và giao diện của Quản trị viên (Admin) – Quản lý Nhân viên & Quản lý Bảng lương công ty.  
> **Nhánh Git**: `feature/tv2-admin-employee`, `feature/tv2-admin-salary`  
> **Tài liệu tham chiếu**: [project_plan.md](file:///d:/Code/Test2/Docs/project_plan.md) | [Project.md](file:///d:/Code/Test2/Docs/Project.md)

---

## 1. Mục tiêu & Phạm vi trách nhiệm

Thành viên 2 đảm nhiệm trụ cột quản lý kinh doanh của ứng dụng:
1. **Quản lý hồ sơ nhân viên (Employee CRUD)**: Danh sách, tìm kiếm, xem chi tiết, thêm mới (kèm tạo tài khoản đăng nhập), chỉnh sửa và xóa nhân viên (kèm xác nhận an toàn).
2. **Logic tính toán lương chuẩn xác (`SalaryCalculator`)**: Xử lý tách giờ hành chính và Overtime (1.5x), viết Unit Test để cam kết không có sai số.
3. **Quản lý bảng lương công ty (`SalaryManagement`)**: Tra cứu bảng lương theo tháng/năm, tính toán lại quỹ lương, cập nhật trạng thái thanh toán (`PENDING` -> `PAID`), khóa bản ghi lương khi đã thanh toán.
4. **Trải nghiệm người dùng (UX)**: Đảm bảo giao diện hiện đại, có Material Components (CardView, TextInputLayout, Chip, FAB), hỗ trợ trạng thái trống (Empty State) và thông báo lỗi rõ ràng.

---

## 2. File & Package phụ trách độc quyền

Để hạn chế tối đa merge conflict với TV1 và TV3, TV2 chỉ tập trung làm việc trên các file sau:

```
app/src/main/
├── java/com/team/payroll/
│   ├── data/repository/
│   │   ├── EmployeeRepository.kt      (Xử lý CRUD NV, search, validate trùng)
│   │   └── SalaryRepository.kt        (Upsert lương tháng, recalculate, markPaid)
│   ├── domain/
│   │   └── SalaryCalculator.kt        (Logic công thức tính tiền lương chuẩn & OT)
│   └── ui/admin/
│       ├── employee/
│       │   ├── EmployeeListFragment.kt
│       │   ├── EmployeeListViewModel.kt
│       │   ├── EmployeeAdapter.kt
│       │   ├── EmployeeFormFragment.kt (Thêm & Sửa NV)
│       │   ├── EmployeeFormViewModel.kt
│       │   └── EmployeeDetailFragment.kt
│       └── salary/
│           ├── SalaryManagementFragment.kt
│           ├── SalaryManagementViewModel.kt
│           ├── SalaryAdapter.kt
│           └── SalaryDetailDialog.kt
├── res/
│   ├── layout/
│   │   ├── fragment_employee_list.xml
│   │   ├── item_employee.xml
│   │   ├── fragment_employee_form.xml
│   │   ├── fragment_employee_detail.xml
│   │   ├── fragment_salary_management.xml
│   │   ├── item_salary.xml
│   │   └── dialog_salary_detail.xml
│   └── values/
│       └── strings_admin.xml           (Toàn bộ chuỗi ngôn ngữ của Admin)
└── test/java/com/team/payroll/
    └── domain/
        └── SalaryCalculatorTest.kt     (Unit Test thuật toán tính lương)
```

---

## 3. Lịch trình công việc 6 ngày (Sprint 6 Days)

### 📅 NGÀY 1: Dựng Layout XML & Khởi tạo `EmployeeRepository`
*Mục tiêu: Hoàn tất toàn bộ giao diện tĩnh phân hệ Nhân viên trước khi TV1 bàn giao CSDL.*

- **Buổi sáng (08:00 - 12:00)**:
  - Tạo nhánh `feature/tv2-admin-employee` từ nhánh `develop`.
  - Tạo file `res/values/strings_admin.xml` chứa các nhãn, thông báo lỗi cho admin.
  - Dựng layout `fragment_employee_list.xml`: `SearchView`, `RecyclerView`, `FloatingActionButton (FAB)`, View cảnh báo rỗng (Empty State: icon + chữ "Chưa có nhân viên nào").
  - Dựng layout `item_employee.xml`: Material CardView hiển thị Avatar chữ cái đầu, Mã NV, Họ tên, Email, Mức lương giờ (`hourlyRate`), nút menu hoặc icon xem chi tiết.
  - Dựng layout `fragment_employee_form.xml`: Các trường `TextInputLayout` (Mã NV, Tên đăng nhập, Mật khẩu, Họ tên, Email, SĐT, Lương theo giờ), nút Lưu & nút Hủy.
- **Buổi chiều (13:30 - 17:30)**:
  - Tiếp nhận DAO và Database từ TV1 (được merge vào `develop` lúc 12:00). Thực hiện `git merge develop`.
  - Viết `EmployeeRepository.kt`:
    - `getAllEmployees(): Flow<List<Employee>>`
    - `searchEmployees(query: String): Flow<List<Employee>>`
    - `getEmployeeById(id: Int): Flow<Employee?>`
    - `isCodeExists(code: String): Boolean`
    - `isUsernameExists(username: String): Boolean`
    - `insertEmployeeWithAccount(employee: Employee, account: Account): Long` (Bọc trong `@Transaction` để tạo Account và Employee đồng thời).
    - `updateEmployee(employee: Employee)`
    - `deleteEmployee(employee: Employee)` (Kích hoạt Room Cascade để xóa log và lương liên quan).

---

### 📅 NGÀY 2: Hoàn thiện CRUD Nhân viên (Danh sách & Form Thêm/Sửa)
*Mục tiêu: Đăng nhập Admin có thể xem được danh sách mẫu và tạo mới nhân viên thành công.*

- **Buổi sáng (08:00 - 12:00)**:
  - Cài đặt `EmployeeAdapter.kt` với `DiffUtil.ItemCallback` để render danh sách mượt mà.
  - Xây dựng `EmployeeListViewModel.kt`: Tải danh sách nhân viên qua `StateFlow`/`LiveData`, xử lý debounce tìm kiếm với `SearchView` (lọc theo Họ tên, Mã NV hoặc Email).
  - Kết nối `EmployeeListFragment.kt`: Gán Adapter, bắt sự kiện click item để mở màn hình chi tiết, click FAB mở form thêm mới.
- **Buổi chiều (13:30 - 17:30)**:
  - Xây dựng `EmployeeFormViewModel.kt` & `EmployeeFormFragment.kt`:
    - **Logic kiểm tra hợp lệ (Form Validation)**:
      - Mã NV: Không được để trống, kiểm tra không bị trùng trong CSDL.
      - Username & Mật khẩu: Chỉ nhập khi tạo mới, độ dài tối thiểu >= 6 ký tự, kiểm tra trùng username.
      - Họ tên: Bắt buộc, không để trống.
      - Email: Định dạng chuẩn RFC Regex `Patterns.EMAIL_ADDRESS`.
      - Số điện thoại: 10 chữ số, bắt đầu bằng `0`.
      - Lương theo giờ (`hourlyRate`): Phải là số dương `> 0`.
    - Hỗ trợ chế độ Sửa (Edit Mode): Tải lại thông tin NV cũ, ẩn trường đổi username/password (hoặc chỉ sửa thông tin cá nhân).
  - Kiểm thử tạo thử nhân viên mới, sau đó đăng xuất và thử đăng nhập bằng tài khoản nhân viên vừa tạo.

---

### 📅 NGÀY 3: Chi tiết/Xóa Nhân viên & Thuật toán Tính Lương (`SalaryCalculator`)
*Mục tiêu: Hoàn tất 100% phân hệ Quản lý NV + Viết Unit Test logic lương.*

- **Buổi sáng (08:00 - 12:00)**:
  - Xây dựng `EmployeeDetailFragment.kt`: Hiển thị toàn bộ thông tin chi tiết của nhân viên, mức lương giờ, ngày tham gia.
  - Thêm chức năng **Xóa nhân viên**:
    - Hiển thị `MaterialAlertDialogBuilder` cảnh báo: *"Hành động này sẽ xóa toàn bộ dữ liệu chấm công và bảng lương của nhân viên này. Bạn có chắc chắn muốn xóa?"*.
    - Khi xác nhận -> Gọi `employeeRepository.deleteEmployee()`.
    - Điều hướng trở lại danh sách với thông báo `Snackbar` thành công.
  - Tạo PR cho phân hệ Quản lý nhân viên (`feature/tv2-admin-employee` -> `develop`).
- **Buổi chiều (13:30 - 17:30)**:
  - Tạo nhánh mới: `feature/tv2-admin-salary`.
  - Cài đặt `SalaryCalculator.kt` trong package `domain/`:
    ```kotlin
    object SalaryCalculator {
        const val STANDARD_DAILY_HOURS = 8.0
        const val OT_MULTIPLIER = 1.5

        data class SalaryResult(
            val regularHours: Double,
            val overtimeHours: Double,
            val totalHours: Double,
            val regularSalary: Double,
            val overtimeSalary: Double,
            val totalSalary: Double
        )

        fun calculate(
            workLogs: List<WorkLog>,
            hourlyRate: Double
        ): SalaryResult { ... }
    }
    ```
  - Viết bộ **Unit Test** `SalaryCalculatorTest.kt` kiểm tra các trường hợp:
    1. Làm việc đúng 8h/ngày (0h OT).
    2. Làm việc 10.5h/ngày (8h chuẩn + 2.5h OT nhân hệ số 1.5).
    3. Tháng có nhiều ngày công với số giờ khác nhau.
    4. Không có ngày công nào (0h, lương = 0).
    5. Chỉ tính những ngày có trạng thái `COMPLETED`.
  - Chạy `gradlew test` đảm bảo 100% test case xanh lá (Passed).

---

### 📅 NGÀY 4: Xây dựng `SalaryRepository` & Màn hình Quản lý Lương Admin
*Mục tiêu: Admin có thể theo dõi bảng lương toàn bộ công ty theo tháng, bấm tính lại và chốt thanh toán.*

- **Buổi sáng (08:00 - 12:00)**:
  - Xây dựng `SalaryRepository.kt`:
    - `getSalariesByMonth(month: Int, year: Int): Flow<List<SalaryWithEmployee>>`
    - `upsertMonthlySalary(employeeId: Int, month: Int, year: Int)`: Lấy toàn bộ `WorkLog` COMPLETED trong tháng của NV đó, gọi `SalaryCalculator`, lưu/cập nhật vào bảng `Salary`.
    - `recalculateAllSalaries(month: Int, year: Int)`: Quét toàn bộ nhân viên trong công ty và tính lại lương tháng đó (chỉ áp dụng cho các bản ghi đang ở trạng thái `PENDING`).
    - `markAsPaid(salaryId: Int)`: Cập nhật `status = "PAID"`.
    - `getCompanyMonthlySummary(month: Int, year: Int)`: Tính tổng quỹ lương, tổng giờ làm, tổng giờ OT toàn công ty.
- **Buổi chiều (13:30 - 17:30)**:
  - Dựng layout `fragment_salary_management.xml`:
    - Thanh chọn Tháng / Năm (MonthYearPicker hoặc 2 Spinner).
    - Thẻ CardView Tổng quan quỹ lương: Tổng chi phí lương (VND), Tổng giờ công, Tổng giờ OT.
    - Nút bấm: "Tính lại toàn bộ" (Icon Refresh) và "Lọc".
    - `RecyclerView` danh sách lương từng nhân viên.
  - Dựng layout `item_salary.xml`: Tên nhân viên, Mã NV, Tổng giờ làm (thường + OT), Mức lương thực nhận, Chip trạng thái (`PENDING` màu cam, `PAID` màu xanh lá).
  - Cài đặt `SalaryManagementViewModel.kt` & kết nối với `SalaryManagementFragment.kt`.
  - Thêm chức năng bấm vào từng dòng để mở `SalaryDetailDialog.kt` xem chi tiết và bấm "Xác nhận đã thanh toán".

---

### 📅 NGÀY 5: Tích hợp chéo, Xử lý Edge Cases & Tinh chỉnh UX
*Mục tiêu: Hệ thống vận hành trơn tru, không có lỗi ngoại lệ hoặc giao diện đơ lag.*

- **Buổi sáng (08:00 - 12:00)**:
  - Phối hợp với TV3: Kiểm tra khi TV3 Check-out ở màn hình nhân viên, bảng lương tháng ở màn hình Admin của TV2 có cập nhật số giờ và số tiền chính xác không.
  - Xử lý các Edge Cases quan trọng:
    - Admin đổi `hourlyRate` của nhân viên -> Bấm nút "Tính lại toàn bộ" -> Tiền lương tháng `PENDING` phải nhảy theo mức lương mới.
    - Bản ghi lương đã chuyển sang `PAID` -> Bấm "Tính lại" không được ghi đè, vô hiệu hóa nút chuyển trạng thái để bảo vệ dữ liệu lịch sử.
    - Nhập ký tự đặc biệt, số điện thoại không hợp lệ vào form NV -> Hiển thị lỗi đỏ ngay dưới ô nhập liệu (Helper/Error text).
- **Buổi chiều (13:30 - 17:30)**:
  - Bổ sung hiệu ứng tải (`ProgressBar`) khi đang tính lại lương hoặc tải danh sách nhân viên đông.
  - Bổ sung Empty State minh họa khi công ty chưa có nhân viên hoặc tháng được chọn chưa có ai phát sinh bảng lương.
  - Định dạng hiển thị tiền tệ chuẩn Việt Nam Đồng: `15,000,000 ₫` qua `DecimalFormat` hoặc `NumberFormat`.
  - Code Freeze trước 17:00, tạo Pull Request cuối cùng vào `develop`.

---

### 📅 NGÀY 6: Kiểm thử tổng hợp, Build Release & Chuẩn bị Demo
*Mục tiêu: Đạt điểm tối đa về tốc độ và chất lượng tính năng Admin.*

- **Buổi sáng (08:00 - 12:00)**:
  - Kéo bản build APK release mới nhất từ TV1 về máy thật.
  - Chạy kịch bản kiểm thử Acceptance Criteria phân hệ Admin.
  - Chuẩn bị sẵn dữ liệu demo ấn tượng (3-5 nhân viên có ảnh hưởng lương khác nhau: nhân viên có OT, nhân viên chỉ làm giờ chuẩn, nhân viên mới tạo).
  - Sẵn sàng kịch bản demo cho Giảng viên:
    1. Đăng nhập quyền Admin.
    2. Thêm nhân viên mới NV004.
    3. Tìm kiếm và chỉnh sửa lương giờ.
    4. Vào màn hình Bảng lương, lọc tháng hiện tại, giải thích công thức OT 1.5x.
    5. Bấm "Đánh dấu đã thanh toán" để minh họa khóa dữ liệu lương.

---

## 4. Công thức & Quy tắc Nghiệp vụ bắt buộc (Business Rules)

### 4.1 Quy tắc tính OT và Lương
$$\text{Giờ chuẩn} = \min(\text{Tổng giờ}, 8.0)$$
$$\text{Giờ OT} = \max(\text{Tổng giờ} - 8.0, 0.0)$$
$$\text{Tổng lương} = (\text{Giờ chuẩn} \times \text{hourlyRate}) + (\text{Giờ OT} \times \text{hourlyRate} \times 1.5)$$

> [!IMPORTANT]
> - Chỉ tính lương dựa trên các bản ghi `WorkLog` có `status = "COMPLETED"`. Bỏ qua các bản ghi `IN_PROGRESS` hoặc `INCOMPLETE`.
> - Số giờ làm tròn 2 chữ số thập phân (`round(totalHours * 100.0) / 100.0`).

### 4.2 Tính toàn vẹn dữ liệu khi tạo & xóa Nhân viên
- **Khi tạo mới**: Cần tạo bản ghi `Account` trước để lấy `accountId`, sau đó tạo `Employee` tham chiếu đến `accountId` đó. Toàn bộ phải chạy trong 1 Room `@Transaction`.
- **Khi xóa**: Nhờ cấu hình `onDelete = ForeignKey.CASCADE`, xóa `Employee` sẽ tự động xóa sạch dữ liệu `Account`, `WorkLog`, `Salary` liên quan. TV2 cần viết Dialog xác nhận trước khi thực hiện hành động này.

---

## 5. Checklist nghiệm thu bàn giao (Definition of Done - TV2)

- [ ] **Màn hình Danh sách Nhân viên**:
  - [ ] Hiển thị danh sách đầy đủ, cuộn mượt mà với `RecyclerView`.
  - [ ] Tìm kiếm realtime theo họ tên, mã nhân viên, email hoạt động chính xác.
  - [ ] Hiển thị trạng thái rỗng (Empty State) khi không có kết quả tìm kiếm.
- [ ] **Màn hình Thêm / Sửa Nhân viên**:
  - [ ] Validate đầy đủ các trường: Họ tên, Email regex, SĐT 10 số, lương giờ > 0.
  - [ ] Báo lỗi ngay lập tức khi trùng Mã NV hoặc trùng Tên đăng nhập.
  - [ ] Tạo nhân viên thành công -> Đăng xuất và đăng nhập được bằng tài khoản nhân viên mới tạo.
- [ ] **Màn hình Chi tiết & Xóa Nhân viên**:
  - [ ] Hiển thị đầy đủ thông tin chi tiết.
  - [ ] Xóa nhân viên có dialog xác nhận; sau khi xóa không còn tồn tại trong hệ thống.
- [ ] **Thuật toán & Quản lý Lương**:
  - [ ] `SalaryCalculatorTest` vượt qua 100% test case (Unit test màu xanh).
  - [ ] Hiển thị đúng bảng lương theo từng Tháng / Năm đã chọn.
  - [ ] Nút "Tính lại toàn bộ" cập nhật chính xác số tiền cho các bản ghi `PENDING`.
  - [ ] Nút "Đánh dấu đã thanh toán" chuyển status sang `PAID` và khóa chỉnh sửa thành công.
  - [ ] Định dạng tiền tệ hiển thị đẹp mắt (VD: `8,500,000 đ`).
