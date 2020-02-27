CREATE TABLE if NOT EXISTS members
(
    id                           serial primary key,
    first_name                   varchar(100),
    last_name                    varchar(100),
    employee_number              varchar(100) unique,
    username                     varchar(100) unique,
    shift                        varchar(20),
    job_role                     varchar(30),
    department                   varchar(30),
    area                         varchar(30),
    email                        varchar(100) unique,
    password                     varchar(200),
    roles                        varchar(500),
    permissions                  varchar(500),
    active                       boolean,
    pending_account_registration boolean,
    registration_mail_sent       boolean,
    pending_reset_password       boolean
);
