-- ===================================================================
-- MySQL Database Setup Script for User Management System
-- Database: usermanagement_db
-- ===================================================================

CREATE DATABASE IF NOT EXISTS usermanagement_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE usermanagement_db;

-- -------------------------------------------------------------------
-- Table Structure: users
-- -------------------------------------------------------------------
DROP TABLE IF EXISTS activity_logs;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(64) DEFAULT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_username (username),
    INDEX idx_users_email (email),
    INDEX idx_users_role_status (role, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------------
-- Table Structure: activity_logs (Audit Trail)
-- -------------------------------------------------------------------
CREATE TABLE activity_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    username VARCHAR(50),
    action VARCHAR(50) NOT NULL,
    details VARCHAR(255),
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_logs_action (action),
    INDEX idx_logs_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------------
-- Initial Seed Accounts
-- Verified BCrypt Hashes (Cost factor 10):
-- 'Admin@123'   -> $2a$10$oBPFqFwWB/.qIDFnMyr5neICAvgd/HonvjSIxIR.Sz5.x7TyXRqBm
-- 'Manager@123' -> $2a$10$zlOd64undL1ogQUywBvHie45/0rDFnA3fuIOJirNIu/CkXZNCGd8C
-- 'User@123'    -> $2a$10$b520xn5HIHO1Z43q1CHUVutX4.ivNIPaCZMgKNtyYVvgH900hom8y
-- -------------------------------------------------------------------
INSERT INTO users (username, password_hash, salt, full_name, email, role, status) VALUES
('admin', '$2a$10$oBPFqFwWB/.qIDFnMyr5neICAvgd/HonvjSIxIR.Sz5.x7TyXRqBm', 'adminsalt', 'System Administrator', 'admin@enterprise.com', 'ADMIN', 'ACTIVE'),
('john_doe', '$2a$10$zlOd64undL1ogQUywBvHie45/0rDFnA3fuIOJirNIu/CkXZNCGd8C', 'managersalt', 'Johnathan Doe', 'john.doe@enterprise.com', 'MANAGER', 'ACTIVE'),
('jane_smith', '$2a$10$b520xn5HIHO1Z43q1CHUVutX4.ivNIPaCZMgKNtyYVvgH900hom8y', 'usersalt', 'Jane Smith', 'jane.smith@enterprise.com', 'USER', 'ACTIVE'),
('robert_chen', '$2a$10$b520xn5HIHO1Z43q1CHUVutX4.ivNIPaCZMgKNtyYVvgH900hom8y', 'usersalt', 'Robert Chen', 'robert.chen@enterprise.com', 'USER', 'INACTIVE'),
('sarah_connor', '$2a$10$b520xn5HIHO1Z43q1CHUVutX4.ivNIPaCZMgKNtyYVvgH900hom8y', 'usersalt', 'Sarah Connor', 'sarah.connor@cyberdyne.io', 'USER', 'ACTIVE');

-- Initial audit log entry
INSERT INTO activity_logs (username, action, details, ip_address) VALUES
('SYSTEM', 'DATABASE_INITIALIZATION', 'Database schema and seed accounts successfully created.', '127.0.0.1');

COMMIT;
