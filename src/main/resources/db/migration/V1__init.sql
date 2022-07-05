CREATE TABLE IF NOT EXISTS accounts (
    id TEXT PRIMARY KEY,
    amount BIGINT NOT NULL,
    currency TEXT NOT NULL,
    currency_minor_units SMALLINT NOT NULL,
    version BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS merchants (
    id TEXT PRIMARY KEY,
    account_id TEXT NOT NULL REFERENCES accounts(id),
    version BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS txs (
    id TEXT NOT NULL PRIMARY KEY,
    tx_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS payments (
    id TEXT NOT NULL PRIMARY KEY REFERENCES txs(id),
    source_id TEXT NOT NULL REFERENCES accounts(id),
    merchant_id TEXT NOT NULL REFERENCES merchants(id),
    amount BIGINT NOT NULL,
    currency TEXT NOT NULL,
    currency_minor_units SMALLINT NOT NULL
);

CREATE TABLE IF NOT EXISTS tx_entries (
    id TEXT NOT NULL PRIMARY KEY,
    tx_id TEXT NOT NULL REFERENCES txs(id),
    type TEXT NOT NULL,
    account_id TEXT NOT NULL REFERENCES accounts(id),
    amount BIGINT NOT NULL,
    currency TEXT NOT NULL,
    currency_minor_units SMALLINT NOT NULL
);

CREATE INDEX idx__tx_entries__tx_id ON tx_entries(tx_id);