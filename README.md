# MediCore HMS — Hospital Management System

Bộ ứng dụng quản lý bệnh viện mẫu dùng HTML5, Tailwind CSS qua CDN, JavaScript thuần và Supabase JS v2. Có dữ liệu mẫu để chạy ngay trong trình duyệt; khi cấu hình Supabase, xác thực và phân quyền dựa trên PostgreSQL RLS.

## 1. Cấu trúc

```text
hms-web/
├── chon-vai-tro.html          # Màn hình vào hệ thống / chọn vai trò ở chế độ demo
├── index.html                 # Tổng quan và không gian quản trị của quản lý
├── dang-ky-kham.html          # Y tá: tiếp đón / đăng ký khám
├── thu-tien.html              # Thu ngân: hóa đơn / thanh toán
├── kham-benh.html             # Bác sĩ: khám / chẩn đoán / chỉ định
├── xet-nghiem.html            # Y tá: xử lý xét nghiệm
├── cap-phat.html              # Dược sĩ: thuốc / đơn / cấp phát
├── benh-an.html               # Bác sĩ và y tá: hồ sơ bệnh án
├── thong-ke.html              # Quản lý: thống kê / xuất CSV
├── css/style.css              # Giao diện responsive và màn hình chọn vai trò
├── js/roles.js                # RBAC: vai trò, menu, đường dẫn, quyền truy cập giao diện
├── js/mock-data.js            # Dữ liệu mẫu
├── js/api.js                  # CRUD localStorage hoặc Supabase; chuẩn hóa sdt TEXT[]
├── js/ui.js                   # Layout, menu, modal, thông báo, định dạng
├── js/app.js                  # Luồng nghiệp vụ và hiển thị dữ liệu
└── supabase/schema.sql        # Bảng vai trò, view giới hạn cột, RPC và policy RLS
```

## 2. Chạy demo

1. Giải nén ZIP.
2. Mở thư mục bằng VS Code và chạy `chon-vai-tro.html` qua Live Server (khuyến nghị), hoặc dùng HTTP server tĩnh.
3. Chọn một trong năm vai trò. Dữ liệu mẫu hiển thị ngay và thay đổi lưu trên `localStorage` của trình duyệt.
4. Nút **Đổi vai trò** trên thanh đầu trang quay lại màn hình chọn vai trò. Nút này chỉ nhằm thử giao diện demo.

Không cần Node.js hay bước build. Tailwind, Font Awesome và thư viện Supabase được tải qua CDN nên bản HTML đầy đủ cần kết nối Internet để tải các thư viện đó.

## 3. Ma trận phân quyền

| Vai trò | Chỉ được vào các phân hệ | Không được vào |
|---|---|---|
| **Quản lý bệnh viện** | Tổng quan, bệnh nhân, khám, dược, viện phí, nhân sự, báo cáo | — |
| **Bác sĩ** | Bàn khám, chẩn đoán, chỉ định xét nghiệm, kê đơn, hồ sơ bệnh án | Dashboard quản lý, báo cáo, thu ngân, màn hình quản lý kho/cấp phát |
| **Y tá / Điều dưỡng** | Tiếp đón, đăng ký khám, xét nghiệm, hồ sơ bệnh án | Dashboard quản lý, báo cáo, dược/cấp phát, thu ngân |
| **Dược sĩ** | Đơn thuốc chờ cấp, danh mục thuốc, lô thuốc, tồn kho, xác nhận cấp phát | Hồ sơ bệnh nhân, bệnh án, tiếp đón, khám, xét nghiệm, viện phí, nhân sự, dashboard và báo cáo |
| **Thu ngân** | Hóa đơn viện phí và thanh toán | Hồ sơ bệnh nhân, bệnh án, tiếp đón, khám, dược, nhân sự, dashboard và báo cáo |

Menu chỉ hiển thị các phân hệ được phép. Khi dán trực tiếp URL của trang bị cấm, ứng dụng chuyển về trang làm việc mặc định của vai trò. Các thẻ KPI/tổng quan bị ẩn ở vai trò không phải quản lý. Dược sĩ nhận dữ liệu đơn thuốc qua view chỉ trả mã bệnh nhân cùng thông tin đơn; không truy vấn bảng bệnh nhân, bệnh án hay phiếu khám. Thu ngân nhận hóa đơn qua view tương tự, không cần tải bảng bệnh nhân.

**Giới hạn quan trọng:** chọn vai trò trong chế độ demo là mô phỏng giao diện, không phải cơ chế bảo mật. Người dùng có thể thay đổi `localStorage` trên máy của mình. Chỉ dùng dữ liệu giả ở chế độ này. Khi sử dụng dữ liệu thật, phải cấu hình Supabase Auth và các policy RLS dưới đây.

## 4. Kết nối Supabase và gán quyền thật

