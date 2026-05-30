# Attendance Management System

A desktop attendance management system built with plain Java Swing. It supports adding, updating, deleting, searching, and saving attendance records, plus a live 3D-style attendance visualization.

## Run

```powershell
javac -d out src\com\attendance\AttendanceApp.java
java -cp out com.attendance.AttendanceApp
```

Or:

```powershell
.\run.bat
```

## Features

- Add or update attendance by student ID and date
- Track `PRESENT`, `LATE`, and `ABSENT`
- Search records instantly
- Persist records to `attendance-data.csv`
- Interactive 3D visualization with draggable rotation
