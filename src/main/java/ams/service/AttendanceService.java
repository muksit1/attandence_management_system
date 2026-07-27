package ams.service;

import ams.db.Database;
import ams.model.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public final class AttendanceService {
  public record Student(int id, String rollNo, String name, String className, String contact) { public String toString(){ return rollNo + " — " + name; } }
  public record Subject(int id, String name, String className) { public String toString(){ return name + " (" + className + ")"; } }
  public record Summary(String rollNo, String name, String subject, int sessions, int present, int leave, double percentage) { }

  public User login(String username, String password) throws Exception {
    String sql = "SELECT user_id, username, role FROM users WHERE username=? AND password_hash=?";
    try (Connection c=Database.connect(); PreparedStatement p=c.prepareStatement(sql)) {
      p.setString(1, username.trim()); p.setString(2, hash(password)); ResultSet r=p.executeQuery();
      if (r.next()) return new User(r.getInt(1),r.getString(2),User.Role.valueOf(r.getString(3)));
      return null;
    }
  }
  public List<Student> students() throws Exception { return students("", 0); }
  public List<Student> students(String search, int subjectId) throws Exception {
    String sql="SELECT s.student_id,s.roll_no,s.name,CONCAT(c.class_name,' - ',c.section),s.contact FROM students s JOIN classes c ON c.class_id=s.class_id " +
      (subjectId>0?"JOIN subjects sub ON sub.class_id=s.class_id WHERE sub.subject_id=? AND (s.name LIKE ? OR s.roll_no LIKE ?)":"WHERE s.name LIKE ? OR s.roll_no LIKE ?")+" ORDER BY s.roll_no";
    List<Student> out=new ArrayList<>(); try(Connection c=Database.connect(); PreparedStatement p=c.prepareStatement(sql)) { int i=1; if(subjectId>0)p.setInt(i++,subjectId); p.setString(i++,"%"+search+"%");p.setString(i,"%"+search+"%"); ResultSet r=p.executeQuery(); while(r.next())out.add(new Student(r.getInt(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5))); } return out;
  }
  public List<Subject> subjects() throws Exception { String sql="SELECT sub.subject_id,sub.subject_name,CONCAT(c.class_name,' - ',c.section) FROM subjects sub JOIN classes c ON c.class_id=sub.class_id ORDER BY 2"; List<Subject> out=new ArrayList<>(); try(Connection c=Database.connect();Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){while(r.next())out.add(new Subject(r.getInt(1),r.getString(2),r.getString(3)));}return out; }
  public void saveStudent(String roll,String name,String contact,int classId) throws Exception { try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("INSERT INTO students(roll_no,name,class_id,contact) VALUES(?,?,?,?)")){p.setString(1,roll);p.setString(2,name);p.setInt(3,classId);p.setString(4,contact);p.executeUpdate();} }
  public void deleteStudent(int id) throws Exception { try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("DELETE FROM students WHERE student_id=?")){p.setInt(1,id);p.executeUpdate();} }
  public void saveAttendance(int studentId,int subjectId,LocalDate date,String status,int userId) throws Exception { String sql="INSERT INTO attendance(student_id,subject_id,attendance_date,status,marked_by) VALUES(?,?,?,?,?) ON DUPLICATE KEY UPDATE status=VALUES(status),marked_by=VALUES(marked_by)";try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,studentId);p.setInt(2,subjectId);p.setDate(3,java.sql.Date.valueOf(date));p.setString(4,status);p.setInt(5,userId);p.executeUpdate();} }
  public String existingStatus(int studentId,int subjectId,LocalDate d) throws Exception { try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("SELECT status FROM attendance WHERE student_id=? AND subject_id=? AND attendance_date=?")){p.setInt(1,studentId);p.setInt(2,subjectId);p.setDate(3,java.sql.Date.valueOf(d));ResultSet r=p.executeQuery();return r.next()?r.getString(1):"PRESENT";} }
  public List<Summary> report(Integer studentId) throws Exception { String sql="SELECT s.roll_no,s.name,sub.subject_name,COUNT(a.attendance_id),COALESCE(SUM(a.status='PRESENT'),0),COALESCE(SUM(a.status='LEAVE'),0),ROUND(100*COALESCE(SUM(a.status='PRESENT'),0)/NULLIF(COUNT(a.attendance_id),0),2) FROM students s LEFT JOIN attendance a ON a.student_id=s.student_id LEFT JOIN subjects sub ON sub.subject_id=a.subject_id "+(studentId==null?"":"WHERE s.student_id=? ")+"GROUP BY s.student_id,sub.subject_id ORDER BY s.roll_no,sub.subject_name"; List<Summary> out=new ArrayList<>();try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement(sql)){if(studentId!=null)p.setInt(1,studentId);ResultSet r=p.executeQuery();while(r.next())out.add(new Summary(r.getString(1),r.getString(2),Objects.toString(r.getString(3),"No attendance"),r.getInt(4),r.getInt(5),r.getInt(6),r.getDouble(7)));}return out; }
  public Integer studentIdForUser(int userId) throws Exception {try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("SELECT student_id FROM students WHERE user_id=?")){p.setInt(1,userId);ResultSet r=p.executeQuery();return r.next()?r.getInt(1):null;}}
  public static String hash(String text) throws Exception { byte[] b=MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));StringBuilder s=new StringBuilder();for(byte x:b)s.append(String.format("%02x",x));return s.toString(); }
}
