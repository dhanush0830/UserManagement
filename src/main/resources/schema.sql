-- ============================================================
-- Database Schema for User Management System
-- Compatible with MySQL 8.x / 5.7+ and H2 (MySQL Mode)
-- ============================================================

-- Create Database if not exists (for standalone MySQL setups)
-- CREATE DATABASE IF NOT EXISTS usermanagement_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE usermanagement_db;

-- ------------------------------------------------------------
-- Table: users
-- Core entity storing user accounts, credentials, and roles
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(64) DEFAULT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexing for fast lookups and high-performance queries
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_role_status ON users (role, status);

-- ------------------------------------------------------------
-- Table: activity_logs
-- Audit logging for enterprise security tracking
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS activity_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    username VARCHAR(50),
    action VARCHAR(50) NOT NULL,
    details VARCHAR(255),
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_logs_action ON activity_logs (action);
CREATE INDEX IF NOT EXISTS idx_logs_created_at ON activity_logs (created_at);

-- ------------------------------------------------------------
-- Seed Initial Sample Data (Admin and Standard Users)
-- Verified BCrypt Hashes (Cost factor 10):
-- 'Admin@123'   -> $2a$10$oBPFqFwWB/.qIDFnMyr5neICAvgd/HonvjSIxIR.Sz5.x7TyXRqBm
-- 'Manager@123' -> $2a$10$zlOd64undL1ogQUywBvHie45/0rDFnA3fuIOJirNIu/CkXZNCGd8C
-- 'User@123'    -> $2a$10$b520xn5HIHO1Z43q1CHUVutX4.ivNIPaCZMgKNtyYVvgH900hom8y
-- ------------------------------------------------------------
INSERT INTO users (username, password_hash, salt, full_name, email, role, status)
SELECT 'admin', '$2a$10$oBPFqFwWB/.qIDFnMyr5neICAvgd/HonvjSIxIR.Sz5.x7TyXRqBm', 'adminsalt', 'Administrator', 'admin@enterprise.com', 'ADMIN', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO users (username, password_hash, salt, full_name, email, role, status)
SELECT 'john_doe', '$2a$10$zlOd64undL1ogQUywBvHie45/0rDFnA3fuIOJirNIu/CkXZNCGd8C', 'managersalt', 'Johnathan Doe', 'john.doe@enterprise.com', 'MANAGER', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'john_doe');

INSERT INTO users (username, password_hash, salt, full_name, email, role, status)
SELECT 'jane_smith', '$2a$10$b520xn5HIHO1Z43q1CHUVutX4.ivNIPaCZMgKNtyYVvgH900hom8y', 'usersalt', 'Jane Smith', 'jane.smith@enterprise.com', 'USER', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'jane_smith');

INSERT INTO users (username, password_hash, salt, full_name, email, role, status)
SELECT 'robert_chen', '$2a$10$b520xn5HIHO1Z43q1CHUVutX4.ivNIPaCZMgKNtyYVvgH900hom8y', 'usersalt', 'Robert Chen', 'robert.chen@enterprise.com', 'USER', 'INACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'robert_chen');

INSERT INTO users (username, password_hash, salt, full_name, email, role, status)
SELECT 'sarah_connor', '$2a$10$b520xn5HIHO1Z43q1CHUVutX4.ivNIPaCZMgKNtyYVvgH900hom8y', 'usersalt', 'Sarah Connor', 'sarah.connor@cyberdyne.io', 'USER', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'sarah_connor');
