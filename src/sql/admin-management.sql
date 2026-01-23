-- Table: systeme_config
CREATE TABLE systeme_config (
    id_config BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom_plateforme VARCHAR(255) NOT NULL,
    langue_defaut VARCHAR(50),
    fuseau_horaire VARCHAR(50),
    mode_maintenance BOOLEAN DEFAULT FALSE,
    email_support VARCHAR(255),
    date_maj TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: structure_academique
CREATE TABLE structure_academique (
    id_structure BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom_structure VARCHAR(255) NOT NULL,
    type_structure VARCHAR(50) NOT NULL,
    code_structure VARCHAR(100),
    adresse VARCHAR(255),
    responsable VARCHAR(255),
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: etablissement
CREATE TABLE etablissement (
    id_etab BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom_etab VARCHAR(255) NOT NULL,
    code_etab VARCHAR(100),
    ville VARCHAR(100),
    statut VARCHAR(50) NOT NULL,
    capacite_etudiants INT,
    date_ouverture DATE,
    structure_id BIGINT,
    FOREIGN KEY (structure_id) REFERENCES structure_academique(id_structure) ON DELETE CASCADE
);

-- Table: supervision
CREATE TABLE supervision (
    id_action BIGINT AUTO_INCREMENT PRIMARY KEY,
    action VARCHAR(255) NOT NULL,
    utilisateur VARCHAR(255),
    type_action VARCHAR(50) NOT NULL,
    resultat VARCHAR(50) NOT NULL,
    date_action TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
