create schema if not exists public;

create table if not exists public.order(
    id      bigserial primary key,
    user_id bigint not null,
    type    text not null
);

create table if not exists public.order_single_payment(
    id                          bigint not null primary key,
    single_payment_product_id   bigint not null,
    paid_amount                 int not null,
    paid_at                     timestamptz not null
);

create table if not exists public.order_subscription(
    id                          bigint not null primary key,
    subscription_product_id     bigint not null,
    payment_amount_per_month    int not null,
    last_paid_at                timestamptz not null
);
