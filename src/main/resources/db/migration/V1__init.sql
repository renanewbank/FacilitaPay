-- V1__init.sql

create table users (
  id uuid primary key,
  name varchar(120) not null,
  email varchar(160) not null unique,
  password_hash varchar(255) not null,
  created_at timestamp not null default now(),
  updated_at timestamp not null default now()
);

create table cards (
  id uuid primary key,
  user_id uuid not null references users(id),
  brand varchar(30) not null,
  last4 varchar(4) not null,
  token varchar(200) not null,
  holder_name varchar(120) not null,
  exp_month int not null,
  exp_year int not null,
  created_at timestamp not null default now(),
  updated_at timestamp not null default now()
);

create table payments (
  id uuid primary key,
  user_id uuid not null references users(id),
  amount_cents bigint not null,
  currency varchar(10) not null default 'BRL',
  receiver_type varchar(10) not null,           -- PIX | LINK
  receiver_payload text,                        -- compatível c/ @Lob String (H2/Postgres)
  status varchar(20) not null,                  -- CREATED | AUTHORIZING | AUTHORIZED | PAID | FAILED | CANCELED
  created_at timestamp not null default now(),
  updated_at timestamp not null default now()
);

create table payment_splits (
  id uuid primary key,
  payment_id uuid not null references payments(id),
  card_id uuid not null references cards(id),
  amount_cents bigint not null,
  status varchar(20) not null,                  -- PENDING | AUTHORIZED | CAPTURED | FAILED | CANCELED
  external_auth_id varchar(100),
  external_capture_id varchar(100),
  created_at timestamp not null default now(),
  updated_at timestamp not null default now()
);

create table payouts (
  id uuid primary key,
  payment_id uuid not null references payments(id),
  method varchar(10) not null,                  -- PIX | LINK
  amount_cents bigint not null,
  status varchar(20) not null,                  -- PENDING | SENT | CONFIRMED | FAILED
  external_id varchar(100),
  created_at timestamp not null default now(),
  updated_at timestamp not null default now()
);
