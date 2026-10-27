/* Thành phần giao diện dùng chung: layout, modal, thông báo, định dạng và HTML an toàn. */
(function () {
  const $ = (selector, root = document) => root.querySelector(selector);
  const escapeHTML = value => String(value ?? '').replace(/[&<>"']/g, ch => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[ch]));
  const money = value => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(Number(value || 0));
  const date = value => {
    if (!value) return '—';
    const d = new Date(`${String(value).slice(0, 10)}T00:00:00`);
    return Number.isNaN(d.getTime()) ? escapeHTML(value) : d.toLocaleDateString('vi-VN');
  };
  const dateTime = value => value ? new Date(value).toLocaleString('vi-VN') : '—';
  const initials = value => String(value || 'NV').trim().split(/\s+/).slice(-2).map(s => s[0] || '').join('').toUpperCase();
  const phoneText = value => (Array.isArray(value) ? value : value ? [value] : []).join(', ') || '—';
  const statusClass = value => {
    const s = String(value || '').toLowerCase();
    if (/đã thanh toán|đã cấp phát|hoàn thành|đã khám|hoạt động|ổn định|đang làm việc/.test(s)) return 'success';
    if (/chờ|chưa thanh toán|đang theo dõi|sắp hết hạn/.test(s)) return 'warning';
    if (/hết hạn|hủy|ngừng|quá hạn|không hoạt động/.test(s)) return 'danger';
    return 'neutral';
  };
  function badge(text) { return `<span class="status-badge ${statusClass(text)}"><span class="status-dot"></span>${escapeHTML(text || 'Chưa xác định')}</span>`; }
  function showToast(message, type = 'success') {
    let host = $('#toast-host');
    if (!host) { host = document.createElement('div'); host.id = 'toast-host'; host.className = 'toast-host'; document.body.appendChild(host); }
    const node = document.createElement('div');
    node.className = `toast toast-${type}`;
    node.innerHTML = `<i class="fa-solid ${type === 'error' ? 'fa-circle-exclamation' : type === 'warning' ? 'fa-triangle-exclamation' : 'fa-circle-check'}"></i><span>${escapeHTML(message)}</span>`;
    host.appendChild(node);
    setTimeout(() => { node.classList.add('toast-out'); setTimeout(() => node.remove(), 240); }, 3400);
  }
  function shell({ active = 'dashboard', title = 'Tổng quan', subtitle = 'Theo dõi hoạt động khám chữa bệnh', standalone = false } = {}) {
    const roleKey = window.HMSRoles?.getRole() || 'manager';
    const roleDef = window.HMSRoles?.definitions?.[roleKey] || window.HMSRoles?.definitions?.manager;
    const employee = window.HMSRoles?.employeeFor(roleKey) || { name: 'Nhân viên', role: 'Nhân viên y tế' };
    const currentPage = document.body.dataset.page || 'main';
    const menu = window.HMSRoles?.menuFor(roleKey) || [];
    const activeKey = roleKey === 'manager' ? (currentPage === 'main' ? (location.hash.replace('#', '') || active) : active) : currentPage;
    const mainLinks = menu.filter(item => item.tab);
    const utilityLinks = menu.filter(item => !item.tab);
    const navHtml = list => list.map(item => {
      const isActive = item.key === activeKey || (!item.tab && currentPage === item.key);
      return `<a class="nav-link ${isActive ? 'active' : ''}" href="${item.href}" ${item.tab ? `data-module-link="${item.key}"` : ''}><i class="fa-solid ${item.icon}"></i><span>${escapeHTML(item.label)}</span>${item.key === 'exams' ? '<span class="nav-count" id="nav-queue-count">—</span>' : ''}</a>`;
    }).join('');
    const demoRoleSwitch = !window.HMSApi.isSupabase
      ? '<button class="role-switch-button" id="change-role-button" type="button"><i class="fa-solid fa-arrow-right-arrow-left"></i><span>Đổi vai trò</span></button>'
      : '';
    const globalSearch = roleKey === 'manager'
      ? '<label class="global-search"><i class="fa-solid fa-magnifying-glass"></i><input id="global-search" type="search" placeholder="Tìm bệnh nhân, mã phiếu..." autocomplete="off"><kbd>⌘ K</kbd></label>'
      : '';
    document.getElementById('app-root').innerHTML = `
      <div class="app-shell role-${roleKey}">
        <aside class="sidebar" id="sidebar">
          <div class="brand"><div class="brand-mark"><i class="fa-solid fa-heart-pulse"></i></div><div><div class="brand-name">MediCore</div><div class="brand-caption">HOSPITAL SYSTEM</div></div><button class="sidebar-close" id="sidebar-close" aria-label="Đóng menu"><i class="fa-solid fa-xmark"></i></button></div>
          <div class="facility"><div class="facility-icon"><i class="fa-solid fa-building-circle-check"></i></div><div><strong>Bệnh viện đa khoa</strong><span>Cơ sở Hà Nội</span></div><i class="fa-solid fa-chevron-down facility-chevron"></i></div>
          <div class="nav-label">${roleKey === 'manager' ? 'TRUNG TÂM ĐIỀU HÀNH' : 'PHÂN HỆ ĐƯỢC CẤP QUYỀN'}</div>
          <nav class="main-nav">${navHtml(mainLinks.length ? mainLinks : menu)}</nav>
          ${utilityLinks.length ? `<div class="nav-divider"></div><div class="nav-label">${roleKey === 'manager' ? 'QUẢN TRỊ & BÁO CÁO' : 'TÁC VỤ'}</div><nav class="main-nav role-nav">${navHtml(utilityLinks)}</nav>` : ''}
          <div class="sidebar-access-note"><i class="fa-solid fa-shield-halved"></i><span>Quyền hiện tại: <strong>${escapeHTML(roleDef.label)}</strong></span></div>
          <div class="sidebar-bottom"><div class="support-card"><div class="support-icon"><i class="fa-solid fa-headset"></i></div><div><strong>Cần hỗ trợ?</strong><span>Hướng dẫn sử dụng HMS</span></div><i class="fa-solid fa-arrow-up-right-from-square"></i></div><div class="sidebar-version"><span class="live-dot"></span> Hệ thống sẵn sàng <span class="version-no">v2.0.0</span></div></div>
        </aside>
        <div class="sidebar-scrim" id="sidebar-scrim"></div>
        <div class="workspace">
          <header class="topbar"><button class="mobile-menu" id="mobile-menu" aria-label="Mở menu"><i class="fa-solid fa-bars"></i></button><div class="breadcrumb"><span>HMS</span><i class="fa-solid fa-chevron-right"></i><strong>${escapeHTML(title)}</strong></div><div class="topbar-spacer"></div>${globalSearch}<button class="icon-button notification-button" id="notification-button" aria-label="Thông báo"><i class="fa-regular fa-bell"></i><span></span></button><div class="topbar-divider"></div><div class="current-user"><span class="avatar" id="staff-avatar">${initials(employee.name)}</span><span class="staff-meta"><strong id="staff-name">${escapeHTML(employee.name)}</strong><small id="staff-role">${escapeHTML(roleDef.shortLabel)}</small></span></div>${demoRoleSwitch}<button class="icon-button sign-out-button" id="sign-out-button" aria-label="Đăng xuất / thoát vai trò" title="Đăng xuất / thoát vai trò"><i class="fa-solid fa-arrow-right-from-bracket"></i></button></header>
          <main class="page-content"><div class="page-heading"><div><div class="eyebrow"><span class="live-dot"></span> ${roleKey === 'manager' ? 'TRUNG TÂM ĐIỀU HÀNH Y TẾ' : `KHÔNG GIAN LÀM VIỆC · ${escapeHTML(roleDef.shortLabel.toLocaleUpperCase('vi'))}`}</div><h1 id="page-title">${escapeHTML(title)}</h1><p id="page-subtitle">${escapeHTML(subtitle)}</p></div><div class="heading-actions" id="page-actions"></div></div><div id="app-content"></div><footer class="page-footer"><span>© ${new Date().getFullYear()} MediCore HMS</span><span><i class="fa-solid fa-shield-heart"></i> Quyền truy cập theo vai trò · Bảo mật dữ liệu y tế</span></footer></main>
        </div>
      </div>
      <div id="modal-root"></div><div id="toast-host" class="toast-host"></div>`;
    bindShell();
  }
  function bindShell() {
    $('#mobile-menu')?.addEventListener('click', () => { $('#sidebar')?.classList.add('open'); $('#sidebar-scrim')?.classList.add('show'); });
    $('#sidebar-close')?.addEventListener('click', closeSidebar);
    $('#sidebar-scrim')?.addEventListener('click', closeSidebar);
    $('#change-role-button')?.addEventListener('click', () => { location.href = 'chon-vai-tro.html'; });
    window.addEventListener('hashchange', () => {
      const activeTab = location.hash.replace('#', '') || 'dashboard';
      document.querySelectorAll('[data-module-link]').forEach(link => link.classList.toggle('active', link.dataset.moduleLink === activeTab));
    });
    $('#notification-button')?.addEventListener('click', () => showToast('Không có thông báo mới.', 'warning'));
    $('#sign-out-button')?.addEventListener('click', async () => {
      try {
        if (window.HMSApi.isSupabase) {
          const result = await window.HMSApi.signOut();
          if (result.error) throw result.error;
        }
        localStorage.removeItem('hms_current_role');
        localStorage.removeItem('hms_current_staff');
        localStorage.removeItem('hms_demo_role_selected');
        location.href = 'chon-vai-tro.html';
      } catch (error) { showToast(error.message || 'Đăng xuất không thành công.', 'error'); }
    });
    $('#global-search')?.addEventListener('keydown', e => {
      if (e.key === 'Enter') {
        const query = e.target.value.trim();
        if (!query) return;
        location.hash = '#patients';
        window.dispatchEvent(new HashChangeEvent('hashchange'));
        setTimeout(() => { const local = $('#patient-search'); if (local) { local.value = query; local.dispatchEvent(new Event('input')); } else showToast('Đã chuyển đến danh sách bệnh nhân.'); }, 120);
      }
    });
  }
  function closeSidebar() { $('#sidebar')?.classList.remove('open'); $('#sidebar-scrim')?.classList.remove('show'); }
  function setHeading(title, subtitle, actions = '') {
    if ($('#page-title')) $('#page-title').textContent = title;
    if ($('#page-subtitle')) $('#page-subtitle').textContent = subtitle || '';
    if ($('#page-actions')) $('#page-actions').innerHTML = actions;
  }
  function openModal({ title, subtitle = '', body, submitText = 'Lưu thông tin', size = 'md', onSubmit, onReady }) {
    const host = $('#modal-root');
    if (!host) return;
    host.innerHTML = `<div class="modal-backdrop" id="modal-backdrop"><section class="modal-dialog modal-${size}" role="dialog" aria-modal="true" aria-labelledby="modal-title"><div class="modal-header"><div><h2 id="modal-title">${escapeHTML(title)}</h2>${subtitle ? `<p>${escapeHTML(subtitle)}</p>` : ''}</div><button type="button" class="modal-close" id="modal-close" aria-label="Đóng"><i class="fa-solid fa-xmark"></i></button></div><form id="hms-modal-form"><div class="modal-body">${body}</div><div class="modal-footer"><button type="button" class="btn btn-ghost" id="modal-cancel">Hủy</button><button type="submit" class="btn btn-primary"><i class="fa-solid fa-check"></i> ${escapeHTML(submitText)}</button></div></form></section></div>`;
    const close = () => { host.innerHTML = ''; };
    $('#modal-close')?.addEventListener('click', close);
    $('#modal-cancel')?.addEventListener('click', close);
    $('#modal-backdrop')?.addEventListener('click', e => { if (e.target.id === 'modal-backdrop') close(); });
    document.addEventListener('keydown', function escHandler(e) { if (e.key === 'Escape') { close(); document.removeEventListener('keydown', escHandler); } }, { once: true });
    $('#hms-modal-form')?.addEventListener('submit', async e => {
      e.preventDefault();
      const button = e.currentTarget.querySelector('button[type="submit"]');
      const old = button.innerHTML;
      button.disabled = true; button.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang lưu...';
      try { await onSubmit?.(new FormData(e.currentTarget), e.currentTarget); close(); }
      catch (error) { console.error(error); showToast(error.message || 'Không thể lưu dữ liệu.', 'error'); }
      finally { if (button.isConnected) { button.disabled = false; button.innerHTML = old; } }
    });
    onReady?.($('#hms-modal-form'));
  }
  function field(label, name, value = '', type = 'text', attrs = '') {
    return `<label class="form-field"><span>${escapeHTML(label)}</span><input type="${escapeHTML(type)}" name="${escapeHTML(name)}" value="${escapeHTML(value)}" ${attrs}></label>`;
  }
  function selectField(label, name, options, value = '', attrs = '') {
    return `<label class="form-field"><span>${escapeHTML(label)}</span><select name="${escapeHTML(name)}" ${attrs}>${options.map(o => `<option value="${escapeHTML(o.value)}" ${String(o.value) === String(value) ? 'selected' : ''}>${escapeHTML(o.label)}</option>`).join('')}</select></label>`;
  }
  function emptyState(icon, title, text) { return `<div class="empty-state"><div class="empty-state-icon"><i class="fa-solid ${escapeHTML(icon)}"></i></div><strong>${escapeHTML(title)}</strong><p>${escapeHTML(text)}</p></div>`; }
  function tableWrap(headers, rows, emptyText = 'Chưa có dữ liệu phù hợp.') {
    return `<div class="table-scroll"><table class="data-table"><thead><tr>${headers.map(h => `<th>${h}</th>`).join('')}</tr></thead><tbody>${rows || `<tr><td colspan="${headers.length}">${emptyState('fa-inbox', 'Không có dữ liệu', emptyText)}</td></tr>`}</tbody></table></div>`;
  }
  window.HMSUI = { $, escapeHTML, money, date, dateTime, initials, phoneText, statusClass, badge, showToast, shell, setHeading, openModal, field, selectField, emptyState, tableWrap };
})();
