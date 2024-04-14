create schema if not exists public;

create table if not exists public.sample_entity(
    id                  bigint not null primary key,
    unique_property     text not null,
    non_unique_property int not null,
    constraint unique_property__uniq unique (unique_property)
);
