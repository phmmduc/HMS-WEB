-- ==============================================================================
-- MediCore HMS - Standard Sample Seed Data for Supabase PostgreSQL
-- Chạy script này trong Supabase SQL Editor sau khi đã chạy schema.sql
-- ==============================================================================

-- 1. CHUYÊN KHOA
INSERT INTO chuyenkhoa (id_ck, ten_ck) VALUES
('CK01', 'Nội tổng hợp'),
('CK02', 'Tim mạch'),
('CK03', 'Xét nghiệm'),
('CK04', 'Nhi khoa')
ON CONFLICT (id_ck) DO NOTHING;

-- 2. PHÒNG KHÁM
INSERT INTO phongkham (id_pk, id_ck, ten_pk, vi_tri, trang_thai) VALUES
('PK01', 'CK01', 'Phòng khám Nội tổng hợp', 'A-101', 'Hoạt động'),
('PK02', 'CK02', 'Phòng khám Tim mạch', 'A-202', 'Hoạt động'),
('PK03', 'CK03', 'Phòng xét nghiệm', 'B-105', 'Hoạt động'),
('PK04', 'CK04', 'Phòng khám Nhi', 'C-102', 'Hoạt động')
ON CONFLICT (id_pk) DO NOTHING;

-- 3. NHÂN VIÊN
INSERT INTO nhanvien (id_nv, ho_ten, gioi_tinh, ngay_sinh, vai_tro, trang_thai) VALUES
('NV001', 'BS. Nguyễn Hoàng An', 'Nam', '1980-02-12', 'Bác sĩ', 'DANG_LAM'),
('NV002', 'BS. Trần Mai Phương', 'Nữ', '1985-09-21', 'Bác sĩ', 'DANG_LAM'),
('NV003', 'Điều dưỡng Lê Thu Trang', 'Nữ', '1992-06-07', 'Y tá', 'DANG_LAM'),
('NV004', 'DS. Phạm Quốc Huy', 'Nam', '1988-04-18', 'Dược sĩ', 'DANG_LAM'),
('NV005', 'Nguyễn Hải Yến', 'Nữ', '1991-12-03', 'Thu ngân', 'DANG_LAM'),
('NV006', 'Đỗ Văn Khánh', 'Nam', '1978-08-13', 'Quản lý', 'DANG_LAM'),
('NV007', 'Điều dưỡng Hoàng Minh Thảo', 'Nữ', '1994-03-15', 'Y tá', 'DANG_LAM'),
('NV008', 'DS. Vũ Thị Mai', 'Nữ', '1990-11-20', 'Dược sĩ', 'DANG_LAM'),
('NV009', 'Đỗ Hoàng Long', 'Nam', '1993-05-10', 'Thu ngân', 'DANG_LAM'),
('NV010', 'Trần Đình Trọng', 'Nam', '1982-10-05', 'Quản lý', 'DANG_LAM'),
('NV011', 'BS. Lê Hoàng Nam', 'Nam', '1989-07-25', 'Bác sĩ', 'DANG_LAM')
ON CONFLICT (id_nv) DO NOTHING;

-- SỐ ĐIỆN THOẠI NHÂN VIÊN
INSERT INTO nhanvien_sdt (id_nv, sdt) VALUES
('NV001', '0903001001'),
('NV002', '0903001002'),
('NV003', '0903001003'),
('NV004', '0903001004'),
('NV005', '0903001005'),
('NV006', '0903001006'),
('NV007', '0903001007'),
('NV008', '0903001008'),
('NV009', '0903001009'),
('NV010', '0903001010'),
('NV011', '0903001011')
ON CONFLICT (id_nv, sdt) DO NOTHING;

-- BÁC SĨ
INSERT INTO bacsy (id_nv, id_ck, hoc_vi, so_cchn) VALUES
('NV001', 'CK01', 'Thạc sĩ · Nội tổng hợp', 'CCHN-NV001'),
('NV002', 'CK02', 'Bác sĩ CKI · Tim mạch', 'CCHN-NV002'),
('NV011', 'CK04', 'Bác sĩ CKI · Nhi khoa', 'CCHN-NV011')
ON CONFLICT (id_nv) DO NOTHING;

-- Y TÁ
INSERT INTO yta (id_nv, cap_bac) VALUES
('NV003', 'Điều dưỡng đại học'),
('NV007', 'Cử nhân điều dưỡng')
ON CONFLICT (id_nv) DO NOTHING;

