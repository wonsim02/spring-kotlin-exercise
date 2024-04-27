create schema if not exists public;

create table if not exists public.cat(
    id      bigserial,
    name    text not null,
    species text not null
);

create table if not exists public.dog(
    id      bigint not null primary key
        -- public.cat 테이블의 id 행의 sequence의 다음 값을 사용합니다.
        default nextval(pg_get_serial_sequence('public.cat', 'id')),
    name    text not null,
    species text not null
);
