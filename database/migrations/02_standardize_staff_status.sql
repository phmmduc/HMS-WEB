-- ==============================================================================
-- Migration: Chuẩn hóa mã trạng thái nhân sự (nhanvien.trang_thai)
-- Đảm bảo tương thích tuyệt đối với CHECK constraint:
-- CHECK (trang_thai IN ('DANG_LAM', 'NGHI_VIEC', 'TAM_NGHI'))
-- ==============================================================================

-- 1. Chuẩn hóa trạng thái đang làm việc
UPDATE nhanvien
SET trang_thai = 'DANG_LAM'
WHERE trang_thai = 'Đang làm việc' OR trang_thai IS NULL;

-- 2. Chuẩn hóa trạng thái tạm nghỉ / nghỉ phép
UPDATE nhanvien
SET trang_thai = 'TAM_NGHI'
WHERE trang_thai IN ('Tạm nghỉ', 'Nghỉ phép');

-- 3. Chuẩn hóa trạng thái nghỉ việc
UPDATE nhanvien
SET trang_thai = 'NGHI_VIEC'
WHERE trang_thai IN ('Nghỉ việc', 'Đã nghỉ việc');

-- 4. Truy vấn xác minh kết quả
SELECT id_nv, ho_ten, vai_tro, trang_thai
FROM nhanvien
ORDER BY id_nv;

