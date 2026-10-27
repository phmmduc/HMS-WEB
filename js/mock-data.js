/* Dữ liệu demo dùng khi chưa cấu hình Supabase. Dữ liệu được sao chép vào localStorage ở lần chạy đầu. */
(function () {
  const today = new Date();
  const iso = (offset = 0) => {
    const d = new Date(today);
    d.setDate(d.getDate() + offset);
    return d.toISOString().slice(0, 10);
  };
  window.HMS_SEED = {
    benh_nhan: [
      { id_bn: 'BN001', ho_ten: 'Nguyễn Minh Anh', cccd: '001206012345', ngay_sinh: '1998-03-14', gioi_tinh: 'Nữ', sdt: ['0901234567'], dia_chi: 'Cầu Giấy, Hà Nội', ngay_tao: iso(0), trang_thai: 'Đang theo dõi' },
      { id_bn: 'BN002', ho_ten: 'Trần Quốc Bảo', cccd: '001199007654', ngay_sinh: '1987-11-02', gioi_tinh: 'Nam', sdt: ['0912345678', '0988123456'], dia_chi: 'Hà Đông, Hà Nội', ngay_tao: iso(-1), trang_thai: 'Ổn định' },
      { id_bn: 'BN003', ho_ten: 'Lê Thu Hà', cccd: '001203004321', ngay_sinh: '2003-07-19', gioi_tinh: 'Nữ', sdt: ['0934567890'], dia_chi: 'Đống Đa, Hà Nội', ngay_tao: iso(-1), trang_thai: 'Ổn định' },
      { id_bn: 'BN004', ho_ten: 'Phạm Đức Long', cccd: '001190009876', ngay_sinh: '1990-12-08', gioi_tinh: 'Nam', sdt: ['0977654321'], dia_chi: 'Thanh Xuân, Hà Nội', ngay_tao: iso(-2), trang_thai: 'Đang theo dõi' },
      { id_bn: 'BN005', ho_ten: 'Đỗ Ngọc Linh', cccd: '001205001122', ngay_sinh: '2005-01-25', gioi_tinh: 'Nữ', sdt: ['0966123456'], dia_chi: 'Long Biên, Hà Nội', ngay_tao: iso(-3), trang_thai: 'Ổn định' },
      { id_bn: 'BN006', ho_ten: 'Vũ Thành Nam', cccd: '001185009911', ngay_sinh: '1985-09-30', gioi_tinh: 'Nam', sdt: ['0944112233'], dia_chi: 'Nam Từ Liêm, Hà Nội', ngay_tao: iso(-4), trang_thai: 'Ổn định' }
    ],
    nguoi_nha: [
      { id_nt: 'NT001', id_bn: 'BN001', ho_ten: 'Nguyễn Văn Minh', moi_quan_he: 'Bố', gioi_tinh: 'Nam', sdt: ['0909988776'] },
      { id_nt: 'NT002', id_bn: 'BN003', ho_ten: 'Lê Thị Hương', moi_quan_he: 'Mẹ', gioi_tinh: 'Nữ', sdt: ['0901234432'] }
    ],
    bao_hiem_yte: [
      { id_bhyt: 'BHYT001', id_bn: 'BN001', so_bhyt: 'HN4010123456789', muc_huong: 80, ngay_bat_dau: iso(-180), ngay_ket_thuc: iso(185), noi_dang_ky: 'Trạm y tế phường Dịch Vọng', nhom_doi_tuong: 'Hộ gia đình' },
      { id_bhyt: 'BHYT002', id_bn: 'BN002', so_bhyt: 'HN4010987654321', muc_huong: 95, ngay_bat_dau: iso(-365), ngay_ket_thuc: iso(30), noi_dang_ky: 'Bệnh viện quận Hà Đông', nhom_doi_tuong: 'Người lao động' },
      { id_bhyt: 'BHYT003', id_bn: 'BN005', so_bhyt: 'HN4010112233445', muc_huong: 100, ngay_bat_dau: iso(-90), ngay_ket_thuc: iso(275), noi_dang_ky: 'Bệnh viện Nhi Trung ương', nhom_doi_tuong: 'Trẻ em' }
    ],
    benh_an: [
      { id_benhan: 'BA001', id_bn: 'BN001', ngay_tao: iso(-60), tien_su_benh: 'Viêm mũi dị ứng theo mùa', trang_thai: 'Đang theo dõi' },
      { id_benhan: 'BA002', id_bn: 'BN002', ngay_tao: iso(-120), tien_su_benh: 'Tăng huyết áp nhẹ', trang_thai: 'Đang theo dõi' },
      { id_benhan: 'BA003', id_bn: 'BN004', ngay_tao: iso(-30), tien_su_benh: 'Chưa ghi nhận', trang_thai: 'Mở' }
    ],
    chuyen_khoa: [
      { id_ck: 'CK01', ten_ck: 'Nội tổng hợp' }, { id_ck: 'CK02', ten_ck: 'Tim mạch' },
      { id_ck: 'CK03', ten_ck: 'Xét nghiệm' }, { id_ck: 'CK04', ten_ck: 'Nhi khoa' }
    ],
    phong_kham: [
      { id_pk: 'PK01', ten_phong: 'Phòng khám Nội tổng hợp', so_phong: 'A-101', trang_thai: 'Hoạt động', id_ck: 'CK01' },
      { id_pk: 'PK02', ten_phong: 'Phòng khám Tim mạch', so_phong: 'A-202', trang_thai: 'Hoạt động', id_ck: 'CK02' },
      { id_pk: 'PK03', ten_phong: 'Phòng xét nghiệm', so_phong: 'B-105', trang_thai: 'Hoạt động', id_ck: 'CK03' },
      { id_pk: 'PK04', ten_phong: 'Phòng khám Nhi', so_phong: 'C-102', trang_thai: 'Hoạt động', id_ck: 'CK04' }
    ],
    nhan_vien: [
      { id_nv: 'NV001', ho_ten: 'BS. Nguyễn Hoàng An', gioi_tinh: 'Nam', ngay_sinh: '1980-02-12', sdt: ['0903001001'], email: 'an.nguyen@hms.local', vai_tro: 'Bác sĩ', trang_thai: 'Đang làm việc' },
      { id_nv: 'NV002', ho_ten: 'BS. Trần Mai Phương', gioi_tinh: 'Nữ', ngay_sinh: '1985-09-21', sdt: ['0903001002'], email: 'phuong.tran@hms.local', vai_tro: 'Bác sĩ', trang_thai: 'Đang làm việc' },
      { id_nv: 'NV003', ho_ten: 'Điều dưỡng Lê Thu Trang', gioi_tinh: 'Nữ', ngay_sinh: '1992-06-07', sdt: ['0903001003'], email: 'trang.le@hms.local', vai_tro: 'Y tá', trang_thai: 'Đang làm việc' },
      { id_nv: 'NV004', ho_ten: 'DS. Phạm Quốc Huy', gioi_tinh: 'Nam', ngay_sinh: '1988-04-18', sdt: ['0903001004'], email: 'huy.pham@hms.local', vai_tro: 'Dược sĩ', trang_thai: 'Đang làm việc' },
      { id_nv: 'NV005', ho_ten: 'Nguyễn Hải Yến', gioi_tinh: 'Nữ', ngay_sinh: '1991-12-03', sdt: ['0903001005'], email: 'yen.nguyen@hms.local', vai_tro: 'Thu ngân', trang_thai: 'Đang làm việc' },
      { id_nv: 'NV006', ho_ten: 'Đỗ Văn Khánh', gioi_tinh: 'Nam', ngay_sinh: '1978-08-13', sdt: ['0903001006'], email: 'khanh.do@hms.local', vai_tro: 'Quản lý', trang_thai: 'Đang làm việc' }
    ],
    bac_sy: [
      { id_nv: 'NV001', hoc_vi: 'Thạc sĩ', so_cchn: 'CCHN-00129', id_ck: 'CK01' },
      { id_nv: 'NV002', hoc_vi: 'Bác sĩ chuyên khoa I', so_cchn: 'CCHN-00817', id_ck: 'CK02' }
    ],
    y_ta: [{ id_nv: 'NV003', cap_bac: 'Điều dưỡng đại học' }],
    duoc_sy: [{ id_nv: 'NV004', bang_cap: 'Dược sĩ đại học' }],
    loai_xet_nghiem: [
      { id_lxn: 'XN001', ten_lxn: 'Công thức máu', chi_phi: 180000, trang_thai: 'Hoạt động' },
      { id_lxn: 'XN002', ten_lxn: 'Đường huyết', chi_phi: 90000, trang_thai: 'Hoạt động' },
      { id_lxn: 'XN003', ten_lxn: 'Chức năng gan', chi_phi: 220000, trang_thai: 'Hoạt động' },
      { id_lxn: 'XN004', ten_lxn: 'Tổng phân tích nước tiểu', chi_phi: 120000, trang_thai: 'Hoạt động' }
    ],
    chan_doan: [
      { id_cd: 'CD001', ma_icd: 'J06.9', ten_benh: 'Nhiễm trùng đường hô hấp trên cấp, không đặc hiệu' },
      { id_cd: 'CD002', ma_icd: 'I10', ten_benh: 'Tăng huyết áp vô căn' },
      { id_cd: 'CD003', ma_icd: 'J30.4', ten_benh: 'Viêm mũi dị ứng, không đặc hiệu' },
      { id_cd: 'CD004', ma_icd: 'R50.9', ten_benh: 'Sốt, không đặc hiệu' },
      { id_cd: 'CD005', ma_icd: 'E11.9', ten_benh: 'Đái tháo đường type 2, không biến chứng' }
    ],
    phieu_kham: [
      { id_phieukham: 'PKM001', id_bn: 'BN001', id_pk: 'PK01', id_nv: 'NV001', id_benhan: 'BA001', id_cd: 'CD003', ngay_dky: iso(0), thoi_gian_kham: '08:15', trieu_chung: 'Hắt hơi, sổ mũi', phi_kham: 150000, trang_thai: 'Đã khám' },
      { id_phieukham: 'PKM002', id_bn: 'BN002', id_pk: 'PK02', id_nv: 'NV002', id_benhan: 'BA002', id_cd: 'CD002', ngay_dky: iso(0), thoi_gian_kham: '09:10', trieu_chung: 'Đau đầu nhẹ, huyết áp cao', phi_kham: 180000, trang_thai: 'Chờ thanh toán' },
      { id_phieukham: 'PKM003', id_bn: 'BN003', id_pk: 'PK01', id_nv: 'NV001', id_benhan: null, id_cd: null, ngay_dky: iso(0), thoi_gian_kham: '', trieu_chung: '', phi_kham: 150000, trang_thai: 'Chờ khám' },
      { id_phieukham: 'PKM004', id_bn: 'BN004', id_pk: 'PK03', id_nv: 'NV002', id_benhan: 'BA003', id_cd: null, ngay_dky: iso(0), thoi_gian_kham: '', trieu_chung: 'Mệt mỏi', phi_kham: 150000, trang_thai: 'Chờ xét nghiệm' },
      { id_phieukham: 'PKM005', id_bn: 'BN005', id_pk: 'PK04', id_nv: 'NV001', id_benhan: null, id_cd: null, ngay_dky: iso(0), thoi_gian_kham: '', trieu_chung: '', phi_kham: 120000, trang_thai: 'Chờ khám' }
    ],
    ket_qua_xet_nghiem: [
      { id_kqxn: 'KQ001', id_phieukham: 'PKM001', id_lxn: 'XN001', ket_qua: 'Các chỉ số trong giới hạn tham chiếu', ket_luan: 'Chưa ghi nhận bất thường', thoi_gian: iso(0), trang_thai: 'Hoàn thành' },
      { id_kqxn: 'KQ002', id_phieukham: 'PKM004', id_lxn: 'XN002', ket_qua: '', ket_luan: '', thoi_gian: '', trang_thai: 'Chờ lấy mẫu' }
    ],
    thuoc: [
      { id_thuoc: 'T001', ten_thuoc: 'Paracetamol 500 mg', nha_san_xuat: 'DHG Pharma', can_ke_don: false, so_luong_ton_kho: 420, don_vi: 'viên' },
      { id_thuoc: 'T002', ten_thuoc: 'Amoxicillin 500 mg', nha_san_xuat: 'Imexpharm', can_ke_don: true, so_luong_ton_kho: 180, don_vi: 'viên' },
      { id_thuoc: 'T003', ten_thuoc: 'Cetirizine 10 mg', nha_san_xuat: 'Stella', can_ke_don: false, so_luong_ton_kho: 95, don_vi: 'viên' },
      { id_thuoc: 'T004', ten_thuoc: 'Amlodipine 5 mg', nha_san_xuat: 'GSK', can_ke_don: true, so_luong_ton_kho: 70, don_vi: 'viên' },
      { id_thuoc: 'T005', ten_thuoc: 'Oresol', nha_san_xuat: 'Pharmedic', can_ke_don: false, so_luong_ton_kho: 24, don_vi: 'gói' }
    ],
    lo_thuoc: [
      { id_lo: 'LO001', id_thuoc: 'T001', so_lo: 'PAR-2608', han_su_dung: iso(220), so_luong_nhap: 500, so_luong_ton: 300, gia_nhap: 450, gia_ban: 800 },
      { id_lo: 'LO002', id_thuoc: 'T001', so_lo: 'PAR-2610', han_su_dung: iso(360), so_luong_nhap: 200, so_luong_ton: 120, gia_nhap: 470, gia_ban: 800 },
      { id_lo: 'LO003', id_thuoc: 'T002', so_lo: 'AMX-2605', han_su_dung: iso(140), so_luong_nhap: 300, so_luong_ton: 180, gia_nhap: 1200, gia_ban: 2000 },
      { id_lo: 'LO004', id_thuoc: 'T003', so_lo: 'CET-2609', han_su_dung: iso(45), so_luong_nhap: 120, so_luong_ton: 95, gia_nhap: 900, gia_ban: 1500 },
      { id_lo: 'LO005', id_thuoc: 'T004', so_lo: 'AML-2604', han_su_dung: iso(310), so_luong_nhap: 100, so_luong_ton: 70, gia_nhap: 1800, gia_ban: 3000 },
      { id_lo: 'LO006', id_thuoc: 'T005', so_lo: 'ORS-2607', han_su_dung: iso(28), so_luong_nhap: 60, so_luong_ton: 24, gia_nhap: 2500, gia_ban: 4000 }
    ],
    don_thuoc: [
      { id_dt: 'DT001', id_phieukham: 'PKM001', id_nv: 'NV001', ngay_ke: iso(0), tong_tien: 23000, ghi_chu: 'Uống sau ăn', trang_thai: 'Chờ cấp phát' },
      { id_dt: 'DT002', id_phieukham: 'PKM002', id_nv: 'NV002', ngay_ke: iso(0), tong_tien: 18000, ghi_chu: '', trang_thai: 'Đã cấp phát' }
    ],
    chi_tiet_don_thuoc: [
      { id_ctdt: 'CT001', id_dt: 'DT001', id_thuoc: 'T003', so_luong: 7, lieu_dung: '1 viên/lần', cach_dung: 'Uống sau ăn tối', ghi_chu: 'Dùng 7 ngày', don_gia: 1500 },
      { id_ctdt: 'CT002', id_dt: 'DT001', id_thuoc: 'T001', so_luong: 20, lieu_dung: '1 viên khi sốt', cach_dung: 'Uống với nước', ghi_chu: 'Không quá liều ghi trên hướng dẫn', don_gia: 800 },
      { id_ctdt: 'CT003', id_dt: 'DT002', id_thuoc: 'T004', so_luong: 6, lieu_dung: '1 viên/ngày', cach_dung: 'Uống buổi sáng', ghi_chu: '', don_gia: 3000 }
    ],
    phieu_thu_vien_phi: [
      { id_hoadon: 'HD001', id_phieukham: 'PKM001', so_tien: 363000, loai_thu: 'Khám + xét nghiệm + thuốc', phuong_thuc_tt: 'Chuyển khoản', thoi_gian_thu: iso(0), trang_thai: 'Đã thanh toán' },
      { id_hoadon: 'HD002', id_phieukham: 'PKM002', so_tien: 198000, loai_thu: 'Khám + thuốc', phuong_thuc_tt: 'Tiền mặt', thoi_gian_thu: '', trang_thai: 'Chưa thanh toán' },
      { id_hoadon: 'HD003', id_phieukham: 'PKM004', so_tien: 370000, loai_thu: 'Khám + xét nghiệm', phuong_thuc_tt: '', thoi_gian_thu: '', trang_thai: 'Chưa thanh toán' }
    ]
  };
})();
