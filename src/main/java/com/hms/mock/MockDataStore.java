package com.hms.mock;

import com.hms.model.*;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MockDataStore {

    public final Map<String, Patient> patients = new ConcurrentHashMap<>();
    public final Map<String, PatientRelative> relatives = new ConcurrentHashMap<>();
    public final Map<String, HealthInsurance> insurances = new ConcurrentHashMap<>();
    public final Map<String, MedicalRecord> medicalRecords = new ConcurrentHashMap<>();
    public final Map<String, Specialty> specialties = new ConcurrentHashMap<>();
    public final Map<String, ClinicRoom> rooms = new ConcurrentHashMap<>();
    public final Map<String, Staff> staffMembers = new ConcurrentHashMap<>();
    public final Map<String, LabType> labTypes = new ConcurrentHashMap<>();
    public final Map<String, Diagnosis> diagnoses = new ConcurrentHashMap<>();
    public final Map<String, Examination> examinations = new ConcurrentHashMap<>();
    public final Map<String, LabResult> labResults = new ConcurrentHashMap<>();
    public final Map<String, Medicine> medicines = new ConcurrentHashMap<>();
    public final Map<String, MedicineLot> medicineLots = new ConcurrentHashMap<>();
    public final Map<String, Prescription> prescriptions = new ConcurrentHashMap<>();
    public final Map<String, PrescriptionDetail> prescriptionDetails = new ConcurrentHashMap<>();
    public final Map<String, Invoice> invoices = new ConcurrentHashMap<>();

    private final AtomicLong patientSeq = new AtomicLong(7);
    private final AtomicLong examSeq = new AtomicLong(6);
    private final AtomicLong invoiceSeq = new AtomicLong(4);
    private final AtomicLong rxSeq = new AtomicLong(3);
    private final AtomicLong rxDetailSeq = new AtomicLong(4);
    private final AtomicLong labResultSeq = new AtomicLong(3);

    @PostConstruct
    public void init() {
        resetData();
    }

    public synchronized void resetData() {
        patients.clear();
        relatives.clear();
        insurances.clear();
        medicalRecords.clear();
        specialties.clear();
        rooms.clear();
        staffMembers.clear();
        labTypes.clear();
        diagnoses.clear();
        examinations.clear();
        labResults.clear();
        medicines.clear();
        medicineLots.clear();
        prescriptions.clear();
        prescriptionDetails.clear();
        invoices.clear();

        LocalDate today = LocalDate.now();

        // 1. Specialties
        specialties.put("CK01", new Specialty("CK01", "Nội tổng hợp"));
        specialties.put("CK02", new Specialty("CK02", "Tim mạch"));
        specialties.put("CK03", new Specialty("CK03", "Xét nghiệm"));
        specialties.put("CK04", new Specialty("CK04", "Nhi khoa"));

        // 2. Clinic Rooms
        rooms.put("PK01", new ClinicRoom("PK01", "Phòng khám Nội tổng hợp", "A-101", "Hoạt động", "CK01"));
        rooms.put("PK02", new ClinicRoom("PK02", "Phòng khám Tim mạch", "A-202", "Hoạt động", "CK02"));
        rooms.put("PK03", new ClinicRoom("PK03", "Phòng xét nghiệm", "B-105", "Hoạt động", "CK03"));
        rooms.put("PK04", new ClinicRoom("PK04", "Phòng khám Nhi", "C-102", "Hoạt động", "CK04"));
        for (ClinicRoom r : rooms.values()) {
            Specialty sp = specialties.get(r.getIdCk());
            if (sp != null) r.setTenChuyenKhoa(sp.getTenCk());
        }

        // 3. Staff Members
        staffMembers.put("NV001", new Staff("NV001", "BS. Nguyễn Hoàng An", "Nam", "1980-02-12", List.of("0903001001"), "an.nguyen@hms.local", "Bác sĩ", "Đang làm việc", "Thạc sĩ · Nội tổng hợp"));
        staffMembers.put("NV002", new Staff("NV002", "BS. Trần Mai Phương", "Nữ", "1985-09-21", List.of("0903001002"), "phuong.tran@hms.local", "Bác sĩ", "Đang làm việc", "Bác sĩ CKI · Tim mạch"));
        staffMembers.put("NV003", new Staff("NV003", "Điều dưỡng Lê Thu Trang", "Nữ", "1992-06-07", List.of("0903001003"), "trang.le@hms.local", "Y tá", "Đang làm việc", "Điều dưỡng đại học"));
        staffMembers.put("NV004", new Staff("NV004", "DS. Phạm Quốc Huy", "Nam", "1988-04-18", List.of("0903001004"), "huy.pham@hms.local", "Dược sĩ", "Đang làm việc", "Dược sĩ đại học"));
        staffMembers.put("NV005", new Staff("NV005", "Nguyễn Hải Yến", "Nữ", "1991-12-03", List.of("0903001005"), "yen.nguyen@hms.local", "Thu ngân", "Đang làm việc", "Chuyên viên viện phí"));
        staffMembers.put("NV006", new Staff("NV006", "Đỗ Văn Khánh", "Nam", "1978-08-13", List.of("0903001006"), "khanh.do@hms.local", "Quản lý", "Đang làm việc", "Ban Giám đốc"));

        // 4. Lab Types
        labTypes.put("XN001", new LabType("XN001", "Công thức máu", 180000L, "Hoạt động"));
        labTypes.put("XN002", new LabType("XN002", "Đường huyết", 90000L, "Hoạt động"));
        labTypes.put("XN003", new LabType("XN003", "Chức năng gan (AST/ALT)", 220000L, "Hoạt động"));
        labTypes.put("XN004", new LabType("XN004", "Tổng phân tích nước tiểu", 120000L, "Hoạt động"));

        // 5. Diagnoses
        diagnoses.put("CD001", new Diagnosis("CD001", "J06.9", "Nhiễm trùng đường hô hấp trên cấp, không đặc hiệu"));
        diagnoses.put("CD002", new Diagnosis("CD002", "I10", "Tăng huyết áp vô căn"));
        diagnoses.put("CD003", new Diagnosis("CD003", "J30.4", "Viêm mũi dị ứng, không đặc hiệu"));
        diagnoses.put("CD004", new Diagnosis("CD004", "R50.9", "Sốt, không đặc hiệu"));
        diagnoses.put("CD005", new Diagnosis("CD005", "E11.9", "Đái tháo đường type 2, không biến chứng"));

        // 6. Patients
        patients.put("BN001", new Patient("BN001", "Nguyễn Minh Anh", "001206012345", "1998-03-14", "Nữ", List.of("0901234567"), "Cầu Giấy, Hà Nội", today.toString(), "Đang theo dõi"));
        patients.put("BN002", new Patient("BN002", "Trần Quốc Bảo", "001199007654", "1987-11-02", "Nam", List.of("0912345678", "0988123456"), "Hà Đông, Hà Nội", today.minusDays(1).toString(), "Ổn định"));
        patients.put("BN003", new Patient("BN003", "Lê Thu Hà", "001203004321", "2003-07-19", "Nữ", List.of("0934567890"), "Đống Đa, Hà Nội", today.minusDays(1).toString(), "Ổn định"));
        patients.put("BN004", new Patient("BN004", "Phạm Đức Long", "001190009876", "1990-12-08", "Nam", List.of("0977654321"), "Thanh Xuân, Hà Nội", today.minusDays(2).toString(), "Đang theo dõi"));
        patients.put("BN005", new Patient("BN005", "Đỗ Ngọc Linh", "001205001122", "2005-01-25", "Nữ", List.of("0966123456"), "Long Biên, Hà Nội", today.minusDays(3).toString(), "Ổn định"));
        patients.put("BN006", new Patient("BN006", "Vũ Thành Nam", "001185009911", "1985-09-30", "Nam", List.of("0944112233"), "Nam Từ Liêm, Hà Nội", today.minusDays(4).toString(), "Ổn định"));

        // Relatives
        PatientRelative rel1 = new PatientRelative("NT001", "BN001", "Nguyễn Văn Minh", "Bố", "Nam", List.of("0909988776"));
        PatientRelative rel2 = new PatientRelative("NT002", "BN003", "Lê Thị Hương", "Mẹ", "Nữ", List.of("0901234432"));
        relatives.put("NT001", rel1);
        relatives.put("NT002", rel2);
        patients.get("BN001").setRelative(rel1);
        patients.get("BN003").setRelative(rel2);

        // Insurance
        HealthInsurance bh1 = new HealthInsurance("BHYT001", "BN001", "HN4010123456789", 80, today.minusDays(180).toString(), today.plusDays(185).toString(), "Trạm y tế phường Dịch Vọng", "Hộ gia đình");
        HealthInsurance bh2 = new HealthInsurance("BHYT002", "BN002", "HN4010987654321", 95, today.minusDays(365).toString(), today.plusDays(30).toString(), "Bệnh viện quận Hà Đông", "Người lao động");
        HealthInsurance bh3 = new HealthInsurance("BHYT003", "BN005", "HN4010112233445", 100, today.minusDays(90).toString(), today.plusDays(275).toString(), "Bệnh viện Nhi Trung ương", "Trẻ em");
        insurances.put("BHYT001", bh1);
        insurances.put("BHYT002", bh2);
        insurances.put("BHYT003", bh3);
        patients.get("BN001").setInsurance(bh1);
        patients.get("BN002").setInsurance(bh2);
        patients.get("BN005").setInsurance(bh3);

        // Medical Records
        medicalRecords.put("BA001", new MedicalRecord("BA001", "BN001", today.minusDays(60).toString(), "Viêm mũi dị ứng theo mùa", "Đang theo dõi"));
        medicalRecords.put("BA002", new MedicalRecord("BA002", "BN002", today.minusDays(120).toString(), "Tăng huyết áp nhẹ", "Đang theo dõi"));
        medicalRecords.put("BA003", new MedicalRecord("BA003", "BN004", today.minusDays(30).toString(), "Chưa ghi nhận bất thường", "Mở"));

        // 7. Medicines & Lots
        medicines.put("T001", new Medicine("T001", "Paracetamol 500 mg", "DHG Pharma", false, 420, "viên"));
        medicines.put("T002", new Medicine("T002", "Amoxicillin 500 mg", "Imexpharm", true, 180, "viên"));
        medicines.put("T003", new Medicine("T003", "Cetirizine 10 mg", "Stella", false, 95, "viên"));
        medicines.put("T004", new Medicine("T004", "Amlodipine 5 mg", "GSK", true, 70, "viên"));
        medicines.put("T005", new Medicine("T005", "Oresol", "Pharmedic", false, 24, "gói"));

        medicineLots.put("LO001", new MedicineLot("LO001", "T001", "PAR-2608", today.plusDays(220).toString(), 500, 300, 450L, 800L));
        medicineLots.put("LO002", new MedicineLot("LO002", "T001", "PAR-2610", today.plusDays(360).toString(), 200, 120, 470L, 800L));
        medicineLots.put("LO003", new MedicineLot("LO003", "T002", "AMX-2605", today.plusDays(140).toString(), 300, 180, 1200L, 2000L));
        medicineLots.put("LO004", new MedicineLot("LO004", "T003", "CET-2609", today.plusDays(45).toString(), 120, 95, 900L, 1500L));
        medicineLots.put("LO005", new MedicineLot("LO005", "T004", "AML-2604", today.plusDays(310).toString(), 100, 70, 1800L, 3000L));
        medicineLots.put("LO006", new MedicineLot("LO006", "T005", "ORS-2607", today.plusDays(28).toString(), 60, 24, 2500L, 4000L));
        for (MedicineLot lot : medicineLots.values()) {
            Medicine m = medicines.get(lot.getIdThuoc());
            if (m != null) {
                lot.setTenThuoc(m.getTenThuoc());
                lot.setDonVi(m.getDonVi());
            }
        }

        // 8. Examinations
        Examination ex1 = new Examination("PKM001", "BN001", "PK01", "NV001", "BA001", "CD003", today.toString(), "08:15", "Hắt hơi, sổ mũi, ngạt mũi", 150000L, "Đã khám");
        ex1.setKetLuan("Viêm mũi dị ứng cấp, chỉ định dùng thuốc kháng histamin.");
        Examination ex2 = new Examination("PKM002", "BN002", "PK02", "NV002", "BA002", "CD002", today.toString(), "09:10", "Đau đầu nhẹ, huyết áp 145/90", 180000L, "Chờ thanh toán");
        ex2.setKetLuan("Tăng huyết áp vô căn độ 1, duy trì thuốc hạ áp hàng ngày.");
        Examination ex3 = new Examination("PKM003", "BN003", "PK01", "NV001", null, null, today.toString(), "", "Ho khan, rát họng", 150000L, "Chờ khám");
        Examination ex4 = new Examination("PKM004", "BN004", "PK03", "NV002", "BA003", null, today.toString(), "", "Mệt mỏi kéo dài, khát nước", 150000L, "Chờ xét nghiệm");
        Examination ex5 = new Examination("PKM005", "BN005", "PK04", "NV001", null, null, today.toString(), "", "Sốt nhẹ 38 độ, biếng ăn", 120000L, "Chờ khám");

        examinations.put("PKM001", ex1);
        examinations.put("PKM002", ex2);
        examinations.put("PKM003", ex3);
        examinations.put("PKM004", ex4);
        examinations.put("PKM005", ex5);

        // 9. Prescriptions & Details
        Prescription dt1 = new Prescription("DT001", "PKM001", "NV001", today.toString(), 23000L, "Uống sau khi ăn, tái khám sau 7 ngày", "Chờ cấp phát");
        PrescriptionDetail ctdt1 = new PrescriptionDetail("CT001", "DT001", "T003", 7, "1 viên/lần", "Uống sau ăn tối", "Dùng trong 7 ngày", 1500L);
        PrescriptionDetail ctdt2 = new PrescriptionDetail("CT002", "DT001", "T001", 20, "1 viên khi sốt > 38.5°C", "Uống với nước ấm", "Cách nhau tối thiểu 4-6 tiếng", 800L);
        ctdt1.setTenThuoc(medicines.get("T003").getTenThuoc());
        ctdt1.setDonVi(medicines.get("T003").getDonVi());
        ctdt2.setTenThuoc(medicines.get("T001").getTenThuoc());
        ctdt2.setDonVi(medicines.get("T001").getDonVi());
        dt1.setItems(new ArrayList<>(List.of(ctdt1, ctdt2)));
        prescriptionDetails.put("CT001", ctdt1);
        prescriptionDetails.put("CT002", ctdt2);
        prescriptions.put("DT001", dt1);
        ex1.setPrescription(dt1);

        Prescription dt2 = new Prescription("DT002", "PKM002", "NV002", today.toString(), 18000L, "Uống cố định 8h sáng hàng ngày", "Đã cấp phát");
        PrescriptionDetail ctdt3 = new PrescriptionDetail("CT003", "DT002", "T004", 6, "1 viên/ngày", "Uống buổi sáng sau ăn", "Đo huyết áp trước khi uống", 3000L);
        ctdt3.setTenThuoc(medicines.get("T004").getTenThuoc());
        ctdt3.setDonVi(medicines.get("T004").getDonVi());
        dt2.setItems(new ArrayList<>(List.of(ctdt3)));
        prescriptionDetails.put("CT003", ctdt3);
        prescriptions.put("DT002", dt2);
        ex2.setPrescription(dt2);

        // 10. Lab Results
        LabResult kq1 = new LabResult("KQ001", "PKM001", "XN001", "RBC 4.5, WBC 7.2, PLT 240 (Bình thường)", "Chưa ghi nhận bất thường về huyết học", today.toString(), "Hoàn thành");
        LabResult kq2 = new LabResult("KQ002", "PKM004", "XN002", "", "", "", "Chờ lấy mẫu");
        labResults.put("KQ001", kq1);
        labResults.put("KQ002", kq2);
        ex1.getLabResults().add(kq1);
        ex4.getLabResults().add(kq2);

        // 11. Invoices
        Invoice hd1 = new Invoice("HD001", "PKM001", 353000L, "Khám + xét nghiệm + thuốc", "Chuyển khoản", today.toString(), "Đã thanh toán");
        Invoice hd2 = new Invoice("HD002", "PKM002", 198000L, "Khám + thuốc", "Tiền mặt", "", "Chưa thanh toán");
        Invoice hd3 = new Invoice("HD003", "PKM004", 240000L, "Khám + xét nghiệm", "", "", "Chưa thanh toán");
        invoices.put("HD001", hd1);
        invoices.put("HD002", hd2);
        invoices.put("HD003", hd3);

        // Link patient names & extra info
        linkAll();
    }

    public void linkAll() {
        // 1. Link Clinic Rooms -> Specialties (Bắt buộc)
        for (ClinicRoom r : rooms.values()) {
            if (r.getIdCk() == null || r.getIdCk().isBlank()) {
                throw new IllegalStateException("Phòng khám [" + r.getIdPk() + "] thiếu mã chuyên khoa bắt buộc (idCk)");
            }
            Specialty sp = specialties.get(r.getIdCk());
            if (sp == null) {
                throw new IllegalStateException("Phòng khám [" + r.getIdPk() + "] tham chiếu chuyên khoa không tồn tại: " + r.getIdCk());
            }
            r.setTenChuyenKhoa(sp.getTenCk());
        }

        // 2. Link Relatives -> Patients (Bắt buộc)
        for (PatientRelative rel : relatives.values()) {
            if (rel.getIdBn() == null || rel.getIdBn().isBlank()) {
                throw new IllegalStateException("Người nhà [" + rel.getIdNt() + "] thiếu mã bệnh nhân bắt buộc (idBn)");
            }
            Patient p = patients.get(rel.getIdBn());
            if (p == null) {
                throw new IllegalStateException("Người nhà [" + rel.getIdNt() + "] tham chiếu bệnh nhân không tồn tại: " + rel.getIdBn());
            }
            p.setRelative(rel);
        }

        // 3. Link Insurances -> Patients (Bắt buộc)
        for (HealthInsurance bh : insurances.values()) {
            if (bh.getIdBn() == null || bh.getIdBn().isBlank()) {
                throw new IllegalStateException("BHYT [" + bh.getIdBhyt() + "] thiếu mã bệnh nhân bắt buộc (idBn)");
            }
            Patient p = patients.get(bh.getIdBn());
            if (p == null) {
                throw new IllegalStateException("BHYT [" + bh.getIdBhyt() + "] tham chiếu bệnh nhân không tồn tại: " + bh.getIdBn());
            }
            p.setInsurance(bh);
        }

        // 4. Validate Medical Records -> Patients (Bắt buộc)
        for (MedicalRecord mr : medicalRecords.values()) {
            if (mr.getIdBn() == null || mr.getIdBn().isBlank()) {
                throw new IllegalStateException("Bệnh án [" + mr.getIdBenhan() + "] thiếu mã bệnh nhân bắt buộc (idBn)");
            }
            if (!patients.containsKey(mr.getIdBn())) {
                throw new IllegalStateException("Bệnh án [" + mr.getIdBenhan() + "] tham chiếu bệnh nhân không tồn tại: " + mr.getIdBn());
            }
        }

        // 5. Link Medicine Lots -> Medicines (Bắt buộc)
        for (MedicineLot lot : medicineLots.values()) {
            if (lot.getIdThuoc() == null || lot.getIdThuoc().isBlank()) {
                throw new IllegalStateException("Lô thuốc [" + lot.getIdLo() + "] thiếu mã thuốc bắt buộc (idThuoc)");
            }
            Medicine m = medicines.get(lot.getIdThuoc());
            if (m == null) {
                throw new IllegalStateException("Lô thuốc [" + lot.getIdLo() + "] tham chiếu thuốc không tồn tại: " + lot.getIdThuoc());
            }
            lot.setTenThuoc(m.getTenThuoc());
            lot.setDonVi(m.getDonVi());
        }

        // 6. Link Prescription Details -> Medicines (Bắt buộc)
        for (PrescriptionDetail ctdt : prescriptionDetails.values()) {
            if (ctdt.getIdThuoc() == null || ctdt.getIdThuoc().isBlank()) {
                throw new IllegalStateException("Chi tiết đơn thuốc [" + ctdt.getIdCtdt() + "] thiếu mã thuốc bắt buộc (idThuoc)");
            }
            Medicine m = medicines.get(ctdt.getIdThuoc());
            if (m == null) {
                throw new IllegalStateException("Chi tiết đơn thuốc [" + ctdt.getIdCtdt() + "] tham chiếu thuốc không tồn tại: " + ctdt.getIdThuoc());
            }
            ctdt.setTenThuoc(m.getTenThuoc());
            ctdt.setDonVi(m.getDonVi());
        }

        // 7. Link Examinations -> Patient, Room, Doctor (Bắt buộc) & Diagnosis, MedicalRecord (Tùy chọn)
        for (Examination ex : examinations.values()) {
            if (ex.getIdBn() == null || ex.getIdBn().isBlank()) {
                throw new IllegalStateException("Phiếu khám [" + ex.getIdPhieukham() + "] thiếu mã bệnh nhân bắt buộc (idBn)");
            }
            Patient p = patients.get(ex.getIdBn());
            if (p == null) {
                throw new IllegalStateException("Phiếu khám [" + ex.getIdPhieukham() + "] tham chiếu bệnh nhân không tồn tại: " + ex.getIdBn());
            }
            ex.setPatientName(p.getHoTen());
            ex.setPatientPhone(p.getPhoneDisplay());

            if (ex.getIdPk() == null || ex.getIdPk().isBlank()) {
                throw new IllegalStateException("Phiếu khám [" + ex.getIdPhieukham() + "] thiếu mã phòng khám bắt buộc (idPk)");
            }
            ClinicRoom r = rooms.get(ex.getIdPk());
            if (r == null) {
                throw new IllegalStateException("Phiếu khám [" + ex.getIdPhieukham() + "] tham chiếu phòng khám không tồn tại: " + ex.getIdPk());
            }
            ex.setRoomName(r.getTenPhong() + " (" + r.getSoPhong() + ")");

            if (ex.getIdNv() == null || ex.getIdNv().isBlank()) {
                throw new IllegalStateException("Phiếu khám [" + ex.getIdPhieukham() + "] thiếu mã bác sĩ phụ trách bắt buộc (idNv)");
            }
            Staff st = staffMembers.get(ex.getIdNv());
            if (st == null) {
                throw new IllegalStateException("Phiếu khám [" + ex.getIdPhieukham() + "] tham chiếu bác sĩ/nhân viên không tồn tại: " + ex.getIdNv());
            }
            ex.setDoctorName(st.getHoTen());

            // Chẩn đoán ICD: Chỉ có khi đã khám. Khi "Chờ khám" hoặc "Chờ xét nghiệm", trường này hợp lệ ở giá trị null
            if (ex.getIdCd() != null && !ex.getIdCd().isBlank()) {
                Diagnosis cd = diagnoses.get(ex.getIdCd());
                if (cd == null) {
                    throw new IllegalStateException("Phiếu khám [" + ex.getIdPhieukham() + "] tham chiếu mã chẩn đoán ICD không tồn tại: " + ex.getIdCd());
                }
                ex.setDiagnosisName(cd.getTenBenh());
                ex.setIcdCode(cd.getMaIcd());
            } else {
                ex.setDiagnosisName("Chưa có chẩn đoán");
                ex.setIcdCode("");
            }

            // Bệnh án: Tùy chọn
            if (ex.getIdBenhan() != null && !ex.getIdBenhan().isBlank()) {
                if (!medicalRecords.containsKey(ex.getIdBenhan())) {
                    throw new IllegalStateException("Phiếu khám [" + ex.getIdPhieukham() + "] tham chiếu bệnh án không tồn tại: " + ex.getIdBenhan());
                }
            }
        }

        // 8. Link Prescriptions -> Examinations, Doctors & attach items (Bắt buộc)
        for (Prescription rx : prescriptions.values()) {
            if (rx.getIdPhieukham() == null || rx.getIdPhieukham().isBlank()) {
                throw new IllegalStateException("Đơn thuốc [" + rx.getIdDt() + "] thiếu mã phiếu khám bắt buộc (idPhieukham)");
            }
            Examination ex = examinations.get(rx.getIdPhieukham());
            if (ex == null) {
                throw new IllegalStateException("Đơn thuốc [" + rx.getIdDt() + "] tham chiếu phiếu khám không tồn tại: " + rx.getIdPhieukham());
            }
            rx.setPatientId(ex.getIdBn());
            rx.setPatientName(ex.getPatientName());
            ex.setPrescription(rx);

            if (rx.getIdNv() == null || rx.getIdNv().isBlank()) {
                throw new IllegalStateException("Đơn thuốc [" + rx.getIdDt() + "] thiếu mã bác sĩ kê đơn bắt buộc (idNv)");
            }
            Staff st = staffMembers.get(rx.getIdNv());
            if (st == null) {
                throw new IllegalStateException("Đơn thuốc [" + rx.getIdDt() + "] tham chiếu bác sĩ không tồn tại: " + rx.getIdNv());
            }
            rx.setDoctorName(st.getHoTen());

            List<PrescriptionDetail> items = prescriptionDetails.values().stream()
                    .filter(d -> rx.getIdDt().equals(d.getIdDt()))
                    .collect(java.util.stream.Collectors.toList());
            if (!items.isEmpty()) {
                rx.setItems(items);
            }
        }

        // 9. Link Lab Results -> Lab Types, Examinations (Bắt buộc)
        for (LabResult kq : labResults.values()) {
            if (kq.getIdLxn() == null || kq.getIdLxn().isBlank()) {
                throw new IllegalStateException("Kết quả xét nghiệm [" + kq.getIdKqxn() + "] thiếu mã loại xét nghiệm bắt buộc (idLxn)");
            }
            LabType lt = labTypes.get(kq.getIdLxn());
            if (lt == null) {
                throw new IllegalStateException("Kết quả xét nghiệm [" + kq.getIdKqxn() + "] tham chiếu loại xét nghiệm không tồn tại: " + kq.getIdLxn());
            }
            kq.setTenLxn(lt.getTenLxn());
            kq.setChiPhi(lt.getChiPhi());

            if (kq.getIdPhieukham() == null || kq.getIdPhieukham().isBlank()) {
                throw new IllegalStateException("Kết quả xét nghiệm [" + kq.getIdKqxn() + "] thiếu mã phiếu khám bắt buộc (idPhieukham)");
            }
            Examination ex = examinations.get(kq.getIdPhieukham());
            if (ex == null) {
                throw new IllegalStateException("Kết quả xét nghiệm [" + kq.getIdKqxn() + "] tham chiếu phiếu khám không tồn tại: " + kq.getIdPhieukham());
            }
            kq.setPatientName(ex.getPatientName());
            kq.setPatientId(ex.getIdBn());
            if (!ex.getLabResults().contains(kq)) {
                ex.getLabResults().add(kq);
            }
        }

        // 10. Link Invoices -> Examinations (Bắt buộc)
        for (Invoice inv : invoices.values()) {
            if (inv.getIdPhieukham() == null || inv.getIdPhieukham().isBlank()) {
                throw new IllegalStateException("Hóa đơn viện phí [" + inv.getIdHoadon() + "] thiếu mã phiếu khám bắt buộc (idPhieukham)");
            }
            Examination ex = examinations.get(inv.getIdPhieukham());
            if (ex == null) {
                throw new IllegalStateException("Hóa đơn viện phí [" + inv.getIdHoadon() + "] tham chiếu phiếu khám không tồn tại: " + inv.getIdPhieukham());
            }
            inv.setIdBn(ex.getIdBn());
            inv.setPatientName(ex.getPatientName());
            inv.setPatientPhone(ex.getPatientPhone());
        }
    }

    public String nextPatientId() {
        return String.format("BN%03d", patientSeq.getAndIncrement());
    }

    public String nextExamId() {
        return String.format("PKM%03d", examSeq.getAndIncrement());
    }

    public String nextInvoiceId() {
        return String.format("HD%03d", invoiceSeq.getAndIncrement());
    }

    public String nextPrescriptionId() {
        return String.format("DT%03d", rxSeq.getAndIncrement());
    }

    public String nextPrescriptionDetailId() {
        return String.format("CT%03d", rxDetailSeq.getAndIncrement());
    }

    public String nextLabResultId() {
        return String.format("KQ%03d", labResultSeq.getAndIncrement());
    }
}

