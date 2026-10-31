package com.hms.service;

import com.hms.entity.NhanVien;
import com.hms.entity.PhieuKham;
import com.hms.entity.PhieuThuVienPhi;
import com.hms.model.Invoice;
import com.hms.repository.NhanVienRepository;
import com.hms.repository.PhieuKhamRepository;
import com.hms.repository.PhieuThuVienPhiRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BillingDbService {

    private final PhieuThuVienPhiRepository phieuThuRepo;
    private final PhieuKhamRepository phieuKhamRepo;
    private final NhanVienRepository nhanVienRepo;
    private final EntityModelMapper mapper;

    public BillingDbService(PhieuThuVienPhiRepository phieuThuRepo,
                           PhieuKhamRepository phieuKhamRepo,
                           NhanVienRepository nhanVienRepo,
                           EntityModelMapper mapper) {
        this.phieuThuRepo = phieuThuRepo;
        this.phieuKhamRepo = phieuKhamRepo;
        this.nhanVienRepo = nhanVienRepo;
        this.mapper = mapper;
    }

    public List<Invoice> findAllInvoices() {
        return phieuThuRepo.findAll().stream()
                .map(mapper::toInvoiceModel)
                .collect(Collectors.toList());
    }

    public Optional<Invoice> findInvoiceById(String id) {
        if (id == null) return Optional.empty();
        return phieuThuRepo.findById(id).map(mapper::toInvoiceModel);
    }

    @Transactional
    public Invoice saveInvoice(Invoice invoice) {
        if (invoice == null || invoice.getIdHoadon() == null) return null;

        PhieuKham pk = null;
        if (invoice.getIdPhieukham() != null) {
            pk = phieuKhamRepo.findById(invoice.getIdPhieukham()).orElse(null);
        }

        NhanVien nv = null;
        if (invoice.getIdNv() != null) {
            nv = nhanVienRepo.findById(invoice.getIdNv()).orElse(null);
        }

        PhieuThuVienPhi entity = mapper.toPhieuThuVienPhiEntity(invoice, pk, nv);
        phieuThuRepo.save(entity);
        return findInvoiceById(invoice.getIdHoadon()).orElse(invoice);
    }

    @Transactional
    public Invoice payInvoice(String id, String paymentMethod, String cashierId) {
        if (id == null) return null;
        return phieuThuRepo.findById(id).map(pt -> {
            pt.setTrangThai("DA_THU");
            pt.setPhuongThucTt(paymentMethod != null ? paymentMethod : "TIEN_MAT");
            pt.setThoiGianThu(LocalDateTime.now());
            if (cashierId != null) {
                nhanVienRepo.findById(cashierId).ifPresent(pt::setNhanVien);
            }
            phieuThuRepo.save(pt);
            return mapper.toInvoiceModel(pt);
        }).orElse(null);
    }
}
