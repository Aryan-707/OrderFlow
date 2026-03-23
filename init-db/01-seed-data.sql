-- This script runs automatically on first Postgres container init.
-- It creates seed data for all microservices.

-- Payment service: seed customers
CREATE TABLE IF NOT EXISTS customer (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    amount_available INT NOT NULL DEFAULT 0,
    amount_reserved INT NOT NULL DEFAULT 0,
    version BIGINT DEFAULT 0
);

INSERT INTO customer (id, name, amount_available, amount_reserved, version)
VALUES (1, 'Aryan Aggarwal', 10000, 0, 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO customer (id, name, amount_available, amount_reserved, version)
VALUES (2, 'Priya Sharma', 7500, 0, 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO customer (id, name, amount_available, amount_reserved, version)
VALUES (3, 'Rahul Verma', 5000, 0, 0)
ON CONFLICT (id) DO NOTHING;

-- Seat inventory service: seed seats
CREATE TABLE IF NOT EXISTS seat (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    seats_available INT NOT NULL DEFAULT 0,
    seats_reserved INT NOT NULL DEFAULT 0,
    version BIGINT DEFAULT 0
);

INSERT INTO seat (id, name, seats_available, seats_reserved, version)
VALUES (1, 'Section A - Row 1', 50, 0, 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO seat (id, name, seats_available, seats_reserved, version)
VALUES (2, 'Section B - Row 1', 30, 0, 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO seat (id, name, seats_available, seats_reserved, version)
VALUES (3, 'VIP Lounge', 10, 0, 0)
ON CONFLICT (id) DO NOTHING;
