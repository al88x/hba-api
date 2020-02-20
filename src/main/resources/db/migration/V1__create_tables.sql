CREATE TABLE if NOT EXISTS members
(
    id              serial primary key,
    first_name      varchar(100),
    middle_name     varchar(100),
    last_name       varchar(100),
    employee_number varchar(100) unique,
    username        varchar(100) unique,
    email           varchar(100) unique,
    password        varchar(200),
    roles           varchar(500),
    permissions     varchar(500),
    active          boolean,
    pending_confirmation boolean
);