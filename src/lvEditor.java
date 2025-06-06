package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.util.Properties;
import java.lang.Object;
import com.formdev.flatlaf.FlatLightLaf;
	 
public class lvEditor { 

    private static File currentFile = null;
    private static int fontSize;

    public static void main(String[] args) {
 
        Properties config = new Properties();
        try (FileInputStream in = new FileInputStream("config.properties")) {
            config.load(in);
            fontSize = Integer.parseInt(config.getProperty("fontsize", "15"));
        } catch (IOException e) {
            fontSize = 15;
            saveConfig(config);
        }
	    JFrame frame = new JFrame("lvEditor");
        /* add comment from next 2 lines after these 2 comments when running .java so it doesnt crash, add it back when making a fat jar (uber jar) using build commands in README */
        ImageIcon icon = new ImageIcon(lvEditor.class.getResource("/icon.png"));
        frame.setIconImage(icon.getImage());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
        frame.setSize(300, 300);
        frame.setLocationRelativeTo(null);
        setFont(new Font("Segoe UI", Font.PLAIN, fontSize));
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        JTextArea textwrite = new JTextArea();

        if (args.length > 0) {
            String filename = args[0];
            File file = new File(filename);

            if (file.exists()) {
                try {
                    String content = Files.readString(file.toPath());
                    textwrite.setText(content);
                } catch (IOException e) {
                    JOptionPane.showConfirmDialog(frame, "Error reading file!", "lvEditor", JOptionPane.WARNING_MESSAGE);
                    e.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(frame, "File not found!", "lvEditor", JOptionPane.WARNING_MESSAGE);
            }
        }

        JMenuBar menubar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenu format = new JMenu("Format");
        JMenu help = new JMenu("Help");

        JMenuItem save = new JMenuItem("Save", null);
        save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        JMenuItem open = new JMenuItem("Open", null);
        open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        JMenuItem exit = new JMenuItem("Exit", null);

        JMenuItem zoomin = new JMenuItem("Zoom in", null);
        zoomin.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_DOWN_MASK));
        JMenuItem zoomout = new JMenuItem("Zoom out", null);
        zoomout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));

        JMenuItem report = new JMenuItem("Report issue", null);
        JMenuItem about = new JMenuItem("About", null);

        file.add(save);
        file.add(open);
        file.addSeparator();
        file.add(exit);

        format.add(zoomin);
        format.add(zoomout);

        help.add(report);
        help.addSeparator();
        help.add(about);
        
        open.addActionListener(e -> {
            File openedFile = openFile(frame);

            if (openedFile.exists()) {
                try {
                    String content = Files.readString(openedFile.toPath());
                    textwrite.setText(content);
                } catch (IOException ex) {
                    JOptionPane.showConfirmDialog(frame, "Error reading file!", "lvEditor", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "File not found!", "lvEditor", JOptionPane.WARNING_MESSAGE);
            }
        });

        save.addActionListener(e -> {
            File fileItem = currentFile != null ? currentFile : saveFile(frame);
            if (fileItem != null) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileItem))) {
                    writer.write(textwrite.getText());
                    currentFile = fileItem;
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error saving file!", "lvEditor", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        zoomout.addActionListener(e -> {
            fontSize = Math.max(5, fontSize - 5);
            textwrite.setFont(new Font("Segoe UI", Font.PLAIN, fontSize));
            saveConfig(config);
        });
        
        zoomin.addActionListener(e -> {
            fontSize = Math.min(50, fontSize + 5);
            textwrite.setFont(new Font("Segoe UI", Font.PLAIN, fontSize));
            saveConfig(config);
        });

        exit.addActionListener(e -> {
            System.exit(0);
        });

        report.addActionListener(e -> {
            try {
                String url = "https://github.com/fragis0/lvEditor/issues/new";
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
            } catch (java.io.IOException ex) {
                System.out.println(ex.getMessage());
            }
        });

        about.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "lvEditor 1.0\nSimple, modern look and small text editor.\n    [ developed by: hvdev ]", "lvEditor", JOptionPane.INFORMATION_MESSAGE);
        });

        menubar.add(file);
        menubar.add(format);
        menubar.add(help);
        frame.setJMenuBar(menubar);

        JScrollPane scroll = new JScrollPane(textwrite);
        frame.getContentPane().add(scroll);
        frame.setVisible(true);
    }
    
    public static void saveConfig(Properties config) {
        try (FileOutputStream out = new FileOutputStream("config.properties")) {
            config.setProperty("fontsize", Integer.toString(fontSize));
            config.store(out, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setFont(Font font) {

        UIManager.put("TextArea.font", font);
    }

    public static File openFile(JFrame fr) {

        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(fr);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            return selected;
        }

        return null;
    }

    public static File saveFile(JFrame fr) {

        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(fr);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            return selected;
        }

        return null;
    }
}
