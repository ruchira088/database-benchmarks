CREATE TABLE person (
    id VARCHAR(36) UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    username VARCHAR(128) UNIQUE NOT NULL,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    age INTEGER NOT NULL,

    PRIMARY KEY (id)
);