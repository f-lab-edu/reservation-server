CREATE USER IF NOT EXISTS 'reservation'@'%' IDENTIFIED BY 'reservation123!';
GRANT ALL PRIVILEGES ON *.* TO 'reservation'@'%';

CREATE DATABASE IF NOT EXISTS reservation DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;