-- Table: system_config
CREATE TABLE system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    platform_name VARCHAR(255) NOT NULL,
    default_language VARCHAR(50),
    timezone VARCHAR(50),
    maintenance_mode BOOLEAN DEFAULT FALSE,
    support_email VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: academic_structure
CREATE TABLE academic_structure (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    code VARCHAR(100),
    address VARCHAR(255),
    manager VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: institution
CREATE TABLE institution (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100),
    city VARCHAR(100),
    status VARCHAR(50) NOT NULL,
    student_capacity INT,
    opening_date DATE,
    structure_id BIGINT,
    FOREIGN KEY (structure_id) REFERENCES academic_structure(id) ON DELETE CASCADE
);

-- Table: supervision
CREATE TABLE supervision (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    action VARCHAR(255) NOT NULL,
    user VARCHAR(255),
    type VARCHAR(50) NOT NULL,
    result VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
