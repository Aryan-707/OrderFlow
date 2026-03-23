-- Seed customers with initial balance
MERGE INTO customer (id, name, amount_available, amount_reserved, version) KEY(id)
VALUES (1, 'Aryan Aggarwal', 10000, 0, 0);

MERGE INTO customer (id, name, amount_available, amount_reserved, version) KEY(id)
VALUES (2, 'Priya Sharma', 7500, 0, 0);

MERGE INTO customer (id, name, amount_available, amount_reserved, version) KEY(id)
VALUES (3, 'Rahul Verma', 5000, 0, 0);