-- DƯỢC SĨ
INSERT INTO duocsi (id_nv, bang_cap) VALUES
('NV004', 'Dược sĩ đại học'),
('NV008', 'Dược sĩ lâm sàng')
ON CONFLICT (id_nv) DO NOTHING;

-- 4. CHẨN ĐOÁN BỆNH ICD-10
INSERT INTO chandoan (id_icd, ten_benh) VALUES
('CD001', 'Nhiễm trùng đường hô hấp trên cấp, không đặc hiệu (J06.9)'),
('CD002', 'Tăng huyết áp vô căn (I10)'),
('CD003', 'Viêm mũi dị ứng, không đặc hiệu (J30.4)'),
('CD004', 'Sốt, không đặc hiệu (R50.9)'),
('CD005', 'Đái tháo đường type 2, không biến chứng (E11.9)')
ON CONFLICT (id_icd) DO NOTHING;

-- 5. LOẠI XÉT NGHIỆM
INSERT INTO loaixetnghiem (id_lxn, ten_loai, chi_phi) VALUES
('XN001', 'Công thức máu', 180000.00),
('XN002', 'Đường huyết', 90000.00),
('XN003', 'Chức năng gan (AST/ALT)', 220000.00),
('XN004', 'Tổng phân tích nước tiểu', 120000.00)
ON CONFLICT (id_lxn) DO NOTHING;

-- 6. THUỐC
INSERT INTO thuoc (id_thuoc, ten_thuoc, hoat_chat, dang_bao_che, don_vi, can_ke_don, so_luong_ton_kho, gia_tham_khao, trang_thai) VALUES
('T001', 'Paracetamol 500 mg', 'Paracetamol', '500 mg', 'viên', FALSE, 420, 800.00, 'Đang kinh doanh'),
('T002', 'Amoxicillin 500 mg', 'Amoxicillin', '500 mg', 'viên', TRUE, 180, 2000.00, 'Đang kinh doanh'),
('T003', 'Cetirizine 10 mg', 'Cetirizine 2HCl', '10 mg', 'viên', FALSE, 95, 1500.00, 'Đang kinh doanh'),
('T004', 'Amlodipine 5 mg', 'Amlodipine besylate', '5 mg', 'viên', TRUE, 70, 3000.00, 'Đang kinh doanh'),
('T005', 'Oresol', 'Glucose, Natri & Kali', '27.9 g/gói', 'gói', FALSE, 24, 4000.00, 'Đang kinh doanh'),
('T006', 'Vitamin C 500 mg', 'Acid ascorbic', '500 mg', 'viên', FALSE, 0, 1200.00, 'Đang kinh doanh')
ON CONFLICT (id_thuoc) DO NOTHING;

-- LÔ THUỐC
INSERT INTO lothuoc (id_lo, id_thuoc, so_lo, han_su_dung, so_luong_nhap, so_luong_con_lai, gia_nhap, gia_ban, trang_thai) VALUES
('LO001', 'T001', 'PAR-2608', CURRENT_DATE + INTERVAL '220 days', 500, 300, 450.00, 800.00, 'Hoạt động'),
('LO002', 'T001', 'PAR-2610', CURRENT_DATE + INTERVAL '360 days', 200, 120, 470.00, 800.00, 'Hoạt động'),
('LO003', 'T002', 'AMX-2605', CURRENT_DATE + INTERVAL '140 days', 300, 180, 1200.00, 2000.00, 'Hoạt động'),
('LO004', 'T003', 'CET-2609', CURRENT_DATE + INTERVAL '45 days', 120, 95, 900.00, 1500.00, 'Hoạt động'),
('LO005', 'T004', 'AML-2604', CURRENT_DATE + INTERVAL '310 days', 100, 70, 1800.00, 3000.00, 'Hoạt động'),
('LO006', 'T005', 'ORS-2607', CURRENT_DATE + INTERVAL '28 days', 60, 24, 2500.00, 4000.00, 'Hoạt động')
ON CONFLICT (id_lo) DO NOTHING;

