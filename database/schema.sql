-- =============================================================================
-- MediCore HMS (XônPanh HMS) - PostgreSQL Database Schema for Supabase
-- Target Schema specification (lowercase table names without underscores)
-- =============================================================================

-- 1. BỆNH NHÂN & LIÊN QUAN
CREATE TABLE IF NOT EXISTS benhnhan (
    id_bn VARCHAR(10) PRIMARY KEY,
    ho_ten VARCHAR(100) NOT NULL,
    ngay_sinh DATE,
    cccd CHAR(12),
    gioi_tinh VARCHAR(10)
);

CREATE TABLE IF NOT EXISTS benhnhan_sdt (
    id_bn VARCHAR(10) NOT NULL REFERENCES benhnhan(id_bn) ON DELETE CASCADE,
    sdt VARCHAR(15) NOT NULL,
    PRIMARY KEY (id_bn, sdt)
);

CREATE TABLE IF NOT EXISTS nguoithan (
    id_nt VARCHAR(10) PRIMARY KEY,
    id_bn VARCHAR(10) NOT NULL REFERENCES benhnhan(id_bn) ON DELETE CASCADE,
    ho_ten VARCHAR(100),
    moi_quan_he VARCHAR(50),
    gioi_tinh VARCHAR(10)
);

CREATE TABLE IF NOT EXISTS nguoithan_sdt (
    id_nt VARCHAR(10) NOT NULL REFERENCES nguoithan(id_nt) ON DELETE CASCADE,
    sdt VARCHAR(15) NOT NULL,
    PRIMARY KEY (id_nt, sdt)
);

CREATE TABLE IF NOT EXISTS baohiemyte (
    sobhyt VARCHAR(15) PRIMARY KEY,
    id_bn VARCHAR(10) NOT NULL REFERENCES benhnhan(id_bn) ON DELETE CASCADE,
    muc_huong SMALLINT CHECK (muc_huong >= 0 AND muc_huong <= 100),
    nhom_doi_tuong VARCHAR(50),
    noi_dang_ky VARCHAR(150),
    ngay_bat_dau DATE,
    ngay_ket_thuc DATE,
    CHECK (ngay_ket_thuc >= ngay_bat_dau)
);

CREATE TABLE IF NOT EXISTS benhan (
    id_benhan VARCHAR(10) PRIMARY KEY,
    id_bn VARCHAR(10) NOT NULL REFERENCES benhnhan(id_bn) ON DELETE CASCADE,
    ngay_tao DATE,
    tien_su_benh TEXT,
    trang_thai VARCHAR(20) CHECK (trang_thai IN ('DANG_MO', 'DONG'))
);

