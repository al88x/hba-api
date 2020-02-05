CREATE TABLE if NOT EXISTS users
(
    id              serial primary key,
    username        varchar(100),
    password        varchar(200),
    roles           varchar(500),
    permissions     varchar(500),
    active          boolean
);