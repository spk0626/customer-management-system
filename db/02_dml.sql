-- =============================================================
-- Customer Management System — DML (seed data)
-- Run AFTER 01_ddl.sql
-- =============================================================

USE customer_mgmt;

-- -------------------------------------------------------------
-- Countries (master data — never shown on frontend)
-- -------------------------------------------------------------
INSERT INTO country (name, code) VALUES
('Sri Lanka',       'LKA'),
('India',           'IND'),
('United Kingdom',  'GBR'),
('United States',   'USA'),
('Australia',       'AUS'),
('Singapore',       'SGP'),
('Germany',         'DEU'),
('Canada',          'CAN')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- -------------------------------------------------------------
-- Cities — Sri Lanka
-- -------------------------------------------------------------
INSERT INTO city (name, country_id) VALUES
('Colombo',     (SELECT id FROM country WHERE code = 'LKA')),
('Kandy',       (SELECT id FROM country WHERE code = 'LKA')),
('Galle',       (SELECT id FROM country WHERE code = 'LKA')),
('Jaffna',      (SELECT id FROM country WHERE code = 'LKA')),
('Negombo',     (SELECT id FROM country WHERE code = 'LKA')),
('Matara',      (SELECT id FROM country WHERE code = 'LKA')),
('Kurunegala',  (SELECT id FROM country WHERE code = 'LKA')),
('Anuradhapura',(SELECT id FROM country WHERE code = 'LKA'))
ON DUPLICATE KEY UPDATE country_id = VALUES(country_id);

-- Cities — India
INSERT INTO city (name, country_id) VALUES
('Mumbai',      (SELECT id FROM country WHERE code = 'IND')),
('Delhi',       (SELECT id FROM country WHERE code = 'IND')),
('Bangalore',   (SELECT id FROM country WHERE code = 'IND')),
('Chennai',     (SELECT id FROM country WHERE code = 'IND'))
ON DUPLICATE KEY UPDATE country_id = VALUES(country_id);

-- Cities — UK
INSERT INTO city (name, country_id) VALUES
('London',      (SELECT id FROM country WHERE code = 'GBR')),
('Manchester',  (SELECT id FROM country WHERE code = 'GBR'))
ON DUPLICATE KEY UPDATE country_id = VALUES(country_id);

-- Cities — other
INSERT INTO city (name, country_id) VALUES
('New York',    (SELECT id FROM country WHERE code = 'USA')),
('Sydney',      (SELECT id FROM country WHERE code = 'AUS')),
('Singapore',   (SELECT id FROM country WHERE code = 'SGP'))
ON DUPLICATE KEY UPDATE country_id = VALUES(country_id);

-- -------------------------------------------------------------
-- Sample customers
-- -------------------------------------------------------------
INSERT INTO customer (name, date_of_birth, nic_number, is_active) VALUES
('Kamal Perera',    '1990-05-15', '901234567V', 1),
('Nimal Silva',     '1985-11-22', '851234567V', 1),
('Priya Fernando',  '1992-03-08', '922345678V', 1),
('Sunil Rajapaksa', '1978-07-30', '781234567V', 1),
('Amara Wijesinghe','1995-01-14', '951234567V', 1);

-- Sample mobile numbers
INSERT INTO customer_mobile (customer_id, mobile_number, is_primary) VALUES
((SELECT id FROM customer WHERE nic_number='901234567V'), '+94771234567', 1),
((SELECT id FROM customer WHERE nic_number='901234567V'), '+94112345678', 0),
((SELECT id FROM customer WHERE nic_number='851234567V'), '+94772345678', 1),
((SELECT id FROM customer WHERE nic_number='922345678V'), '+94773456789', 1),
((SELECT id FROM customer WHERE nic_number='781234567V'), '+94774567890', 1);

-- Sample addresses
INSERT INTO customer_address (customer_id, address_line1, address_line2, city_id, is_primary) VALUES
(
    (SELECT id FROM customer WHERE nic_number='901234567V'),
    '42 Galle Road', 'Colpetty',
    (SELECT id FROM city WHERE name='Colombo'),
    1
),
(
    (SELECT id FROM customer WHERE nic_number='851234567V'),
    '15 Kandy Road', NULL,
    (SELECT id FROM city WHERE name='Kandy'),
    1
),
(
    (SELECT id FROM customer WHERE nic_number='922345678V'),
    '8 Beach Road', 'Unawatuna',
    (SELECT id FROM city WHERE name='Galle'),
    1
);

-- Sample family relationship: Kamal ↔ Nimal
INSERT INTO customer_family (customer_id, family_member_id) VALUES
(
    (SELECT id FROM customer WHERE nic_number='901234567V'),
    (SELECT id FROM customer WHERE nic_number='851234567V')
),
(
    (SELECT id FROM customer WHERE nic_number='851234567V'),
    (SELECT id FROM customer WHERE nic_number='901234567V')
);
