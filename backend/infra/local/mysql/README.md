docker compose up -d


# after install

mysql -h 127.0.0.1 -u root -p
or 
mysql -h 127.0.0.1 -u root -prootpassword

then run the following to allow root access from outside of container if needed.

ALTER USER 'root'@'%' IDENTIFIED BY 'rootpassword';
FLUSH PRIVILEGES;

# Create Database and User
CREATE DATABASE quickledger;
CREATE USER 'QuickLedger'@'%' IDENTIFIED BY 'Password123!';
GRANT ALL PRIVILEGES ON quickledger.* TO 'QuickLedger'@'%';
FLUSH PRIVILEGES;


