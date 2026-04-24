-- =============================================================
-- Customer Management System — DDL
-- Target: MariaDB 10.6+
-- Run this script once against an empty database
-- =============================================================

CREATE DATABASE IF NOT EXISTS customer_mgmt
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE customer_mgmt;

-- -------------------------------------------------------------
-- Master data: country
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS country (
    id      BIGINT          NOT NULL AUTO_INCREMENT,
    name    VARCHAR(100)    NOT NULL,
    code    VARCHAR(3)      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_country_name UNIQUE (name),
    CONSTRAINT uq_country_code UNIQUE (code)
) ENGINE=InnoDB;

-- -------------------------------------------------------------
-- Master data: city
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS city (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    country_id  BIGINT      NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_city_country (country_id),
    CONSTRAINT fk_city_country FOREIGN KEY (country_id)
        REFERENCES country(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- -------------------------------------------------------------
-- Core: customer
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    name            VARCHAR(200)    NOT NULL,
    date_of_birth   DATE            NOT NULL,
    nic_number      VARCHAR(20)     NOT NULL,
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    CONSTRAINT uq_customer_nic UNIQUE (nic_number),
    INDEX idx_customer_name (name)
) ENGINE=InnoDB;

-- -------------------------------------------------------------
-- Child: customer_mobile  (multiple per customer, optional)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer_mobile (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    customer_id     BIGINT      NOT NULL,
    mobile_number   VARCHAR(20) NOT NULL,
    is_primary      TINYINT(1)  NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_mobile_customer (customer_id),
    CONSTRAINT fk_mobile_customer FOREIGN KEY (customer_id)
        REFERENCES customer(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -------------------------------------------------------------
-- Child: customer_address  (multiple per customer, optional)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer_address (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    customer_id     BIGINT      NOT NULL,
    address_line1   VARCHAR(255),
    address_line2   VARCHAR(255),
    city_id         BIGINT,
    is_primary      TINYINT(1)  NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_address_customer (customer_id),
    CONSTRAINT fk_address_customer FOREIGN KEY (customer_id)
        REFERENCES customer(id) ON DELETE CASCADE,
    CONSTRAINT fk_address_city FOREIGN KEY (city_id)
        REFERENCES city(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- -------------------------------------------------------------
-- Join table: customer_family  (M:N self-referencing)
-- Both customer_id and family_member_id reference customer.
-- The application maintains both directions of the link.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customer_family (
    customer_id         BIGINT  NOT NULL,
    family_member_id    BIGINT  NOT NULL,
    PRIMARY KEY (customer_id, family_member_id),
    CONSTRAINT fk_family_customer FOREIGN KEY (customer_id)
        REFERENCES customer(id) ON DELETE CASCADE,
    CONSTRAINT fk_family_member FOREIGN KEY (family_member_id)
        REFERENCES customer(id) ON DELETE CASCADE,
    -- Prevent a customer from being their own family member
    CONSTRAINT chk_no_self_family CHECK (customer_id <> family_member_id)
) ENGINE=InnoDB;

-- -------------------------------------------------------------
-- Async job tracking: bulk_upload_job
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bulk_upload_job (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    file_name           VARCHAR(255)    NOT NULL,
    status              ENUM('PENDING','PROCESSING','COMPLETED','FAILED') NOT NULL DEFAULT 'PENDING',
    total_records       INT,
    processed_records   INT             NOT NULL DEFAULT 0,
    failed_records      INT             NOT NULL DEFAULT 0,
    error_message       VARCHAR(2000),
    started_at          DATETIME,
    completed_at        DATETIME,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB;