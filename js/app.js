/* MediCore HMS - logic giao diện. Vanilla JavaScript, dùng HMSApi cho mọi thao tác dữ liệu. */
(function () {
  const UI = window.HMSUI;
  const TABLES = ['benh_nhan','nguoi_nha','bao_hiem_yte','benh_an','chuyen_khoa','phong_kham','nhan_vien','bac_sy','y_ta','duoc_sy','loai_xet_nghiem','chan_doan','phieu_kham','ket_qua_xet_nghiem','thuoc','lo_thuoc','don_thuoc','chi_tiet_don_thuoc','phieu_thu_vien_phi'];
  const PAGE_CONFIG = {
    main: ['dashboard','Tổng quan','Theo dõi hoạt động khám chữa bệnh'],
    registration: ['patients','Đăng ký khám','Tiếp đón bệnh nhân và tạo phiếu khám mới'],
    billing: ['billing','Thu viện phí','Quản lý hóa đơn và trạng thái thanh toán'],
    doctor: ['exams','Bàn khám bác sĩ','Đọc hồ sơ, ghi nhận khám và chỉ định cận lâm sàng'],
    lab: ['exams','Xét nghiệm','Tiếp nhận chỉ định và cập nhật kết quả xét nghiệm'],
    dispense: ['pharmacy','Cấp phát thuốc','Duyệt đơn thuốc và cập nhật tồn kho theo lô'],
    records: ['patients','Hồ sơ bệnh án','Tra cứu bệnh án và lịch sử khám chữa bệnh'],
    reports: ['dashboard','Thống kê & báo cáo','Tổng hợp chỉ số vận hành theo dữ liệu hiện có']
  };
  let pageType = document.body.dataset.page || 'main';
  let all = {};
  let activeTab = location.hash.replace('#','') || 'dashboard';
  let searchText = '';
  let staffFilter = 'all';

  const e = UI.escapeHTML;
  const $ = UI.$;
  const dayToday = () => new Date().toISOString().slice(0,10);
  const rowBy = (table, key, value) => (all[table] || []).find(row => String(row[key]) === String(value));
  const namePatient = id => rowBy('benh_nhan','id_bn',id)?.ho_ten || 'Bệnh nhân đã xóa';
  const nameStaff = id => rowBy('nhan_vien','id_nv',id)?.ho_ten || 'Chưa phân công';
  const nameRoom = id => rowBy('phong_kham','id_pk',id)?.ten_phong || 'Chưa xếp phòng';
  const nameDrug = id => rowBy('thuoc','id_thuoc',id)?.ten_thuoc || 'Thuốc không xác định';
  const nameLab = id => rowBy('loai_xet_nghiem','id_lxn',id)?.ten_lxn || 'Xét nghiệm';
  const nameDiagnosis = id => { const d = rowBy('chan_doan','id_cd',id); return d ? `${d.ma_icd} · ${d.ten_benh}` : 'Chưa có chẩn đoán'; };
  const todayExams = () => (all.phieu_kham || []).filter(x => x.ngay_dky === dayToday());
  const patientInitials = name => UI.initials(name);
  const person = (name, meta = '', tone = '') => `<div class="patient-cell"><span class="person-avatar ${tone}">${e(patientInitials(name))}</span><span><span class="person-name">${e(name)}</span>${meta ? `<span class="person-meta">${e(meta)}</span>` : ''}</span></div>`;
  const table = (headers, rows, empty = 'Chưa có dữ liệu phù hợp.') => UI.tableWrap(headers, rows, empty);
  const options = (items, key, label, first = '-- Chọn --') => [{ value:'', label:first }, ...(items || []).map(item => ({ value:item[key], label:label(item) }))];
  const currentStaff = () => localStorage.getItem('hms_current_staff') || 'NV001';
  const showError = err => { console.error(err); UI.showToast(err?.message || 'Không thể tải dữ liệu. Kiểm tra cấu hình API.', 'error'); };

  function tablesForCurrentPage() {
    const role = HMSRoles.getRole();
    if (role === 'manager') return TABLES;
    const map = {
      registration: ['benh_nhan','nguoi_nha','bao_hiem_yte','benh_an','chuyen_khoa','phong_kham','nhan_vien','bac_sy','loai_xet_nghiem','chan_doan','phieu_kham'],
      billing: ['v_hms_phieu_thu_vien_phi'],
      doctor: ['benh_nhan','bao_hiem_yte','benh_an','chuyen_khoa','phong_kham','nhan_vien','bac_sy','loai_xet_nghiem','chan_doan','phieu_kham','ket_qua_xet_nghiem','thuoc','lo_thuoc','don_thuoc','chi_tiet_don_thuoc'],
      lab: ['benh_nhan','phieu_kham','loai_xet_nghiem','ket_qua_xet_nghiem'],
      dispense: ['v_hms_don_thuoc_cap_phat','chi_tiet_don_thuoc','thuoc','lo_thuoc'],
      records: ['benh_nhan','nguoi_nha','bao_hiem_yte','benh_an','phieu_kham','chan_doan'],
      reports: TABLES
    };
    return map[pageType] || [];
  }

  async function loadAll() {
    const names = tablesForCurrentPage();
    const results = await Promise.all(names.map(async name => {
      const targetName = name === 'v_hms_don_thuoc_cap_phat' ? 'don_thuoc'
        : name === 'v_hms_phieu_thu_vien_phi' ? 'phieu_thu_vien_phi' : name;
      return [targetName, await HMSApi.list(name)];
    }));
    all = Object.fromEntries(results);
    const pending = (all.phieu_kham || []).filter(x => ['Chờ khám','Chờ xét nghiệm'].includes(x.trang_thai)).length;
    const queue = $('#nav-queue-count'); if (queue) queue.textContent = String(pending);
  }
  async function refresh() {
    try { await loadAll(); if (pageType === 'main') renderMain(); else renderStandalone(); }
    catch (err) { showError(err); $('#app-content').innerHTML = UI.emptyState('fa-triangle-exclamation','Không tải được dữ liệu','Kiểm tra bảng/view được cấp quyền trong Supabase hoặc xóa URL/key để chạy chế độ demo.'); }
  }
  async function begin() {
    if (HMSApi.isSupabase) {
      try {
        const { data, error } = await HMSApi.getSession();
        if (error) throw error;
        if (!data?.session) { renderAuthScreen(); return; }
        const { data: assignment, error: roleError } = await HMSApi.getAssignedRole();
        if (roleError) throw roleError;
        const role = HMSRoles.normalizeRole(assignment?.role);
        if (!role) { renderUnassignedRoleScreen(); return; }
        localStorage.setItem('hms_current_role', role);
        if (assignment.employee_id) localStorage.setItem('hms_current_staff', assignment.employee_id);
        if (pageType === 'role-select') { location.replace(HMSRoles.startPage(role)); return; }
        if (!HMSRoles.canAccess(role, pageType)) {
          sessionStorage.setItem('hms_access_notice', 'Tài khoản hiện tại không được cấp quyền vào phân hệ đó.');
          location.replace(HMSRoles.startPage(role));
          return;
        }
        mountApp();
      } catch (error) {
        console.error(error);
        if (pageType === 'role-select') renderAuthScreen(error.message || 'Không thể xác định vai trò tài khoản.');
        else $('#app-root').innerHTML = `<div class="access-error-screen"><div class="access-error-card"><span class="access-error-icon"><i class="fa-solid fa-shield-halved"></i></span><h1>Không thể xác minh quyền truy cập</h1><p>${e(error.message || 'Kiểm tra bảng hms_user_roles và chính sách RLS trong Supabase.')}</p><a href="chon-vai-tro.html" class="btn btn-primary">Quay lại</a></div></div>`;
      }
      return;
    }

    if (pageType === 'role-select') { renderRolePicker(); return; }
    const role = HMSRoles.getRole();
    if (!role) { location.replace('chon-vai-tro.html'); return; }
    if (!HMSRoles.canAccess(role, pageType)) {
      sessionStorage.setItem('hms_access_notice', 'Vai trò hiện tại không có quyền truy cập phân hệ vừa chọn.');
      location.replace(HMSRoles.startPage(role));
      return;
    }
    mountApp();
  }

  function renderRolePicker() {
    const root = document.getElementById('app-root');
    const roles = Object.values(HMSRoles.definitions);
    const stored = HMSRoles.getRole();
    root.innerHTML = `<div class="role-landing">
      <aside class="role-hero"><div class="role-hero-grid"></div><div class="role-hero-top"><div class="brand-mark"><i class="fa-solid fa-heart-pulse"></i></div><div><strong>MediCore</strong><span>HOSPITAL MANAGEMENT SYSTEM</span></div><span class="role-version">HMS · 2.0</span></div>
        <div class="role-hero-main"><span class="role-overline"><i class="fa-solid fa-shield-heart"></i> NỀN TẢNG VẬN HÀNH Y TẾ</span><h1>Đúng vai trò.<br><em>Đúng quyền hạn.</em></h1><p>Một không gian làm việc riêng cho từng bộ phận, giảm nhiễu thông tin và bảo vệ dữ liệu người bệnh.</p><div class="role-hero-metrics"><div><strong>05</strong><span>Vai trò làm việc</span></div><div><strong>01</strong><span>Hệ thống thống nhất</span></div><div><strong>RBAC</strong><span>Quyền theo vai trò</span></div></div></div>
        <div class="role-hero-foot"><span><i class="fa-solid fa-lock"></i> Quyền truy cập được kiểm soát</span><span>© ${new Date().getFullYear()} MediCore</span></div>
      </aside>
      <main class="role-selection-panel"><div class="role-selection-top"><span class="environment-label"><span class="live-dot"></span> ${HMSApi.isSupabase ? 'ĐĂNG NHẬP BẢO MẬT' : 'CHẾ ĐỘ DEMO · DỮ LIỆU MẪU'}</span><span class="role-help"><i class="fa-regular fa-circle-question"></i> Chưa rõ vai trò? Liên hệ quản trị viên</span></div>
        <div class="role-intro"><div class="role-intro-icon"><i class="fa-solid fa-door-open"></i></div><span class="role-step">BƯỚC 01 / 01</span><h2>Chọn vị trí làm việc</h2><p>Mỗi vai trò chỉ hiển thị các phân hệ cần thiết cho công việc.</p></div>
        <div class="role-grid">${roles.map((r, i) => `<button type="button" class="role-card ${stored === r.key ? 'selected' : ''} role-tone-${r.tone}" data-role-choice="${r.key}" aria-pressed="${stored === r.key}"><span class="role-card-top"><span class="role-card-icon"><i class="fa-solid ${r.icon}"></i></span><span class="role-card-check"><i class="fa-solid fa-check"></i></span></span><strong>${e(r.label)}</strong><span class="role-card-desc">${e(r.description)}</span><span class="role-feature-list">${r.features.map(f => `<span><i class="fa-solid fa-check"></i>${e(f)}</span>`).join('')}</span></button>`).join('')}</div>
        <div class="role-selection-bottom"><div class="role-selected-copy"><span class="role-selected-indicator"></span><span id="role-selection-hint">${stored ? `Vai trò đã chọn: ${e(HMSRoles.definitions[stored].label)}` : 'Chọn một vai trò để tiếp tục'}</span></div><button type="button" id="continue-role-button" class="btn btn-primary role-continue" ${stored ? '' : 'disabled'}>Vào không gian làm việc <i class="fa-solid fa-arrow-right"></i></button></div>
        <p class="role-security-note"><i class="fa-solid fa-circle-info"></i> Bản demo lưu vai trò trong trình duyệt. Khi kết nối Supabase, vai trò phải được quản trị viên gán trong cơ sở dữ liệu; người dùng không tự nâng quyền bằng màn hình này.</p>
      </main>
    </div>`;
    let selectedRole = stored;
    root.querySelectorAll('[data-role-choice]').forEach(card => card.addEventListener('click', () => {
      selectedRole = card.dataset.roleChoice;
      root.querySelectorAll('[data-role-choice]').forEach(item => {
        const selected = item.dataset.roleChoice === selectedRole;
        item.classList.toggle('selected', selected);
        item.setAttribute('aria-pressed', String(selected));
      });
      const hint = $('#role-selection-hint');
      if (hint) hint.textContent = `Vai trò đã chọn: ${HMSRoles.definitions[selectedRole].label}`;
      const button = $('#continue-role-button');
      if (button) button.disabled = false;
    }));
    $('#continue-role-button')?.addEventListener('click', () => {
      if (!selectedRole) return;
      HMSRoles.setDemoRole(selectedRole);
      location.href = HMSRoles.startPage(selectedRole);
    });
  }

  function renderAuthScreen(message = '') {
    const root = document.getElementById('app-root');
    root.innerHTML = `<div class="auth-layout"><div class="auth-card"><div class="brand auth-brand"><div class="brand-mark"><i class="fa-solid fa-heart-pulse"></i></div><div><div class="brand-name">MediCore</div><div class="brand-caption">HOSPITAL SYSTEM</div></div></div><div class="auth-badge"><i class="fa-solid fa-shield-halved"></i> KHU VỰC BẢO MẬT</div><h1>Đăng nhập hệ thống</h1><p class="auth-description">Đăng nhập bằng tài khoản Supabase Auth do quản trị viên bệnh viện cấp. Vai trò sẽ được đọc từ cơ sở dữ liệu, không chọn thủ công.</p>${message?`<div class="inline-note auth-error"><i class="fa-solid fa-circle-exclamation"></i><span>${e(message)}</span></div>`:''}<form id="hms-login-form"><label class="form-field"><span>Email công việc</span><input type="email" name="email" placeholder="ten@benhvien.vn" required autocomplete="username"></label><label class="form-field" style="margin-top:14px"><span>Mật khẩu</span><input type="password" name="password" placeholder="Nhập mật khẩu" required autocomplete="current-password"></label><button class="btn btn-primary auth-submit" type="submit"><i class="fa-solid fa-right-to-bracket"></i> Đăng nhập</button><p class="auth-footnote"><i class="fa-solid fa-lock"></i> Không sử dụng tài khoản anon/service_role để đăng nhập. Liên hệ quản trị viên nếu chưa được gán vai trò.</p></form></div><div class="auth-decoration"><div class="auth-orb orb-one"></div><div class="auth-orb orb-two"></div><div class="auth-big-icon"><i class="fa-solid fa-heart-pulse"></i></div><h2>Chăm sóc sức khỏe<br>kết nối trên một nền tảng.</h2><p>Tiếp đón, khám chữa bệnh, dược và viện phí được vận hành trong những không gian riêng theo quyền hạn.</p><div class="auth-decoration-bottom"><i class="fa-solid fa-shield-heart"></i> Bảo vệ thông tin người bệnh là ưu tiên hàng đầu.</div></div></div>`;
    $('#hms-login-form')?.addEventListener('submit', async ev => {
      ev.preventDefault();
      const form = ev.currentTarget;
      const btn = form.querySelector('button[type=submit]');
      btn.disabled = true;
      btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang xác thực...';
      try {
        const fd = new FormData(form);
        const { data, error } = await HMSApi.signIn(String(fd.get('email') || '').trim(), String(fd.get('password') || ''));
        if (error) throw error;
        if (!data?.session) throw new Error('Không nhận được phiên đăng nhập.');
        await begin();
      } catch (error) {
        console.error(error);
        renderAuthScreen(error.message || 'Đăng nhập thất bại. Kiểm tra email và mật khẩu.');
      } finally {
        if (btn.isConnected) { btn.disabled = false; btn.innerHTML = '<i class="fa-solid fa-right-to-bracket"></i> Đăng nhập'; }
      }
    });
  }

  function renderUnassignedRoleScreen() {
    const root = document.getElementById('app-root');
    root.innerHTML = `<div class="access-error-screen"><div class="access-error-card"><span class="access-error-icon"><i class="fa-solid fa-user-lock"></i></span><div class="auth-badge">TÀI KHOẢN ĐÃ XÁC THỰC</div><h1>Chưa được gán vai trò</h1><p>Tài khoản đã đăng nhập nhưng chưa có bản ghi trong <code>hms_user_roles</code>. Quản trị viên cần gán vai trò và mã nhân viên trước khi sử dụng hệ thống.</p><button id="unassigned-signout" class="btn btn-primary">Đăng xuất</button></div></div>`;
    $('#unassigned-signout')?.addEventListener('click', async () => { await HMSApi.signOut(); location.replace('chon-vai-tro.html'); });
  }

  function mountApp() {
    const config = PAGE_CONFIG[pageType] || PAGE_CONFIG.main;
    UI.shell({ active: config[0], title: config[1], subtitle: config[2], standalone: pageType !== 'main' });
    const accessNotice = sessionStorage.getItem('hms_access_notice');
    if (accessNotice) { sessionStorage.removeItem('hms_access_notice'); setTimeout(() => UI.showToast(accessNotice, 'warning'), 120); }
    if (!HMSApi.isSupabase) UI.setHeading(config[1], config[2], '<span class="pill"><i class="fa-solid fa-flask"></i> Chế độ dữ liệu mẫu</span>');
    $('#app-content')?.addEventListener('click', onContentClick);
    document.addEventListener('click', onDocumentClick);
    window.addEventListener('hashchange', () => {
      if (pageType !== 'main') return;
      const next = location.hash.replace('#','');
      if (['dashboard','patients','exams','pharmacy','billing','staff'].includes(next)) { activeTab = next; renderMain(); }
    });
    refresh();
  }
  function renderMain() {
    const tabs = [
      ['dashboard','fa-chart-pie','Tổng quan'],['patients','fa-hospital-user','Tiếp đón & Bệnh nhân'],['exams','fa-stethoscope','Khám bệnh & Xét nghiệm'],
      ['pharmacy','fa-pills','Dược & Đơn thuốc'],['billing','fa-file-invoice-dollar','Viện phí'],['staff','fa-user-doctor','Nhân sự & Chuyên khoa']
    ];
    const tabHtml = `<div class="module-tabs">${tabs.map(([key,icon,label]) => `<button class="module-tab ${activeTab === key ? 'active' : ''}" data-tab="${key}"><i class="fa-solid ${icon}"></i>${label}</button>`).join('')}</div>`;
    $('#app-content').innerHTML = tabHtml + `<div id="module-panel">${UI.emptyState('fa-spinner','Đang tải dữ liệu','')}</div>`;
    const titles = { dashboard:['Tổng quan','Chào mừng trở lại. Dưới đây là tình hình hoạt động của bệnh viện.'], patients:['Tiếp đón & Bệnh nhân','Quản lý hồ sơ hành chính, người nhà, BHYT và bệnh án.'], exams:['Khám bệnh & Xét nghiệm','Điều phối phòng khám, bác sĩ, chẩn đoán và kết quả cận lâm sàng.'], pharmacy:['Dược & Đơn thuốc','Theo dõi đơn kê, lô thuốc, hạn sử dụng và tồn kho.'], billing:['Viện phí','Theo dõi hóa đơn và đối soát trạng thái thanh toán.'], staff:['Nhân sự & Chuyên khoa','Danh sách nhân viên y tế và thông tin chuyên môn.'] };
    UI.setHeading(titles[activeTab]?.[0] || 'Tổng quan', titles[activeTab]?.[1] || '');
    if (!HMSApi.isSupabase) $('#page-actions').innerHTML = '<button class="btn btn-ghost btn-sm" data-action="reset-demo"><i class="fa-solid fa-rotate-left"></i> Đặt lại dữ liệu demo</button>';
    const panel = $('#module-panel');
    if (!panel) return;
    const renderer = { dashboard:renderDashboard, patients:renderPatients, exams:renderExams, pharmacy:renderPharmacy, billing:renderBilling, staff:renderStaff }[activeTab] || renderDashboard;
    panel.innerHTML = renderer();
    if (activeTab === 'patients') { wireSearch('patient-search','patient-table-body',renderPatientRows); wireFilter('patient-status-filter','patient-table-body',renderPatientRows); }
    if (activeTab === 'staff') { wireSearch('staff-search','staff-table-body',renderStaffRows); wireFilter('staff-role-filter','staff-table-body',renderStaffRows); }
    if (activeTab === 'pharmacy') wireSearch('drug-search','drug-table-body',renderDrugRows);
    if (activeTab === 'billing') wireSearch('invoice-search','invoice-table-body',renderInvoiceRows);
    if (activeTab === 'exams') { wireSearch('exam-search','exam-table-body',renderExamRows); wireFilter('exam-status-filter','exam-table-body',renderExamRows); }
  }
  function wireSearch(inputId, targetId, renderRows) {
    const input = document.getElementById(inputId);
    input?.addEventListener('input', () => { searchText = input.value.trim().toLocaleLowerCase('vi'); const target = document.getElementById(targetId); if (target) target.innerHTML = renderRows(); });
  }
  function wireFilter(inputId, targetId, renderRows) {
    const input = document.getElementById(inputId);
    input?.addEventListener('change', () => { const target = document.getElementById(targetId); if (target) target.innerHTML = renderRows(); });
  }
  function renderDashboard() {
    const today = todayExams();
    const patientsToday = new Set(today.map(x => x.id_bn)).size;
    const pending = (all.phieu_kham || []).filter(x => ['Chờ khám','Chờ xét nghiệm','Chờ thanh toán'].includes(x.trang_thai)).length;
    const revenue = (all.phieu_thu_vien_phi || []).filter(x => x.trang_thai === 'Đã thanh toán' && x.thoi_gian_thu === dayToday()).reduce((s,x) => s + Number(x.so_tien || 0),0);
    const stockTotal = (all.thuoc || []).reduce((s,x) => s + Number(x.so_luong_ton_kho || 0),0);
    const lowStock = (all.thuoc || []).filter(x => Number(x.so_luong_ton_kho || 0) < 50).length;
    const cards = [
      ['Bệnh nhân trong ngày',patientsToday,'fa-hospital-user','blue','Theo phiếu khám hôm nay',''],
      ['Phiếu đang chờ',pending,'fa-clock','orange','Khám, xét nghiệm hoặc thu phí',''],
      ['Doanh thu đã thu',UI.money(revenue),'fa-wallet','green','Hóa đơn ghi nhận hôm nay',''],
      ['Tồn kho thuốc',stockTotal.toLocaleString('vi-VN'),'fa-capsules','violet',`${lowStock} thuốc có tồn dưới 50`,'']
    ];
    const cardsHtml = cards.map(([label,value,icon,tone,sub]) => `<div class="stat-card"><div class="stat-top"><span class="stat-label">${label}</span><span class="stat-icon tone-${tone}"><i class="fa-solid ${icon}"></i></span></div><div class="stat-value">${value}</div><div class="stat-sub">${tone === 'green' ? '<span class="trend-up"><i class="fa-solid fa-check"></i></span>' : '<i class="fa-solid fa-circle-info"></i>'}<span>${sub}</span></div><i class="fa-solid ${icon} stat-watermark"></i></div>`).join('');
    const recent = [...(all.phieu_kham || [])].sort((a,b) => String(b.ngay_dky).localeCompare(String(a.ngay_dky))).slice(0,5).map(x => `<tr><td><span class="id-code">${e(x.id_phieukham)}</span></td><td>${person(namePatient(x.id_bn), x.id_bn)}</td><td>${e(nameRoom(x.id_pk))}</td><td>${e(nameStaff(x.id_nv))}</td><td>${UI.badge(x.trang_thai)}</td><td><button class="btn btn-ghost btn-sm" data-action="doctor" data-id="${e(x.id_phieukham)}">Mở phiếu</button></td></tr>`).join('');
    const maxCount = Math.max(5, ...[8,11,9,15,12,17, today.length || 4]);
    const labels = ['T2','T3','T4','T5','T6','T7','CN'];
    const values = [8,11,9,15,12,17,Math.max(4,today.length)];
    const chart = values.map((v,i) => `<div class="chart-col"><div class="chart-bar ${i === 6 ? 'accent' : ''}" style="height:${Math.max(8,v/maxCount*100)}%" title="${v} lượt"></div><span class="chart-label">${labels[i]}</span></div>`).join('');
    return `<div class="stat-grid">${cardsHtml}</div>
      <div class="section-card" style="margin-bottom:19px"><div class="section-card-header"><div><div class="section-card-title"><i class="fa-solid fa-bolt"></i> Truy cập nhanh</div><div class="section-card-subtitle">Các tác vụ thường dùng trong ca làm việc</div></div></div><div class="section-card-body"><div class="quick-actions">
      <button class="quick-action" data-action="new-patient"><span class="quick-icon"><i class="fa-solid fa-user-plus"></i></span><span><strong>Tiếp nhận bệnh nhân</strong><span>Tạo hồ sơ mới</span></span></button>
      <button class="quick-action" data-action="create-exam"><span class="quick-icon"><i class="fa-solid fa-calendar-plus"></i></span><span><strong>Đăng ký khám</strong><span>Tạo phiếu khám</span></span></button>
      <button class="quick-action" data-tab="billing"><span class="quick-icon"><i class="fa-solid fa-receipt"></i></span><span><strong>Thu viện phí</strong><span>Đối soát hóa đơn</span></span></button>
      <button class="quick-action" data-tab="pharmacy"><span class="quick-icon"><i class="fa-solid fa-prescription-bottle-medical"></i></span><span><strong>Kho dược</strong><span>Kiểm tra tồn kho</span></span></button>
      </div></div></div>
      <div class="dashboard-grid"><section class="section-card"><div class="section-card-header"><div><div class="section-card-title"><i class="fa-solid fa-chart-column"></i> Lượt tiếp nhận trong tuần</div><div class="section-card-subtitle">Minh họa tổng hợp theo ngày trong tuần</div></div><span class="pill">7 ngày</span></div><div class="section-card-body"><div class="chart-wrap">${chart}</div><div class="chart-legend"><span><i class="legend-dot"></i> Các ngày trước</span><span><i class="legend-dot accent"></i> Hôm nay</span></div></div></section>
      <section class="section-card"><div class="section-card-header"><div><div class="section-card-title"><i class="fa-solid fa-boxes-stacked"></i> Cảnh báo tồn kho</div><div class="section-card-subtitle">Thuốc có lượng tồn thấp hoặc gần hết hạn</div></div><button class="text-link" data-tab="pharmacy">Xem kho thuốc <i class="fa-solid fa-arrow-right"></i></button></div><div class="section-card-body"><div class="bar-list">${(all.thuoc || []).slice().sort((a,b) => Number(a.so_luong_ton_kho)-Number(b.so_luong_ton_kho)).slice(0,4).map((m,i) => `<div><div class="bar-item-header"><span>${e(m.ten_thuoc)}</span><strong class="${Number(m.so_luong_ton_kho)<50?'inventory-low':''}">${Number(m.so_luong_ton_kho).toLocaleString('vi-VN')} ${e(m.don_vi || 'đv')}</strong></div><div class="bar-track"><div class="bar-fill" style="width:${Math.min(100,Number(m.so_luong_ton_kho)/Math.max(1,Math.max(...all.thuoc.map(x=>Number(x.so_luong_ton_kho||0))))*100)}%;${Number(m.so_luong_ton_kho)<50?'background:linear-gradient(90deg,#f9ca78,#dc8a22)':''}"></div></div></div>`).join('')}</div></div></section></div>
      <section class="section-card" style="margin-top:18px"><div class="section-card-header"><div><div class="section-card-title"><i class="fa-solid fa-list-check"></i> Hoạt động khám gần đây</div><div class="section-card-subtitle">Các phiếu khám mới nhất trên hệ thống</div></div><button class="text-link" data-tab="exams">Mở danh sách <i class="fa-solid fa-arrow-right"></i></button></div>${table(['MÃ PHIẾU','BỆNH NHÂN','PHÒNG KHÁM','BÁC SĨ','TRẠNG THÁI',''],recent,'Chưa có phiếu khám.')}</section>`;
  }
  function renderPatients() {
    const rows = renderPatientRows();
    return `<div class="section-card"><div class="section-card-header"><div><div class="section-card-title"><i class="fa-solid fa-address-card"></i> Danh sách bệnh nhân</div><div class="section-card-subtitle">Thông tin bệnh nhân và liên kết tới hồ sơ liên quan</div></div><button class="btn btn-primary" data-action="new-patient"><i class="fa-solid fa-plus"></i> Thêm bệnh nhân</button></div><div class="section-card-body"><div class="table-toolbar"><label class="search-control"><i class="fa-solid fa-magnifying-glass"></i><input id="patient-search" type="search" placeholder="Tìm theo họ tên, CCCD hoặc SĐT..."></label><select class="filter-select" id="patient-status-filter"><option value="all">Tất cả trạng thái</option><option>Ổn định</option><option>Đang theo dõi</option></select><span class="toolbar-spacer"></span><span class="result-count">${(all.benh_nhan||[]).length} hồ sơ</span></div><div id="patient-table-body">${rows}</div></div><div class="table-footer"><span>Dữ liệu bệnh nhân • SĐT được xử lý ở dạng mảng TEXT[]</span><span class="pill"><i class="fa-solid fa-shield-halved"></i> Dữ liệu cần bảo mật</span></div></div>`;
  }
  function renderPatientRows() {
    const filter = (searchText || '').toLocaleLowerCase('vi');
    const status = $('#patient-status-filter')?.value || 'all';
    const patients = (all.benh_nhan || []).filter(p => {
      const hay = `${p.ho_ten} ${p.cccd} ${UI.phoneText(p.sdt)} ${p.id_bn}`.toLocaleLowerCase('vi');
      return hay.includes(filter) && (status === 'all' || p.trang_thai === status);
    });
    return table(['BỆNH NHÂN','MÃ BN','CCCD','SỐ ĐIỆN THOẠI','GIỚI TÍNH','NGÀY TẠO','TRẠNG THÁI',''], patients.map((p,i) => {
      const bh = (all.bao_hiem_yte || []).find(x => x.id_bn === p.id_bn);
      return `<tr><td>${person(p.ho_ten,p.dia_chi||'Chưa cập nhật', ['','violet','green','orange'][i%4])}</td><td><span class="id-code">${e(p.id_bn)}</span></td><td>${e(p.cccd||'—')}</td><td>${e(UI.phoneText(p.sdt))}</td><td>${e(p.gioi_tinh||'—')}</td><td>${UI.date(p.ngay_tao)}</td><td>${UI.badge(bh && bh.ngay_ket_thuc < dayToday() ? 'BHYT hết hạn' : (p.trang_thai || 'Ổn định'))}</td><td><div class="row-actions"><button class="btn-icon" title="Xem bệnh án" data-action="record" data-id="${e(p.id_bn)}"><i class="fa-regular fa-folder-open"></i></button><button class="btn-icon" title="Sửa hồ sơ" data-action="edit-patient" data-id="${e(p.id_bn)}"><i class="fa-regular fa-pen-to-square"></i></button></div></td></tr>`;
    }).join(''), 'Không tìm thấy bệnh nhân phù hợp.');
  }
  function renderExams() {
    return `<div class="section-card"><div class="section-card-header"><div><div class="section-card-title"><i class="fa-solid fa-stethoscope"></i> Danh sách phiếu khám</div><div class="section-card-subtitle">Theo dõi hàng đợi, bác sĩ phụ trách và trạng thái xử lý</div></div><button class="btn btn-primary" data-action="create-exam"><i class="fa-solid fa-plus"></i> Tạo phiếu khám</button></div><div class="section-card-body"><div class="table-toolbar"><label class="search-control"><i class="fa-solid fa-magnifying-glass"></i><input id="exam-search" type="search" placeholder="Tìm mã phiếu hoặc tên bệnh nhân..."></label><select class="filter-select" id="exam-status-filter"><option value="all">Tất cả trạng thái</option><option>Chờ khám</option><option>Chờ xét nghiệm</option><option>Chờ thanh toán</option><option>Đã khám</option></select><span class="toolbar-spacer"></span><span class="result-count">${(all.phieu_kham||[]).length} phiếu khám</span></div><div id="exam-table-body">${renderExamRows()}</div></div><div class="table-footer"><span>Chẩn đoán theo mã ICD • Có thể mở quy trình khám để cập nhật triệu chứng và xét nghiệm</span><span class="pill">${todayExams().length} phiếu hôm nay</span></div></div>
      <div class="two-col" style="margin-top:18px"><section class="section-card"><div class="section-card-header"><div><div class="section-card-title"><i class="fa-solid fa-vials"></i> Danh mục xét nghiệm</div><div class="section-card-subtitle">Các loại xét nghiệm đang hoạt động</div></div></div>${table(['MÃ XN','TÊN XÉT NGHIỆM','CHI PHÍ','TRẠNG THÁI'],(all.loai_xet_nghiem||[]).map(x=>`<tr><td><span class="id-code">${e(x.id_lxn)}</span></td><td>${e(x.ten_lxn)}</td><td>${UI.money(x.chi_phi)}</td><td>${UI.badge(x.trang_thai)}</td></tr>`).join(''))}</section>
      <section class="section-card"><div class="section-card-header"><div><div class="section-card-title"><i class="fa-solid fa-stethoscope"></i> Danh mục phòng khám</div><div class="section-card-subtitle">Phòng khám và chuyên khoa</div></div></div>${table(['PHÒNG KHÁM','SỐ PHÒNG','CHUYÊN KHOA','TRẠNG THÁI'],(all.phong_kham||[]).map(x=>`<tr><td>${e(x.ten_phong)}</td><td>${e(x.so_phong)}</td><td>${e(rowBy('chuyen_khoa','id_ck',x.id_ck)?.ten_ck||'—')}</td><td>${UI.badge(x.trang_thai)}</td></tr>`).join(''))}</section></div>`;
  }
  function renderExamRows() {
    const filter = (searchText || '').toLocaleLowerCase('vi');
    const status = $('#exam-status-filter')?.value || 'all';
    const exams = [...(all.phieu_kham || [])].filter(x => `${x.id_phieukham} ${namePatient(x.id_bn)} ${nameStaff(x.id_nv)}`.toLocaleLowerCase('vi').includes(filter) && (status === 'all' || x.trang_thai === status)).sort((a,b)=>String(b.ngay_dky).localeCompare(String(a.ngay_dky)));
    return table(['MÃ PHIẾU','BỆNH NHÂN','PHÒNG','BÁC SĨ','NGÀY ĐĂNG KÝ','CHẨN ĐOÁN','TRẠNG THÁI','TÁC VỤ'],exams.map(x=>`<tr><td><span class="id-code">${e(x.id_phieukham)}</span></td><td>${person(namePatient(x.id_bn),x.id_bn)}</td><td>${e(nameRoom(x.id_pk))}</td><td>${e(nameStaff(x.id_nv))}</td><td>${UI.date(x.ngay_dky)}${x.thoi_gian_kham?`<div class="person-meta">${e(x.thoi_gian_kham)}</div>`:''}</td><td><span title="${e(nameDiagnosis(x.id_cd))}">${e(x.id_cd ? rowBy('chan_doan','id_cd',x.id_cd)?.ma_icd || '—' : '—')}</span></td><td>${UI.badge(x.trang_thai)}</td><td><div class="row-actions"><button class="btn btn-secondary btn-sm" data-action="doctor" data-id="${e(x.id_phieukham)}"><i class="fa-solid fa-stethoscope"></i> Khám</button><button class="btn-icon" title="Kê đơn" data-action="prescribe" data-id="${e(x.id_phieukham)}"><i class="fa-solid fa-prescription"></i></button></div></td></tr>`).join(''),'Không có phiếu khám phù hợp.');
  }
  function renderPharmacy() {
    return `<div class="stat-grid" style="grid-template-columns:repeat(3,minmax(0,1fr))"><div class="stat-card"><div class="stat-top"><span class="stat-label">Mặt hàng thuốc</span><span class="stat-icon tone-blue"><i class="fa-solid fa-capsules"></i></span></div><div class="stat-value">${(all.thuoc||[]).length}</div><div class="stat-sub">Danh mục thuốc hiện có</div></div><div class="stat-card"><div class="stat-top"><span class="stat-label">Lô thuốc</span><span class="stat-icon tone-violet"><i class="fa-solid fa-boxes-stacked"></i></span></div><div class="stat-value">${(all.lo_thuoc||[]).length}</div><div class="stat-sub">Theo dõi hạn dùng theo lô</div></div><div class="stat-card"><div class="stat-top"><span class="stat-label">Đơn chờ cấp phát</span><span class="stat-icon tone-orange"><i class="fa-solid fa-prescription-bottle-medical"></i></span></div><div class="stat-value">${(all.don_thuoc||[]).filter(x=>x.trang_thai==='Chờ cấp phát').length}</div><div class="stat-sub">Cần xử lý tại quầy dược</div></div></div>
      <section class="section-card"><div class="section-card-header"><div><div class="section-card-title"><i class="fa-solid fa-pills"></i> Danh mục và tồn kho thuốc</div><div class="section-card-subtitle">Tồn hiện tại và số lượng theo các lô</div></div><div class="heading-actions"><button class="btn btn-secondary" data-action="new-lot"><i class="fa-solid fa-box-open"></i> Nhập lô thuốc</button><button class="btn btn-primary" data-action="new-drug"><i class="fa-solid fa-plus"></i> Thêm thuốc</button></div></div><div class="section-card-body"><div class="table-toolbar"><label class="search-control"><i class="fa-solid fa-magnifying-glass"></i><input id="drug-search" type="search" placeholder="Tìm tên thuốc, nhà sản xuất..."></label><span class="toolbar-spacer"></span><span class="result-count">${(all.thuoc||[]).length} mặt hàng</span></div><div id="drug-table-body">${renderDrugRows()}</div></div></section>
      <section class="section-card"><div class="section-card-header"><div><div class="section-card-title"><i class="fa-solid fa-box-open"></i> Quản lý lô thuốc</div><div class="section-card-subtitle">Ưu tiên sử dụng lô có hạn dùng gần hơn (FEFO)</div></div></div>${table(['MÃ LÔ','THUỐC','HẠN SỬ DỤNG','SL TỒN LÔ','GIÁ NHẬP','GIÁ BÁN','CẢNH BÁO'],(all.lo_thuoc||[]).map(x=>{const exp=(x.han_su_dung||'')<dayToday();const soon=!exp&&(new Date(x.han_su_dung)-new Date(dayToday()))/86400000<60;return `<tr><td><span class="id-code">${e(x.so_lo||x.id_lo)}</span></td><td>${e(nameDrug(x.id_thuoc))}</td><td>${UI.date(x.han_su_dung)}</td><td class="${Number(x.so_luong_ton||0)<30?'inventory-low':''}">${Number(x.so_luong_ton||0).toLocaleString('vi-VN')}</td><td>${UI.money(x.gia_nhap)}</td><td>${UI.money(x.gia_ban)}</td><td>${UI.badge(exp?'Hết hạn':soon?'Sắp hết hạn':'Bình thường')}</td></tr>`}).join(''))}</section>
      <section class="section-card"><div class="section-card-header"><div><div class="section-card-title"><i class="fa-solid fa-file-prescription"></i> Đơn thuốc</div><div class="section-card-subtitle">Kê đơn từ phiếu khám hoặc chuyển sang trang cấp phát</div></div><a href="cap-phat.html" class="text-link">Mở quầy cấp phát <i class="fa-solid fa-arrow-right"></i></a></div>${table(['MÃ ĐƠN','BỆNH NHÂN','NGÀY KÊ','SỐ MẶT HÀNG','TỔNG TIỀN','TRẠNG THÁI',''],(all.don_thuoc||[]).map(x=>{const exam=rowBy('phieu_kham','id_phieukham',x.id_phieukham);const count=(all.chi_tiet_don_thuoc||[]).filter(d=>d.id_dt===x.id_dt).length;return `<tr><td><span class="id-code">${e(x.id_dt)}</span></td><td>${e(namePatient(exam?.id_bn))}</td><td>${UI.date(x.ngay_ke)}</td><td>${count}</td><td>${UI.money(x.tong_tien)}</td><td>${UI.badge(x.trang_thai)}</td><td>${x.trang_thai==='Chờ cấp phát'?`<button class="btn btn-secondary btn-sm" data-action="dispense" data-id="${e(x.id_dt)}">Cấp phát</button>`:''}</td></tr>`}).join(''))}</section>`;
  }
  function renderDrugRows() {
    const filter=(searchText||'').toLocaleLowerCase('vi');
    const rows=(all.thuoc||[]).filter(x=>`${x.ten_thuoc} ${x.nha_san_xuat} ${x.id_thuoc}`.toLocaleLowerCase('vi').includes(filter));
    return table(['THUỐC','MÃ THUỐC','NHÀ SẢN XUẤT','ĐƠN VỊ','TỒN KHO','KÊ ĐƠN','TRẠNG THÁI'],rows.map((x,i)=>`<tr><td>${person(x.ten_thuoc,x.id_thuoc,['','violet','green','orange'][i%4])}</td><td><span class="id-code">${e(x.id_thuoc)}</span></td><td>${e(x.nha_san_xuat||'—')}</td><td>${e(x.don_vi||'đơn vị')}</td><td class="${Number(x.so_luong_ton_kho)<50?'inventory-low':''}">${Number(x.so_luong_ton_kho||0).toLocaleString('vi-VN')}</td><td>${x.can_ke_don?'<span class="pill">Theo đơn</span>':'<span class="pill">Không bắt buộc</span>'}</td><td>${UI.badge(Number(x.so_luong_ton_kho)<30?'Sắp hết':'Đủ hàng')}</td></tr>`).join(''));
  }
  function renderBilling() {
    const invoices=(all.phieu_thu_vien_phi||[]);
    const paid=invoices.filter(x=>x.trang_thai==='Đã thanh toán').reduce((sum,x)=>sum+Number(x.so_tien||0),0);
    const pending=invoices.filter(x=>x.trang_thai!=='Đã thanh toán').reduce((sum,x)=>sum+Number(x.so_tien||0),0);
    return `<div class="stat-grid" style="grid-template-columns:repeat(3,minmax(0,1fr))"><div class="stat-card"><div class="stat-top"><span class="stat-label">Tổng số hóa đơn</span><span class="stat-icon tone-blue"><i class="fa-solid fa-receipt"></i></span></div><div class="stat-value">${invoices.length}</div><div class="stat-sub">Tất cả hóa đơn trên hệ thống</div></div><div class="stat-card"><div class="stat-top"><span class="stat-label">Đã thu</span><span class="stat-icon tone-green"><i class="fa-solid fa-circle-check"></i></span></div><div class="stat-value" style="font-size:22px">${UI.money(paid)}</div><div class="stat-sub">Tổng tiền hóa đơn đã thanh toán</div></div><div class="stat-card"><div class="stat-top"><span class="stat-label">Còn phải thu</span><span class="stat-icon tone-orange"><i class="fa-solid fa-hourglass-half"></i></span></div><div class="stat-value" style="font-size:22px">${UI.money(pending)}</div><div class="stat-sub">Hóa đơn chưa thanh toán</div></div></div>
      <section class="section-card"><div class="section-card-header"><div><div class="section-card-title"><i class="fa-solid fa-file-invoice-dollar"></i> Danh sách hóa đơn viện phí</div><div class="section-card-subtitle">Hóa đơn liên kết phiếu khám, phương thức và thời gian thu</div></div><a href="thu-tien.html" class="btn btn-primary"><i class="fa-solid fa-cash-register"></i> Mở quầy thu ngân</a></div><div class="section-card-body"><div class="table-toolbar"><label class="search-control"><i class="fa-solid fa-magnifying-glass"></i><input id="invoice-search" type="search" placeholder="Tìm mã hóa đơn, bệnh nhân..."></label><span class="toolbar-spacer"></span><span class="result-count">${invoices.length} hóa đơn</span></div><div id="invoice-table-body">${renderInvoiceRows()}</div></div></section>`;
  }
  function renderInvoiceRows() {
    const filter=(searchText||'').toLocaleLowerCase('vi');
    const cashierMode=HMSRoles.getRole()==='cashier';
    const rows=(all.phieu_thu_vien_phi||[]).filter(x=>`${x.id_hoadon} ${x.id_bn||''} ${x.id_phieukham}`.toLocaleLowerCase('vi').includes(filter));
    return table(['MÃ HÓA ĐƠN','MÃ BỆNH NHÂN','PHIẾU KHÁM','KHOẢN THU','SỐ TIỀN','PHƯƠNG THỨC','THỜI GIAN THU','TRẠNG THÁI',''],rows.map(x=>{const exam=cashierMode?null:rowBy('phieu_kham','id_phieukham',x.id_phieukham);const patientId=x.id_bn||exam?.id_bn||'—';const patientLabel=cashierMode?`Mã BN ${patientId}`:namePatient(patientId);return `<tr><td><span class="id-code">${e(x.id_hoadon)}</span></td><td>${person(patientLabel,patientId)}</td><td><span class="id-code">${e(x.id_phieukham)}</span></td><td>${e(x.loai_thu||'Viện phí')}</td><td><strong style="color:#294459">${UI.money(x.so_tien)}</strong></td><td>${e(x.phuong_thuc_tt||'—')}</td><td>${UI.date(x.thoi_gian_thu)}</td><td>${UI.badge(x.trang_thai)}</td><td>${x.trang_thai!=='Đã thanh toán'?`<button class="btn btn-secondary btn-sm" data-action="pay" data-id="${e(x.id_hoadon)}">Thanh toán</button>`:'<span class="small-muted"><i class="fa-solid fa-check"></i> Đã thu</span>'}</td></tr>`}).join(''),'Không có hóa đơn phù hợp.');
  }
  function renderStaff() {
    return `<div class="section-card"><div class="section-card-header"><div><div class="section-card-title"><i class="fa-solid fa-people-group"></i> Danh sách nhân viên y tế</div><div class="section-card-subtitle">Hồ sơ chung và phân loại chuyên môn theo vai trò</div></div><button class="btn btn-primary" data-action="new-staff"><i class="fa-solid fa-user-plus"></i> Thêm nhân viên</button></div><div class="section-card-body"><div class="table-toolbar"><label class="search-control"><i class="fa-solid fa-magnifying-glass"></i><input id="staff-search" type="search" placeholder="Tìm tên, mã nhân viên, email..."></label><select class="filter-select" id="staff-role-filter"><option value="all">Tất cả vai trò</option><option>Bác sĩ</option><option>Y tá</option><option>Dược sĩ</option><option>Thu ngân</option><option>Quản lý</option></select><span class="toolbar-spacer"></span><span class="result-count">${(all.nhan_vien||[]).length} nhân viên</span></div><div id="staff-table-body">${renderStaffRows()}</div></div></section>
      <div class="two-col" style="margin-top:18px"><section class="section-card"><div class="section-card-header"><div><div class="section-card-title"><i class="fa-solid fa-stethoscope"></i> Chuyên khoa</div><div class="section-card-subtitle">Đơn vị chuyên môn quản lý phòng khám</div></div></div>${table(['MÃ CK','TÊN CHUYÊN KHOA','SỐ PHÒNG'],(all.chuyen_khoa||[]).map(x=>`<tr><td><span class="id-code">${e(x.id_ck)}</span></td><td>${e(x.ten_ck)}</td><td>${(all.phong_kham||[]).filter(r=>r.id_ck===x.id_ck).length}</td></tr>`).join(''))}</section><section class="section-card"><div class="section-card-header"><div><div class="section-card-title"><i class="fa-solid fa-user-shield"></i> Quyền theo vai trò</div><div class="section-card-subtitle">Quyền được cấu hình theo vai trò; backend phải bật RLS</div></div></div><div class="section-card-body"><div class="bar-list">${['Bác sĩ','Y tá','Dược sĩ','Thu ngân','Quản lý'].map((role,i)=>{const n=(all.nhan_vien||[]).filter(x=>x.vai_tro===role).length;return `<div><div class="bar-item-header"><span>${role}</span><strong>${n} nhân sự</strong></div><div class="bar-track"><div class="bar-fill" style="width:${Math.max(3,n/Math.max(1,(all.nhan_vien||[]).length)*100)}%"></div></div></div>`}).join('')}</div></div></section></div>`;
  }
  function renderStaffRows() {
    const filter=(searchText||'').toLocaleLowerCase('vi');
    const role=$('#staff-role-filter')?.value || 'all';
    const list=(all.nhan_vien||[]).filter(x=>`${x.ho_ten} ${x.id_nv} ${x.email} ${x.vai_tro}`.toLocaleLowerCase('vi').includes(filter)&&(role==='all'||x.vai_tro===role));
    return table(['NHÂN VIÊN','MÃ NV','VAI TRÒ','CHUYÊN MÔN','ĐIỆN THOẠI','EMAIL','TRẠNG THÁI',''],list.map((x,i)=>{
      let specialty='';
      if(x.vai_tro==='Bác sĩ'){const b=rowBy('bac_sy','id_nv',x.id_nv);specialty=b?`${b.hoc_vi||'Bác sĩ'} · ${rowBy('chuyen_khoa','id_ck',b.id_ck)?.ten_ck||'Chưa gán CK'}`:'Chưa cập nhật';}
      else if(x.vai_tro==='Y tá') specialty=rowBy('y_ta','id_nv',x.id_nv)?.cap_bac||'Chưa cập nhật';
      else if(x.vai_tro==='Dược sĩ') specialty=rowBy('duoc_sy','id_nv',x.id_nv)?.bang_cap||'Chưa cập nhật';
      else specialty='—';
      return `<tr><td>${person(x.ho_ten,x.email,['','violet','green','orange'][i%4])}</td><td><span class="id-code">${e(x.id_nv)}</span></td><td>${UI.badge(x.vai_tro)}</td><td>${e(specialty)}</td><td>${e(UI.phoneText(x.sdt))}</td><td>${e(x.email||'—')}</td><td>${UI.badge(x.trang_thai||'Đang làm việc')}</td><td><button class="btn-icon" title="Sửa nhân viên" data-action="edit-staff" data-id="${e(x.id_nv)}"><i class="fa-regular fa-pen-to-square"></i></button></td></tr>`;
    }).join(''));
  }

  async function openPatientModal(id = '') {
    const p=rowBy('benh_nhan','id_bn',id)||{};
    const rel=(all.nguoi_nha||[]).find(x=>x.id_bn===id)||{};
    const bh=(all.bao_hiem_yte||[]).find(x=>x.id_bn===id)||{};
    const edit=!!id;
    const body=`<div class="form-grid">
      <div class="form-section-title"><i class="fa-solid fa-user"></i> Thông tin hành chính</div>
      ${UI.field('Họ và tên *','ho_ten',p.ho_ten||'','text','required')}${UI.field('CCCD','cccd',p.cccd||'','text','inputmode="numeric"')}
      ${UI.field('Ngày sinh','ngay_sinh',p.ngay_sinh||'','date')}${UI.selectField('Giới tính','gioi_tinh',[{value:'Nam',label:'Nam'},{value:'Nữ',label:'Nữ'},{value:'Khác',label:'Khác'}],p.gioi_tinh||'Nữ')}
      ${UI.field('Số điện thoại *','sdt',UI.phoneText(p.sdt)==='—'?'':UI.phoneText(p.sdt),'text','required placeholder="Có thể nhập nhiều số, phân tách bằng dấu phẩy"')}
      ${UI.field('Địa chỉ','dia_chi',p.dia_chi||'')}${UI.selectField('Trạng thái','trang_thai',[{value:'Ổn định',label:'Ổn định'},{value:'Đang theo dõi',label:'Đang theo dõi'}],p.trang_thai||'Ổn định')}
      <div class="form-section-title"><i class="fa-solid fa-people-roof"></i> Người nhà / liên hệ khẩn cấp</div>
      ${UI.field('Họ tên người nhà','rel_ho_ten',rel.ho_ten||'')}${UI.field('Mối quan hệ','rel_moi_quan_he',rel.moi_quan_he||'')}
      ${UI.field('SĐT người nhà','rel_sdt',UI.phoneText(rel.sdt)==='—'?'':UI.phoneText(rel.sdt),'text','placeholder="Có thể nhập nhiều số"')}${UI.selectField('Giới tính người nhà','rel_gioi_tinh',[{value:'',label:'-- Chưa chọn --'},{value:'Nam',label:'Nam'},{value:'Nữ',label:'Nữ'},{value:'Khác',label:'Khác'}],rel.gioi_tinh||'')}
      <div class="form-section-title"><i class="fa-solid fa-shield-heart"></i> Thẻ bảo hiểm y tế (BHYT)</div>
      ${UI.field('Mã thẻ BHYT','so_bhyt',bh.so_bhyt||'')}${UI.field('Mức hưởng (%)','muc_huong',bh.muc_huong??'','number','min="0" max="100" step="1"')}
      ${UI.field('Có hiệu lực từ ngày','ngay_bat_dau',bh.ngay_bat_dau||'','date')}${UI.field('Có hiệu lực đến ngày','ngay_ket_thuc',bh.ngay_ket_thuc||'','date')}
      ${UI.field('Nơi đăng ký KCB ban đầu','noi_dang_ky',bh.noi_dang_ky||'')}${UI.field('Nhóm đối tượng','nhom_doi_tuong',bh.nhom_doi_tuong||'')}
      <div class="form-field full"><small>SĐT được lưu thành mảng PostgreSQL TEXT[]. Các số nhập cách nhau bằng dấu phẩy hoặc xuống dòng.</small></div>
      </div>`;
    UI.openModal({title:edit?'Cập nhật hồ sơ bệnh nhân':'Thêm bệnh nhân mới',subtitle:'Thông tin hành chính, liên hệ khẩn cấp và bảo hiểm y tế',body,submitText:edit?'Lưu thay đổi':'Tạo hồ sơ',size:'lg',onSubmit:async fd=>{
      const patient={id_bn:edit?id:HMSApi.uid('BN'),ho_ten:String(fd.get('ho_ten')||'').trim(),cccd:String(fd.get('cccd')||'').trim(),ngay_sinh:fd.get('ngay_sinh')||null,gioi_tinh:fd.get('gioi_tinh')||'Khác',sdt:HMSApi.normalizePhoneArray(fd.get('sdt')),dia_chi:String(fd.get('dia_chi')||'').trim(),trang_thai:fd.get('trang_thai')||'Ổn định',ngay_tao:p.ngay_tao||dayToday()};
      if(!patient.ho_ten)throw new Error('Họ tên là trường bắt buộc.');
      if(edit)await HMSApi.update('benh_nhan',id,patient);else await HMSApi.insert('benh_nhan',patient);
      const relName=String(fd.get('rel_ho_ten')||'').trim();
      if(relName){const relRow={id_nt:rel.id_nt||HMSApi.uid('NT'),id_bn:patient.id_bn,ho_ten:relName,moi_quan_he:String(fd.get('rel_moi_quan_he')||'').trim(),sdt:HMSApi.normalizePhoneArray(fd.get('rel_sdt')),gioi_tinh:fd.get('rel_gioi_tinh')||'Khác'};await HMSApi.save('nguoi_nha',relRow);}
      const so=String(fd.get('so_bhyt')||'').trim();
      if(so){const bhRow={id_bhyt:bh.id_bhyt||HMSApi.uid('BHYT'),id_bn:patient.id_bn,so_bhyt:so,muc_huong:fd.get('muc_huong')===''?null:Number(fd.get('muc_huong')),ngay_bat_dau:fd.get('ngay_bat_dau')||null,ngay_ket_thuc:fd.get('ngay_ket_thuc')||null,noi_dang_ky:String(fd.get('noi_dang_ky')||'').trim(),nhom_doi_tuong:String(fd.get('nhom_doi_tuong')||'').trim()};await HMSApi.save('bao_hiem_yte',bhRow);}
      UI.showToast(edit?'Đã cập nhật hồ sơ bệnh nhân.':'Đã tạo hồ sơ bệnh nhân.');searchText='';await loadAll();pageType==='main'?renderMain():renderStandalone();
    }});
  }
  async function openExamModal() {
    if(!(all.benh_nhan||[]).length){UI.showToast('Hãy tạo hồ sơ bệnh nhân trước.','warning');return;}
    const body=`<div class="inline-note"><i class="fa-solid fa-circle-info"></i><span>Phiếu khám sẽ liên kết bệnh nhân, phòng khám và bác sĩ; trạng thái ban đầu là <strong>Chờ khám</strong>.</span></div><div class="form-grid">
      ${UI.selectField('Bệnh nhân *','id_bn',options(all.benh_nhan,'id_bn',x=>`${x.ho_ten} (${x.id_bn})`),'','required')}
      ${UI.selectField('Phòng khám *','id_pk',options(all.phong_kham.filter(x=>x.trang_thai==='Hoạt động'),'id_pk',x=>`${x.ten_phong} · ${x.so_phong}`),'','required')}
      ${UI.selectField('Bác sĩ phụ trách *','id_nv',options((all.nhan_vien||[]).filter(x=>x.vai_tro==='Bác sĩ'),'id_nv',x=>x.ho_ten),'','required')}
      ${UI.field('Ngày đăng ký','ngay_dky',dayToday(),'date','required')}${UI.field('Phí khám (VNĐ)','phi_kham','150000','number','min="0" required')}
      ${UI.field('Giờ khám dự kiến','thoi_gian_kham','','time')}
      <div class="form-field full"><span>Triệu chứng ban đầu</span><textarea name="trieu_chung" placeholder="Lý do khám, triệu chứng..." rows="3"></textarea></div>
      </div>`;
    UI.openModal({title:'Tạo phiếu khám mới',subtitle:'Tiếp nhận bệnh nhân vào hàng đợi khám',body,submitText:'Tạo phiếu khám',onSubmit:async fd=>{
      const exam={id_phieukham:HMSApi.uid('PKM'),id_bn:fd.get('id_bn'),id_pk:fd.get('id_pk'),id_nv:fd.get('id_nv'),id_benhan:(all.benh_an||[]).find(x=>x.id_bn===fd.get('id_bn'))?.id_benhan||null,id_cd:null,ngay_dky:fd.get('ngay_dky')||dayToday(),thoi_gian_kham:fd.get('thoi_gian_kham')||'',trieu_chung:String(fd.get('trieu_chung')||'').trim(),phi_kham:Number(fd.get('phi_kham')||0),trang_thai:'Chờ khám'};
      let caseRow=(all.benh_an||[]).find(x=>x.id_bn===exam.id_bn);
      if(!caseRow){caseRow=await HMSApi.insert('benh_an',{id_benhan:HMSApi.uid('BA'),id_bn:exam.id_bn,ngay_tao:dayToday(),tien_su_benh:'Chưa ghi nhận',trang_thai:'Mở'});exam.id_benhan=caseRow.id_benhan;}
      await HMSApi.insert('phieu_kham',exam);
      await HMSApi.insert('phieu_thu_vien_phi',{id_hoadon:HMSApi.uid('HD'),id_phieukham:exam.id_phieukham,so_tien:exam.phi_kham,loai_thu:'Phí khám',phuong_thuc_tt:'',thoi_gian_thu:'',trang_thai:'Chưa thanh toán'});
      UI.showToast(`Đã tạo phiếu khám ${exam.id_phieukham}.`);await loadAll();pageType==='main'?renderMain():renderStandalone();
    }});
  }
  async function openDoctorModal(id) {
    const x=rowBy('phieu_kham','id_phieukham',id);if(!x)return;
    const diagnoses=options(all.chan_doan,'id_cd',d=>`${d.ma_icd} — ${d.ten_benh}`);
    const existingLabs=(all.ket_qua_xet_nghiem||[]).filter(r=>r.id_phieukham===id);
    const selectedLabs=existingLabs.map(r=>r.id_lxn);
    const labChecks=(all.loai_xet_nghiem||[]).map(l=>{const previous=existingLabs.find(r=>r.id_lxn===l.id_lxn);return `<div class="lab-order-card"><label class="check-option"><input type="checkbox" name="lab_ids" value="${e(l.id_lxn)}" ${selectedLabs.includes(l.id_lxn)?'checked':''}><span><strong>${e(l.ten_lxn)}</strong><small>${UI.money(l.chi_phi)}</small></span></label><label class="form-field"><span>Kết quả</span><textarea name="lab_result_${e(l.id_lxn)}" rows="2" placeholder="Chưa nhập kết quả...">${e(previous?.ket_qua||'')}</textarea></label><label class="form-field"><span>Kết luận xét nghiệm</span><input name="lab_conclusion_${e(l.id_lxn)}" value="${e(previous?.ket_luan||'')}" placeholder="Nhận xét chuyên môn"></label></div>`}).join('');
    const body=`<div class="profile-summary" style="margin-bottom:16px"><span class="profile-big"><i class="fa-solid fa-user-doctor"></i></span><div><h3>${e(namePatient(x.id_bn))}</h3><p>Mã phiếu ${e(x.id_phieukham)} · ${e(nameRoom(x.id_pk))} · ${e(nameStaff(x.id_nv))}</p></div>${UI.badge(x.trang_thai)}</div>
      <div class="form-grid">${UI.field('Giờ khám','thoi_gian_kham',x.thoi_gian_kham||new Date().toTimeString().slice(0,5),'time')}${UI.selectField('Chẩn đoán ICD','id_cd',diagnoses,x.id_cd||'')}
      <div class="form-field full"><span>Triệu chứng / khám lâm sàng *</span><textarea name="trieu_chung" required rows="3" placeholder="Triệu chứng, dấu hiệu sinh tồn, khám lâm sàng...">${e(x.trieu_chung||'')}</textarea></div>
      <div class="form-field full"><span>Chỉ định xét nghiệm và kết quả theo từng loại</span><div class="lab-result-grid">${labChecks}</div><small>Đánh dấu xét nghiệm cần chỉ định; có thể ghi kết quả nếu đã có dữ liệu xác nhận.</small></div>
      <div class="form-field full"><span>Kết luận / hướng xử trí</span><textarea name="ket_luan" rows="2" placeholder="Kết luận chuyên môn, lời dặn...">${e(x.ket_luan||'')}</textarea></div></div>`;
    UI.openModal({title:'Phiếu khám & chỉ định',subtitle:'Ghi nhận khám bệnh, mã ICD và xét nghiệm',body,submitText:'Lưu kết quả khám',size:'lg',onSubmit:async fd=>{
      const chosen=fd.getAll('lab_ids');
      const examPatch={trieu_chung:String(fd.get('trieu_chung')||'').trim(),id_cd:fd.get('id_cd')||null,thoi_gian_kham:fd.get('thoi_gian_kham')||'',ket_luan:String(fd.get('ket_luan')||'').trim(),trang_thai:chosen.length?'Chờ xét nghiệm':'Chờ thanh toán'};
      await HMSApi.update('phieu_kham',id,examPatch);
      for(const labId of chosen){const exists=(all.ket_qua_xet_nghiem||[]).find(r=>r.id_phieukham===id&&r.id_lxn===labId);const labResult=String(fd.get(`lab_result_${labId}`)||'').trim();const labConclusion=String(fd.get(`lab_conclusion_${labId}`)||'').trim();const resultRow={id_kqxn:exists?.id_kqxn||HMSApi.uid('KQ'),id_phieukham:id,id_lxn:labId,ket_qua:labResult,ket_luan:labConclusion||String(fd.get('ket_luan')||'').trim(),thoi_gian:labResult?dayToday():'',trang_thai:labResult?'Hoàn thành':'Chờ lấy mẫu'};await HMSApi.save('ket_qua_xet_nghiem',resultRow);}
      for(const old of (all.ket_qua_xet_nghiem||[]).filter(r=>r.id_phieukham===id&&!chosen.includes(r.id_lxn))){await HMSApi.delete('ket_qua_xet_nghiem',old.id_kqxn);}
      await syncInvoiceForExam(id);
      UI.showToast('Đã lưu thông tin khám và chỉ định.');await loadAll();pageType==='main'?renderMain():renderStandalone();
    }});
  }
  function medRow(index, medId='', qty=1, dose='', method='', note='') {
    return `<div class="prescription-row" data-med-row style="display:grid;grid-template-columns:minmax(0,2fr) 85px minmax(0,1.1fr) minmax(0,1.4fr) 30px;gap:8px;align-items:end;border:1px solid #e5edf3;padding:11px;border-radius:10px;margin-bottom:9px"><label class="form-field"><span>Thuốc</span><select name="medicine_id" required>${options(all.thuoc.filter(m=>Number(m.so_luong_ton_kho)>0),'id_thuoc',m=>`${m.ten_thuoc} · tồn ${m.so_luong_ton_kho}`, '-- Chọn thuốc --').map(o=>`<option value="${e(o.value)}" ${o.value===medId?'selected':''}>${e(o.label)}</option>`).join('')}</select></label><label class="form-field"><span>Số lượng</span><input name="quantity" type="number" min="1" step="1" value="${e(qty)}" required></label><label class="form-field"><span>Liều dùng</span><input name="dosage" value="${e(dose)}" placeholder="1 viên/lần" required></label><label class="form-field"><span>Cách dùng</span><input name="usage" value="${e(method)}" placeholder="Sau ăn"></label><button type="button" class="btn-icon" data-remove-med title="Xóa thuốc"><i class="fa-solid fa-xmark"></i></button><label class="form-field" style="grid-column:1/-2"><span>Ghi chú</span><input name="medicine_note" value="${e(note)}" placeholder="Số ngày dùng, lưu ý..."></label></div>`;
  }
  async function openPrescriptionModal(examId) {
    const x=rowBy('phieu_kham','id_phieukham',examId);if(!x)return;
    const existing=(all.don_thuoc||[]).find(d=>d.id_phieukham===examId&&d.trang_thai==='Chờ cấp phát');
    const detailRows=existing?(all.chi_tiet_don_thuoc||[]).filter(d=>d.id_dt===existing.id_dt):[];
    const body=`<div class="profile-summary" style="margin-bottom:16px"><span class="profile-big"><i class="fa-solid fa-prescription-bottle-medical"></i></span><div><h3>${e(namePatient(x.id_bn))}</h3><p>Phiếu ${e(examId)} · ${e(nameDiagnosis(x.id_cd))}</p></div></div>
      <div id="prescription-med-list">${detailRows.length?detailRows.map((d,i)=>medRow(i,d.id_thuoc,d.so_luong,d.lieu_dung,d.cach_dung,d.ghi_chu)).join(''):medRow(0)}</div>
      <button type="button" class="btn btn-secondary btn-sm" id="add-med-row"><i class="fa-solid fa-plus"></i> Thêm thuốc</button><div class="soft-divider"></div><div class="form-grid">${UI.field('Ngày kê','ngay_ke',dayToday(),'date','required')}
      <div class="form-field"><span>Ghi chú đơn thuốc</span><input name="ghi_chu" form="hms-modal-form" placeholder="Lời dặn chung cho đơn thuốc" value="${e(existing?.ghi_chu||'')}"></div></div>
      <div class="inline-note" style="margin-top:15px;margin-bottom:0"><i class="fa-solid fa-circle-info"></i><span>Tổng tiền được tính từ giá bán của lô thuốc đầu tiên còn hạn. Tồn kho chỉ giảm khi dược sĩ xác nhận cấp phát.</span></div>`;
    UI.openModal({title:existing?'Cập nhật đơn thuốc':'Kê đơn thuốc',subtitle:'Thuốc kê đơn cần theo chỉ định chuyên môn của bác sĩ',body,submitText:existing?'Lưu đơn thuốc':'Tạo đơn thuốc',size:'lg',onReady:form=>{
      $('#add-med-row')?.addEventListener('click',()=>$('#prescription-med-list')?.insertAdjacentHTML('beforeend',medRow($('#prescription-med-list').querySelectorAll('[data-med-row]').length)));
      $('#prescription-med-list')?.addEventListener('click',ev=>{const btn=ev.target.closest('[data-remove-med]');if(btn){const rows=$('#prescription-med-list').querySelectorAll('[data-med-row]');if(rows.length>1)btn.closest('[data-med-row]').remove();else UI.showToast('Đơn thuốc cần ít nhất một loại thuốc.','warning');}});
    },onSubmit:async fd=>{
      const meds=fd.getAll('medicine_id'),qtys=fd.getAll('quantity'),doses=fd.getAll('dosage'),usages=fd.getAll('usage'),notes=fd.getAll('medicine_note');
      if(!meds.length||meds.some((v,i)=>!v||Number(qtys[i])<=0))throw new Error('Chọn thuốc và số lượng hợp lệ cho tất cả các dòng.');
      const lots=all.lo_thuoc||[];let total=0;const details=[];
      meds.forEach((medId,i)=>{const med=rowBy('thuoc','id_thuoc',medId);const lotsFor=lots.filter(l=>l.id_thuoc===medId&&Number(l.so_luong_ton)>0&&(!l.han_su_dung||l.han_su_dung>=dayToday())).sort((a,b)=>String(a.han_su_dung).localeCompare(String(b.han_su_dung)));const unit=Number(lotsFor[0]?.gia_ban||0);total+=unit*Number(qtys[i]);details.push({id_ctdt:HMSApi.uid('CT'),id_dt:existing?.id_dt||'',id_thuoc:medId,so_luong:Number(qtys[i]),lieu_dung:String(doses[i]||'').trim(),cach_dung:String(usages[i]||'').trim(),ghi_chu:String(notes[i]||'').trim(),don_gia:unit});if(!med)throw new Error('Không tìm thấy thuốc được chọn.');});
      const orderId=existing?.id_dt||HMSApi.uid('DT');
      const order={id_dt:orderId,id_phieukham:examId,id_nv:currentStaff(),ngay_ke:fd.get('ngay_ke')||dayToday(),tong_tien:total,ghi_chu:String(fd.get('ghi_chu')||'').trim(),trang_thai:'Chờ cấp phát'};
      if(existing){await HMSApi.update('don_thuoc',orderId,order);for(const d of (all.chi_tiet_don_thuoc||[]).filter(z=>z.id_dt===orderId))await HMSApi.delete('chi_tiet_don_thuoc',d.id_ctdt);}else await HMSApi.insert('don_thuoc',order);
      for(const d of details){d.id_dt=orderId;await HMSApi.insert('chi_tiet_don_thuoc',d);}
      await syncInvoiceForExam(examId);
      UI.showToast(`Đã lưu đơn thuốc ${orderId}.`);await loadAll();pageType==='main'?renderMain():renderStandalone();
    }});
  }
  async function syncInvoiceForExam(examId) {
    // Khi dùng Supabase, dùng RPC có kiểm tra role để đồng bộ phí mà không cấp quyền đọc bảng hóa đơn cho bác sĩ/y tá.
    if (HMSApi.isSupabase) {
      const { error } = await HMSApi.syncInvoiceForExam(examId);
      if (error) throw error;
      return;
    }
    // Chế độ demo: đồng bộ tổng phí trong localStorage.
    const [exams, labResults, labTypes, orders, invoices] = await Promise.all([
      HMSApi.list('phieu_kham'), HMSApi.list('ket_qua_xet_nghiem'), HMSApi.list('loai_xet_nghiem'), HMSApi.list('don_thuoc'), HMSApi.list('phieu_thu_vien_phi')
    ]);
    const exam=exams.find(x=>x.id_phieukham===examId); if(!exam)return;
    const labIds=new Set(labResults.filter(x=>x.id_phieukham===examId).map(x=>x.id_lxn));
    const labAmount=labTypes.filter(x=>labIds.has(x.id_lxn)).reduce((sum,x)=>sum+Number(x.chi_phi||0),0);
    const prescriptions=orders.filter(x=>x.id_phieukham===examId);
    const medicineAmount=prescriptions.reduce((sum,x)=>sum+Number(x.tong_tien||0),0);
    const total=Number(exam.phi_kham||0)+labAmount+medicineAmount;
    const current=invoices.find(x=>x.id_phieukham===examId);
    const categories=['Phí khám'];if(labAmount)categories.push('Xét nghiệm');if(medicineAmount)categories.push('Thuốc');
    const patch={so_tien:total,loai_thu:categories.join(' + ')};
    if(current){
      if(Number(current.so_tien||0)!==total){patch.trang_thai='Chưa thanh toán';patch.phuong_thuc_tt='';patch.thoi_gian_thu='';}
      await HMSApi.update('phieu_thu_vien_phi',current.id_hoadon,patch);
    } else {
      await HMSApi.insert('phieu_thu_vien_phi',{id_hoadon:HMSApi.uid('HD'),id_phieukham:examId,...patch,phuong_thuc_tt:'',thoi_gian_thu:'',trang_thai:'Chưa thanh toán'});
    }
  }
  async function payInvoice(id) {
    const inv=rowBy('phieu_thu_vien_phi','id_hoadon',id);if(!inv)return;
    const cashierMode=HMSRoles.getRole()==='cashier';
    const exam=cashierMode?null:rowBy('phieu_kham','id_phieukham',inv.id_phieukham);
    const patientLabel=cashierMode?`Mã BN ${inv.id_bn||'—'}`:namePatient(exam?.id_bn||inv.id_bn);
    const body=`<div class="profile-summary" style="margin-bottom:18px"><span class="profile-big"><i class="fa-solid fa-file-invoice-dollar"></i></span><div><h3>Hóa đơn ${e(inv.id_hoadon)}</h3><p>${e(patientLabel)} · Phiếu ${e(inv.id_phieukham)}</p></div></div><div class="metric-row" style="margin-bottom:18px"><div class="metric-mini"><span>Số tiền phải thu</span><strong style="font-size:21px;color:#0284c7">${UI.money(inv.so_tien)}</strong></div><div class="metric-mini"><span>Khoản thu</span><strong style="font-size:12px">${e(inv.loai_thu||'Viện phí')}</strong></div><div class="metric-mini"><span>Trạng thái hiện tại</span><div style="margin-top:8px">${UI.badge(inv.trang_thai)}</div></div></div><div class="form-grid">${UI.selectField('Phương thức thanh toán *','phuong_thuc_tt',[{value:'Tiền mặt',label:'Tiền mặt'},{value:'Chuyển khoản',label:'Chuyển khoản'},{value:'Thẻ',label:'Thẻ ngân hàng'},{value:'Bảo hiểm',label:'Bảo hiểm / đồng chi trả'}],inv.phuong_thuc_tt||'Tiền mặt','required')}${UI.field('Ngày thu','thoi_gian_thu',dayToday(),'date','required')}</div>`;
    UI.openModal({title:'Xác nhận thanh toán viện phí',subtitle:'Chỉ xác nhận sau khi đã nhận hoặc đối soát khoản tiền',body,submitText:'Xác nhận đã thu',onSubmit:async fd=>{
      await HMSApi.update('phieu_thu_vien_phi',id,{phuong_thuc_tt:fd.get('phuong_thuc_tt'),thoi_gian_thu:fd.get('thoi_gian_thu')||dayToday(),trang_thai:'Đã thanh toán'});
      const same=(all.phieu_thu_vien_phi||[]).filter(z=>z.id_phieukham===inv.id_phieukham);const otherPending=same.some(z=>z.id_hoadon!==id&&z.trang_thai!=='Đã thanh toán');
      if(!otherPending&&exam&&HMSRoles.getRole()==='manager')await HMSApi.update('phieu_kham',exam.id_phieukham,{trang_thai:'Hoàn tất'});
      UI.showToast(`Đã ghi nhận thanh toán ${UI.money(inv.so_tien)}.`);await loadAll();pageType==='main'?renderMain():renderStandalone();
    }});
  }
  async function openLabResult(id) {
    const r=rowBy('ket_qua_xet_nghiem','id_kqxn',id);if(!r)return;
    const exam=rowBy('phieu_kham','id_phieukham',r.id_phieukham);
    const body=`<div class="profile-summary" style="margin-bottom:17px"><span class="profile-big"><i class="fa-solid fa-vials"></i></span><div><h3>${e(nameLab(r.id_lxn))}</h3><p>${e(namePatient(exam?.id_bn))} · Phiếu ${e(r.id_phieukham)}</p></div>${UI.badge(r.trang_thai)}</div><div class="form-grid"><div class="form-field full"><span>Kết quả xét nghiệm *</span><textarea name="ket_qua" required rows="4" placeholder="Nhập kết quả, chỉ số và đơn vị...">${e(r.ket_qua||'')}</textarea></div><div class="form-field full"><span>Kết luận xét nghiệm</span><textarea name="ket_luan" rows="3">${e(r.ket_luan||'')}</textarea></div>${UI.field('Ngày thực hiện','thoi_gian',r.thoi_gian||dayToday(),'date')}</div>`;
    UI.openModal({title:'Cập nhật kết quả xét nghiệm',subtitle:'Nhập kết quả đã được kiểm tra bởi nhân viên xét nghiệm có thẩm quyền',body,submitText:'Lưu kết quả',onSubmit:async fd=>{
      await HMSApi.update('ket_qua_xet_nghiem',id,{ket_qua:String(fd.get('ket_qua')||'').trim(),ket_luan:String(fd.get('ket_luan')||'').trim(),thoi_gian:fd.get('thoi_gian')||dayToday(),trang_thai:'Hoàn thành'});
      const rest=(all.ket_qua_xet_nghiem||[]).filter(z=>z.id_phieukham===r.id_phieukham&&z.id_kqxn!==id);
      if(rest.every(z=>z.trang_thai==='Hoàn thành'))await HMSApi.update('phieu_kham',r.id_phieukham,{trang_thai:'Chờ thanh toán'});
      await syncInvoiceForExam(r.id_phieukham);
      UI.showToast('Đã lưu kết quả xét nghiệm.');await loadAll();pageType==='main'?renderMain():renderStandalone();
    }});
  }
  async function dispenseOrder(id) {
    const order=rowBy('don_thuoc','id_dt',id);if(!order)return;
    if(order.trang_thai==='Đã cấp phát'){UI.showToast('Đơn thuốc này đã được cấp phát.','warning');return;}
    const details=(all.chi_tiet_don_thuoc||[]).filter(x=>x.id_dt===id);
    if(!details.length){UI.showToast('Đơn thuốc chưa có chi tiết thuốc.','error');return;}
    const lotPlans=[];
    const qtyByDrug=new Map();
    for(const d of details)qtyByDrug.set(d.id_thuoc,(qtyByDrug.get(d.id_thuoc)||0)+Number(d.so_luong));
    for(const [drugId,totalNeeded] of qtyByDrug){let needed=totalNeeded;const eligible=(all.lo_thuoc||[]).filter(l=>l.id_thuoc===drugId&&Number(l.so_luong_ton)>0&&(!l.han_su_dung||l.han_su_dung>=dayToday())).sort((a,b)=>String(a.han_su_dung).localeCompare(String(b.han_su_dung)));const available=eligible.reduce((sum,l)=>sum+Number(l.so_luong_ton||0),0);if(available<needed){UI.showToast(`Không đủ tồn kho ${nameDrug(drugId)}: cần ${needed}, còn ${available}.`,'error');return;}for(const lot of eligible){if(needed<=0)break;const take=Math.min(needed,Number(lot.so_luong_ton));lotPlans.push({lot,qty:take});needed-=take;}}
    const dispensePatientLabel=HMSRoles.getRole()==='pharmacist'?`Mã bệnh nhân ${order.id_bn||'—'}`:namePatient(rowBy('phieu_kham','id_phieukham',order.id_phieukham)?.id_bn);
    const body=`<div class="inline-note"><i class="fa-solid fa-circle-info"></i><span>Hệ thống sẽ trừ tồn kho theo lô còn hạn, ưu tiên lô có hạn dùng gần nhất. Vui lòng kiểm tra liều và số lượng trước khi xác nhận.</span></div><div class="profile-summary" style="margin-bottom:16px"><span class="profile-big"><i class="fa-solid fa-prescription-bottle-medical"></i></span><div><h3>${e(dispensePatientLabel)}</h3><p>Đơn ${e(id)} · Tổng tiền ${UI.money(order.tong_tien)}</p></div></div>${table(['THUỐC','LIỀU DÙNG','CÁCH DÙNG','SL CẤP'],details.map(d=>`<tr><td>${e(nameDrug(d.id_thuoc))}</td><td>${e(d.lieu_dung||'—')}</td><td>${e(d.cach_dung||'—')}</td><td><strong>${Number(d.so_luong)}</strong></td></tr>`).join(''))}<div class="form-field" style="margin-top:16px"><span>Ghi chú cấp phát</span><textarea name="dispense_note" rows="2" placeholder="Tình trạng bao bì, hướng dẫn bổ sung..."></textarea></div>`;
    UI.openModal({title:'Xác nhận cấp phát thuốc',subtitle:'Kiểm tra đơn thuốc và cập nhật số lượng thực xuất',body,submitText:'Xác nhận đã cấp',onSubmit:async fd=>{
      for(const p of lotPlans){await HMSApi.update('lo_thuoc',p.lot.id_lo,{so_luong_ton:Number(p.lot.so_luong_ton)-p.qty});}
      for(const [drugId,totalQty] of qtyByDrug){const med=rowBy('thuoc','id_thuoc',drugId);await HMSApi.update('thuoc',drugId,{so_luong_ton_kho:Math.max(0,Number(med?.so_luong_ton_kho||0)-totalQty)});}
      await HMSApi.update('don_thuoc',id,{trang_thai:'Đã cấp phát',ghi_chu:[order.ghi_chu,fd.get('dispense_note')].filter(Boolean).join(' | ')});
      UI.showToast(`Đã cấp phát đơn ${id} và trừ tồn kho.`);await loadAll();pageType==='main'?renderMain():renderStandalone();
    }});
  }
  async function openStaffModal(id='') {
    const x=rowBy('nhan_vien','id_nv',id)||{};const b=rowBy('bac_sy','id_nv',id)||{};const y=rowBy('y_ta','id_nv',id)||{};const d=rowBy('duoc_sy','id_nv',id)||{};
    const body=`<div class="form-grid">${UI.field('Họ tên *','ho_ten',x.ho_ten||'','text','required')}${UI.selectField('Vai trò *','vai_tro',[{value:'Bác sĩ',label:'Bác sĩ'},{value:'Y tá',label:'Y tá'},{value:'Dược sĩ',label:'Dược sĩ'},{value:'Thu ngân',label:'Thu ngân'},{value:'Quản lý',label:'Quản lý'}],x.vai_tro||'Bác sĩ','required')}
      ${UI.selectField('Giới tính','gioi_tinh',[{value:'Nam',label:'Nam'},{value:'Nữ',label:'Nữ'},{value:'Khác',label:'Khác'}],x.gioi_tinh||'Khác')}${UI.field('Ngày sinh','ngay_sinh',x.ngay_sinh||'','date')}${UI.field('SĐT','sdt',UI.phoneText(x.sdt)==='—'?'':UI.phoneText(x.sdt),'text','placeholder="Có thể nhập nhiều số"')}${UI.field('Email','email',x.email||'','email')}
      <div class="form-section-title"><i class="fa-solid fa-certificate"></i> Thông tin chuyên môn (tùy vai trò)</div>${UI.field('Học vị bác sĩ','hoc_vi',b.hoc_vi||'')}${UI.field('Số CCHN','so_cchn',b.so_cchn||'')}${UI.selectField('Chuyên khoa','id_ck',options(all.chuyen_khoa,'id_ck',z=>z.ten_ck),b.id_ck||'')}${UI.field('Cấp bậc y tá','cap_bac',y.cap_bac||'')}${UI.field('Bằng cấp dược sĩ','bang_cap',d.bang_cap||'')}${UI.selectField('Trạng thái','trang_thai',[{value:'Đang làm việc',label:'Đang làm việc'},{value:'Tạm nghỉ',label:'Tạm nghỉ'},{value:'Ngừng làm việc',label:'Ngừng làm việc'}],x.trang_thai||'Đang làm việc')}</div>`;
    UI.openModal({title:id?'Cập nhật nhân viên':'Thêm nhân viên',subtitle:'Thông tin nhân sự và hồ sơ chuyên môn theo vai trò',body,submitText:id?'Lưu thay đổi':'Tạo nhân viên',onSubmit:async fd=>{
      const staff={id_nv:id||HMSApi.uid('NV'),ho_ten:String(fd.get('ho_ten')||'').trim(),vai_tro:fd.get('vai_tro'),gioi_tinh:fd.get('gioi_tinh'),ngay_sinh:fd.get('ngay_sinh')||null,sdt:HMSApi.normalizePhoneArray(fd.get('sdt')),email:String(fd.get('email')||'').trim(),trang_thai:fd.get('trang_thai')};
      await HMSApi.save('nhan_vien',staff);
      if(staff.vai_tro==='Bác sĩ')await HMSApi.save('bac_sy',{id_nv:staff.id_nv,hoc_vi:fd.get('hoc_vi'),so_cchn:fd.get('so_cchn'),id_ck:fd.get('id_ck')||null});
      if(staff.vai_tro==='Y tá')await HMSApi.save('y_ta',{id_nv:staff.id_nv,cap_bac:fd.get('cap_bac')});
      if(staff.vai_tro==='Dược sĩ')await HMSApi.save('duoc_sy',{id_nv:staff.id_nv,bang_cap:fd.get('bang_cap')});
      UI.showToast(id?'Đã cập nhật nhân viên.':'Đã thêm nhân viên.');await loadAll();pageType==='main'?renderMain():renderStandalone();
    }});
  }
  async function openDrugModal() {
    const body=`<div class="form-grid">${UI.field('Tên thuốc *','ten_thuoc','','text','required')}${UI.field('Nhà sản xuất','nha_san_xuat')}${UI.field('Đơn vị tính','don_vi','viên')}${UI.field('Số lượng tồn ban đầu','so_luong_ton_kho','0','number','min="0" required')}${UI.selectField('Có cần kê đơn?','can_ke_don',[{value:'false',label:'Không bắt buộc kê đơn'},{value:'true',label:'Cần đơn kê'}])}<div class="form-field full"><small>Đây là mặt hàng danh mục. Để quản lý hạn sử dụng và giá theo lô, hãy thêm lô trong bảng lô thuốc trên Supabase hoặc trang kho dược.</small></div></div>`;
    UI.openModal({title:'Thêm thuốc vào danh mục',body,submitText:'Thêm thuốc',onSubmit:async fd=>{const row={id_thuoc:HMSApi.uid('T'),ten_thuoc:String(fd.get('ten_thuoc')||'').trim(),nha_san_xuat:String(fd.get('nha_san_xuat')||'').trim(),don_vi:fd.get('don_vi')||'đơn vị',so_luong_ton_kho:Number(fd.get('so_luong_ton_kho')||0),can_ke_don:fd.get('can_ke_don')==='true'};if(!row.ten_thuoc)throw new Error('Tên thuốc là trường bắt buộc.');await HMSApi.insert('thuoc',row);UI.showToast('Đã thêm thuốc.');await loadAll();pageType==='main'?renderMain():renderStandalone();}});
  }
  async function openLotModal() {
    if(!(all.thuoc||[]).length){UI.showToast('Hãy thêm danh mục thuốc trước khi nhập lô.','warning');return;}
    const body=`<div class="inline-note"><i class="fa-solid fa-box-open"></i><span>Số lượng nhập sẽ được cộng vào tồn của mặt hàng thuốc. Hạn dùng phải được kiểm tra theo chứng từ của nhà cung cấp.</span></div><div class="form-grid">${UI.selectField('Thuốc *','id_thuoc',options(all.thuoc,'id_thuoc',x=>`${x.ten_thuoc} (${x.id_thuoc})`),'','required')}${UI.field('Mã lô *','so_lo','','text','required')}${UI.field('Hạn sử dụng *','han_su_dung','','date','required')}${UI.field('Số lượng nhập *','so_luong_nhap','1','number','required min="1" step="1"')}${UI.field('Giá nhập / đơn vị (VNĐ)','gia_nhap','0','number','min="0" step="1" required')}${UI.field('Giá bán / đơn vị (VNĐ)','gia_ban','0','number','min="0" step="1" required')}</div>`;
    UI.openModal({title:'Nhập lô thuốc',subtitle:'Tạo mới một lô để theo dõi tồn kho và hạn sử dụng',body,submitText:'Lưu lô thuốc',onSubmit:async fd=>{
      const medId=fd.get('id_thuoc');const qty=Number(fd.get('so_luong_nhap')||0);const expiry=fd.get('han_su_dung');
      if(!medId||!expiry||qty<=0)throw new Error('Vui lòng chọn thuốc, hạn dùng và số lượng hợp lệ.');
      const lot={id_lo:HMSApi.uid('LO'),id_thuoc:medId,so_lo:String(fd.get('so_lo')||'').trim(),han_su_dung:expiry,so_luong_nhap:qty,so_luong_ton:qty,gia_nhap:Number(fd.get('gia_nhap')||0),gia_ban:Number(fd.get('gia_ban')||0)};
      await HMSApi.insert('lo_thuoc',lot);const med=rowBy('thuoc','id_thuoc',medId);await HMSApi.update('thuoc',medId,{so_luong_ton_kho:Number(med?.so_luong_ton_kho||0)+qty});
      UI.showToast(`Đã nhập lô ${lot.so_lo}.`);await loadAll();pageType==='main'?renderMain():renderStandalone();
    }});
  }
  async function openRecordModal(id) {
    const p=rowBy('benh_nhan','id_bn',id);if(!p)return;
    const bh=(all.bao_hiem_yte||[]).find(x=>x.id_bn===id);const rel=(all.nguoi_nha||[]).filter(x=>x.id_bn===id);const caseRow=(all.benh_an||[]).filter(x=>x.id_bn===id);const exams=(all.phieu_kham||[]).filter(x=>x.id_bn===id).sort((a,b)=>String(b.ngay_dky).localeCompare(String(a.ngay_dky)));
    const body=`<div class="profile-summary"><span class="profile-big"><i class="fa-solid fa-user"></i></span><div><h3>${e(p.ho_ten)}</h3><p>${e(p.id_bn)} · CCCD ${e(p.cccd||'Chưa cập nhật')}</p></div>${UI.badge(p.trang_thai||'Ổn định')}</div><div class="soft-divider"></div><div class="info-grid"><div class="info-item"><span>Ngày sinh</span><strong>${UI.date(p.ngay_sinh)}</strong></div><div class="info-item"><span>Giới tính</span><strong>${e(p.gioi_tinh||'—')}</strong></div><div class="info-item"><span>Số điện thoại</span><strong>${e(UI.phoneText(p.sdt))}</strong></div><div class="info-item"><span>Địa chỉ</span><strong>${e(p.dia_chi||'—')}</strong></div><div class="info-item"><span>Mã BHYT</span><strong>${e(bh?.so_bhyt||'Chưa có')}</strong></div><div class="info-item"><span>Mức hưởng</span><strong>${bh?.muc_huong!=null?`${e(bh.muc_huong)}%`:'—'}</strong></div></div><div class="soft-divider"></div><h3 class="subheading">Bệnh án / tiền sử</h3>${caseRow.length?table(['MÃ BỆNH ÁN','NGÀY TẠO','TIỀN SỬ','TRẠNG THÁI'],caseRow.map(c=>`<tr><td><span class="id-code">${e(c.id_benhan)}</span></td><td>${UI.date(c.ngay_tao)}</td><td>${e(c.tien_su_benh||'—')}</td><td>${UI.badge(c.trang_thai)}</td></tr>`).join('')):UI.emptyState('fa-folder-open','Chưa có bệnh án','Bệnh án sẽ xuất hiện khi được tạo và liên kết với bệnh nhân.')}<div class="soft-divider"></div><h3 class="subheading">Lịch sử khám</h3>${exams.length?table(['NGÀY','PHIẾU','TRIỆU CHỨNG','CHẨN ĐOÁN','TRẠNG THÁI'],exams.map(x=>`<tr><td>${UI.date(x.ngay_dky)}</td><td><span class="id-code">${e(x.id_phieukham)}</span></td><td>${e(x.trieu_chung||'—')}</td><td>${e(nameDiagnosis(x.id_cd))}</td><td>${UI.badge(x.trang_thai)}</td></tr>`).join('')):UI.emptyState('fa-stethoscope','Chưa có lịch sử khám','')}<div class="soft-divider"></div><h3 class="subheading">Người nhà</h3>${rel.length?table(['HỌ TÊN','QUAN HỆ','SĐT'],rel.map(r=>`<tr><td>${e(r.ho_ten)}</td><td>${e(r.moi_quan_he||'—')}</td><td>${e(UI.phoneText(r.sdt))}</td></tr>`).join('')):'<div class="small-muted">Chưa khai báo người nhà.</div>'}`;
    UI.openModal({title:'Hồ sơ bệnh án',subtitle:'Tóm tắt hành chính và quá trình khám của bệnh nhân',body,submitText:'Đóng hồ sơ',size:'lg',onSubmit:async()=>{}});
  }
  function renderStandalone() {
    const config=PAGE_CONFIG[pageType]||PAGE_CONFIG.main;
    const pageActions={registration:'<button class="btn btn-primary" data-action="create-exam"><i class="fa-solid fa-plus"></i> Đăng ký lượt khám</button>',billing:'<button class="btn btn-secondary" data-action="refresh"><i class="fa-solid fa-rotate"></i> Làm mới</button>',doctor:'<button class="btn btn-secondary" data-action="refresh"><i class="fa-solid fa-rotate"></i> Làm mới hàng đợi</button>',lab:'<button class="btn btn-secondary" data-action="refresh"><i class="fa-solid fa-rotate"></i> Làm mới kết quả</button>',dispense:'<button class="btn btn-secondary" data-action="refresh"><i class="fa-solid fa-rotate"></i> Cập nhật tồn kho</button>',records:['manager','nurse'].includes(HMSRoles.getRole())?'<button class="btn btn-primary" data-action="new-patient"><i class="fa-solid fa-user-plus"></i> Thêm hồ sơ</button>':'',reports:'<button class="btn btn-secondary" data-action="export-csv"><i class="fa-solid fa-file-csv"></i> Xuất CSV</button>'};
    UI.setHeading(config[1],config[2],`${!HMSApi.isSupabase?'<span class="pill"><i class="fa-solid fa-flask"></i> Dữ liệu demo</span>':''}${pageActions[pageType]||''}`);
    const content={registration:renderRegistration, billing:renderBilling, doctor:renderDoctor, lab:renderLab, dispense:renderDispense, records:renderRecords, reports:renderReports}[pageType]||renderDashboard;
    $('#app-content').innerHTML=content();
    if(pageType==='billing')wireSearch('invoice-search','invoice-table-body',renderInvoiceRows);
    if(pageType==='registration'){wireSearch('exam-search','exam-table-body',renderExamRows);wireFilter('exam-status-filter','exam-table-body',renderExamRows);}
    if(pageType==='doctor')wireSearch('doctor-search','doctor-table-body',renderDoctorRows);
    if(pageType==='lab')wireSearch('lab-search','lab-table-body',renderLabRows);
    if(pageType==='dispense')wireSearch('dispense-search','dispense-table-body',renderDispenseRows);
    if(pageType==='records')wireSearch('record-search','record-table-body',renderRecordRows);
  }
  function renderRegistration() {
    const queue=(all.phieu_kham||[]).filter(x=>x.ngay_dky===dayToday()&&x.trang_thai!=='Hoàn tất').length;
    return `<div class="stat-grid" style="grid-template-columns:repeat(3,minmax(0,1fr))"><div class="stat-card"><div class="stat-top"><span class="stat-label">Bệnh nhân hôm nay</span><span class="stat-icon tone-blue"><i class="fa-solid fa-hospital-user"></i></span></div><div class="stat-value">${new Set(todayExams().map(x=>x.id_bn)).size}</div><div class="stat-sub">Theo phiếu đăng ký</div></div><div class="stat-card"><div class="stat-top"><span class="stat-label">Phiếu đang chờ</span><span class="stat-icon tone-orange"><i class="fa-solid fa-clock"></i></span></div><div class="stat-value">${queue}</div><div class="stat-sub">Chưa hoàn tất quy trình</div></div><div class="stat-card"><div class="stat-top"><span class="stat-label">Tổng hồ sơ bệnh nhân</span><span class="stat-icon tone-green"><i class="fa-solid fa-users"></i></span></div><div class="stat-value">${(all.benh_nhan||[]).length}</div><div class="stat-sub">Đang lưu trong hệ thống</div></div></div>
      <section class="section-card"><div class="section-card-header"><div><div class="section-card-title"><i class="fa-solid fa-clipboard-list"></i> Tiếp đón & đăng ký khám</div><div class="section-card-subtitle">Tạo phiếu mới hoặc rà soát hàng đợi trong ngày</div></div><button class="btn btn-primary" data-action="create-exam"><i class="fa-solid fa-plus"></i> Tạo phiếu khám</button></div><div class="section-card-body"><div class="inline-note"><i class="fa-solid fa-circle-info"></i><span>Nếu bệnh nhân chưa có hồ sơ, hãy tạo bệnh nhân trước. Khi tạo phiếu khám, hệ thống tự tạo một hóa đơn phí khám ở trạng thái chưa thanh toán.</span></div><div class="table-toolbar"><label class="search-control"><i class="fa-solid fa-magnifying-glass"></i><input id="exam-search" type="search" placeholder="Tìm mã phiếu hoặc bệnh nhân..."></label><span class="toolbar-spacer"></span><button class="btn btn-secondary btn-sm" data-action="new-patient"><i class="fa-solid fa-user-plus"></i> Bệnh nhân mới</button></div><div id="exam-table-body">${renderExamRows()}</div></div></section>`;
  }
  function renderDoctor() {
    const pending=(all.phieu_kham||[]).filter(x=>['Chờ khám','Chờ xét nghiệm','Chờ thanh toán'].includes(x.trang_thai)).length;
    return `<div class="stat-grid" style="grid-template-columns:repeat(3,minmax(0,1fr))"><div class="stat-card"><div class="stat-top"><span class="stat-label">Chờ khám</span><span class="stat-icon tone-blue"><i class="fa-solid fa-user-clock"></i></span></div><div class="stat-value">${(all.phieu_kham||[]).filter(x=>x.trang_thai==='Chờ khám').length}</div><div class="stat-sub">Cần bác sĩ tiếp nhận</div></div><div class="stat-card"><div class="stat-top"><span class="stat-label">Chờ xét nghiệm</span><span class="stat-icon tone-orange"><i class="fa-solid fa-vials"></i></span></div><div class="stat-value">${(all.phieu_kham||[]).filter(x=>x.trang_thai==='Chờ xét nghiệm').length}</div><div class="stat-sub">Đang thực hiện cận lâm sàng</div></div><div class="stat-card"><div class="stat-top"><span class="stat-label">Đang xử lý</span><span class="stat-icon tone-violet"><i class="fa-solid fa-list-check"></i></span></div><div class="stat-value">${pending}</div><div class="stat-sub">Tổng số phiếu chưa kết thúc</div></div></div><section class="section-card"><div class="section-card-header"><div><div class="section-card-title"><i class="fa-solid fa-user-doctor"></i> Hàng đợi bác sĩ</div><div class="section-card-subtitle">Mở phiếu để ghi nhận triệu chứng, mã ICD và chỉ định xét nghiệm</div></div></div><div class="section-card-body"><div class="table-toolbar"><label class="search-control"><i class="fa-solid fa-magnifying-glass"></i><input id="doctor-search" type="search" placeholder="Tìm bệnh nhân hoặc mã phiếu..."></label><span class="toolbar-spacer"></span><span class="pill">Vai trò chọn ở thanh trên</span></div><div id="doctor-table-body">${renderDoctorRows()}</div></div></section>`;
  }
  function renderDoctorRows() {
    const filter=(searchText||'').toLocaleLowerCase('vi');
    const rows=(all.phieu_kham||[]).filter(x=>`${x.id_phieukham} ${namePatient(x.id_bn)} ${nameRoom(x.id_pk)}`.toLocaleLowerCase('vi').includes(filter)&&x.trang_thai!=='Hoàn tất');
    return table(['MÃ PHIẾU','BỆNH NHÂN','TRIỆU CHỨNG','PHÒNG KHÁM','NGÀY','TRẠNG THÁI','TÁC VỤ'],rows.map(x=>`<tr><td><span class="id-code">${e(x.id_phieukham)}</span></td><td>${person(namePatient(x.id_bn),x.id_bn)}</td><td style="max-width:230px;white-space:normal">${e(x.trieu_chung||'Chưa ghi triệu chứng')}</td><td>${e(nameRoom(x.id_pk))}</td><td>${UI.date(x.ngay_dky)}</td><td>${UI.badge(x.trang_thai)}</td><td><div class="row-actions"><button class="btn btn-secondary btn-sm" data-action="doctor" data-id="${e(x.id_phieukham)}">Mở phiếu</button><button class="btn-icon" title="Kê đơn" data-action="prescribe" data-id="${e(x.id_phieukham)}"><i class="fa-solid fa-prescription"></i></button></div></td></tr>`).join(''),'Không có phiếu khám trong hàng đợi.');
  }
  function renderLab() {
    const pending=(all.ket_qua_xet_nghiem||[]).filter(x=>x.trang_thai!=='Hoàn thành').length;
    return `<div class="stat-grid" style="grid-template-columns:repeat(3,minmax(0,1fr))"><div class="stat-card"><div class="stat-top"><span class="stat-label">Chỉ định xét nghiệm</span><span class="stat-icon tone-blue"><i class="fa-solid fa-vials"></i></span></div><div class="stat-value">${(all.ket_qua_xet_nghiem||[]).length}</div><div class="stat-sub">Tổng chỉ định trong dữ liệu</div></div><div class="stat-card"><div class="stat-top"><span class="stat-label">Đang chờ kết quả</span><span class="stat-icon tone-orange"><i class="fa-solid fa-hourglass-half"></i></span></div><div class="stat-value">${pending}</div><div class="stat-sub">Chưa được đánh dấu hoàn thành</div></div><div class="stat-card"><div class="stat-top"><span class="stat-label">Đã hoàn thành</span><span class="stat-icon tone-green"><i class="fa-solid fa-circle-check"></i></span></div><div class="stat-value">${(all.ket_qua_xet_nghiem||[]).filter(x=>x.trang_thai==='Hoàn thành').length}</div><div class="stat-sub">Đã nhập kết quả</div></div></div><section class="section-card"><div class="section-card-header"><div><div class="section-card-title"><i class="fa-solid fa-microscope"></i> Danh sách xét nghiệm</div><div class="section-card-subtitle">Chỉ nhập kết quả theo kết quả chuyên môn đã được xác nhận</div></div></div><div class="section-card-body"><div class="table-toolbar"><label class="search-control"><i class="fa-solid fa-magnifying-glass"></i><input id="lab-search" type="search" placeholder="Tìm bệnh nhân, loại xét nghiệm..."></label><span class="toolbar-spacer"></span></div><div id="lab-table-body">${renderLabRows()}</div></div></section>`;
  }
  function renderLabRows() {
    const filter=(searchText||'').toLocaleLowerCase('vi');
    const rows=(all.ket_qua_xet_nghiem||[]).filter(x=>`${namePatient(rowBy('phieu_kham','id_phieukham',x.id_phieukham)?.id_bn)} ${nameLab(x.id_lxn)} ${x.id_phieukham}`.toLocaleLowerCase('vi').includes(filter));
    return table(['MÃ KQ','BỆNH NHÂN','XÉT NGHIỆM','PHIẾU KHÁM','KẾT QUẢ','NGÀY','TRẠNG THÁI',''],rows.map(x=>{const ex=rowBy('phieu_kham','id_phieukham',x.id_phieukham);return `<tr><td><span class="id-code">${e(x.id_kqxn)}</span></td><td>${person(namePatient(ex?.id_bn),ex?.id_bn||'')}</td><td>${e(nameLab(x.id_lxn))}</td><td><span class="id-code">${e(x.id_phieukham)}</span></td><td style="max-width:240px;white-space:normal">${e(x.ket_qua||'Chưa nhập')}</td><td>${UI.date(x.thoi_gian)}</td><td>${UI.badge(x.trang_thai)}</td><td><button class="btn btn-secondary btn-sm" data-action="lab-result" data-id="${e(x.id_kqxn)}">${x.trang_thai==='Hoàn thành'?'Xem / sửa':'Nhập kết quả'}</button></td></tr>`}).join(''),'Chưa có chỉ định xét nghiệm nào.');
  }
  function renderDispense() {
    return `<div class="inline-note"><i class="fa-solid fa-circle-info"></i><span>Chức năng cấp phát sẽ cập nhật <code>don_thuoc.trang_thai</code>, giảm số lượng <code>lo_thuoc.so_luong_ton</code> theo FEFO và đồng bộ <code>thuoc.so_luong_ton_kho</code>. Kiểm tra dữ liệu tồn trước khi xác nhận.</span></div><section class="section-card"><div class="section-card-header"><div><div class="section-card-title"><i class="fa-solid fa-prescription-bottle-medical"></i> Đơn thuốc chờ cấp</div><div class="section-card-subtitle">Xem từng thuốc trong đơn và xác nhận xuất kho</div></div></div><div class="section-card-body"><div class="table-toolbar"><label class="search-control"><i class="fa-solid fa-magnifying-glass"></i><input id="dispense-search" type="search" placeholder="Tìm mã đơn hoặc bệnh nhân..."></label><span class="toolbar-spacer"></span></div><div id="dispense-table-body">${renderDispenseRows()}</div></div></section>
      <section class="section-card"><div class="section-card-header"><div><div class="section-card-title"><i class="fa-solid fa-boxes-stacked"></i> Tồn kho nhanh</div><div class="section-card-subtitle">Số lượng tồn danh mục thuốc</div></div></div>${table(['THUỐC','NHÀ SẢN XUẤT','TỒN','CẢNH BÁO'],(all.thuoc||[]).map(x=>`<tr><td>${e(x.ten_thuoc)}</td><td>${e(x.nha_san_xuat||'—')}</td><td class="${Number(x.so_luong_ton_kho)<50?'inventory-low':''}">${Number(x.so_luong_ton_kho||0).toLocaleString('vi-VN')} ${e(x.don_vi||'')}</td><td>${UI.badge(Number(x.so_luong_ton_kho)<30?'Sắp hết':'Bình thường')}</td></tr>`).join(''))}</section>`;
  }
  function renderDispenseRows() {
    const filter=(searchText||'').toLocaleLowerCase('vi');
    const pharmacistMode=HMSRoles.getRole()==='pharmacist';
    const rows=(all.don_thuoc||[]).filter(x=>`${x.id_dt} ${x.id_bn||''} ${x.id_phieukham}`.toLocaleLowerCase('vi').includes(filter));
    return table(['MÃ ĐƠN','MÃ BỆNH NHÂN','NGÀY KÊ','CHI TIẾT THUỐC','TỔNG TIỀN','TRẠNG THÁI','TÁC VỤ'],rows.map(x=>{const exam=pharmacistMode?null:rowBy('phieu_kham','id_phieukham',x.id_phieukham);const patientId=x.id_bn||exam?.id_bn||'—';const patientLabel=pharmacistMode?`Mã BN ${patientId}`:namePatient(patientId);const detail=(all.chi_tiet_don_thuoc||[]).filter(d=>d.id_dt===x.id_dt);const details=detail.map(d=>`${nameDrug(d.id_thuoc)} × ${d.so_luong}`).join('; ');return `<tr><td><span class="id-code">${e(x.id_dt)}</span></td><td>${person(patientLabel,patientId)}</td><td>${UI.date(x.ngay_ke)}</td><td style="white-space:normal;min-width:180px;max-width:320px">${e(details||'Chưa có chi tiết')}</td><td><strong>${UI.money(x.tong_tien)}</strong></td><td>${UI.badge(x.trang_thai)}</td><td>${x.trang_thai==='Chờ cấp phát'?`<button class="btn btn-primary btn-sm" data-action="dispense" data-id="${e(x.id_dt)}"><i class="fa-solid fa-check"></i> Cấp phát</button>`:'<span class="small-muted">Đã xử lý</span>'}</td></tr>`}).join(''),'Không có đơn thuốc.');
  }
  function renderRecords() {
    const canCreatePatient = ['manager','nurse'].includes(HMSRoles.getRole());
    return `<section class="section-card"><div class="section-card-header"><div><div class="section-card-title"><i class="fa-solid fa-folder-medical"></i> Tra cứu hồ sơ bệnh án</div><div class="section-card-subtitle">Tìm bằng họ tên, mã bệnh nhân, CCCD hoặc số điện thoại</div></div>${canCreatePatient?'<button class="btn btn-primary" data-action="new-patient"><i class="fa-solid fa-plus"></i> Bệnh nhân mới</button>':''}</div><div class="section-card-body"><div class="table-toolbar"><label class="search-control"><i class="fa-solid fa-magnifying-glass"></i><input id="record-search" type="search" placeholder="Nhập thông tin bệnh nhân..."></label><span class="toolbar-spacer"></span><span class="result-count">${(all.benh_nhan||[]).length} hồ sơ</span></div><div id="record-table-body">${renderRecordRows()}</div></div></section>`;
  }
  function renderRecordRows() {
    const filter=(searchText||'').toLocaleLowerCase('vi');
    const rows=(all.benh_nhan||[]).filter(p=>`${p.ho_ten} ${p.id_bn} ${p.cccd} ${UI.phoneText(p.sdt)}`.toLocaleLowerCase('vi').includes(filter));
    return table(['BỆNH NHÂN','MÃ BN','CCCD','BHYT','BỆNH ÁN','LỊCH SỬ KHÁM',''],rows.map(p=>{const bh=(all.bao_hiem_yte||[]).find(x=>x.id_bn===p.id_bn);const cases=(all.benh_an||[]).filter(x=>x.id_bn===p.id_bn).length;const ex=(all.phieu_kham||[]).filter(x=>x.id_bn===p.id_bn).length;return `<tr><td>${person(p.ho_ten,p.dia_chi||'')}</td><td><span class="id-code">${e(p.id_bn)}</span></td><td>${e(p.cccd||'—')}</td><td>${bh?UI.badge(bh.ngay_ket_thuc<dayToday()?'Hết hạn':`Hưởng ${bh.muc_huong}%`):'<span class="small-muted">Chưa có</span>'}</td><td>${cases}</td><td>${ex} phiếu</td><td><button class="btn btn-secondary btn-sm" data-action="record" data-id="${e(p.id_bn)}">Mở bệnh án</button></td></tr>`}).join(''),'Không tìm thấy hồ sơ.');
  }
  function renderReports() {
    const paid=(all.phieu_thu_vien_phi||[]).filter(x=>x.trang_thai==='Đã thanh toán').reduce((s,x)=>s+Number(x.so_tien||0),0);
    const due=(all.phieu_thu_vien_phi||[]).filter(x=>x.trang_thai!=='Đã thanh toán').reduce((s,x)=>s+Number(x.so_tien||0),0);
    const fulfilled=(all.don_thuoc||[]).filter(x=>x.trang_thai==='Đã cấp phát').length;
    const labs=(all.ket_qua_xet_nghiem||[]).filter(x=>x.trang_thai==='Hoàn thành').length;
    const roles=['Bác sĩ','Y tá','Dược sĩ','Thu ngân','Quản lý'];
    const maxRole=Math.max(1,...roles.map(r=>(all.nhan_vien||[]).filter(x=>x.vai_tro===r).length));
    return `<div class="stat-grid"><div class="stat-card"><div class="stat-top"><span class="stat-label">Tổng bệnh nhân</span><span class="stat-icon tone-blue"><i class="fa-solid fa-users"></i></span></div><div class="stat-value">${(all.benh_nhan||[]).length}</div><div class="stat-sub">Hồ sơ bệnh nhân lưu trữ</div></div><div class="stat-card"><div class="stat-top"><span class="stat-label">Doanh thu đã thu</span><span class="stat-icon tone-green"><i class="fa-solid fa-wallet"></i></span></div><div class="stat-value" style="font-size:22px">${UI.money(paid)}</div><div class="stat-sub">Tổng các hóa đơn đã thu</div></div><div class="stat-card"><div class="stat-top"><span class="stat-label">Công nợ chưa thu</span><span class="stat-icon tone-orange"><i class="fa-solid fa-hourglass"></i></span></div><div class="stat-value" style="font-size:22px">${UI.money(due)}</div><div class="stat-sub">Hóa đơn chờ thanh toán</div></div><div class="stat-card"><div class="stat-top"><span class="stat-label">Đơn đã cấp</span><span class="stat-icon tone-violet"><i class="fa-solid fa-pills"></i></span></div><div class="stat-value">${fulfilled}</div><div class="stat-sub">Trong dữ liệu hiện tại</div></div></div>
      <div class="two-col"><section class="section-card"><div class="section-card-header"><div><div class="section-card-title"><i class="fa-solid fa-chart-simple"></i> Nhân sự theo vai trò</div><div class="section-card-subtitle">Phân bổ nhân sự đang lưu trong hệ thống</div></div></div><div class="section-card-body"><div class="bar-list">${roles.map(r=>{const n=(all.nhan_vien||[]).filter(x=>x.vai_tro===r).length;return `<div><div class="bar-item-header"><span>${r}</span><strong>${n}</strong></div><div class="bar-track"><div class="bar-fill" style="width:${n/maxRole*100}%"></div></div></div>`}).join('')}</div></div></section><section class="section-card"><div class="section-card-header"><div><div class="section-card-title"><i class="fa-solid fa-clipboard-check"></i> Chỉ số vận hành</div><div class="section-card-subtitle">Tổng hợp từ bảng nghiệp vụ, không phải số liệu kế toán chuẩn</div></div></div><div class="section-card-body"><div class="bar-list">${[['Phiếu khám',(all.phieu_kham||[]).length],['Xét nghiệm hoàn thành',labs],['Đơn thuốc',(all.don_thuoc||[]).length],['Mặt hàng thuốc',(all.thuoc||[]).length],['Phòng khám',(all.phong_kham||[]).length]].map(([label,value])=>`<div><div class="bar-item-header"><span>${label}</span><strong>${value}</strong></div><div class="bar-track"><div class="bar-fill" style="width:${Math.min(100,Number(value)/Math.max(1,(all.phieu_kham||[]).length)*100)}%"></div></div></div>`).join('')}</div></div></section></div>
      <section class="section-card" style="margin-top:18px"><div class="section-card-header"><div><div class="section-card-title"><i class="fa-solid fa-download"></i> Xuất dữ liệu báo cáo</div><div class="section-card-subtitle">Tải danh sách phiếu khám dạng CSV để mở bằng Excel</div></div><button class="btn btn-primary" data-action="export-csv"><i class="fa-solid fa-file-csv"></i> Xuất CSV</button></div></section>`;
  }

  async function onContentClick(ev) {
    const tab=ev.target.closest('[data-tab]');if(tab){activeTab=tab.dataset.tab;location.hash=activeTab;searchText='';renderMain();return;}
    const moduleLink=ev.target.closest('[data-module-link]');if(moduleLink&&pageType==='main'){ev.preventDefault();activeTab=moduleLink.dataset.moduleLink;location.hash=activeTab;searchText='';renderMain();return;}
    const action=ev.target.closest('[data-action]');if(action)await runAction(action);
  }
  async function runAction(action) {
    const id=action.dataset.id;const key=action.dataset.action;
    try{
      if(key==='new-patient')await openPatientModal();
      else if(key==='edit-patient')await openPatientModal(id);
      else if(key==='create-exam')await openExamModal();
      else if(key==='doctor')await openDoctorModal(id);
      else if(key==='prescribe')await openPrescriptionModal(id);
      else if(key==='pay')await payInvoice(id);
      else if(key==='lab-result')await openLabResult(id);
      else if(key==='dispense')await dispenseOrder(id);
      else if(key==='record')await openRecordModal(id);
      else if(key==='new-staff')await openStaffModal();
      else if(key==='edit-staff')await openStaffModal(id);
      else if(key==='new-drug')await openDrugModal();
      else if(key==='new-lot')await openLotModal();
      else if(key==='refresh'){searchText='';await refresh();UI.showToast('Đã làm mới dữ liệu.');}
      else if(key==='reset-demo'){if(confirm('Đặt lại toàn bộ dữ liệu mẫu? Các thay đổi trên trình duyệt sẽ bị mất.')){await HMSApi.resetDemo();searchText='';await refresh();UI.showToast('Đã đặt lại dữ liệu demo.');}}
      else if(key==='export-csv')exportExamsCsv();
    }catch(err){showError(err);}
  }
  async function onDocumentClick(ev) {
    const action=ev.target.closest('[data-action]');
    if(action&&!action.closest('#app-content')){await runAction(action);return;}
    const filter=ev.target.closest('#patient-status-filter, #exam-status-filter, #staff-role-filter');
    if(filter){const targetId=filter.id==='patient-status-filter'?'patient-table-body':filter.id==='exam-status-filter'?'exam-table-body':'staff-table-body';const render=filter.id==='patient-status-filter'?renderPatientRows:filter.id==='exam-status-filter'?renderExamRows:renderStaffRows;const target=$(`#${targetId}`);if(target)target.innerHTML=render();}
  }
  function exportExamsCsv() {
    const rows=[['id_phieukham','id_bn','ten_benh_nhan','id_pk','ten_phong','id_nv','bac_si','ngay_dky','trieu_chung','id_cd','trang_thai'],...(all.phieu_kham||[]).map(x=>[x.id_phieukham,x.id_bn,namePatient(x.id_bn),x.id_pk,nameRoom(x.id_pk),x.id_nv,nameStaff(x.id_nv),x.ngay_dky,x.trieu_chung||'',x.id_cd||'',x.trang_thai])];
    const csv='\uFEFF'+rows.map(row=>row.map(value=>`"${String(value??'').replace(/"/g,'""')}"`).join(',')).join('\r\n');
    const blob=new Blob([csv],{type:'text/csv;charset=utf-8;'});const url=URL.createObjectURL(blob);const link=document.createElement('a');link.href=url;link.download=`hms-phieu-kham-${dayToday()}.csv`;link.click();URL.revokeObjectURL(url);UI.showToast('Đã xuất file CSV.');
  }

  function init() { if(!document.getElementById('app-root'))return;begin(); }
  window.HMSApp = { init, refresh, openPatientModal, openExamModal, openDoctorModal, openPrescriptionModal, payInvoice, openLabResult, dispenseOrder, loadAll };
  document.addEventListener('DOMContentLoaded',init);
})();
