USE labtrace;

CREATE USER IF NOT EXISTS 'labtrace'@'localhost'
IDENTIFIED BY 'Labtrace@123456';

CREATE USER IF NOT EXISTS 'labtrace'@'%'
IDENTIFIED BY 'Labtrace@123456';

GRANT ALL PRIVILEGES ON labtrace.* TO 'labtrace'@'localhost';

GRANT ALL PRIVILEGES ON labtrace.* TO 'labtrace'@'%';

FLUSH PRIVILEGES;

INSERT INTO lab_user (username, password, real_name, role, status)
SELECT
  'admin',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
  '管理员',
  'ADMIN',
  1
WHERE NOT EXISTS (
  SELECT 1 FROM lab_user WHERE username = 'admin'
);
