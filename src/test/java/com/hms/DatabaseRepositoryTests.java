package com.hms;

import com.hms.entity.*;
import com.hms.entity.id.*;
import com.hms.model.*;
import com.hms.repository.*;
import com.hms.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DatabaseRepositoryTests {

    @Autowired
    private ChuyenKhoaRepository chuyenKhoaRepo;

    @Autowired
    private PhongKhamRepository phongKhamRepo;

    @Autowired
    private NhanVienRepository nhanVienRepo;

    @Autowired
    private NhanVienSdtRepository nhanVienSdtRepo;

    @Autowired
    private BacSyRepository bacSyRepo;

    @Autowired
    private BenhNhanRepository benhNhanRepo;

    @Autowired
    private BenhNhanSdtRepository benhNhanSdtRepo;

    @Autowired
    private BaoHiemYTeRepository baoHiemYTeRepo;

    @Autowired
    private ThuocRepository thuocRepo;

    @Autowired
    private LoThuocRepository loThuocRepo;

    @Autowired
    private PatientDbService patientDbService;

    @Autowired
    private StaffDbService staffDbService;

    @Autowired
    private DepartmentRoomDbService departmentRoomDbService;

    @Autowired
    private PharmacyDbService pharmacyDbService;

    @Autowired
    private BillingDbService billingDbService;

    @Autowired
    private EntityModelMapper mapper;

    @Test
    @DisplayName("Verify Specialty and Clinic Room CRUD with foreign key")
    @Transactional
    void testSpecialtyAndRoomRepositories() {
        ChuyenKhoa ck = new ChuyenKhoa("CK_TEST", "Khoa Thử Nghiệm");
        chuyenKhoaRepo.save(ck);

        Optional<ChuyenKhoa> foundCk = chuyenKhoaRepo.findById("CK_TEST");
        assertThat(foundCk).isPresent();
        assertThat(foundCk.get().getTenCk()).isEqualTo("Khoa Thử Nghiệm");

        PhongKham pk = new PhongKham("PK_TEST", ck, "Phòng Khám Thử Nghiệm", "Tầng 1", "Hoạt động");
        phongKhamRepo.save(pk);

        Optional<PhongKham> foundPk = phongKhamRepo.findById("PK_TEST");
        assertThat(foundPk).isPresent();
        assertThat(foundPk.get().getChuyenKhoa().getIdCk()).isEqualTo("CK_TEST");

        List<PhongKham> roomsByCk = phongKhamRepo.findByChuyenKhoaIdCk("CK_TEST");
        assertThat(roomsByCk).hasSize(1);
    }

    @Test
    @DisplayName("Verify Staff and Doctor repository with composite phone number")
    @Transactional
    void testStaffAndDoctorRepositories() {
        NhanVien nv = new NhanVien();
        nv.setIdNv("NV_TEST");
        nv.setHoTen("BS. Test Developer");
        nv.setGioiTinh("Nam");
        nv.setEmail("test.dev@hms.local");
        nv.setVaiTro("Bác sĩ");
        nv.setTrangThai("DANG_LAM");
        nhanVienRepo.save(nv);

        NhanVienSdt sdt = new NhanVienSdt(new NhanVienSdtId("NV_TEST", "0909112233"), nv);
        nhanVienSdtRepo.save(sdt);

        BacSy bs = new BacSy();
        bs.setIdNv("NV_TEST");
        bs.setNhanVien(nv);
        bs.setHocVi("Tiến sĩ Y khoa");
        bs.setSoCchn("CCHN-NV-TEST");
        bacSyRepo.save(bs);

        Optional<Staff> staffModel = staffDbService.findStaffById("NV_TEST");
        assertThat(staffModel).isPresent();
        assertThat(staffModel.get().getVaiTro()).isEqualTo("Bác sĩ");
        assertThat(staffModel.get().getTrangThai()).isEqualTo("DANG_LAM");
        assertThat(staffModel.get().getTrangThaiDisplay()).isEqualTo("Đang làm việc");
        assertThat(staffModel.get().getPhoneNumbers()).contains("0909112233");
    }

    @Test
    @DisplayName("Verify Patient service and composite key operations")
    @Transactional
    void testPatientServiceAndRepositories() {
        Patient patient = new Patient();
        patient.setIdBn("BN_TEST");
        patient.setHoTen("Bệnh Nhân Thử Nghiệm");
        patient.setCccd("001206999999");
        patient.setNgaySinh("1995-10-10");
        patient.setGioiTinh("Nữ");
        patient.setDiaChi("Hà Nội");
        patient.setSdt(List.of("0988000111", "0988000222"));

        Patient saved = patientDbService.savePatient(patient);
        assertThat(saved).isNotNull();

        Optional<Patient> retrieved = patientDbService.findPatientById("BN_TEST");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getHoTen()).isEqualTo("Bệnh Nhân Thử Nghiệm");
        assertThat(retrieved.get().getSdt()).contains("0988000111", "0988000222");
    }

    @Test
    @DisplayName("Verify Pharmacy medicine and lot stock calculation")
    @Transactional
    void testPharmacyRepositories() {
        Thuoc thuoc = new Thuoc();
        thuoc.setIdThuoc("T_TEST");
        thuoc.setTenThuoc("Paracetamol Test 500mg");
        thuoc.setNhaSanXuat("Dược Hậu Giang");
        thuoc.setCanKeDon(false);
        thuoc.setSoLuongTonKho(150);
        thuoc.setDonVi("Viên");
        thuocRepo.save(thuoc);

        LoThuoc lot = new LoThuoc();
        lot.setIdLo("LO_TEST");
        lot.setThuoc(thuoc);
        lot.setSoLo("LOT-2026-01");
        lot.setHanSuDung(LocalDate.now().plusYears(1));
        lot.setSoLuongNhap(150);
        lot.setSoLuongConLai(150);
        lot.setGiaNhap(BigDecimal.valueOf(500));
        lot.setGiaBan(BigDecimal.valueOf(1000));
        loThuocRepo.save(lot);

        Integer sumStock = loThuocRepo.sumStockByMedicine("T_TEST");
        assertThat(sumStock).isEqualTo(150);
    }
}
