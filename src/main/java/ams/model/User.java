package ams.model;
public record User(int id, String username, Role role) {
  public enum Role { ADMIN, TEACHER, STUDENT }
}
