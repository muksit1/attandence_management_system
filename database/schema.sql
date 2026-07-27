CREATE TABLE IF NOT EXISTS classes (
  class_id INT AUTO_INCREMENT PRIMARY KEY, class_name VARCHAR(80) NOT NULL, section VARCHAR(20) NOT NULL,
  UNIQUE KEY uq_class_section (class_name, section)
);
CREATE TABLE IF NOT EXISTS users (
  user_id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(50) NOT NULL UNIQUE,
  password_hash CHAR(64) NOT NULL, role ENUM('ADMIN','TEACHER','STUDENT') NOT NULL
);
CREATE TABLE IF NOT EXISTS students (
  student_id INT AUTO_INCREMENT PRIMARY KEY, roll_no VARCHAR(30) NOT NULL UNIQUE, name VARCHAR(120) NOT NULL,
  class_id INT NOT NULL, contact VARCHAR(40), user_id INT UNIQUE,
  FOREIGN KEY (class_id) REFERENCES classes(class_id) ON DELETE RESTRICT,
  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);
CREATE TABLE IF NOT EXISTS subjects (
  subject_id INT AUTO_INCREMENT PRIMARY KEY, subject_name VARCHAR(100) NOT NULL, class_id INT NOT NULL,
  teacher_id INT, UNIQUE KEY uq_subject_class (subject_name, class_id),
  FOREIGN KEY (class_id) REFERENCES classes(class_id) ON DELETE CASCADE,
  FOREIGN KEY (teacher_id) REFERENCES users(user_id) ON DELETE SET NULL
);
CREATE TABLE IF NOT EXISTS attendance (
  attendance_id INT AUTO_INCREMENT PRIMARY KEY, student_id INT NOT NULL, subject_id INT NOT NULL,
  attendance_date DATE NOT NULL, status ENUM('PRESENT','ABSENT','LEAVE') NOT NULL,
  marked_by INT, UNIQUE KEY uq_attendance (student_id, subject_id, attendance_date),
  FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
  FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE,
  FOREIGN KEY (marked_by) REFERENCES users(user_id) ON DELETE SET NULL
);
INSERT IGNORE INTO classes (class_id, class_name, section) VALUES (1, 'Computer Science', 'A');
INSERT IGNORE INTO users (user_id, username, password_hash, role) VALUES
  (1, 'admin', SHA2('password',256), 'ADMIN'), (2, 'teacher', SHA2('password',256), 'TEACHER'), (3, 'student', SHA2('password',256), 'STUDENT');
INSERT IGNORE INTO students (student_id, roll_no, name, class_id, contact, user_id) VALUES
  (1, 'CS-001', 'Demo Student', 1, 'demo@example.com', 3);
INSERT IGNORE INTO subjects (subject_id, subject_name, class_id, teacher_id) VALUES (1, 'Java Programming', 1, 2);
