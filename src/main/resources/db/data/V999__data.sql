INSERT INTO accounts (id, amount, currency, currency_minor_units, version)
VALUES
    ('1', 250000, 'PHP', 2, 0),
    ('2', 150000, 'PHP', 2, 0),
    ('3', 50000, 'PHP', 2, 0);

INSERT INTO merchants (id, account_id, version)
VALUES
    ('A', '1', 0);
