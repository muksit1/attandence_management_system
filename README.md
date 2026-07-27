# Attendance Management System

A Java Swing desktop application based on the supplied proposal. It uses JDBC with MySQL and supports role-based login, student records, subjects, attendance marking, reports, and defaulter tracking.

## Prerequisites

- Java 17 or newer
- MySQL 8+
- MySQL Connector/J on the runtime classpath (or use the Maven configuration in `pom.xml`)

## Set up

1. Create a database and load the schema:
   ```sql
   CREATE DATABASE attendance_management;
   ```
   Run `database/schema.sql` against it.
2. Set these environment variables if your database details differ:
   `AMS_DB_URL`, `AMS_DB_USER`, `AMS_DB_PASSWORD`.
3. Start the application with Maven (`mvn compile exec:java`) or configure the project in an IDE and run `ams.App`.

Demo accounts (password is `password`): `admin`, `teacher`, and `student`.

## Roles

- **Admin:** manages students and subjects; views all reports and defaulters.
- **Teacher:** marks attendance and views reports/defaulters.
- **Student:** views only their own attendance summary.
