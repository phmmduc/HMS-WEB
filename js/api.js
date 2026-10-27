/* Lớp dữ liệu duy nhất của ứng dụng.
   - Chưa cấu hình: dùng Mock Data + localStorage.
   - Đã cấu hình: gọi Supabase JS v2.
   Lưu ý: bảng/column trên Supabase phải trùng tên với supabase/schema.sql.
*/
(function () {
  const SUPABASE_URL = ''; // TODO: Điền Project URL của bạn
  const SUPABASE_ANON_KEY = ''; // TODO: Điền anon/public key của bạn
  const STORAGE_KEY = 'hms_mock_database_v1';
  const PK = {
    benh_nhan: 'id_bn', nguoi_nha: 'id_nt', bao_hiem_yte: 'id_bhyt', benh_an: 'id_benhan',
    chuyen_khoa: 'id_ck', phong_kham: 'id_pk', nhan_vien: 'id_nv', bac_sy: 'id_nv', y_ta: 'id_nv', duoc_sy: 'id_nv',
    loai_xet_nghiem: 'id_lxn', chan_doan: 'id_cd', phieu_kham: 'id_phieukham', ket_qua_xet_nghiem: 'id_kqxn',
    thuoc: 'id_thuoc', lo_thuoc: 'id_lo', don_thuoc: 'id_dt', chi_tiet_don_thuoc: 'id_ctdt', phieu_thu_vien_phi: 'id_hoadon'
  };
  const isConfigured = () => Boolean(SUPABASE_URL.trim() && SUPABASE_ANON_KEY.trim() && window.supabase?.createClient);
  let client = null;
  if (isConfigured()) client = window.supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY);

  function clone(value) { return JSON.parse(JSON.stringify(value)); }
  function readStore() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (raw) return JSON.parse(raw);
    } catch (e) { console.warn('Không đọc được localStorage; sẽ dùng dữ liệu demo trong bộ nhớ.', e); }
    const seed = clone(window.HMS_SEED || {});
    try { localStorage.setItem(STORAGE_KEY, JSON.stringify(seed)); } catch (_) {}
    return seed;
  }
  let store = readStore();
  function persist() {
    try { localStorage.setItem(STORAGE_KEY, JSON.stringify(store)); }
    catch (e) { console.warn('Không lưu được localStorage.', e); }
  }
  function normalizePhoneArray(value) {
    // PostgreSQL TEXT[]: luôn gửi mảng, kể cả form chỉ có một số điện thoại.
    if (Array.isArray(value)) return value.map(v => String(v).trim()).filter(Boolean);
    if (value === null || value === undefined || value === '') return [];
    return String(value).split(/[,;\n]/).map(v => v.trim()).filter(Boolean);
  }
  function normaliseRow(table, row) {
    const copy = { ...row };
    if (['benh_nhan', 'nguoi_nha', 'nhan_vien'].includes(table) && 'sdt' in copy) copy.sdt = normalizePhoneArray(copy.sdt);
    return copy;
  }
  function uid(prefix = 'ID') {
    const stamp = new Date().toISOString().replace(/[-:TZ.]/g, '').slice(0, 14);
    const rand = Math.random().toString(36).slice(2, 7).toUpperCase();
    return `${prefix}-${stamp}-${rand}`;
  }
  async function list(table, options = {}) {
    if (client) {
      let query = client.from(table).select('*');
      if (options.orderBy) query = query.order(options.orderBy, { ascending: options.ascending !== false });
      if (options.limit) query = query.limit(options.limit);
      const { data, error } = await query;
      if (error) throw error;
      return (data || []).map(row => normaliseRow(table, row));
    }
    let rows;
    if (table === 'v_hms_don_thuoc_cap_phat') {
      const exams = clone(store.phieu_kham || []);
      const examById = new Map(exams.map(row => [String(row.id_phieukham), row]));
      rows = clone(store.don_thuoc || []).map(row => ({ ...row, id_bn: examById.get(String(row.id_phieukham))?.id_bn || null }));
    } else if (table === 'v_hms_phieu_thu_vien_phi') {
      const exams = clone(store.phieu_kham || []);
      const examById = new Map(exams.map(row => [String(row.id_phieukham), row]));
      rows = clone(store.phieu_thu_vien_phi || []).map(row => ({ ...row, id_bn: examById.get(String(row.id_phieukham))?.id_bn || null }));
    } else {
      rows = clone(store[table] || []);
    }
    if (options.orderBy) rows.sort((a, b) => String(a[options.orderBy] ?? '').localeCompare(String(b[options.orderBy] ?? '')) * (options.ascending === false ? -1 : 1));
    if (options.limit) rows = rows.slice(0, options.limit);
    return rows.map(row => normaliseRow(table, row));
  }
  async function insert(table, row) {
    const item = normaliseRow(table, row);
    if (client) {
      const { data, error } = await client.from(table).insert(item).select().single();
      if (error) throw error;
      return normaliseRow(table, data);
    }
    const key = PK[table];
    if (key && !item[key]) item[key] = uid(table.toUpperCase());
    if (!store[table]) store[table] = [];
    store[table].push(clone(item));
    persist();
    return clone(item);
  }
  async function update(table, id, patch, idField = PK[table]) {
    const item = normaliseRow(table, patch);
    if (client) {
      const { data, error } = await client.from(table).update(item).eq(idField, id).select().single();
      if (error) throw error;
      return normaliseRow(table, data);
    }
    const rows = store[table] || [];
    const index = rows.findIndex(row => String(row[idField]) === String(id));
    if (index < 0) throw new Error(`Không tìm thấy bản ghi ${table}.${idField}=${id}`);
    rows[index] = { ...rows[index], ...clone(item) };
    persist();
    return clone(rows[index]);
  }
  async function remove(table, id, idField = PK[table]) {
    if (client) {
      const { error } = await client.from(table).delete().eq(idField, id);
      if (error) throw error;
      return true;
    }
    store[table] = (store[table] || []).filter(row => String(row[idField]) !== String(id));
    persist();
    return true;
  }
  async function save(table, row, idField = PK[table]) {
    const value = normaliseRow(table, row);
    if (value[idField]) {
      const rows = await list(table);
      const existing = rows.find(item => String(item[idField]) === String(value[idField]));
      if (existing) return update(table, value[idField], value, idField);
    }
    return insert(table, value);
  }
  async function resetDemo() {
    if (client) throw new Error('Không thể reset dữ liệu Supabase từ nút demo.');
    store = clone(window.HMS_SEED || {});
    persist();
    return true;
  }
  async function getSession() {
    if (!client) return { data: { session: null }, error: null };
    return client.auth.getSession();
  }
  async function signIn(email, password) {
    if (!client) throw new Error('Supabase chưa được cấu hình.');
    return client.auth.signInWithPassword({ email, password });
  }
  async function signOut() {
    if (!client) return { error: null };
    return client.auth.signOut();
  }
  async function getAssignedRole() {
    if (!client) return { data: { role: null, employee_id: null, user: null }, error: null };
    const { data: userData, error: userError } = await client.auth.getUser();
    if (userError) return { data: null, error: userError };
    const user = userData?.user;
    if (!user) return { data: { role: null, employee_id: null, user: null }, error: null };
    const { data: assignment, error } = await client
      .from('hms_user_roles')
      .select('role, employee_id')
      .eq('user_id', user.id)
      .maybeSingle();
    if (error) return { data: null, error };
    return { data: { role: assignment?.role || null, employee_id: assignment?.employee_id || null, user }, error: null };
  }
  async function syncInvoiceForExam(examId) {
    if (!client) return { data: null, error: null };
    return client.rpc('hms_sync_invoice_for_exam', { p_exam_id: examId });
  }
  window.HMSApi = { list, insert, update, delete: remove, save, uid, normalizePhoneArray, isSupabase: !!client, resetDemo, primaryKeys: PK, getSession, signIn, signOut, getAssignedRole, syncInvoiceForExam };
})();
