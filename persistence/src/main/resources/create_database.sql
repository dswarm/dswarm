-- create the database and user for d:swarm

CREATE DATABASE IF NOT EXISTS dmp DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_bin;
CREATE USER IF NOT EXISTS 'dmp'@'localhost' IDENTIFIED BY 'dmp';
GRANT ALL PRIVILEGES ON dmp.* TO 'dmp'@localhost IDENTIFIED BY 'dmp';
FLUSH PRIVILEGES;
