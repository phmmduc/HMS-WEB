-- =============================================================================
-- MediCore HMS (XônPanh HMS) - Non-Destructive Schema Extension Migration
-- Thêm các cột bổ trợ phục vụ giao diện MediCore HMS trên Supabase
-- Đảm bảo an toàn: KHÔNG DROP TABLE, KHÔNG XÓA DỮ LIỆU HIỆN CÓ
-- =============================================================================

-- 1. BẢNG benhnhan (Bổ sung địa chỉ, ngày tạo, trạng thái theo dõi)
ALTER TABLE benhnhan ADD COLUMN IF NOT EXISTS dia_chi VARCHAR(255);
ALTER TABLE benhnhan ADD COLUMN IF NOT EXISTS ngay_tao DATE DEFAULT CURRENT_DATE;
ALTER TABLE benhnhan ADD COLUMN IF NOT EXISTS trang_thai VARCHAR(50) DEFAULT 'Ổn định';

-- 2. BẢNG phongkham (Bổ sung trạng thái hoạt động)
ALTER TABLE phongkham ADD COLUMN IF NOT EXISTS trang_thai VARCHAR(50) DEFAULT 'Hoạt động';

-- 3. BẢNG nhanvien (Bổ sung trạng thái làm việc)
ALTER TABLE nhanvien ADD COLUMN IF NOT EXISTS trang_thai VARCHAR(50) DEFAULT 'Đang làm việc';

-- 4. BẢNG bacsy (Bổ sung phòng khám trực tiếp phân công)
ALTER TABLE bacsy ADD COLUMN IF NOT EXISTS id_pk VARCHAR(10) REFERENCES phongkham(id_pk);

-- 5. BẢNG loaixetnghiem (Bổ sung trạng thái hoạt động)
ALTER TABLE loaixetnghiem ADD COLUMN IF NOT EXISTS trang_thai VARCHAR(50) DEFAULT 'Hoạt động';

-- 6. BẢNG phieukham (Bổ sung số thứ tự hàng đợi và chỉ số sinh tồn)
ALTER TABLE phieukham ADD COLUMN IF NOT EXISTS so_thu_tu INTEGER;
ALTER TABLE phieukham ADD COLUMN IF NOT EXISTS mach INTEGER;
ALTER TABLE phieukham ADD COLUMN IF NOT EXISTS nhiet_do NUMERIC(4,1);
ALTER TABLE phieukham ADD COLUMN IF NOT EXISTS huyet_ap VARCHAR(20);
ALTER TABLE phieukham ADD COLUMN IF NOT EXISTS nhip_tho INTEGER;
ALTER TABLE phieukham ADD COLUMN IF NOT EXISTS can_nang NUMERIC(5,2);
ALTER TABLE phieukham ADD COLUMN IF NOT EXISTS chieu_cao NUMERIC(5,2);

-- 7. BẢNG thuoc (Bổ sung đơn vị tính, hàm lượng, hoạt chất, danh mục, trạng thái)
ALTER TABLE thuoc ADD COLUMN IF NOT EXISTS don_vi VARCHAR(50) DEFAULT 'Viên';
ALTER TABLE thuoc ADD COLUMN IF NOT EXISTS ham_luong VARCHAR(100);
ALTER TABLE thuoc ADD COLUMN IF NOT EXISTS hoat_chat VARCHAR(150);
ALTER TABLE thuoc ADD COLUMN IF NOT EXISTS danh_muc VARCHAR(100);
ALTER TABLE thuoc ADD COLUMN IF NOT EXISTS trang_thai VARCHAR(50) DEFAULT 'Hoạt động';

-- 8. BẢNG chitietdonthuoc (Bổ sung lô thuốc, đơn giá và thành tiền chi tiết)
ALTER TABLE chitietdonthuoc ADD COLUMN IF NOT EXISTS id_lo VARCHAR(10) REFERENCES lothuoc(id_lo);
ALTER TABLE chitietdonthuoc ADD COLUMN IF NOT EXISTS don_gia NUMERIC(12,2);
ALTER TABLE chitietdonthuoc ADD COLUMN IF NOT EXISTS thanh_tien NUMERIC(12,2);

-- 9. BẢNG phieuthuvienphi (Bổ sung bóc tách khoản mục viện phí & ghi chú)
ALTER TABLE phieuthuvienphi ADD COLUMN IF NOT EXISTS tien_kham NUMERIC(12,2) DEFAULT 0;
ALTER TABLE phieuthuvienphi ADD COLUMN IF NOT EXISTS tien_xet_nghiem NUMERIC(12,2) DEFAULT 0;
ALTER TABLE phieuthuvienphi ADD COLUMN IF NOT EXISTS tien_thuoc NUMERIC(12,2) DEFAULT 0;
ALTER TABLE phieuthuvienphi ADD COLUMN IF NOT EXISTS bhyt_chi_tra NUMERIC(12,2) DEFAULT 0;
ALTER TABLE phieuthuvienphi ADD COLUMN IF NOT EXISTS benh_nhan_tra NUMERIC(12,2) DEFAULT 0;
ALTER TABLE phieuthuvienphi ADD COLUMN IF NOT EXISTS ghi_chu TEXT;

-- 10. Index tối ưu hiệu năng truy vấn tìm kiếm
CREATE INDEX IF NOT EXISTS idx_benhnhan_hoten ON benhnhan(ho_ten);
CREATE INDEX IF NOT EXISTS idx_benhnhan_cccd ON benhnhan(cccd);
CREATE INDEX IF NOT EXISTS idx_phieukham_ngaykham ON phieukham(ngay_kham);
CREATE INDEX IF NOT EXISTS idx_phieukham_trangthai ON phieukham(trang_thai);
CREATE INDEX IF NOT EXISTS idx_lothuoc_hansudung ON lothuoc(han_su_dung);
CREATE INDEX IF NOT EXISTS idx_phieuthu_thoigian ON phieuthuvienphi(thoi_gian_thu);