-- 2. CHUYÊN KHOA, PHÒNG KHÁM & NHÂN SỰ
CREATE TABLE IF NOT EXISTS chuyenkhoa (
    id_ck VARCHAR(10) PRIMARY KEY,
    ten_ck VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS phongkham (
    id_pk VARCHAR(10) PRIMARY KEY,
    id_ck VARCHAR(10) REFERENCES chuyenkhoa(id_ck) ON DELETE SET NULL,
    ten_phong VARCHAR(100),
    so_phong VARCHAR(10) UNIQUE,
    trang_thai VARCHAR(20) CHECK (trang_thai IN ('HOAT_DONG', 'TAM_NGUNG', 'BAO_TRI'))
);

CREATE TABLE IF NOT EXISTS nhanvien (
    id_nv VARCHAR(10) PRIMARY KEY,
    ho_ten VARCHAR(100),
    gioi_tinh VARCHAR(10),
    ngay_sinh DATE,
    vai_tro VARCHAR(20),
    trang_thai VARCHAR(20) CHECK (trang_thai IN ('DANG_LAM', 'NGHI_VIEC', 'TAM_NGHI'))
);

CREATE TABLE IF NOT EXISTS nhanvien_sdt (
    id_nv VARCHAR(10) NOT NULL REFERENCES nhanvien(id_nv) ON DELETE CASCADE,
    sdt VARCHAR(15) NOT NULL,
    PRIMARY KEY (id_nv, sdt)
);

CREATE TABLE IF NOT EXISTS bacsy (
    id_nv VARCHAR(10) PRIMARY KEY REFERENCES nhanvien(id_nv) ON DELETE CASCADE,
    id_ck VARCHAR(10) REFERENCES chuyenkhoa(id_ck) ON DELETE SET NULL,
    hoc_vi VARCHAR(50),
    so_cchn VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS yta (
    id_nv VARCHAR(10) PRIMARY KEY REFERENCES nhanvien(id_nv) ON DELETE CASCADE,
    cap_bac VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS duocsi (
    id_nv VARCHAR(10) PRIMARY KEY REFERENCES nhanvien(id_nv) ON DELETE CASCADE,
    bang_cap VARCHAR(100)
);

-- 3. CHẨN ĐOÁN & XÉT NGHIỆM
CREATE TABLE IF NOT EXISTS chandoan (
    id_icd VARCHAR(10) PRIMARY KEY,
    ten_benh VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS loaixetnghiem (
    id_lxn VARCHAR(10) PRIMARY KEY,
    ten_lxn VARCHAR(100),
    chi_phi DECIMAL(12,2),
    trang_thai VARCHAR(20) CHECK (trang_thai IN ('HOAT_DONG', 'NGUNG'))
);

-- 4. PHIẾU KHÁM & QUAN HỆ
CREATE TABLE IF NOT EXISTS phieukham (
    id_phieukham VARCHAR(15) PRIMARY KEY,
    id_benhan VARCHAR(10) NOT NULL REFERENCES benhan(id_benhan) ON DELETE CASCADE,
    id_pk VARCHAR(10) REFERENCES phongkham(id_pk) ON DELETE SET NULL,
    id_bacsy VARCHAR(10) REFERENCES bacsy(id_nv) ON DELETE SET NULL,
    sobhyt VARCHAR(15) REFERENCES baohiemyte(sobhyt) ON DELETE SET NULL,
    thoi_gian_dky TIMESTAMP,
    thoi_gian_kham TIMESTAMP,
    trieu_chung TEXT,
    phi_kham DECIMAL(12,2),
    trang_thai VARCHAR(20) CHECK (trang_thai IN ('CHO_KHAM', 'DANG_KHAM', 'DA_KHAM', 'HUY'))
);

CREATE TABLE IF NOT EXISTS phieukham_nhanvien (
    id_phieukham VARCHAR(15) NOT NULL REFERENCES phieukham(id_phieukham) ON DELETE CASCADE,
    id_nv VARCHAR(10) NOT NULL REFERENCES nhanvien(id_nv) ON DELETE RESTRICT,
    thoi_gian_tiep_nhan TIMESTAMP,
    PRIMARY KEY (id_phieukham, id_nv)
);

CREATE TABLE IF NOT EXISTS phieukham_chandoan (
    id_phieukham VARCHAR(15) NOT NULL REFERENCES phieukham(id_phieukham) ON DELETE CASCADE,
    id_icd VARCHAR(10) NOT NULL REFERENCES chandoan(id_icd) ON DELETE RESTRICT,
    loai VARCHAR(10) CHECK (loai IN ('CHINH', 'PHU')),
    ghi_chu VARCHAR(255),
    PRIMARY KEY (id_phieukham, id_icd)
);

CREATE TABLE IF NOT EXISTS ketquaxetnghiem (
    id_kqxn VARCHAR(15) PRIMARY KEY,
    id_phieukham VARCHAR(15) NOT NULL REFERENCES phieukham(id_phieukham) ON DELETE CASCADE,
    id_lxn VARCHAR(10) NOT NULL REFERENCES loaixetnghiem(id_lxn) ON DELETE RESTRICT,
    ket_qua TEXT,
    ket_luan TEXT,
    thoi_gian TIMESTAMP,
    trang_thai VARCHAR(20) CHECK (trang_thai IN ('CHO_KET_QUA', 'DA_CO_KET_QUA', 'HUY'))
);

-- 5. THUỐC, LÔ THUỐC & ĐƠN THUỐC
CREATE TABLE IF NOT EXISTS thuoc (
    id_thuoc VARCHAR(10) PRIMARY KEY,
    ten_thuoc VARCHAR(150),
    nha_san_xuat VARCHAR(150),
    can_ke_don BOOLEAN,
    so_luong_ton_kho INTEGER CHECK (so_luong_ton_kho >= 0)
);

CREATE TABLE IF NOT EXISTS lothuoc (
    id_lo VARCHAR(10) PRIMARY KEY,
    id_thuoc VARCHAR(10) NOT NULL REFERENCES thuoc(id_thuoc) ON DELETE RESTRICT,
    so_lo VARCHAR(30),
    han_su_dung DATE,
    so_luong_nhap INTEGER CHECK (so_luong_nhap >= 0),
    so_luong_con INTEGER CHECK (so_luong_con >= 0 AND so_luong_con <= so_luong_nhap),
    gia_nhap DECIMAL(12,2) CHECK (gia_nhap >= 0),
    gia_ban DECIMAL(12,2) CHECK (gia_ban >= 0),
    UNIQUE (id_thuoc, so_lo)
);

CREATE TABLE IF NOT EXISTS donthuoc (
    id_dt VARCHAR(15) PRIMARY KEY,
    id_phieukham VARCHAR(15) NOT NULL REFERENCES phieukham(id_phieukham) ON DELETE CASCADE,
    id_duocsi VARCHAR(10) REFERENCES duocsi(id_nv) ON DELETE SET NULL,
    ngay_ke DATE,
    ghi_chu VARCHAR(255),
    trang_thai VARCHAR(20) CHECK (trang_thai IN ('CHO_CAP_PHAT', 'DA_CAP_PHAT', 'HUY'))
);

CREATE TABLE IF NOT EXISTS chitietdonthuoc (
    id_dt VARCHAR(15) NOT NULL REFERENCES donthuoc(id_dt) ON DELETE CASCADE,
    id_thuoc VARCHAR(10) NOT NULL REFERENCES thuoc(id_thuoc) ON DELETE RESTRICT,
    so_luong INTEGER CHECK (so_luong > 0),
    lieu_dung VARCHAR(100),
    cach_dung VARCHAR(255),
    ghi_chu VARCHAR(255),
    don_gia DECIMAL(12,2) CHECK (don_gia >= 0),
    PRIMARY KEY (id_dt, id_thuoc)
);

-- 6. VIỆN PHÍ & HÓA ĐƠN
CREATE TABLE IF NOT EXISTS phieuthuvienphi (
    id_hoadon VARCHAR(15) PRIMARY KEY,
    id_phieukham VARCHAR(15) NOT NULL REFERENCES phieukham(id_phieukham) ON DELETE CASCADE,
    id_nv VARCHAR(10) NOT NULL REFERENCES nhanvien(id_nv) ON DELETE RESTRICT,
    so_tien DECIMAL(12,2),
    loai_thu VARCHAR(20) CHECK (loai_thu IN ('TAM_UNG', 'THANH_TOAN', 'HOAN_TIEN')),
    phuong_thuc_tt VARCHAR(20) CHECK (phuong_thuc_tt IN ('TIEN_MAT', 'CHUYEN_KHOAN', 'THE')),
    thoi_gian_thu TIMESTAMP,
    trang_thai VARCHAR(20) CHECK (trang_thai IN ('DA_THU', 'HUY'))
);

-- 7. CÁC VIEW TỔNG HỢP
CREATE OR REPLACE VIEW v_donthuoc_tongtien AS
SELECT 
    id_dt, 
    COALESCE(SUM(so_luong * don_gia), 0) AS tong_tien
FROM chitietdonthuoc
GROUP BY id_dt;

CREATE OR REPLACE VIEW v_phieukham_tongquan AS
SELECT 
    pk.id_phieukham,
    pk.id_benhan,
    ba.id_bn,
    bn.ho_ten AS ho_ten_bn,
    pk.id_pk,
    pkh.ten_phong,
    pk.id_bacsy,
    nv_bs.ho_ten AS ho_ten_bacsy,
    pk.thoi_gian_dky,
    pk.thoi_gian_kham,
    pk.phi_kham,
    pk.trang_thai,
    EXISTS (SELECT 1 FROM ketquaxetnghiem k WHERE k.id_phieukham = pk.id_phieukham) AS co_xn,
    EXISTS (SELECT 1 FROM donthuoc d WHERE d.id_phieukham = pk.id_phieukham) AS ke_don
FROM phieukham pk
JOIN benhan ba ON ba.id_benhan = pk.id_benhan
JOIN benhnhan bn ON bn.id_bn = ba.id_bn
LEFT JOIN phongkham pkh ON pkh.id_pk = pk.id_pk
LEFT JOIN bacsy bs ON bs.id_nv = pk.id_bacsy
LEFT JOIN nhanvien nv_bs ON nv_bs.id_nv = bs.id_nv;

