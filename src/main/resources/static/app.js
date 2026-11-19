const $ = s => document.querySelector(s);
const esc = v => String(v ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
let token = localStorage.getItem('token'), role = localStorage.getItem('role'), who = localStorage.getItem('name'), current = null;
const forms = {}, acts = {};

async function api(path, method = 'GET', body) {
  const r = await fetch('/api' + path, {
    method, body: body ? JSON.stringify(body) : undefined,
    headers: {'Content-Type': 'application/json', ...(token ? {Authorization: 'Bearer ' + token} : {})}
  });
  if (r.status === 401 && token) { logout(); throw new Error('Session expired'); }
  const d = await r.json().catch(() => null);
  if (!r.ok) throw new Error((d && (d.error || d.message)) || 'Error ' + r.status);
  return d;
}
function toast(m, bad) { const t = $('#toast'); t.textContent = m; t.className = bad ? 'bad show' : 'show'; setTimeout(() => t.className = '', 2500); }
const money = n => '₹' + Number(n || 0).toLocaleString('en-IN');
const badge = s => `<span class="b ${esc(s)}">${esc(String(s).replace('_', ' '))}</span>`;
const clean = o => Object.fromEntries(Object.entries(o).filter(([, v]) => v !== ''));
const field = (n, l, t = 'text', x = '') => `<label>${l}<input name="${n}" type="${t}" ${x}></label>`;
const btn = (act, id, label, cls = '') => `<button class="sm ${cls}" data-act="${act}" data-id="${id}">${label}</button>`;
const table = (cols, rows) => rows.length
  ? `<div class="scroll"><table><tr>${cols.map(c => `<th>${c[0]}</th>`).join('')}</tr>${rows.map(r => `<tr>${cols.map(c => `<td>${c[1](r)}</td>`).join('')}</tr>`).join('')}</table></div>`
  : '<p class="muted">Nothing here yet.</p>';
const opts = (arr, f) => arr.map(x => `<option value="${x.id}">${esc(f(x))}</option>`).join('');

/* ---------- admin views ---------- */
async function dash() {
  const d = await api('/reports/dashboard');
  const c = (l, v) => `<div class="card"><small>${l}</small><b>${v}</b></div>`;
  return `<div class="cards">${c('Occupancy', d.occupancyRate + '%')}${c('Beds occupied', d.occupiedBeds + ' / ' + d.totalBeds)}${c('Active tenants', d.activeTenants)}
    ${c('Outstanding dues', money(d.outstanding))}${c('Collected', money(d.collected))}${c('Open complaints', d.openComplaints)}</div>
    <h3>Monthly revenue</h3>` + table([['Month', r => esc(r.month)], ['Collected', r => money(r.total)]], d.monthlyRevenue);
}
async function rooms() {
  const b = await api('/beds');
  return `<form data-form="room" class="row">${field('roomNumber', 'Room no.', 'text', 'required')}${field('floor', 'Floor', 'number', 'required')}
    <label>Type<select name="roomType"><option>SINGLE</option><option>DOUBLE</option><option>TRIPLE</option></select></label>
    ${field('rentPerBed', 'Rent per bed', 'number', 'required min=1')}${field('beds', 'Beds', 'number', 'required value=2 min=1 max=10')}<button>Add room</button></form>` +
    table([['Bed ID', r => r.id], ['Room', r => esc(r.room.roomNumber)], ['Floor', r => r.room.floor], ['Type', r => esc(r.room.roomType)],
      ['Bed', r => esc(r.bedLabel)], ['Rent', r => money(r.room.rentPerBed)], ['Status', r => badge(r.status)]], b);
}
async function tenants() {
  const t = await api('/tenants');
  return `<form data-form="tenant" class="row">${field('name', 'Name', 'text', 'required')}${field('email', 'Email', 'email', 'required')}${field('phone', 'Phone', 'text', 'required')}
    ${field('idProofType', 'ID proof type')}${field('idProofNumber', 'ID number')}${field('guardianPhone', 'Guardian phone')}${field('password', 'Password (default Welcome@123)', 'text', 'minlength=6')}<button>Add tenant</button></form>` +
    table([['ID', r => r.id], ['Name', r => esc(r.user.name)], ['Email', r => esc(r.user.email)], ['Phone', r => esc(r.phone)], ['Joined', r => esc(r.joinedOn)]], t);
}
async function allocs() {
  const [t, b, a] = await Promise.all([api('/tenants'), api('/beds?status=AVAILABLE'), api('/allocations')]);
  return `<form data-form="alloc" class="row"><label>Tenant<select name="tenantId">${opts(t, x => x.user.name)}</select></label>
    <label>Bed<select name="bedId">${opts(b, x => x.room.roomNumber + '-' + x.bedLabel + ' (' + money(x.room.rentPerBed) + ')')}</select></label>
    ${field('deposit', 'Deposit', 'number')}${field('startDate', 'Start date', 'date')}<button>Allocate</button></form>` +
    table([['Tenant', r => esc(r.tenant.user.name)], ['Bed', r => esc(r.bed.room.roomNumber + '-' + r.bed.bedLabel)], ['Rent', r => money(r.monthlyRent)],
      ['Deposit', r => money(r.deposit)], ['From', r => esc(r.startDate)], ['To', r => esc(r.endDate || '')],
      ['', r => r.active ? btn('transfer', r.id, 'Transfer', 'ghost') + ' ' + btn('checkout', r.id, 'Checkout') : badge('CLOSED')]], a);
}
async function invoices() {
  const l = await api('/invoices');
  return `<form data-form="gen" class="row">${field('month', 'Generate invoices for month', 'month', 'required')}<button>Generate</button></form>` +
    table([['Tenant', r => esc(r.allocation.tenant.user.name)], ['Month', r => esc(r.monthYear)], ['Amount', r => money(r.amount)], ['Late fee', r => money(r.lateFee)],
      ['Due', r => esc(r.dueDate)], ['Status', r => badge(r.status)], ['', r => r.status === 'PAID' ? '' : btn('pay', r.id, 'Record payment')]], l);
}
async function complaintsAdmin() {
  const l = await api('/complaints');
  return table([['Tenant', r => esc(r.tenant.user.name)], ['Title', r => esc(r.title)], ['Category', r => esc(r.category)], ['Details', r => esc(r.description)],
    ['Status', r => badge(r.status)], ['', r => r.status === 'RESOLVED' ? '' : btn('cprog', r.id, 'In progress', 'ghost') + ' ' + btn('cdone', r.id, 'Resolve')]], l);
}
async function notices() {
  const n = await api('/notices');
  const f = role === 'ADMIN' ? `<form data-form="notice" class="row">${field('title', 'Title', 'text', 'required')}${field('message', 'Message', 'text', 'required')}<button>Post</button></form>` : '';
  return f + (n.length ? n.map(x => `<div class="note"><b>${esc(x.title)}</b><p>${esc(x.message)}</p><small class="muted">${esc(x.postedBy)} · ${esc(String(x.postedAt).slice(0, 10))}</small></div>`).join('') : '<p class="muted">No notices.</p>');
}
async function audit() {
  const l = await api('/audit');
  return table([['When', r => esc(String(r.at).replace('T', ' ').slice(0, 19))], ['Actor', r => esc(r.actor)], ['Action', r => esc(r.action)], ['Details', r => esc(r.details)]], l);
}

/* ---------- tenant views ---------- */
async function myStay() {
  const m = await api('/me'), a = m.allocation;
  const c = (l, v) => `<div class="card"><small>${l}</small><b>${v}</b></div>`;
  if (!a) return `<h3>Hello, ${esc(m.name)}</h3><p class="muted">No bed allocated yet. Please contact the owner.</p>`;
  return `<h3>Hello, ${esc(m.name)}</h3><div class="cards">${c('Room', esc(a.bed.room.roomNumber))}${c('Bed', esc(a.bed.bedLabel))}${c('Monthly rent', money(a.monthlyRent))}${c('Deposit', money(a.deposit))}${c('Since', esc(a.startDate))}</div>`;
}
async function myInvoices() {
  const l = await api('/invoices/mine');
  return table([['Month', r => esc(r.monthYear)], ['Amount', r => money(r.amount)], ['Late fee', r => money(r.lateFee)], ['Due', r => esc(r.dueDate)],
    ['Status', r => badge(r.status)], ['', r => r.status === 'PAID' ? '' : btn('pay', r.id, 'Pay now')]], l);
}
async function myComplaints() {
  const l = await api('/complaints/mine');
  return `<form data-form="complaint" class="row">${field('title', 'Title', 'text', 'required')}${field('description', 'Describe the issue')}
    <label>Category<select name="category"><option>MAINTENANCE</option><option>FOOD</option><option>WIFI</option><option>CLEANING</option><option>OTHER</option></select></label><button>Raise complaint</button></form>` +
    table([['Title', r => esc(r.title)], ['Category', r => esc(r.category)], ['Raised', r => esc(String(r.createdAt).slice(0, 10))], ['Status', r => badge(r.status)]], l);
}

const TABS = {
  ADMIN: {Dashboard: dash, Rooms: rooms, Tenants: tenants, Allocations: allocs, Invoices: invoices, Complaints: complaintsAdmin, Notices: notices, 'Audit log': audit},
  TENANT: {'My stay': myStay, 'My invoices': myInvoices, Complaints: myComplaints, Notices: notices}
};

/* ---------- actions ---------- */
forms.login = async d => {
  const r = await api('/auth/login', 'POST', d);
  token = r.token; role = r.role; who = r.name;
  localStorage.setItem('token', token); localStorage.setItem('role', role); localStorage.setItem('name', who);
  start();
};
forms.room = async d => { await api('/rooms', 'POST', {...d, floor: +d.floor, rentPerBed: +d.rentPerBed, beds: +d.beds}); show(); };
forms.tenant = async d => { await api('/tenants', 'POST', clean(d)); show(); };
forms.alloc = async d => {
  const b = clean(d); b.tenantId = +b.tenantId; b.bedId = +b.bedId; if (b.deposit) b.deposit = +b.deposit;
  await api('/allocations', 'POST', b); show();
};
forms.gen = async d => { const r = await api('/invoices/generate?month=' + encodeURIComponent(d.month), 'POST'); toast(r.created + ' invoice(s) created'); show(); };
forms.notice = async d => { await api('/notices', 'POST', d); show(); };
forms.complaint = async d => { await api('/complaints', 'POST', d); show(); };
acts.checkout = async id => { await api(`/allocations/${id}/checkout`, 'POST'); show(); };
acts.transfer = async id => {
  const n = prompt('Enter the new Bed ID (see the Rooms tab, status AVAILABLE)'); if (!n) return;
  await api(`/allocations/${id}/transfer`, 'POST', {newBedId: +n}); show();
};
acts.pay = async id => {
  const admin = role === 'ADMIN';
  await api(`/invoices/${id}/pay`, 'POST', {method: admin ? 'CASH' : 'UPI', reference: (admin ? 'ADMIN-' : 'MOCK-') + Date.now()}); show();
};
acts.cprog = async id => { await api(`/complaints/${id}/status`, 'PATCH', {status: 'IN_PROGRESS'}); show(); };
acts.cdone = async id => { await api(`/complaints/${id}/status`, 'PATCH', {status: 'RESOLVED'}); show(); };

document.addEventListener('submit', async e => {
  const n = e.target.dataset.form; if (!n) return;
  e.preventDefault();
  try { await forms[n](Object.fromEntries(new FormData(e.target))); if (n !== 'login') toast('Done'); } catch (err) { toast(err.message, true); }
});
document.addEventListener('click', async e => {
  const b = e.target.closest('[data-act]'); if (!b) return;
  try { await acts[b.dataset.act](b.dataset.id); toast('Done'); } catch (err) { toast(err.message, true); }
});
$('nav').addEventListener('click', e => { if (e.target.tagName === 'BUTTON') show(e.target.textContent); });

async function show(name) {
  current = name || current || Object.keys(TABS[role])[0];
  document.querySelectorAll('nav button').forEach(b => b.classList.toggle('on', b.textContent === current));
  try { $('#view').innerHTML = await TABS[role][current](); }
  catch (e) { $('#view').innerHTML = `<p class="err">${esc(e.message)}</p>`; }
}
function logout() { localStorage.clear(); token = role = who = null; start(); }
function start() {
  $('#login').hidden = !!token; $('#app').hidden = !token;
  if (!token) return;
  $('#who').textContent = who + ' · ' + role;
  $('nav').innerHTML = Object.keys(TABS[role]).map(t => `<button>${t}</button>`).join('');
  current = null; show();
}
start();
