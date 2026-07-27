package ams.ui;

import ams.model.User;
import ams.service.AttendanceService;
import ams.service.AttendanceService.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public final class MainFrame extends JFrame {
  private final AttendanceService service = new AttendanceService();
  private User user;
  private final JPanel content = new JPanel(new BorderLayout());
  public MainFrame() { setTitle("Attendance Management System"); setDefaultCloseOperation(EXIT_ON_CLOSE); setSize(1060,680); setLocationRelativeTo(null); showLogin(); }
  private void showLogin() {
    JPanel p=new JPanel(new GridBagLayout()); p.setBorder(new EmptyBorder(40,40,40,40)); GridBagConstraints g=new GridBagConstraints();g.insets=new Insets(8,8,8,8);g.fill=GridBagConstraints.HORIZONTAL;
    JLabel heading=new JLabel("Attendance Management System",SwingConstants.CENTER);heading.setFont(heading.getFont().deriveFont(Font.BOLD,24f));
    JTextField username=new JTextField("admin",18); JPasswordField password=new JPasswordField("password",18); JButton login=new JButton("Sign in"); JLabel hint=new JLabel("Demo accounts: admin, teacher, student — password: password",SwingConstants.CENTER);
    g.gridx=0;g.gridy=0;g.gridwidth=2;p.add(heading,g);g.gridwidth=1;g.gridy++;p.add(new JLabel("Username"),g);g.gridx=1;p.add(username,g);g.gridx=0;g.gridy++;p.add(new JLabel("Password"),g);g.gridx=1;p.add(password,g);g.gridx=0;g.gridy++;g.gridwidth=2;p.add(login,g);g.gridy++;p.add(hint,g);
    login.addActionListener(e->{try{user=service.login(username.getText(),new String(password.getPassword()));if(user==null)error("Invalid username or password.");else showApp();}catch(Exception x){error(x.getMessage());}});
    setContentPane(p);revalidate();
  }
  private void showApp() {
    JPanel root=new JPanel(new BorderLayout()); JLabel title=new JLabel("  AMS  |  Signed in as "+user.username()+" ("+user.role()+")");title.setFont(title.getFont().deriveFont(Font.BOLD,16f)); JButton logout=new JButton("Log out");logout.addActionListener(e->showLogin()); JPanel top=new JPanel(new BorderLayout());top.setBorder(new EmptyBorder(8,8,8,8));top.add(title);top.add(logout,BorderLayout.EAST);root.add(top,BorderLayout.NORTH);
    JTabbedPane tabs=new JTabbedPane();
    if(user.role()!=User.Role.STUDENT) { tabs.addTab("Students",studentsPanel()); tabs.addTab("Mark attendance",attendancePanel()); }
    tabs.addTab("Reports",reportPanel(user.role()==User.Role.STUDENT)); if(user.role()!=User.Role.STUDENT)tabs.addTab("Defaulters",defaultersPanel());
    root.add(tabs);setContentPane(root);revalidate();repaint();
  }
  private JPanel studentsPanel() {
    JPanel p=new JPanel(new BorderLayout(8,8));p.setBorder(new EmptyBorder(12,12,12,12)); DefaultTableModel model=table("ID","Roll no.","Name","Class","Contact"); JTable table=new JTable(model); JTextField search=new JTextField(); JButton refresh=new JButton("Search / refresh"), add=new JButton("Add student"), remove=new JButton("Delete selected"); JPanel tools=new JPanel();tools.add(new JLabel("Search:"));tools.add(search);tools.add(refresh);tools.add(add);tools.add(remove);p.add(tools,BorderLayout.NORTH);p.add(new JScrollPane(table));
    Runnable load=()-> {try {fillStudents(model,service.students(search.getText(),0));}catch(Exception x){error(x.getMessage());}};refresh.addActionListener(e->load.run());add.addActionListener(e->addStudent(load));remove.addActionListener(e->{int row=table.getSelectedRow();if(row<0){error("Select a student first.");return;}if(confirm("Delete this student and their attendance records?"))try{service.deleteStudent((int)model.getValueAt(row,0));load.run();}catch(Exception x){error(x.getMessage());}});load.run();return p;
  }
  private void addStudent(Runnable onSave) {
    JTextField roll=new JTextField(),name=new JTextField(),contact=new JTextField(); Object[] fields={"Roll no.",roll,"Name",name,"Contact",contact,"Class ID (default demo class is 1)",new JLabel("1")}; if(JOptionPane.showConfirmDialog(this,fields,"Add student",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
    try{if(roll.getText().isBlank()||name.getText().isBlank())throw new IllegalArgumentException("Roll number and name are required.");service.saveStudent(roll.getText().trim(),name.getText().trim(),contact.getText().trim(),1);onSave.run();}catch(Exception e){error(e.getMessage());}
  }
  private JPanel attendancePanel() {
    JPanel p=new JPanel(new BorderLayout(8,8));p.setBorder(new EmptyBorder(12,12,12,12)); JComboBox<Subject> subject=new JComboBox<>(); JTextField date=new JTextField(LocalDate.now().toString(),10); JButton load=new JButton("Load class"); JPanel tools=new JPanel();tools.add(new JLabel("Subject:"));tools.add(subject);tools.add(new JLabel("Date (YYYY-MM-DD):"));tools.add(date);tools.add(load);p.add(tools,BorderLayout.NORTH);DefaultTableModel model=table("ID","Roll no.","Student","Status");JTable rows=new JTable(model);rows.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(new JComboBox<>(new String[]{"PRESENT","ABSENT","LEAVE"})));p.add(new JScrollPane(rows));JButton save=new JButton("Save attendance");p.add(save,BorderLayout.SOUTH);
    try{for(Subject s:service.subjects())subject.addItem(s);}catch(Exception e){error(e.getMessage());}
    load.addActionListener(e->{try{Subject s=(Subject)subject.getSelectedItem();if(s==null)throw new IllegalArgumentException("Create a subject in the database first.");LocalDate d=LocalDate.parse(date.getText());model.setRowCount(0);for(Student st:service.students("",s.id()))model.addRow(new Object[]{st.id(),st.rollNo(),st.name(),service.existingStatus(st.id(),s.id(),d)});}catch(Exception x){error(x.getMessage());}});
    save.addActionListener(e->{try{Subject s=(Subject)subject.getSelectedItem();LocalDate d=LocalDate.parse(date.getText());if(s==null)throw new IllegalArgumentException("Select a subject.");for(int i=0;i<model.getRowCount();i++)service.saveAttendance((int)model.getValueAt(i,0),s.id(),d,model.getValueAt(i,3).toString(),user.id());info("Attendance saved for "+model.getRowCount()+" students.");}catch(Exception x){error(x.getMessage());}});return p;
  }
  private JPanel reportPanel(boolean mine) {
    JPanel p=new JPanel(new BorderLayout(8,8));p.setBorder(new EmptyBorder(12,12,12,12)); DefaultTableModel model=table("Roll no.","Student","Subject","Sessions","Present","Leave","Attendance %"); JTable t=new JTable(model);JButton refresh=new JButton(mine?"Refresh my report":"Refresh report");p.add(refresh,BorderLayout.NORTH);p.add(new JScrollPane(t));
    Runnable load=()->{try{Integer id=mine?service.studentIdForUser(user.id()):null;if(mine&&id==null)throw new IllegalStateException("Your user account is not linked to a student record.");fillReport(model,service.report(id));}catch(Exception e){error(e.getMessage());}};refresh.addActionListener(e->load.run());load.run();return p;
  }
  private JPanel defaultersPanel() {
    JPanel p=new JPanel(new BorderLayout(8,8));p.setBorder(new EmptyBorder(12,12,12,12)); JTextField minimum=new JTextField("75",4);JButton refresh=new JButton("Find defaulters");JPanel top=new JPanel();top.add(new JLabel("Minimum attendance %:"));top.add(minimum);top.add(refresh);p.add(top,BorderLayout.NORTH);DefaultTableModel model=table("Roll no.","Student","Subject","Sessions","Present","Leave","Attendance %");p.add(new JScrollPane(new JTable(model)));Runnable load=()->{try{double limit=Double.parseDouble(minimum.getText());model.setRowCount(0);for(Summary s:service.report(null))if(s.sessions()>0&&s.percentage()<limit)model.addRow(summaryRow(s));}catch(Exception e){error(e.getMessage());}};refresh.addActionListener(e->load.run());load.run();return p;
  }
  private static DefaultTableModel table(String... h){return new DefaultTableModel(h,0){public boolean isCellEditable(int r,int c){return c==3&&getColumnName(c).equals("Status");}};}
  private static void fillStudents(DefaultTableModel m,List<Student> data){m.setRowCount(0);for(Student s:data)m.addRow(new Object[]{s.id(),s.rollNo(),s.name(),s.className(),s.contact()});}
  private static void fillReport(DefaultTableModel m,List<Summary> data){m.setRowCount(0);for(Summary s:data)m.addRow(summaryRow(s));}
  private static Object[] summaryRow(Summary s){return new Object[]{s.rollNo(),s.name(),s.subject(),s.sessions(),s.present(),s.leave(),s.percentage()+"%"};}
  private boolean confirm(String message){return JOptionPane.showConfirmDialog(this,message,"Confirm",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION;}
  private void error(String message){JOptionPane.showMessageDialog(this,message,"Attendance Management System",JOptionPane.ERROR_MESSAGE);}
  private void info(String message){JOptionPane.showMessageDialog(this,message,"Attendance Management System",JOptionPane.INFORMATION_MESSAGE);}
}
