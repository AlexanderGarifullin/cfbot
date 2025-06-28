-- liquibase formatted sql

-- changeset alexandergarifullin:1
CREATE SCHEMA IF NOT EXISTS cf;

-- changeset alexandergarifullin:2
CREATE TABLE IF NOT EXISTS cf.groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    owner BIGINT NOT NULL
);

-- changeset alexandergarifullin:3
CREATE TABLE IF NOT EXISTS cf.group_membership (
    id BIGSERIAL PRIMARY KEY,
    group_id INT NOT NULL REFERENCES cf.groups(id) ON DELETE CASCADE,
    membership_codeforces_name VARCHAR(50) NOT NULL
);