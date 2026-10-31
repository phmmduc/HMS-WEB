# MediCore HMS — Hospital Management System

> Hệ thống Quản lý Bệnh viện Toàn diện được xây dựng trên nền tảng **Java 21**, **Spring Boot 3.3.5**, **Spring Data JPA**, **Hibernate ORM**, **Thymeleaf**, **Tailwind CSS** và kết nối cơ sở dữ liệu **Supabase PostgreSQL**.

---

## MỤC LỤC
1. [Giới thiệu dự án](#1-giới-thiệu-dự-án)
2. [Công nghệ sử dụng](#2-công-nghệ-sử-dụng)
3. [Cấu trúc dự án](#3-cấu-trúc-dự-án)
4. [Yêu cầu cài đặt](#4-yêu-cầu-cài-đặt)
5. [Cấu hình Supabase PostgreSQL](#5-cấu-hình-supabase-postgresql)
6. [Hướng dẫn Khởi chạy Ứng dụng](#6-hướng-dẫn-khởi-chạy-ứng-dụng)
7. [Truy cập giao diện & Chọn vai trò làm việc](#7-truy-cập-giao-diện--chọn-vai-trò-làm-việc)
8. [Mô tả chi tiết các phân hệ](#8-mô-tả-chi-tiết-các-phân-hệ)
9. [Xử lý sự cố thường gặp (Troubleshooting)](#9-xử-lý-sự-cố-thường-gặp-troubleshooting)
10. [Tình trạng dự án & Lộ trình hoàn thiện](#10-tình-trạng-dự-án--lộ-trình-hoàn-thiện)

---

## 1. GIỚI THIỆU DỰ ÁN

- **Tên dự án:** MediCore HMS (Hospital Management System)
- **Mục tiêu:** Cung cấp giải pháp phần mềm quản lý tổng thể quy trình khám chữa bệnh tại các cơ sở y tế và bệnh viện đa khoa, từ tiếp đón bệnh nhân, phân công phòng khám, chẩn đoán & kê đơn thuốc, xét nghiệm cận lâm sàng, cấp phát thuốc kho dược theo chuẩn FEFO, đến thanh toán viện phí và theo dõi hồ sơ bệnh án điện tử.
- **Triết lý thiết kế:**
  - **Giao diện chuẩn y tế hiện đại:** Tông màu chủ đạo xanh y tế `#0284c7`, giao diện trực quan, rõ ràng, tối ưu hóa cho màn hình làm việc của bác sĩ, y tá, dược sĩ và thu ngân.
  - **Kiến trúc tầng kép (Dual-Layer Architecture):** Tách bạch giữa JPA Entity lưu trữ chuẩn 24 bảng + 2 view PostgreSQL và View Model giao diện, đảm bảo an toàn dữ liệu, tính toàn vẹn nghiệp vụ và không làm gián đoạn trải nghiệm người dùng.

---

## 2. CÔNG NGHỆ SỬ DỤNG

- **Ngôn ngữ:** Java 21 (LTS)
- **Framework nền tảng:** Spring Boot 3.3.5
- **Tầng Web MVC:** Spring MVC & Thymeleaf Template Engine
- **Tầng Dữ liệu:** Spring Data JPA & Hibernate ORM 6.5
- **Hệ quản trị Cơ sở dữ liệu:** PostgreSQL (Supabase Shared Pooler) & H2 Database (cho In-Memory Test/Fallback)
- **Công cụ Build & Quản lý Thư viện:** Apache Maven 3.9+
- **Giao diện người dùng:** Tailwind CSS (CDN), Font Awesome 6.5, Vanilla JavaScript

---

## 3. CẤU TRÚC DỰ ÁN

```text
MediCore-HMS/
├── pom.xml                               # Cấu hình Maven dependencies và build
├── mvnw / mvnw.cmd                       # Maven Wrapper cross-platform
├── README.md                             # Tài liệu tổng quan dự án (file này)
├── .gitignore                            # Quy tắc loại trừ Git (target/, .env, *.log)
├── .env.example                          # File mẫu thiết lập biến môi trường Supabase
│
├── .vscode/
│   ├── launch.json                       # Cấu hình Debug Spring Boot với .env
│   └── settings.json                     # Cấu hình Java workspace trong VS Code
│
├── .agents/skills/supabase/
│   └── SKILL.md                          # Hướng dẫn thao tác Supabase cho AI coding agents
│
├── database/
│   ├── schema.sql                        # DDL chuẩn 24 bảng + 2 view Supabase PostgreSQL
│   ├── migrations/
│   │   └── 01_migration_additions.sql    # Migration an toàn không phá hủy (ADD COLUMN, INDEX)
│   └── seeds/
│       └── 01_sample_data.sql            # Script SQL nạp dữ liệu mẫu ban đầu
│
├── docs/
│   ├── DATABASE_MAPPING.md               # Bảng đối chiếu 24 bảng, JPA Entity & Service
│   ├── DATABASE_GUIDE.md                 # Hướng dẫn kết nối Supabase, pooler & troubleshoot
│   ├── PROJECT_STRUCTURE.md              # Giải thích chi tiết cấu trúc thư mục source code
│   └── DEVELOPMENT_GUIDE.md              # Hướng dẫn thêm module, Controller, JPA mới
│
└── src/
    ├── main/
    │   ├── java/com/hms/
    │   │   ├── HmsApplication.java       # Class khởi chạy ứng dụng
    │   │   ├── controller/               # 11 Controllers điều hướng Spring MVC
    │   │   ├── entity/                   # 24 JPA Entities + 2 View Entities
    │   │   │   └── id/                   # 6 Composite Key Classes (@Embeddable)
    │   │   ├── repository/               # 26 Spring Data JPA Repositories
    │   │   ├── service/                  # 9 Database Services & Model Mapper
    │   │   ├── model/                    # View Models giao diện người dùng
    │   │   ├── dto/                      # Form DTOs submit dữ liệu
    │   │   └── mock/                     # In-Memory Fallback Services cho demo
    │   └── resources/
    │       ├── application.properties    # Cấu hình kết nối Supabase & Spring Boot
    │       ├── static/                   # Tài nguyên CSS, JS tương tác
    │       └── templates/                # 14 Thymeleaf HTML templates theo phân hệ
    └── test/
        ├── java/com/hms/
        │   ├── HmsApplicationTests.java     # 32 ca kiểm thử Controller & luồng giao diện
        │   └── DatabaseRepositoryTests.java # 4 ca kiểm thử CRUD JPA Repositories
        └── resources/
            └── application.properties       # Cấu hình H2 PostgreSQL mode cho tests
```

---

## 4. YÊU CẦU CÀI ĐẶT

Trước khi chạy dự án, hãy đảm bảo máy tính của bạn đã cài đặt các công cụ sau:

1. **Java Development Kit (JDK):** Phiên bản **JDK 21** trở lên (Oracle OpenJDK, Eclipse Temurin hoặc Amazon Corretto).
   - Kiểm tra: `java -version`
2. **Apache Maven:** Phiên bản **3.9+** (hoặc sử dụng trực tiếp `./mvnw` / `mvnw.cmd` đi kèm).
   - Kiểm tra: `mvn -version`
3. **Môi trường phát triển (IDE):**
   - Visual Studio Code (kèm Extension Pack for Java và Spring Boot Extension Pack) hoặc IntelliJ IDEA 2023+.
4. **Git:** Phiên bản 2.30+ để quản lý mã nguồn.

---

## 5. CẤU HÌNH SUPABASE POSTGRESQL

Hệ thống kết nối tới cơ sở dữ liệu Supabase thông qua **Shared Pooler (Session mode)**:

- **Host:** `aws-0-ap-northeast-2.pooler.supabase.com`
- **Port:** `5432`
- **Database:** `postgres`
- **Username:** `postgres.abbivalfllpmdwxddnra`
- **JDBC Connection URL:**
  ```text
  jdbc:postgresql://aws-0-ap-northeast-2.pooler.supabase.com:5432/postgres?sslmode=require
  ```

### Các bước thiết lập kết nối:

1. **Khởi tạo bảng trong Supabase SQL Editor:**
   - Mở [Supabase Dashboard](https://supabase.com/dashboard) -> Chọn Project của bạn -> Vào **SQL Editor**.
   - Mở file [`database/schema.sql`](file:///Users/phmmduc/Desktop/Project/HMS-WEB/database/schema.sql), dán vào SQL Editor và nhấn **Run** để tạo đủ 24 bảng và 2 view.
   - Mở tiếp file [`database/migrations/01_migration_additions.sql`](file:///Users/phmmduc/Desktop/Project/HMS-WEB/database/migrations/01_migration_additions.sql) và nhấn **Run** để thêm các cột mở rộng và index hiệu năng.

2. **Tạo file `.env` từ `.env.example`:**
   ```bash
   cp .env.example .env
   ```
   Điền mật khẩu database thực tế vào biến `SUPABASE_DB_PASSWORD` trong file `.env`:
   ```env
   SUPABASE_DB_URL=jdbc:postgresql://aws-0-ap-northeast-2.pooler.supabase.com:5432/postgres?sslmode=require
   SUPABASE_DB_USERNAME=postgres.abbivalfllpmdwxddnra
   SUPABASE_DB_PASSWORD=<MẬT_KHẨU_DATABASE_CỦA_BẠN>
   SPRING_JPA_HIBERNATE_DDL_AUTO=none
   ```

> [!CAUTION]
> **Bảo mật mật khẩu:** Tuyệt đối không commit file `.env` hoặc ghi mật khẩu trực tiếp vào `application.properties`. Ứng dụng đã cấu hình `.gitignore` để tự động loại trừ file `.env`.

---

## 6. HƯỚNG DẪN KHỞI CHẠY ỨNG DỤNG

### 6.1. Trên macOS / Linux

**Chạy kiểm thử tự động (Unit & Repository Tests):**
```bash
mvn clean test
```

**Khởi chạy với biến môi trường Supabase:**
```bash
export SUPABASE_DB_URL="jdbc:postgresql://aws-0-ap-northeast-2.pooler.supabase.com:5432/postgres?sslmode=require"
export SUPABASE_DB_USERNAME="postgres.abbivalfllpmdwxddnra"
export SUPABASE_DB_PASSWORD="<MẬT_KHẨU_DATABASE_CỦA_BẠN>"
export SPRING_JPA_HIBERNATE_DDL_AUTO="none"

mvn spring-boot:run
```

Hoặc nạp nhanh từ `.env`:
```bash
env $(cat .env | grep -v '^#' | xargs) mvn spring-boot:run
```

### 6.2. Trên Windows

**Chạy kiểm thử bằng Maven Wrapper:**
```cmd
mvnw.cmd clean test
```

**Khởi chạy bằng CMD:**
```cmd
set SUPABASE_DB_URL=jdbc:postgresql://aws-0-ap-northeast-2.pooler.supabase.com:5432/postgres?sslmode=require
set SUPABASE_DB_USERNAME=postgres.abbivalfllpmdwxddnra
set SUPABASE_DB_PASSWORD=<MẬT_KHẨU_DATABASE_CỦA_BẠN>
set SPRING_JPA_HIBERNATE_DDL_AUTO=none

mvnw.cmd spring-boot:run
```

**Khởi chạy bằng PowerShell:**
```powershell
$env:SUPABASE_DB_URL="jdbc:postgresql://aws-0-ap-northeast-2.pooler.supabase.com:5432/postgres?sslmode=require"
$env:SUPABASE_DB_USERNAME="postgres.abbivalfllpmdwxddnra"
$env:SUPABASE_DB_PASSWORD="<MẬT_KHẨU_DATABASE_CỦA_BẠN>"
$env:SPRING_JPA_HIBERNATE_DDL_AUTO="none"

mvnw.cmd spring-boot:run
```

### 6.3. Khởi chạy bằng VS Code Debugger
- Mở VS Code -> Nhấn phím `F5` hoặc chọn menu **Run and Debug** -> **Launch MediCore HMS (Spring Boot)**. VS Code sẽ tự động tải các biến môi trường từ file `.env`.

---

## 7. TRUY CẬP GIAO DIỆN & CHỌN VAI TRÒ LÀM VIỆC

Sau khi ứng dụng khởi động thành công, mở trình duyệt web và truy cập:

```text
http://localhost:8080
```

Hệ thống cung cấp màn hình chọn **Không gian làm việc theo vai trò (Role Workspace)**:

| STT | Vai trò | URL mặc định | Phạm vi nghiệp vụ |
|:---:|:---|:---|:---|
| 1 | **Quản lý bệnh viện** | `/dashboard` | Toàn quyền quản trị: Dashboard KPI doanh thu, tiếp đón, khám bệnh, xét nghiệm, dược, viện phí, hồ sơ bệnh án, quản lý nhân sự, khoa phòng và báo cáo xuất CSV. |
| 2 | **Bác sĩ** | `/examination` | Danh sách bệnh nhân theo phòng khám, màn hình chi tiết khám bệnh, chẩn đoán ICD-10, chỉ định xét nghiệm và kê đơn thuốc liên kết kho dược. |
| 3 | **Y tá / Điều dưỡng** | `/registration` | Tiếp đón bệnh nhân mới, đăng ký lượt khám, quản lý danh mục và kết quả xét nghiệm (`/laboratory`). |
| 4 | **Dược sĩ** | `/pharmacy` | Duyệt đơn thuốc chờ cấp phát, cấp phát trừ kho theo FEFO (hạn dùng gần nhất), quản lý danh mục thuốc và danh sách lô thuốc. |
| 5 | **Thu ngân** | `/billing` | Tiếp nhận phiếu thu viện phí, xác nhận thanh toán, đăng ký khám bệnh và in hóa đơn viện phí chuẩn y tế (`/billing/print/{id}`). |

---

## 8. MÔ TẢ CHI TIẾT CÁC PHÂN HỆ

1. **Dashboard & Thống kê KPI (`/dashboard`):**
   - Tổng quan số lượt khám, doanh thu ngày/tháng, số bệnh nhân đang chờ, biểu đồ tương tác doanh thu và phân bổ lượt khám theo chuyên khoa.
2. **Tiếp đón & Đăng ký khám (`/registration`):**
   - Quản lý hồ sơ bệnh nhân, thông tin liên lạc, thẻ BHYT và thân nhân.
   - Đăng ký phiếu khám mới: tự động lọc danh sách bác sĩ theo phòng khám được chọn.
3. **Khám bệnh & Kê đơn thuốc (`/examination`):**
   - Màn hình chi tiết phiếu khám: nhập triệu chứng, chọn chẩn đoán mã ICD-10, chỉ định cận lâm sàng.
   - Form kê đơn thuốc động: chọn thuốc từ danh mục, nhập số lượng, liều dùng, cách dùng; tự động tạo hóa đơn tiền thuốc.
4. **Xét nghiệm Cận lâm sàng (`/laboratory`):**
   - Danh sách phiếu chỉ định xét nghiệm, cập nhật kết quả và kết luận xét nghiệm.
   - Quản lý danh mục loại xét nghiệm (`/laboratory/types`): thêm mới, cập nhật chi phí, xóa loại xét nghiệm.
5. **Kho Dược & Cấp phát thuốc (`/pharmacy`):**
   - Quản lý danh mục biệt dược, hoạt chất, tồn kho.
   - Quản lý lô thuốc theo hạn sử dụng, tính năng loại bỏ lô thuốc hết hạn an toàn.
   - Cấp phát đơn thuốc trừ kho tự động theo nguyên tắc FEFO (First Expired, First Out).
6. **Thu viện phí & In hóa đơn (`/billing`):**
   - Danh sách hóa đơn khám bệnh và tiền thuốc.
   - Xác nhận thanh toán (tiền mặt, chuyển khoản, BHYT) và in phiếu thu viện phí chuẩn khổ giấy in y tế.
7. **Hồ sơ Bệnh án Điện tử (`/medical-records`):**
   - Tra cứu lịch sử các lần khám bệnh của từng bệnh nhân, xem chi tiết phiếu khám, đơn thuốc và kết quả xét nghiệm liên quan.
8. **Nhân sự, Chuyên khoa & Phòng khám (`/staff`):**
   - Danh bạ nhân viên bệnh viện theo vai trò.
   - Thêm/sửa/xóa nhân viên, chuyên khoa và phòng khám.
   - Tính năng chuyển đổi qua lại giữa các nhân viên trong cùng vai trò phục vụ demo công việc.
9. **Báo cáo Thống kê (`/reports`):**
   - Thống kê doanh thu theo khoảng thời gian, nhóm dịch vụ và hỗ trợ xuất dữ liệu ra file CSV.

---

## 9. XỬ LÝ SỰ CỐ THƯỜNG GẶP (TROUBLESHOOTING)

| Lỗi gặp phải | Nguyên nhân | Cách khắc phục |
|:---|:---|:---|
| **Port 8080 already in use** | Một tiến trình khác (Tomcat, Docker) đang chiếm cổng 8080. | Tắt tiến trình cũ: `lsof -ti:8080 \| xargs kill -9` hoặc đổi port trong `application.properties`: `server.port=8081`. |
| **FATAL: password authentication failed** | Mật khẩu database `SUPABASE_DB_PASSWORD` bị sai hoặc để trống. | Kiểm tra mật khẩu database trên Supabase Dashboard; kiểm tra lại lệnh `export SUPABASE_DB_PASSWORD="<password>"`. |
| **Connection timed out** | Không kết nối được tới host trực tiếp hoặc mạng IPv4 bị giới hạn. | Sử dụng Shared Pooler URL đã cấu hình sẵn (`aws-0-ap-northeast-2.pooler.supabase.com:5432`). |
| **Relation "xyz" does not exist** | Chưa chạy script DDL trên Supabase SQL Editor. | Mở Supabase SQL Editor và chạy toàn bộ nội dung file `database/schema.sql`. |
| **ApplicationContext failure / DDL validate error** | Cột trong database không khớp với JPA Entity. | Chạy script bổ sung `database/migrations/01_migration_additions.sql` để thêm cột mở rộng. |
| **Maven dependency error / build failure** | Cache dependency Maven cục bộ bị lỗi. | Chạy lệnh làm sạch và tải lại dependency: `mvn clean test -U`. |

---

## 10. TÌNH TRẠNG DỰ ÁN & LỘ TRÌNH HOÀN THIỆN

### 10.1. Các phần đã hoàn thành và sẵn sàng kết nối Database
- **Schema & DDL:** Đã chuẩn hóa toàn bộ 24 bảng quan hệ và 2 view theo chuẩn Supabase PostgreSQL trong `database/schema.sql`.
- **JPA & Repositories:** Đã xây dựng 24 JPA Entity + 2 View Entity + 6 Composite PKs và 26 Spring Data JPA Repositories tương ứng 100%.
- **Database Services & Mapper:** Đã xây dựng `EntityModelMapper` và 9 Database Services độc lập thực hiện CRUD cho Bệnh nhân, Nhân sự, Khoa phòng, Phiếu khám, Thuốc, Xét nghiệm và Viện phí.
- **Auto-seeding:** Class `DatabaseDataSeeder` tự động gieo dữ liệu chuẩn khi kết nối database Supabase lần đầu.
- **Automated Testing:** 36/36 ca kiểm thử tự động (`HmsApplicationTests` + `DatabaseRepositoryTests`) vượt qua với kết quả `BUILD SUCCESS`.

### 10.2. Các phần đang duy trì Fallback In-Memory
- Tầng Controller hiện tại đang ưu tiên phục vụ người dùng thông qua các Mock Service để bảo toàn tuyệt đối 100% trải nghiệm và giao diện của MediCore HMS. Các Mock Service này đã được tích hợp adapter an toàn: khi có kết nối Supabase, dữ liệu sẽ được đồng bộ trực tiếp xuống PostgreSQL mà không gây gián đoạn hay lỗi hệ thống nếu database tạm thời mất kết nối.
- Khi người dùng cung cấp mật khẩu Supabase hợp lệ, hệ thống hoàn toàn sẵn sàng chuyển đổi trực tiếp sang chế độ thuần Database Service mà không cần thay đổi bất kỳ dòng mã nào trên giao diện.

---

## 11. TÀI LIỆU THAM KHẢO BỔ SUNG

- [DATABASE_MAPPING.md](file:///Users/phmmduc/Desktop/Project/HMS-WEB/docs/DATABASE_MAPPING.md) — Bảng đối chiếu chi tiết 24 bảng, JPA Entity, khóa chính và quan hệ.
- [DATABASE_GUIDE.md](file:///Users/phmmduc/Desktop/Project/HMS-WEB/docs/DATABASE_GUIDE.md) — Hướng dẫn kết nối Supabase, cấu hình pooler và truy vấn thử nghiệm.
- [PROJECT_STRUCTURE.md](file:///Users/phmmduc/Desktop/Project/HMS-WEB/docs/PROJECT_STRUCTURE.md) — Tài liệu chi tiết cấu trúc thư mục và quy tắc tổ chức source code.
- [DEVELOPMENT_GUIDE.md](file:///Users/phmmduc/Desktop/Project/HMS-WEB/docs/DEVELOPMENT_GUIDE.md) — Hướng dẫn phát triển module và tính năng mới chuẩn Spring Boot.
