
CREATE DATABASE giljabi CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'giljabi'@'localhost' IDENTIFIED BY 'giljabi';

GRANT ALL PRIVILEGES ON giljabi.* TO 'giljabi'@'localhost';
