create schema if not exists public;

create table if not exists public."user"
(
    id         bigserial primary key,
    name       text        not null
);

create table if not exists public.video
(
    id         bigserial primary key,
    title      text        not null
);

create table if not exists public.watch_history
(
    id         bigserial primary key,
    user_id    bigint      not null,
    video_id   bigint      not null
);

create table if not exists public.play_list
(
    id         bigserial primary key,
    title      text        not null,
    video_ids  bigint[]    not null
);
