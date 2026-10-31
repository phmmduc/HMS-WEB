package com.hms;

import com.hms.dto.*;
import com.hms.controller.GlobalControllerAdvice;
import com.hms.mock.MockBillingService;
import com.hms.mock.MockDataStore;
import com.hms.mock.MockExaminationService;
import com.hms.mock.MockPharmacyService;
import com.hms.mock.MockStaffService;
import com.hms.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HmsApplicationTests {

    @Autowired
    private MockDataStore dataStore;

    @Autowired
    private MockExaminationService examinationService;

    @Autowired
    private MockStaffService staffService;

    @Autowired
    private MockPharmacyService pharmacyService;

    @Autowired
    private MockBillingService billingService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        dataStore.resetData();
    }

    @Test
    void contextLoads() {
        assertThat(dataStore).isNotNull();
        assertThat(dataStore.examinations).isNotEmpty();
        assertThat(dataStore.patients).isNotEmpty();
        assertThat(dataStore.rooms).isNotEmpty();
        assertThat(dataStore.staffMembers).isNotEmpty();
        assertThat(dataStore.medicines).isNotEmpty();
        assertThat(dataStore.invoices).isNotEmpty();
    }

    @Test
    void testRootUrlReturnsRoleSelectView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/role-select"));
    }

    @Test
    void testAllModulePagesRenderSuccessfully() throws Exception {
        String[] pages = {
                "/dashboard",
                "/registration",
                "/examination",
                "/pharmacy",
                "/billing",
                "/laboratory",
                "/medical-records",
                "/reports",
                "/staff"
        };
        for (String page : pages) {
            mockMvc.perform(get(page))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void testStaffSwitching() throws Exception {
        // Switch to Doctor B (NV002)
        mockMvc.perform(get("/switch-staff/NV002"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        Staff st2 = staffService.getStaffById("NV002");
        assertThat(st2).isNotNull();
        assertThat(st2.getHoTen()).isEqualTo("BS. Trần Mai Phương");
    }

    @Test
    void testRegistrationFlow() throws Exception {
        // Post a valid exam registration
        mockMvc.perform(post("/registration/exam")
                        .param("idBn", "BN001")
                        .param("idPk", "PK01")
                        .param("idNv", "NV001")
                        .param("ngayDky", "2026-10-28")
                        .param("thoiGianKham", "09:00")
                        .param("trieuChung", "Đau đầu, sốt nhẹ")
                        .param("phiKham", "150000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registration?tab=exams"))
                .andExpect(flash().attributeExists("successMessage"));

        // Post invalid registration with missing fields
        mockMvc.perform(post("/registration/exam")
                        .param("idBn", "")
                        .param("idPk", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void testDoctorConsultationAndPrescriptionFlow() throws Exception {
        DoctorExamDto form = new DoctorExamDto();
        form.setIdPhieukham("PKM001");
        form.setIdCd("CD001");
        form.setTrieuChung("Sốt cao 38.5 độ, ho khan, ngực bình thường");
        form.setKetLuan("Theo dõi nhiễm siêu vi đường hô hấp trên");
        form.setMedicineId(List.of("T001", "T003"));
        form.setQuantity(List.of(10, 5));
        form.setDosage(List.of("2 viên/lần", "1 viên/ngày"));
        form.setUsage(List.of("Sau ăn", "Tối"));
        form.setMedicineNote(List.of("Khi sốt > 38.5C", ""));
        form.setRxNgayKe("2026-10-28");
        form.setRxGhiChu("Uống nhiều nước ấm, tái khám sau 3 ngày");

        Examination updated = examinationService.updateDoctorExamination(form);
        assertThat(updated).isNotNull();
        assertThat(updated.getPrescription()).isNotNull();
        assertThat(updated.getPrescription().getItems()).hasSize(2);
        // T001: 800 * 10 = 8000; T003: 1500 * 5 = 7500 => Total Rx = 15500
        assertThat(updated.getPrescription().getTongTien()).isEqualTo(15500L);

        // Verification via MockMvc save endpoint
        mockMvc.perform(post("/examination/save")
                        .param("idPhieukham", "PKM001")
                        .param("idCd", "CD002")
                        .param("trieuChung", "Huyết áp ổn định")
                        .param("medicineId", "T001")
                        .param("quantity", "20"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/examination"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void testStaffCrudAndConstraints() {
        // 1. Add new staff
        StaffFormDto newStaff = new StaffFormDto();
        newStaff.setHoTen("BS. Nguyễn Văn Nghiệm");
        newStaff.setVaiTro("Bác sĩ");
        newStaff.setGioiTinh("Nam");
        newStaff.setPhone("0987654321");
        newStaff.setEmail("nghiem.nguyen@hms.local");
        newStaff.setSpecialtyDetail("Chuyên khoa II");
        Staff created = staffService.createStaff(newStaff);
        assertThat(created.getIdNv()).isNotNull();
        assertThat(staffService.getStaffById(created.getIdNv())).isNotNull();

        // 2. Delete non-referenced staff should succeed
        staffService.deleteStaff(created.getIdNv());
        assertThat(staffService.getStaffById(created.getIdNv())).isNull();

        // 3. Attempting to delete staff referenced by existing examination should fail with constraint exception
        assertThatThrownBy(() -> staffService.deleteStaff("NV001"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Không thể xóa nhân sự NV001");
    }

    @Test
    void testStaffStatusNormalizationAndFlow() throws Exception {
        // Case 1: Thêm nhân viên mới với trạng thái "Đang làm việc" / "DANG_LAM"
        StaffFormDto staff1 = new StaffFormDto();
        staff1.setHoTen("BS. Phạm Minh Đức");
        staff1.setVaiTro("Bác sĩ");
        staff1.setGioiTinh("Nam");
        staff1.setTrangThai("DANG_LAM");
        Staff created1 = staffService.createStaff(staff1);
        assertThat(created1.getTrangThai()).isEqualTo("DANG_LAM");
        assertThat(created1.getTrangThaiDisplay()).isEqualTo("Đang làm việc");
        assertThat(created1.getTrangThaiBadgeClass()).isEqualTo("pill-success");

        // Thêm nhân viên gửi chuỗi tiếng Việt "Đang làm việc" -> tự động chuẩn hóa sang DANG_LAM
        StaffFormDto staff1VN = new StaffFormDto();
        staff1VN.setHoTen("BS. Nguyễn Hoạt Động");
        staff1VN.setVaiTro("Bác sĩ");
        staff1VN.setTrangThai("Đang làm việc");
        Staff created1VN = staffService.createStaff(staff1VN);
        assertThat(created1VN.getTrangThai()).isEqualTo("DANG_LAM");
        assertThat(created1VN.getTrangThaiDisplay()).isEqualTo("Đang làm việc");

        // Case 2: Thêm nhân viên với trạng thái "Tạm nghỉ" / "TAM_NGHI"
        StaffFormDto staff2 = new StaffFormDto();
        staff2.setHoTen("Điều dưỡng Trần Thị Nghỉ Phép");
        staff2.setVaiTro("Y tá");
        staff2.setTrangThai("TAM_NGHI");
        Staff created2 = staffService.createStaff(staff2);
        assertThat(created2.getTrangThai()).isEqualTo("TAM_NGHI");
        assertThat(created2.getTrangThaiDisplay()).isEqualTo("Tạm nghỉ");
        assertThat(created2.getTrangThaiBadgeClass()).isEqualTo("pill-warning");

        // Case 3: Thêm nhân viên với trạng thái "Nghỉ việc" / "NGHI_VIEC"
        StaffFormDto staff3 = new StaffFormDto();
        staff3.setHoTen("DS. Lê Văn Nghỉ");
        staff3.setVaiTro("Dược sĩ");
        staff3.setTrangThai("NGHI_VIEC");
        Staff created3 = staffService.createStaff(staff3);
        assertThat(created3.getTrangThai()).isEqualTo("NGHI_VIEC");
        assertThat(created3.getTrangThaiDisplay()).isEqualTo("Nghỉ việc");

        // Case 4: Chỉnh sửa trạng thái của nhân viên đã tồn tại
        StaffFormDto updateDto = new StaffFormDto();
        updateDto.setIdNv(created1.getIdNv());
        updateDto.setHoTen("BS. Phạm Minh Đức (Cập nhật)");
        updateDto.setVaiTro("Bác sĩ");
        updateDto.setTrangThai("TAM_NGHI");
        Staff updated = staffService.updateStaff(updateDto);
        assertThat(updated.getTrangThai()).isEqualTo("TAM_NGHI");
        assertThat(updated.getTrangThaiDisplay()).isEqualTo("Tạm nghỉ");

        updateDto.setTrangThai("NGHI_VIEC");
        Staff updated2 = staffService.updateStaff(updateDto);
        assertThat(updated2.getTrangThai()).isEqualTo("NGHI_VIEC");
        assertThat(updated2.getTrangThaiDisplay()).isEqualTo("Nghỉ việc");

        // Case 5: Tải lại trang và kiểm tra giao diện /staff hiển thị đúng mã value và text tiếng Việt
        mockMvc.perform(get("/staff"))
                .andExpect(status().isOk())
                .andExpect(view().name("staff/index"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("DANG_LAM")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("TAM_NGHI")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("NGHI_VIEC")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Đang làm việc")));

        // Dọn dẹp dữ liệu test
        staffService.deleteStaff(created1.getIdNv());
        staffService.deleteStaff(created1VN.getIdNv());
        staffService.deleteStaff(created2.getIdNv());
        staffService.deleteStaff(created3.getIdNv());
    }

    @Test
    void testSpecialtyAndRoomCrudAndConstraints() {
        // 1. Add specialty
        Specialty sp = new Specialty(null, "Răng Hàm Mặt");
        Specialty createdSp = staffService.createSpecialty(sp);
        assertThat(createdSp.getIdCk()).isNotNull();

        // 2. Add room
        ClinicRoom room = new ClinicRoom(null, "Phòng Răng Hàm Mặt", "C-301", "Hoạt động", createdSp.getIdCk());
        ClinicRoom createdRoom = staffService.createRoom(room);
        assertThat(createdRoom.getIdPk()).isNotNull();
        assertThat(createdRoom.getTenChuyenKhoa()).isEqualTo("Răng Hàm Mặt");

        // 3. Deleting specialty while room references it should fail
        assertThatThrownBy(() -> staffService.deleteSpecialty(createdSp.getIdCk()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Không thể xóa chuyên khoa");

        // 4. Deleting room without exams should succeed
        staffService.deleteRoom(createdRoom.getIdPk());
        assertThat(staffService.getAllRooms().stream().noneMatch(r -> r.getIdPk().equals(createdRoom.getIdPk()))).isTrue();

        // 5. Specialty can now be deleted
        staffService.deleteSpecialty(createdSp.getIdCk());
        assertThat(staffService.getAllSpecialties().stream().noneMatch(s -> s.getIdCk().equals(createdSp.getIdCk()))).isTrue();

        // 6. Deleting room with existing exam (PK01) must fail
        assertThatThrownBy(() -> staffService.deleteRoom("PK01"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Không thể xóa phòng khám PK01");
    }

    @Test
    void testPharmacyDispenseAndBillingPayFlow() throws Exception {
        // Dispense prescription DT001
        mockMvc.perform(post("/pharmacy/dispense")
                        .param("idDt", "DT001"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pharmacy?tab=prescriptions"))
                .andExpect(flash().attributeExists("successMessage"));

        // Pay invoice HD002
        mockMvc.perform(post("/billing/pay")
                        .param("idHoadon", "HD002")
                        .param("phuongThucTt", "Tiền mặt")
                        .param("thoiGianThu", "2026-10-28 10:15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/billing"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void testMedicineManagementCrudAndBusinessRules() throws Exception {
        // 1. Thêm thuốc mới hợp lệ
        MedicineFormDto dto = new MedicineFormDto();
        dto.setTenThuoc("Cefuroxime 500mg");
        dto.setHoatChat("Cefuroxime axetil");
        dto.setHamLuong("500 mg");
        dto.setDonVi("viên");
        dto.setGiaBan(12000L);
        dto.setNhaSanXuat("Hậu Giang Pharma");
        dto.setCanKeDon(true);

        Medicine created = pharmacyService.createMedicine(dto);
        assertThat(created).isNotNull();
        assertThat(created.getIdThuoc()).isNotNull();
        assertThat(created.getSoLuongTonKho()).isEqualTo(0);
        assertThat(created.getTrangThai()).isEqualTo("Đang kinh doanh");

        // 2. Không được để trống tên thuốc hoặc đơn vị
        MedicineFormDto invalidDto = new MedicineFormDto();
        invalidDto.setTenThuoc("");
        assertThatThrownBy(() -> pharmacyService.createMedicine(invalidDto))
                .isInstanceOf(IllegalArgumentException.class);

        // 3. Xóa thuốc chưa có lô và chưa có đơn (created thuốc mới) -> xóa vật lý thành công
        String deleteMsg = pharmacyService.deleteMedicine(created.getIdThuoc());
        assertThat(deleteMsg).contains("Đã xóa hoàn toàn thuốc");
        assertThat(pharmacyService.getMedicineById(created.getIdThuoc())).isNull();

        // 4. Xóa thuốc đang có lô và đơn thuốc (T001) -> không xóa vật lý mà chuyển sang 'Ngừng kinh doanh'
        String preventMsg = pharmacyService.deleteMedicine("T001");
        assertThat(preventMsg).contains("Ngừng kinh doanh");
        Medicine t001 = pharmacyService.getMedicineById("T001");
        assertThat(t001).isNotNull();
        assertThat(t001.isDiscontinued()).isTrue();

        // 5. Kiểm tra qua MockMvc
        mockMvc.perform(post("/pharmacy/medicine/create")
                        .param("tenThuoc", "Berberin 50mg")
                        .param("donVi", "viên")
                        .param("giaBan", "500"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pharmacy?tab=medicines"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void testMedicineLotImportAndStockSync() throws Exception {
        // T006 ban đầu có tồn kho = 0
        Medicine t006 = pharmacyService.getMedicineById("T006");
        assertThat(t006.getSoLuongTonKho()).isEqualTo(0);

        // 1. Nhập lô mới cho T006
        MedicineLotFormDto lotDto = new MedicineLotFormDto();
        lotDto.setIdThuoc("T006");
        lotDto.setSoLo("VTC-2026-X1");
        lotDto.setSoLuongNhap(150);
        lotDto.setGiaNhap(800L);
        lotDto.setGiaBan(1200L);
        lotDto.setNgayNhap("2026-10-28");
        lotDto.setHanSuDung("2027-10-28");
        lotDto.setNhaCungCap("Dược TW1");

        MedicineLot lot = pharmacyService.createLot(lotDto);
        assertThat(lot).isNotNull();
        assertThat(lot.getIdLo()).isNotNull();
        assertThat(lot.getSoLuongTon()).isEqualTo(150);

        // Tồn kho T006 tự động cập nhật lên 150
        assertThat(t006.getSoLuongTonKho()).isEqualTo(150);

        // 2. Không cho phép trùng số lô
        assertThatThrownBy(() -> pharmacyService.createLot(lotDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("đã tồn tại");

        // 3. Không cho phép hạn sử dụng trước hoặc bằng ngày nhập
        MedicineLotFormDto badDateDto = new MedicineLotFormDto();
        badDateDto.setIdThuoc("T006");
        badDateDto.setSoLo("VTC-BAD-DATE");
        badDateDto.setSoLuongNhap(50);
        badDateDto.setNgayNhap("2026-10-28");
        badDateDto.setHanSuDung("2026-10-10"); // HSD trước ngày nhập
        assertThatThrownBy(() -> pharmacyService.createLot(badDateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("phải sau ngày nhập");

        // 4. MockMvc kiểm tra endpoint POST /pharmacy/lot/create
        mockMvc.perform(post("/pharmacy/lot/create")
                        .param("idThuoc", "T006")
                        .param("soLo", "VTC-MVC-01")
                        .param("soLuongNhap", "50")
                        .param("giaNhap", "700")
                        .param("hanSuDung", "2027-05-15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pharmacy?tab=lots"))
                .andExpect(flash().attributeExists("successMessage"));

        assertThat(t006.getSoLuongTonKho()).isEqualTo(200); // 150 + 50
    }

    @Test
    void testDeleteExpiredLotRules() throws Exception {
        // LO001 còn hạn đến tương lai -> Không được phép loại bỏ
        MedicineLot validLot = pharmacyService.getLotById("LO001");
        assertThat(validLot.isExpired()).isFalse();
        assertThatThrownBy(() -> pharmacyService.deleteExpiredLot("LO001"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("còn hạn sử dụng");

        // LO008 là lô hết hạn (AMX-2510), chưa từng xuất kho (soLuongTon = soLuongNhap = 50)
        MedicineLot expLot008 = pharmacyService.getLotById("LO008");
        assertThat(expLot008.isExpired()).isTrue();
        String msg008 = pharmacyService.deleteExpiredLot("LO008");
        assertThat(msg008).contains("Đã loại bỏ lô thuốc hết hạn");
        assertThat(pharmacyService.getLotById("LO008")).isNull();

        // LO007 là lô hết hạn (PAR-2512), đã từng cấp phát (soLuongNhap = 100, soLuongTon = 15)
        MedicineLot expLot007 = pharmacyService.getLotById("LO007");
        assertThat(expLot007.isExpired()).isTrue();
        String msg007 = pharmacyService.deleteExpiredLot("LO007");
        assertThat(msg007).contains("Đã loại bỏ lô thuốc hết hạn");
        // Kiểm tra bảo toàn lịch sử: trạng thái Đã loại bỏ, tồn = 0
        MedicineLot checked007 = pharmacyService.getLotById("LO007");
        assertThat(checked007).isNotNull();
        assertThat(checked007.isRemoved()).isTrue();
        assertThat(checked007.getSoLuongTon()).isEqualTo(0);

        // Kiểm tra qua MockMvc
        mockMvc.perform(post("/pharmacy/lot/delete")
                        .param("idLo", "LO007"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pharmacy?tab=lots"));
    }

    @Test
    void testBillingRevenueReportAndInteractiveDrillDown() throws Exception {
        // 1. Báo cáo theo tháng 10/2026: Trục hoành có đủ 31 ngày
        RevenueReportDto monthReport = billingService.getRevenueReport("month", 2026, 10, null, null);
        assertThat(monthReport).isNotNull();
        assertThat(monthReport.getChartLabels()).hasSize(31);
        assertThat(monthReport.getTotalPaid()).isGreaterThan(0L);

        // Tổng doanh thu tháng = tổng các cột ngày
        long sumPaidMonth = monthReport.getChartPaidData().stream().mapToLong(Long::longValue).sum();
        assertThat(monthReport.getTotalPaid()).isEqualTo(sumPaidMonth);

        // 2. Click drill-down vào ngày cụ thể (ngày hôm nay có hóa đơn HD001 đã thanh toán)
        String todayStr = LocalDate.now().toString();
        RevenueReportDto dayClickReport = billingService.getRevenueReport("month", LocalDate.now().getYear(), LocalDate.now().getMonthValue(), null, todayStr);
        assertThat(dayClickReport.getSelectedKey()).isEqualTo(todayStr);
        List<Invoice> dayInvoices = billingService.filterInvoices("", "all", "month", LocalDate.now().getYear(), LocalDate.now().getMonthValue(), null, todayStr);
        assertThat(dayInvoices).isNotEmpty();
        for (Invoice inv : dayInvoices) {
            assertThat(inv.getEffectiveDate()).isEqualTo(todayStr);
        }

        // 3. Báo cáo theo năm 2026: Trục hoành có đủ 12 tháng
        RevenueReportDto yearReport = billingService.getRevenueReport("year", LocalDate.now().getYear(), null, null, null);
        assertThat(yearReport.getChartLabels()).hasSize(12);
        long sumPaidYear = yearReport.getChartPaidData().stream().mapToLong(Long::longValue).sum();
        assertThat(yearReport.getTotalPaid()).isEqualTo(sumPaidYear);

        // 4. MockMvc kiểm tra trang /billing
        mockMvc.perform(get("/billing")
                        .param("view", "month")
                        .param("year", String.valueOf(LocalDate.now().getYear()))
                        .param("month", String.valueOf(LocalDate.now().getMonthValue()))
                        .param("selectedKey", todayStr))
                .andExpect(status().isOk())
                .andExpect(view().name("billing/index"))
                .andExpect(model().attributeExists("report"))
                .andExpect(model().attributeExists("invoices"));
    }

    @Test
    void testMedicalRecordsExaminationDetailModal() throws Exception {
        // Kiểm tra xem trang hồ sơ bệnh án BN001 hiển thị đủ modal chi tiết phiếu khám với 5 mục A, B, C, D, E
        mockMvc.perform(get("/medical-records")
                        .param("selectedId", "BN001"))
                .andExpect(status().isOk())
                .andExpect(view().name("medical-records/index"))
                .andExpect(model().attributeExists("patients"))
                .andExpect(model().attributeExists("selectedPatient"))
                .andExpect(model().attributeExists("records"))
                .andExpect(model().attributeExists("exams"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Chi tiết phiếu khám bệnh")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("A. Thông tin hành chính lượt khám")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("B. Thông tin bệnh nhân tại thời điểm khám")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("C. Kết quả khám lâm sàng & Cận lâm sàng")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("D. Đơn thuốc đính kèm lượt khám")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("E. Bệnh án liên kết & Tiền sử bệnh")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("modal-exam-PKM001")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Paracetamol")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Công thức máu")));
    }

    @Test
    void testDoctorScopeRestrictionAndStaffSwitching() throws Exception {
        Staff docA = dataStore.staffMembers.get("NV001"); // BS. Nguyễn Hoàng An (PK01)
        Staff docB = dataStore.staffMembers.get("NV002"); // BS. Trần Mai Phương (PK02)
        assertThat(docA).isNotNull();
        assertThat(docB).isNotNull();
        assertThat(docA.getIdPk()).isEqualTo("PK01");
        assertThat(docB.getIdPk()).isEqualTo("PK02");

        // 1. Doctor A chỉ thấy bệnh nhân liên quan đến mình và PK01
        List<Patient> patientsA = examinationService.getExaminationsForStaff(docA, null, null, null)
                .stream().map(ex -> dataStore.patients.get(ex.getIdBn())).distinct().toList();
        List<Examination> examsA = examinationService.getExaminationsForStaff(docA, null, null, null);
        assertThat(examsA).allMatch(ex -> "NV001".equals(ex.getIdNv()) || "PK01".equals(ex.getIdPk()));
        // Không chứa phiếu của Bác sĩ B thuộc PK02
        assertThat(examsA).noneMatch(ex -> "PKM002".equals(ex.getIdPhieukham()) || "PKM004".equals(ex.getIdPhieukham()));

        // 2. Doctor B chỉ thấy bệnh nhân liên quan đến mình và PK02
        List<Examination> examsB = examinationService.getExaminationsForStaff(docB, null, null, null);
        assertThat(examsB).allMatch(ex -> "NV002".equals(ex.getIdNv()) || "PK02".equals(ex.getIdPk()));
        // Không chứa phiếu của Bác sĩ A thuộc PK01
        assertThat(examsB).noneMatch(ex -> "PKM001".equals(ex.getIdPhieukham()) || "PKM003".equals(ex.getIdPhieukham()));

        // 3. MockMvc truy cập /examination với session của BS A (Role = DOCTOR)
        mockMvc.perform(get("/examination")
                        .sessionAttr(com.hms.controller.GlobalControllerAdvice.SESSION_ROLE, Role.DOCTOR)
                        .sessionAttr(com.hms.controller.GlobalControllerAdvice.SESSION_STAFF, docA))
                .andExpect(status().isOk())
                .andExpect(view().name("examination/index"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("PKM001")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("PKM002"))));

        // 4. Bác sĩ A không được phép mở chi tiết phiếu khám của Bác sĩ B (PKM002)
        mockMvc.perform(get("/examination/detail/PKM002")
                        .sessionAttr(com.hms.controller.GlobalControllerAdvice.SESSION_ROLE, Role.DOCTOR)
                        .sessionAttr(com.hms.controller.GlobalControllerAdvice.SESSION_STAFF, docA))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/examination"))
                .andExpect(flash().attributeExists("errorMessage"));

        // 5. Bác sĩ B mở được phiếu khám của mình (PKM002)
        mockMvc.perform(get("/examination/detail/PKM002")
                        .sessionAttr(com.hms.controller.GlobalControllerAdvice.SESSION_ROLE, Role.DOCTOR)
                        .sessionAttr(com.hms.controller.GlobalControllerAdvice.SESSION_STAFF, docB))
                .andExpect(status().isOk())
                .andExpect(view().name("examination/detail"));
    }

    @Test
    void testRichMockDataCoverage() {
        // Kiểm tra bác sĩ mới NV011
        Staff docNam = dataStore.staffMembers.get("NV011");
        assertThat(docNam).isNotNull();
        assertThat(docNam.getHoTen()).isEqualTo("BS. Lê Hoàng Nam");
        assertThat(docNam.getSpecialtyDetail()).contains("Nhi khoa");
        assertThat(docNam.getIdPk()).isEqualTo("PK04");

        // Kiểm tra bệnh nhân mới BN007
        Patient bn007 = dataStore.patients.get("BN007");
        assertThat(bn007).isNotNull();
        assertThat(bn007.getHoTen()).isEqualTo("Hoàng Thu Trang");
        assertThat(bn007.getInsurance()).isNotNull();
        assertThat(bn007.getInsurance().getMucHuong()).isEqualTo(80);

        // Kiểm tra bệnh nhân có nhiều lượt khám
        List<Examination> bn001Exams = dataStore.examinations.values().stream()
                .filter(e -> "BN001".equals(e.getIdBn())).toList();
        assertThat(bn001Exams.size()).isGreaterThanOrEqualTo(2);

        List<Examination> bn002Exams = dataStore.examinations.values().stream()
                .filter(e -> "BN002".equals(e.getIdBn())).toList();
        assertThat(bn002Exams.size()).isGreaterThanOrEqualTo(2);

        // Kiểm tra các trạng thái phiếu khám khác nhau
        long completed = dataStore.examinations.values().stream().filter(e -> "Hoàn tất".equals(e.getTrangThai())).count();
        long examined = dataStore.examinations.values().stream().filter(e -> "Đã khám".equals(e.getTrangThai())).count();
        long waiting = dataStore.examinations.values().stream().filter(e -> "Chờ khám".equals(e.getTrangThai())).count();
        assertThat(completed).isGreaterThan(0);
        assertThat(examined).isGreaterThan(0);
        assertThat(waiting).isGreaterThan(0);

        // Kiểm tra phiếu có xét nghiệm vs không có xét nghiệm
        boolean hasLab = dataStore.examinations.values().stream().anyMatch(e -> !e.getLabResults().isEmpty());
        boolean noLab = dataStore.examinations.values().stream().anyMatch(e -> e.getLabResults().isEmpty());
        assertThat(hasLab).isTrue();
        assertThat(noLab).isTrue();

        // Kiểm tra phiếu có đơn thuốc vs không có đơn thuốc
        boolean hasPresc = dataStore.examinations.values().stream().anyMatch(Examination::hasPrescription);
        boolean noPresc = dataStore.examinations.values().stream().anyMatch(e -> !e.hasPrescription());
        assertThat(hasPresc).isTrue();
        assertThat(noPresc).isTrue();
    }

    @Test
    void testDoctorFilteringByRoomService() {
        // PK01 has NV001
        List<Staff> pk01Doctors = staffService.getDoctorsByRoom("PK01");
        assertThat(pk01Doctors).extracting(Staff::getIdNv).containsExactly("NV001");

        // PK02 has NV002
        List<Staff> pk02Doctors = staffService.getDoctorsByRoom("PK02");
        assertThat(pk02Doctors).extracting(Staff::getIdNv).containsExactly("NV002");

        // PK03 (Lab) has no doctor
        List<Staff> pk03Doctors = staffService.getDoctorsByRoom("PK03");
        assertThat(pk03Doctors).isEmpty();

        // PK04 (Pediatrics) has NV011
        List<Staff> pk04Doctors = staffService.getDoctorsByRoom("PK04");
        assertThat(pk04Doctors).extracting(Staff::getIdNv).containsExactly("NV011");

        // Null/empty room
        assertThat(staffService.getDoctorsByRoom(null)).isEmpty();
        assertThat(staffService.getDoctorsByRoom("")).isEmpty();
    }

    @Test
    void testDoctorFilteringInRegistrationTemplate() throws Exception {
        var mvcResult = mockMvc.perform(get("/registration"))
                .andExpect(status().isOk())
                .andReturn();

        String html = mvcResult.getResponse().getContentAsString();
        // Check element IDs
        assertThat(html).contains("id=\"exam-room-select\"");
        assertThat(html).contains("id=\"exam-doctor-select\"");
        // Check data-room-id attributes for doctors
        assertThat(html).contains("data-room-id=\"PK01\"");
        assertThat(html).contains("data-room-id=\"PK02\"");
        assertThat(html).contains("data-room-id=\"PK04\"");
    }

    @Test
    void testDoctorsByRoomRestEndpoint() throws Exception {
        mockMvc.perform(get("/registration/doctors-by-room").param("roomId", "PK01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idNv").value("NV001"))
                .andExpect(jsonPath("$[0].hoTen").value("BS. Nguyễn Hoàng An"));

        mockMvc.perform(get("/registration/doctors-by-room").param("roomId", "PK03"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    void testRegistrationDoctorRoomValidationRejectMismatch() throws Exception {
        // NV001 belongs to PK01, attempt to assign to PK02 (Tim mạch)
        mockMvc.perform(post("/registration/exam")
                        .param("idBn", "BN001")
                        .param("idPk", "PK02")
                        .param("idNv", "NV001")
                        .param("ngayDky", "2026-10-28")
                        .param("thoiGianKham", "09:30")
                        .param("phiKham", "150000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registration?tab=exams"))
                .andExpect(flash().attributeExists("errorMessage"));

        // Verify that exam was NOT created
        boolean exists = dataStore.examinations.values().stream()
                .anyMatch(ex -> "PK02".equals(ex.getIdPk()) && "NV001".equals(ex.getIdNv()));
        assertThat(exists).isFalse();
    }

    @Test
    void testRegistrationDoctorRoomValidationRejectRoomWithNoDoctor() throws Exception {
        // PK03 has no doctor, attempting to register exam with any doctor should fail
        mockMvc.perform(post("/registration/exam")
                        .param("idBn", "BN001")
                        .param("idPk", "PK03")
                        .param("idNv", "NV001"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registration?tab=exams"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void testRegistrationDoctorRoomValidationSuccessMatch() throws Exception {
        // NV002 belongs to PK02, valid assignment
        mockMvc.perform(post("/registration/exam")
                        .param("idBn", "BN002")
                        .param("idPk", "PK02")
                        .param("idNv", "NV002")
                        .param("ngayDky", "2026-10-28")
                        .param("thoiGianKham", "10:00")
                        .param("phiKham", "150000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registration?tab=exams"))
                .andExpect(flash().attributeExists("successMessage"));

        // Verify that exam WAS created
        boolean exists = dataStore.examinations.values().stream()
                .anyMatch(ex -> "PK02".equals(ex.getIdPk()) && "NV002".equals(ex.getIdNv()) && "BN002".equals(ex.getIdBn()));
        assertThat(exists).isTrue();
    }

    @Test
    void testDoctorReassignmentSync() throws Exception {
        // Reassign NV001 to PK02
        StaffFormDto dto = new StaffFormDto();
        dto.setIdNv("NV001");
        dto.setHoTen("BS. Nguyễn Hoàng An");
        dto.setIdPk("PK02");
        staffService.updateStaff(dto);

        // Now PK02 should have NV001
        List<Staff> pk02Doctors = staffService.getDoctorsByRoom("PK02");
        assertThat(pk02Doctors).extracting(Staff::getIdNv).contains("NV001", "NV002");

        // Now creating exam with PK02 and NV001 should succeed!
        mockMvc.perform(post("/registration/exam")
                        .param("idBn", "BN003")
                        .param("idPk", "PK02")
                        .param("idNv", "NV001")
                        .param("ngayDky", "2026-10-28")
                        .param("thoiGianKham", "10:30")
                        .param("phiKham", "150000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registration?tab=exams"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    // ==========================================
    // CHỨC NĂNG A: QUẢN LÝ LOẠI XÉT NGHIỆM CHO Y TÁ
    // ==========================================

    @Test
    void testLaboratoryTypesViewRendersCorrectly() throws Exception {
        var mvcResult = mockMvc.perform(get("/laboratory/types"))
                .andExpect(status().isOk())
                .andExpect(view().name("laboratory/types"))
                .andExpect(model().attributeExists("labTypes"))
                .andExpect(model().attributeExists("totalCount"))
                .andExpect(model().attributeExists("activeCount"))
                .andExpect(model().attributeExists("inactiveCount"))
                .andReturn();

        String html = mvcResult.getResponse().getContentAsString();
        assertThat(html).contains("Quản lý loại xét nghiệm");
        assertThat(html).contains("id=\"modal-add-lab-type\"");
        assertThat(html).contains("XN001");
        assertThat(html).contains("Công thức máu");
    }

    @Test
    void testAddLabTypeSuccessAndSyncWithExamination() throws Exception {
        // 1. Add new lab type XN005
        mockMvc.perform(post("/laboratory/types/create")
                        .param("idLxn", "XN005")
                        .param("tenLxn", "Đo điện tim (ECG)")
                        .param("chiPhi", "150000")
                        .param("trangThai", "Hoạt động"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/laboratory/types"))
                .andExpect(flash().attributeExists("successMessage"));

        // 2. Verify stored in MockDataStore
        LabType created = staffService.getLabTypeById("XN005");
        assertThat(created).isNotNull();
        assertThat(created.getTenLxn()).isEqualTo("Đo điện tim (ECG)");
        assertThat(created.getChiPhi()).isEqualTo(150000L);
        assertThat(created.getTrangThai()).isEqualTo("Hoạt động");

        // 3. Verify it is present in active lab types
        List<LabType> activeLabs = staffService.getActiveLabTypes();
        assertThat(activeLabs).extracting(LabType::getIdLxn).contains("XN005");

        // 4. Verify it synchronizes into doctor's consultation order form at /examination/detail/PKM001
        var examResult = mockMvc.perform(get("/examination/detail/PKM001"))
                .andExpect(status().isOk())
                .andReturn();
        String examHtml = examResult.getResponse().getContentAsString();
        assertThat(examHtml).contains("Đo điện tim (ECG)");
        assertThat(examHtml).contains("value=\"XN005\"");
    }

    @Test
    void testAddLabTypeDuplicateIdRejection() throws Exception {
        // Attempt to create lab type with existing ID XN001
        mockMvc.perform(post("/laboratory/types/create")
                        .param("idLxn", "XN001")
                        .param("tenLxn", "Trùng mã xét nghiệm")
                        .param("chiPhi", "200000")
                        .param("trangThai", "Hoạt động"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/laboratory/types"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void testDeleteLabTypeReferencedRejection() throws Exception {
        // XN001 is referenced in labResults (KQ001, KQ004)
        mockMvc.perform(post("/laboratory/types/delete")
                        .param("idLxn", "XN001"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/laboratory/types"))
                .andExpect(flash().attributeExists("errorMessage"));

        // Must NOT be deleted
        assertThat(staffService.getLabTypeById("XN001")).isNotNull();
    }

    @Test
    void testDeleteLabTypeUnreferencedSuccess() throws Exception {
        // XN004 is NOT referenced in any lab result
        mockMvc.perform(post("/laboratory/types/delete")
                        .param("idLxn", "XN004"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/laboratory/types"))
                .andExpect(flash().attributeExists("successMessage"));

        // Must be deleted
        assertThat(staffService.getLabTypeById("XN004")).isNull();
    }

    @Test
    void testToggleLabTypeStatus() throws Exception {
        // Toggle XN002 from "Hoạt động" to "Ngừng hoạt động"
        mockMvc.perform(post("/laboratory/types/toggle")
                        .param("idLxn", "XN002"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/laboratory/types"))
                .andExpect(flash().attributeExists("successMessage"));

        LabType lt = staffService.getLabTypeById("XN002");
        assertThat(lt.getTrangThai()).isEqualTo("Ngừng hoạt động");

        // Active list should not contain XN002
        assertThat(staffService.getActiveLabTypes()).extracting(LabType::getIdLxn).doesNotContain("XN002");
    }

    // ==========================================
    // CHỨC NĂNG B: LUỒNG CHỌN VAI TRÒ -> CHỌN NHÂN SỰ
    // ==========================================

    @Test
    void testRoleSelectionRedirectsToStaffSelection() throws Exception {
        // 1. Doctor role selection redirects to /select-staff?role=doctor
        mockMvc.perform(get("/select-role/doctor"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/select-staff?role=doctor"));

        // 2. Nurse role selection redirects to /select-staff?role=nurse
        mockMvc.perform(get("/select-role/nurse"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/select-staff?role=nurse"));

        // 3. Manager role selection redirects to /select-staff?role=manager
        mockMvc.perform(get("/select-role/manager"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/select-staff?role=manager"));

        // 4. Pharmacist role selection redirects to /select-staff?role=pharmacist
        mockMvc.perform(get("/select-role/pharmacist"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/select-staff?role=pharmacist"));

        // 5. Cashier role selection redirects to /select-staff?role=cashier
        mockMvc.perform(get("/select-role/cashier"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/select-staff?role=cashier"));
    }

    @Test
    void testStaffSelectionPageFiltersByRoleCorrectly() throws Exception {
        // A. Doctor role: only doctors shown
        var docResult = mockMvc.perform(get("/select-staff").param("role", "doctor"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/staff-select"))
                .andExpect(model().attributeExists("staffList"))
                .andReturn();
        @SuppressWarnings("unchecked")
        List<Staff> doctors = (List<Staff>) docResult.getModelAndView().getModel().get("staffList");
        assertThat(doctors).isNotEmpty();
        assertThat(doctors).allMatch(s -> "Bác sĩ".equalsIgnoreCase(s.getVaiTro()));
        assertThat(doctors).extracting(Staff::getIdNv).contains("NV001", "NV002", "NV011");

        // B. Nurse role: only nurses shown
        var nurseResult = mockMvc.perform(get("/select-staff").param("role", "nurse"))
                .andExpect(status().isOk())
                .andReturn();
        @SuppressWarnings("unchecked")
        List<Staff> nurses = (List<Staff>) nurseResult.getModelAndView().getModel().get("staffList");
        assertThat(nurses).isNotEmpty();
        assertThat(nurses).allMatch(s -> "Y tá".equalsIgnoreCase(s.getVaiTro()));
        assertThat(nurses).extracting(Staff::getIdNv).contains("NV003", "NV007");

        // C. Pharmacist role: only pharmacists shown
        var rxResult = mockMvc.perform(get("/select-staff").param("role", "pharmacist"))
                .andExpect(status().isOk())
                .andReturn();
        @SuppressWarnings("unchecked")
        List<Staff> pharmacists = (List<Staff>) rxResult.getModelAndView().getModel().get("staffList");
        assertThat(pharmacists).isNotEmpty();
        assertThat(pharmacists).allMatch(s -> "Dược sĩ".equalsIgnoreCase(s.getVaiTro()));
        assertThat(pharmacists).extracting(Staff::getIdNv).contains("NV004", "NV008");

        // D. Cashier role: only cashiers shown
        var cashResult = mockMvc.perform(get("/select-staff").param("role", "cashier"))
                .andExpect(status().isOk())
                .andReturn();
        @SuppressWarnings("unchecked")
        List<Staff> cashiers = (List<Staff>) cashResult.getModelAndView().getModel().get("staffList");
        assertThat(cashiers).isNotEmpty();
        assertThat(cashiers).allMatch(s -> "Thu ngân".equalsIgnoreCase(s.getVaiTro()));
        assertThat(cashiers).extracting(Staff::getIdNv).contains("NV005", "NV009");

        // E. Manager role: only managers shown
        var mgrResult = mockMvc.perform(get("/select-staff").param("role", "manager"))
                .andExpect(status().isOk())
                .andReturn();
        @SuppressWarnings("unchecked")
        List<Staff> managers = (List<Staff>) mgrResult.getModelAndView().getModel().get("staffList");
        assertThat(managers).isNotEmpty();
        assertThat(managers).allMatch(s -> "Quản lý".equalsIgnoreCase(s.getVaiTro()));
        assertThat(managers).extracting(Staff::getIdNv).contains("NV006", "NV010");
    }

    @Test
    void testChooseStaffSetsSessionAndRedirectsToRoleWorkspace() throws Exception {
        // Choose Doctor NV001 -> should redirect to /examination
        mockMvc.perform(get("/choose-staff/NV001"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/examination"))
                .andExpect(request().sessionAttribute(GlobalControllerAdvice.SESSION_ROLE, Role.DOCTOR))
                .andExpect(request().sessionAttribute(GlobalControllerAdvice.SESSION_STAFF, staffService.getStaffById("NV001")));

        // Choose Nurse NV003 -> should redirect to /registration
        mockMvc.perform(get("/choose-staff/NV003"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registration"))
                .andExpect(request().sessionAttribute(GlobalControllerAdvice.SESSION_ROLE, Role.NURSE))
                .andExpect(request().sessionAttribute(GlobalControllerAdvice.SESSION_STAFF, staffService.getStaffById("NV003")));

        // Choose Pharmacist NV004 -> should redirect to /pharmacy
        mockMvc.perform(get("/choose-staff/NV004"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pharmacy"))
                .andExpect(request().sessionAttribute(GlobalControllerAdvice.SESSION_ROLE, Role.PHARMACIST));

        // Choose Cashier NV005 -> should redirect to /billing
        mockMvc.perform(get("/choose-staff/NV005"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/billing"))
                .andExpect(request().sessionAttribute(GlobalControllerAdvice.SESSION_ROLE, Role.CASHIER));

        // Choose Manager NV006 -> should redirect to /dashboard
        mockMvc.perform(get("/choose-staff/NV006"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(request().sessionAttribute(GlobalControllerAdvice.SESSION_ROLE, Role.MANAGER));
    }
}
