create schema if not exists public;

create table if not exists public.test_entity(
    id              bigserial primary key,
    string_property text not null,
    array_property  text[] not null
);
