const KEY = "LOGISTICS_DEMO_DB_V1";

function load() {
  try {
    const raw = localStorage.getItem(KEY);
    if (raw) return JSON.parse(raw);
  } catch {}
  return {
    customers: [
      { id: "C001", name: "王小明", phone: "0912345678", address: "台北市信義區..." },
      { id: "C002", name: "陳大華", phone: "0987654321", address: "新北市板橋區..." },
    ],
    parcels: [],
    invoices: [],
  };
}

function save(db) {
  localStorage.setItem(KEY, JSON.stringify(db));
}

function genId(prefix) {
  return (
    prefix +
    "-" +
    Math.random().toString(36).slice(2, 6).toUpperCase() +
    Date.now().toString().slice(-4)
  );
}

function nowStr() {
  const d = new Date();
  const pad = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(
    d.getHours()
  )}:${pad(d.getMinutes())}`;
}

/** Auth (demo) */
export function mockLogin({ username, password, role }) {
  if (!username || !password) throw new Error("請輸入帳號與密碼");
  const login = { username, role: role || "STAFF" };
  localStorage.setItem("DEMO_LOGIN", JSON.stringify(login));
  return login;
}

export function mockLogout() {
  localStorage.removeItem("DEMO_LOGIN");
}

export function getLogin() {
  try {
    return JSON.parse(localStorage.getItem("DEMO_LOGIN") || "null");
  } catch {
    return null;
  }
}

/** Customer */
export function listCustomers() {
  return load().customers;
}

export function createCustomer({ name, phone, address }) {
  const db = load();
  const id = genId("C");
  const customer = { id, name, phone, address };
  db.customers.unshift(customer);
  save(db);
  return customer;
}

/** Pricing */
export function quote({ weight, distance }) {
  const amount = Math.round(Number(weight) * 10 + Number(distance) * 2);
  return { amount };
}

/** Order / Parcel */
export function createParcel({ sender, receiver, weight, distance }) {
  const db = load();
  const trackingNumber = genId("TRK");
  const { amount } = quote({ weight, distance });

  const parcel = {
    trackingNumber,
    sender,
    receiver,
    weight: Number(weight),
    distance: Number(distance),
    chargeAmount: amount,
    status: "CREATED",
    events: [{ time: nowStr(), type: "CREATED", remarks: "訂單已建立" }],
  };

  db.parcels.unshift(parcel);
  save(db);
  return parcel;
}

export function findParcel(trackingNumber) {
  const db = load();
  return db.parcels.find((x) => x.trackingNumber === trackingNumber) || null;
}

export function addTrackingEvent(trackingNumber, { eventType, remarks }) {
  const db = load();
  const idx = db.parcels.findIndex((x) => x.trackingNumber === trackingNumber);
  if (idx < 0) throw new Error("查無此追蹤碼");

  const parcel = db.parcels[idx];
  parcel.status = eventType;
  parcel.events.push({ time: nowStr(), type: eventType, remarks: remarks || "" });

  save(db);
  return parcel;
}

/** Billing */
export function listInvoices() {
  return load().invoices;
}

export function generateInvoice({ trackingNumber }) {
  const db = load();
  const parcel = db.parcels.find((x) => x.trackingNumber === trackingNumber);
  if (!parcel) throw new Error("查無包裹");

  const invoice = {
    id: genId("INV"),
    trackingNumber,
    customer: parcel.sender,
    amount: parcel.chargeAmount,
    status: "OPEN",
    createdAt: nowStr(),
  };

  db.invoices.unshift(invoice);
  save(db);
  return invoice;
}

export function markInvoicePaid(invoiceId) {
  const db = load();
  const inv = db.invoices.find((x) => x.id === invoiceId);
  if (!inv) throw new Error("查無帳單");
  inv.status = "PAID";
  save(db);
  return inv;
}
