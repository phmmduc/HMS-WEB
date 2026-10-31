package com.hms.model;

import java.util.ArrayList;
import java.util.List;

public class Staff {
    private String idNv;
    private String hoTen;
    private String gioiTinh;
    private String ngaySinh;
    private List<String> sdt = new ArrayList<>();
    private String email;
    private String vaiTro; // "Bác sĩ", "Y tá", "Dược sĩ", "Thu ngân", "Quản lý"
    private String trangThai = "DANG_LAM"; // Database: DANG_LAM, TAM_NGHI, NGHI_VIEC
    private String specialtyDetail; // e.g. "Thạc sĩ · Nội tổng hợp" hoặc "Điều dưỡng đại học"
    private String idPk; // Mã phòng khám phân công (dành cho Bác sĩ)
    private String roomName;

    public Staff() {}

    public Staff(String idNv, String hoTen, String gioiTinh, String ngaySinh, List<String> sdt, String email, String vaiTro, String trangThai, String specialtyDetail) {
        this(idNv, hoTen, gioiTinh, ngaySinh, sdt, email, vaiTro, trangThai, specialtyDetail, null);
    }

    public Staff(String idNv, String hoTen, String gioiTinh, String ngaySinh, List<String> sdt, String email, String vaiTro, String trangThai, String specialtyDetail, String idPk) {
        this.idNv = idNv;
        this.hoTen = hoTen;
        this.gioiTinh = gioiTinh;
        this.ngaySinh = ngaySinh;
        this.sdt = sdt != null ? sdt : new ArrayList<>();
        this.email = email;
        this.vaiTro = vaiTro;
        this.trangThai = normalizeTrangThai(trangThai);
        this.specialtyDetail = specialtyDetail;
        this.idPk = idPk;
    }

    public String getIdNv() { return idNv; }
    public void setIdNv(String idNv) { this.idNv = idNv; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public String getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(String ngaySinh) { this.ngaySinh = ngaySinh; }

    public List<String> getSdt() { return sdt; }
    public void setSdt(List<String> sdt) { this.sdt = sdt; }

    public List<String> getPhoneNumbers() { return sdt; }
    public void setPhoneNumbers(List<String> phoneNumbers) { this.sdt = phoneNumbers; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = normalizeTrangThai(trangThai); }

    public String getTrangThaiDisplay() {
        if (trangThai == null || trangThai.isBlank()) return "Đang làm việc";
        String code = normalizeTrangThai(trangThai);
        return switch (code) {
            case "TAM_NGHI" -> "Tạm nghỉ";
            case "NGHI_VIEC" -> "Nghỉ việc";
            default -> "Đang làm việc";
        };
    }

    public String getTrangThaiBadgeClass() {
        String code = normalizeTrangThai(trangThai);
        return switch (code) {
            case "TAM_NGHI" -> "pill-warning";
            case "NGHI_VIEC" -> "pill-secondary";
            default -> "pill-success";
        };
    }

    public static String normalizeTrangThai(String raw) {
        if (raw == null || raw.isBlank()) return "DANG_LAM";
        String clean = raw.trim();
        if ("DANG_LAM".equalsIgnoreCase(clean) || "Đang làm việc".equalsIgnoreCase(clean)) {
            return "DANG_LAM";
        }
        if ("TAM_NGHI".equalsIgnoreCase(clean) || "Tạm nghỉ".equalsIgnoreCase(clean) || "Nghỉ phép".equalsIgnoreCase(clean)) {
            return "TAM_NGHI";
        }
        if ("NGHI_VIEC".equalsIgnoreCase(clean) || "Nghỉ việc".equalsIgnoreCase(clean) || "Đã nghỉ việc".equalsIgnoreCase(clean)) {
            return "NGHI_VIEC";
        }
        return "DANG_LAM";
    }

    public String getSpecialtyDetail() { return specialtyDetail; }
    public void setSpecialtyDetail(String specialtyDetail) { this.specialtyDetail = specialtyDetail; }

    public String getChucDanh() { return specialtyDetail; }
    public void setChucDanh(String chucDanh) { this.specialtyDetail = chucDanh; }

    public String getInitials() {
        if (hoTen == null || hoTen.trim().isEmpty()) return "NV";
        String[] parts = hoTen.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[parts.length - 2].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    public String getPhoneDisplay() {
        return (sdt != null && !sdt.isEmpty()) ? String.join(", ", sdt) : "—";
    }

    public String getIdPk() { return idPk; }
    public void setIdPk(String idPk) { this.idPk = idPk; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
}

