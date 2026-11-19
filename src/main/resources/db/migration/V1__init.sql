CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY, name VARCHAR(100) NOT NULL, email VARCHAR(150) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL, role VARCHAR(20) NOT NULL, active BOOLEAN NOT NULL DEFAULT TRUE);
CREATE TABLE rooms (
  id BIGSERIAL PRIMARY KEY, room_number VARCHAR(20) NOT NULL UNIQUE, floor INT NOT NULL,
  room_type VARCHAR(20) NOT NULL, rent_per_bed NUMERIC(10,2) NOT NULL CHECK (rent_per_bed > 0));
CREATE TABLE beds (
  id BIGSERIAL PRIMARY KEY, room_id BIGINT NOT NULL REFERENCES rooms(id), bed_label VARCHAR(5) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE', UNIQUE (room_id, bed_label));
CREATE TABLE tenants (
  id BIGSERIAL PRIMARY KEY, user_id BIGINT NOT NULL UNIQUE REFERENCES users(id), phone VARCHAR(20) NOT NULL,
  id_proof_type VARCHAR(30), id_proof_number VARCHAR(50), guardian_phone VARCHAR(20), joined_on DATE NOT NULL);
CREATE TABLE allocations (
  id BIGSERIAL PRIMARY KEY, tenant_id BIGINT NOT NULL REFERENCES tenants(id), bed_id BIGINT NOT NULL REFERENCES beds(id),
  start_date DATE NOT NULL, end_date DATE, monthly_rent NUMERIC(10,2) NOT NULL, deposit NUMERIC(10,2) NOT NULL DEFAULT 0,
  active BOOLEAN NOT NULL DEFAULT TRUE);
-- Core integrity rules: one active allocation per bed and per tenant, enforced by the database itself.
CREATE UNIQUE INDEX uq_alloc_bed_active ON allocations(bed_id) WHERE active;
CREATE UNIQUE INDEX uq_alloc_tenant_active ON allocations(tenant_id) WHERE active;
CREATE TABLE invoices (
  id BIGSERIAL PRIMARY KEY, allocation_id BIGINT NOT NULL REFERENCES allocations(id), month_year VARCHAR(7) NOT NULL,
  amount NUMERIC(10,2) NOT NULL, late_fee NUMERIC(10,2) NOT NULL DEFAULT 0, due_date DATE NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING', UNIQUE (allocation_id, month_year));
CREATE TABLE payments (
  id BIGSERIAL PRIMARY KEY, invoice_id BIGINT NOT NULL REFERENCES invoices(id), amount NUMERIC(10,2) NOT NULL,
  method VARCHAR(20) NOT NULL, reference VARCHAR(80), paid_at TIMESTAMP NOT NULL);
CREATE TABLE complaints (
  id BIGSERIAL PRIMARY KEY, tenant_id BIGINT NOT NULL REFERENCES tenants(id), title VARCHAR(150) NOT NULL,
  description TEXT, category VARCHAR(30), status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
  created_at TIMESTAMP NOT NULL, updated_at TIMESTAMP NOT NULL);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_complaints_tenant ON complaints(tenant_id);
