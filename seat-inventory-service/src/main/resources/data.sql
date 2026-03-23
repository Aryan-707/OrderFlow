-- Seed seats with initial availability
MERGE INTO seat (id, name, seats_available, seats_reserved, version) KEY(id)
VALUES (1, 'Section A - Row 1', 50, 0, 0);

MERGE INTO seat (id, name, seats_available, seats_reserved, version) KEY(id)
VALUES (2, 'Section B - Row 1', 30, 0, 0);

MERGE INTO seat (id, name, seats_available, seats_reserved, version) KEY(id)
VALUES (3, 'VIP Lounge', 10, 0, 0);