1. Tạo project Supabase và kiểm tra cấu trúc các bảng có trùng với schema của ứng dụng. Nếu chưa có bảng, chạy toàn bộ `supabase/schema.sql` bằng SQL Editor với tài khoản quản trị dự án. Nếu bạn đã có schema riêng theo ERD, cần đồng bộ tên cột/khóa trước khi chạy.
2. Trong Supabase Auth, tạo một tài khoản cho từng nhân viên.
3. Mở `js/api.js`, điền Project URL và **publishable/anon key**. Tuyệt đối không đưa `service_role` key vào frontend.
4. Trong SQL Editor, gán UUID người dùng với đúng vai trò và mã nhân viên. Ví dụ (thay UUID và `NV006` bằng dữ liệu thật):

```sql
insert into public.hms_user_roles (user_id, role, employee_id)
values ('UUID-USER-TRONG-SUPABASE-AUTH', 'manager', 'NV006');

-- Các giá trị role hợp lệ: manager, doctor, nurse, pharmacist, cashier
-- Ví dụ gán dược sĩ:
insert into public.hms_user_roles (user_id, role, employee_id)
values ('UUID-USER-DUOC-SI', 'pharmacist', 'NV004');
```

5. Mỗi nhân viên cần có đúng một bản ghi vai trò trong `hms_user_roles`. Khi đăng nhập, ứng dụng lấy quyền từ bảng này, không cho tài khoản Supabase tự chọn hoặc tự nâng vai trò trong màn hình. Nếu tài khoản chưa được gán vai trò, hệ thống từ chối vào ứng dụng.
6. Schema cấu hình RLS theo vai trò; view `v_hms_don_thuoc_cap_phat` giới hạn cột dữ liệu dược sĩ, view `v_hms_phieu_thu_vien_phi` giới hạn dữ liệu thu ngân, và RPC `hms_sync_invoice_for_exam` hỗ trợ đồng bộ viện phí mà không cần mở quyền đọc hóa đơn cho bác sĩ/y tá. Sau khi triển khai, nên thử từng tài khoản thật bằng các truy vấn/URL được phép và bị cấm.

### Vai trò và quyền trên máy chủ

- `hms_user_roles`: ánh xạ `auth.users.id` sang `role` và `employee_id`. Người dùng chỉ đọc được vai trò của mình; quản lý có quyền quản trị ánh xạ vai trò.
- `hms_current_role()`: hàm phía cơ sở dữ liệu tra cứu vai trò theo phiên đăng nhập hiện tại.
- Policy RLS cho phép quản lý truy cập tất cả phân hệ; các vai trò còn lại chỉ có quyền trên các bảng cần thiết cho nghiệp vụ.
- Dược sĩ không có policy đọc `benh_nhan`, `nguoi_nha`, `bao_hiem_yte`, `benh_an` hoặc `phieu_kham`. Chỉ view cấp phát được phép hiển thị các mã cần thiết để xử lý đơn.
- Dashboard và báo cáo chỉ có route giao diện cho quản lý; quyền truy cập bảng thống kê rộng cũng bị chặn bằng RLS đối với các vai trò còn lại.

RLS là lớp kiểm soát quan trọng nhưng không thay thế kiểm thử nghiệp vụ, audit log, phân quyền theo phạm vi bệnh nhân, backup và quy trình bảo vệ dữ liệu cá nhân. Cần rà soát policy với schema thực tế trước khi đưa dữ liệu bệnh nhân thật vào hệ thống.

## 5. Quy tắc `sdt TEXT[]`

Các trường `sdt` của `benh_nhan`, `nguoi_nha` và `nhan_vien` được chuẩn hóa qua `HMSApi.normalizePhoneArray()`. Giá trị nhập `0901234567, 0911222333` được gửi thành mảng PostgreSQL: `['0901234567', '0911222333']`. Khi nhận dữ liệu, giao diện dùng `HMSUI.phoneText()` để hiển thị danh sách số. Không stringify mảng thành JSON string.

## 6. Luồng demo chính

- **Tiếp đón:** tạo/sửa bệnh nhân, người nhà và BHYT; tạo phiếu khám.
- **Khám:** bác sĩ ghi triệu chứng, chọn chẩn đoán ICD, chỉ định xét nghiệm và kê đơn.
- **Xét nghiệm:** cập nhật kết quả và kết luận.
- **Dược:** xem đơn theo mã bệnh nhân, quản lý thuốc/lô và xác nhận cấp phát; tồn kho giảm theo lô gần hạn dùng (FEFO).
- **Viện phí:** thu ngân xem hóa đơn qua danh sách rút gọn, cập nhật phương thức và trạng thái thanh toán.
- **Quản lý:** xem dashboard, dữ liệu toàn viện, nhân sự và xuất báo cáo CSV.

## 7. Phạm vi

Đây là bộ prototype phục vụ phát triển bài tập lớn, không phải phần mềm y tế đã được kiểm định để vận hành thực tế. Cần bổ sung audit log, giao dịch kho nguyên tử chống cấp phát đồng thời, phân quyền theo phạm vi hồ sơ, kiểm thử bảo mật, quy trình hoàn/hủy thanh toán, quản lý phiên, chính sách backup và rà soát pháp lý trước khi đưa vào vận hành.