-- 7. BỆNH NHÂN
INSERT INTO benhnhan (id_bn, ho_ten, cccd, ngay_sinh, gioi_tinh, dia_chi, trang_thai) VALUES
('BN001', 'Nguyễn Minh Anh', '001206012345', '1998-03-14', 'Nữ', 'Cầu Giấy, Hà Nội', 'Đang theo dõi'),
('BN002', 'Trần Quốc Bảo', '001199007654', '1987-11-02', 'Nam', 'Hà Đông, Hà Nội', 'Ổn định'),
('BN003', 'Lê Thu Hà', '001203004321', '2003-07-19', 'Nữ', 'Đống Đa, Hà Nội', 'Ổn định'),
('BN004', 'Phạm Đức Long', '001190009876', '1990-12-08', 'Nam', 'Thanh Xuân, Hà Nội', 'Đang theo dõi'),
('BN005', 'Đỗ Ngọc Linh', '001205001122', '2005-01-25', 'Nữ', 'Long Biên, Hà Nội', 'Ổn định'),
('BN006', 'Vũ Thành Nam', '001185009911', '1985-09-30', 'Nam', 'Nam Từ Liêm, Hà Nội', 'Ổn định'),
('BN007', 'Hoàng Thu Trang', '001205008899', '1995-05-18', 'Nữ', 'Cầu Giấy, Hà Nội', 'Ổn định')
ON CONFLICT (id_bn) DO NOTHING;

-- SỐ ĐIỆN THOẠI BỆNH NHÂN
INSERT INTO benhnhan_sdt (id_bn, sdt) VALUES
('BN001', '0901234567'),
('BN002', '0912345678'),
('BN002', '0988123456'),
('BN003', '0934567890'),
('BN004', '0977654321'),
('BN005', '0966123456'),
('BN006', '0944112233'),
('BN007', '0988776655')
ON CONFLICT (id_bn, sdt) DO NOTHING;

-- NGƯỜI THÂN
INSERT INTO nguoithan (id_nt, id_bn, ho_ten, quan_he) VALUES
('NT001', 'BN001', 'Nguyễn Văn Minh', 'Bố'),
('NT002', 'BN003', 'Lê Thị Hương', 'Mẹ'),
('NT003', 'BN007', 'Hoàng Văn Hùng', 'Bố')
ON CONFLICT (id_nt) DO NOTHING;

-- BẢO HIỂM Y TẾ
INSERT INTO baohiemyte (sobhyt, id_bn, muc_huong, nhom_doi_tuong, noi_dang_ky, ngay_bat_dau, ngay_ket_thuc) VALUES
('HN4010123456789', 'BN001', 80, 'Hộ gia đình', 'Trạm y tế phường Dịch Vọng', CURRENT_DATE - INTERVAL '180 days', CURRENT_DATE + INTERVAL '185 days'),
('HN4010987654321', 'BN002', 95, 'Người lao động', 'Bệnh viện quận Hà Đông', CURRENT_DATE - INTERVAL '365 days', CURRENT_DATE + INTERVAL '30 days'),
('HN4010112233445', 'BN005', 100, 'Trẻ em', 'Bệnh viện Nhi Trung ương', CURRENT_DATE - INTERVAL '90 days', CURRENT_DATE + INTERVAL '275 days'),
('HN4010556677889', 'BN007', 80, 'Người lao động', 'Bệnh viện E Hà Nội', CURRENT_DATE - INTERVAL '120 days', CURRENT_DATE + INTERVAL '245 days')
ON CONFLICT (sobhyt) DO NOTHING;

-- BỆNH ÁN
INSERT INTO benhan (id_ba, id_bn, ngay_tao, tien_su_benh, trang_thai) VALUES
('BA001', 'BN001', CURRENT_DATE - INTERVAL '60 days', 'Viêm mũi dị ứng theo mùa', 'DANG_MO'),
('BA002', 'BN002', CURRENT_DATE - INTERVAL '120 days', 'Tăng huyết áp nhẹ', 'DANG_MO'),
('BA003', 'BN004', CURRENT_DATE - INTERVAL '30 days', 'Chưa ghi nhận bất thường', 'DANG_MO'),
('BA004', 'BN006', CURRENT_DATE - INTERVAL '45 days', 'Tăng huyết áp nhẹ, rối loạn lipid', 'DANG_MO'),
('BA005', 'BN007', CURRENT_DATE - INTERVAL '10 days', 'Viêm họng tái phát nhiều lần', 'DANG_MO')
ON CONFLICT (id_ba) DO NOTHING;

