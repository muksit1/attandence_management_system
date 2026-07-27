package ams;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import ams.ui.MainFrame;

public final class App {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) { }
      new MainFrame().setVisible(true);
    }
    );
  }
}
