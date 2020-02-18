CREATE TABLE if NOT EXISTS members
(
    id              serial primary key,
    first_name      varchar(100),
    middle_name     varchar(100),
    last_name       varchar(100),
    employee_number int unique,
    username        varchar(100) unique,
    password        varchar(200),
    roles           varchar(500),
    permissions     varchar(500),
    active          boolean
);

-- insert into members (first_name, middle_name, last_name, employee_number, username, password, roles, permissions, active)
-- values ('Alex', null, 'Catarau', 10011032, 'AleCat', '$2y$10$mbmAkdm6hi7LyVBaGRBwTOgu9I.rTxo80ZUcI/GSTimZN7unr0MbC', 'ADMIN', null, true );
--
--
-- SELECT (id, first_name, middle_name, last_name, username) from members " +
--             "where roles = 'USER' order by first_name asc LIMIT: limit OFFSET: offset;