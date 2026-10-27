/* Role-Based Access Control (RBAC) dùng chung cho giao diện.
   Chế độ demo lưu lựa chọn trong localStorage; chế độ Supabase lấy role từ hms_user_roles.
   Phân quyền thực sự phải được cưỡng chế thêm ở PostgreSQL RLS, không chỉ dựa vào JavaScript.
*/
(function () {
  const DEFINITIONS = {
    manager: {
      key: 'manager', dbRole: 'manager', label: 'Quản lý bệnh viện', shortLabel: 'Quản lý',
      description: 'Theo dõi hoạt động tổng thể, nhân sự và báo cáo điều hành.',
      employeeId: 'NV006', employeeName: 'Đỗ Văn Khánh', icon: 'fa-chart-line', tone: 'blue',
      permissions: ['dashboard', 'patients', 'exams', 'pharmacy', 'billing', 'staff', 'reports'],
      features: ['Dashboard & KPI toàn viện', 'Báo cáo và thống kê', 'Quản trị tất cả phân hệ'],
      start: 'index.html#dashboard'
    },
    doctor: {
      key: 'doctor', dbRole: 'doctor', label: 'Bác sĩ', shortLabel: 'Bác sĩ',
      description: 'Khám bệnh, cập nhật chẩn đoán và theo dõi hồ sơ chuyên môn.',
      employeeId: 'NV001', employeeName: 'BS. Nguyễn Hoàng An', icon: 'fa-stethoscope', tone: 'cyan',
      permissions: ['doctor', 'records'],
      features: ['Hàng đợi khám', 'Chẩn đoán & chỉ định', 'Hồ sơ bệnh án được phân công'],
      start: 'kham-benh.html'
    },
    nurse: {
      key: 'nurse', dbRole: 'nurse', label: 'Y tá / Điều dưỡng', shortLabel: 'Y tá',
      description: 'Tiếp đón, đăng ký khám, hỗ trợ xét nghiệm và tra cứu hồ sơ được phép.',
      employeeId: 'NV003', employeeName: 'Điều dưỡng Lê Thu Trang', icon: 'fa-user-nurse', tone: 'green',
      permissions: ['registration', 'lab', 'records'],
      features: ['Tiếp đón & đăng ký khám', 'Nhập kết quả xét nghiệm', 'Tra cứu hồ sơ nghiệp vụ'],
      start: 'dang-ky-kham.html'
    },
    pharmacist: {
      key: 'pharmacist', dbRole: 'pharmacist', label: 'Dược sĩ', shortLabel: 'Dược sĩ',
      description: 'Chỉ làm việc với đơn thuốc, danh mục thuốc, lô thuốc và tồn kho.',
      employeeId: 'NV004', employeeName: 'DS. Phạm Quốc Huy', icon: 'fa-capsules', tone: 'violet',
      permissions: ['dispense'],
      features: ['Đơn thuốc chờ cấp phát', 'Thuốc & tồn kho', 'Lô thuốc và hạn sử dụng'],
      start: 'cap-phat.html'
    },
    cashier: {
      key: 'cashier', dbRole: 'cashier', label: 'Thu ngân', shortLabel: 'Thu ngân',
      description: 'Tiếp nhận thanh toán và xử lý hóa đơn viện phí.',
      employeeId: 'NV005', employeeName: 'Nguyễn Hải Yến', icon: 'fa-cash-register', tone: 'orange',
      permissions: ['billing'],
      features: ['Danh sách hóa đơn', 'Xác nhận thanh toán', 'Phương thức & ngày thu'],
      start: 'thu-tien.html'
    }
  };

  const PAGE_ROLES = {
    main: ['manager'],
    registration: ['manager', 'nurse'],
    billing: ['manager', 'cashier'],
    doctor: ['manager', 'doctor'],
    lab: ['manager', 'nurse'],
    dispense: ['manager', 'pharmacist'],
    records: ['manager', 'doctor', 'nurse'],
    reports: ['manager']
  };

  function normalizeRole(value) {
    if (!value) return '';
    const raw = String(value).trim().toLocaleLowerCase('vi');
    if (DEFINITIONS[raw]) return raw;
    const folded = raw.normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/đ/g, 'd');
    if (/quan ly|manager|admin|administrator/.test(folded)) return 'manager';
    if (/bac si|doctor|physician/.test(folded)) return 'doctor';
    if (/y ta|dieu duong|nurse|nursing/.test(folded)) return 'nurse';
    if (/duoc si|pharmacist|pharmacy/.test(folded)) return 'pharmacist';
    if (/thu ngan|cashier|billing/.test(folded)) return 'cashier';
    return '';
  }

  function getRole() {
    return normalizeRole(localStorage.getItem('hms_current_role'));
  }

  function setDemoRole(roleValue) {
    const role = normalizeRole(roleValue);
    if (!role) throw new Error('Vai trò không hợp lệ.');
    const definition = DEFINITIONS[role];
    localStorage.setItem('hms_current_role', role);
    localStorage.setItem('hms_current_staff', definition.employeeId);
    localStorage.setItem('hms_demo_role_selected', 'true');
    return definition;
  }

  function startPage(roleValue) {
    const role = normalizeRole(roleValue);
    return (DEFINITIONS[role] || DEFINITIONS.manager).start;
  }

  function canAccess(roleValue, pageType) {
    const role = normalizeRole(roleValue);
    return Boolean(role && (PAGE_ROLES[pageType] || []).includes(role));
  }

  function employeeFor(roleValue) {
    const role = normalizeRole(roleValue) || getRole();
    const definition = DEFINITIONS[role];
    if (!definition) return { id: '', name: 'Nhân viên', role: '', shortRole: '' };
    return {
      id: localStorage.getItem('hms_current_staff') || definition.employeeId,
      name: definition.employeeName,
      role: definition.label,
      shortRole: definition.shortLabel
    };
  }

  function menuFor(roleValue) {
    const role = normalizeRole(roleValue);
    if (role === 'manager') {
      return [
        { key: 'dashboard', label: 'Tổng quan', icon: 'fa-chart-pie', href: 'index.html#dashboard', tab: true },
        { key: 'patients', label: 'Tiếp đón & Bệnh nhân', icon: 'fa-hospital-user', href: 'index.html#patients', tab: true },
        { key: 'exams', label: 'Khám bệnh & Xét nghiệm', icon: 'fa-stethoscope', href: 'index.html#exams', tab: true },
        { key: 'pharmacy', label: 'Dược & Đơn thuốc', icon: 'fa-pills', href: 'index.html#pharmacy', tab: true },
        { key: 'billing', label: 'Viện phí', icon: 'fa-file-invoice-dollar', href: 'index.html#billing', tab: true },
        { key: 'staff', label: 'Nhân sự & Chuyên khoa', icon: 'fa-user-doctor', href: 'index.html#staff', tab: true },
        { key: 'reports', label: 'Báo cáo thống kê', icon: 'fa-chart-column', href: 'thong-ke.html', tab: false }
      ];
    }
    const map = {
      doctor: [
        { key: 'doctor', label: 'Bàn khám bác sĩ', icon: 'fa-stethoscope', href: 'kham-benh.html' },
        { key: 'records', label: 'Hồ sơ bệnh án', icon: 'fa-folder-medical', href: 'benh-an.html' }
      ],
      nurse: [
        { key: 'registration', label: 'Tiếp đón & Đăng ký khám', icon: 'fa-clipboard-list', href: 'dang-ky-kham.html' },
        { key: 'lab', label: 'Xét nghiệm', icon: 'fa-vials', href: 'xet-nghiem.html' },
        { key: 'records', label: 'Hồ sơ bệnh án', icon: 'fa-folder-medical', href: 'benh-an.html' }
      ],
      pharmacist: [
        { key: 'dispense', label: 'Dược & Cấp phát thuốc', icon: 'fa-capsules', href: 'cap-phat.html' }
      ],
      cashier: [
        { key: 'billing', label: 'Thu viện phí', icon: 'fa-cash-register', href: 'thu-tien.html' }
      ]
    };
    return map[role] || [];
  }

  window.HMSRoles = {
    definitions: DEFINITIONS,
    pageRoles: PAGE_ROLES,
    normalizeRole,
    getRole,
    setDemoRole,
    startPage,
    canAccess,
    employeeFor,
    menuFor
  };
})();
