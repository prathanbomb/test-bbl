CREATE TABLE users
(
    id       BIGINT NOT NULL,
    name     VARCHAR(255),
    username VARCHAR(255),
    email    VARCHAR(255),
    phone    VARCHAR(255),
    website  VARCHAR(255),
    CONSTRAINT pk_users PRIMARY KEY (id)
);