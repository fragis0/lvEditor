import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Properties;
import com.formdev.flatlaf.FlatLightLaf;
import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

public class lvEditor { 

    private static File currentFile = null;
    private static int fontSize;
    private static UndoManager undoManager;

    public static void main(String[] args) {
        
        // sanwic beter
        //    -stockman50
        Properties config = new Properties();
        try (FileInputStream in = new FileInputStream("config.properties")) {
            config.load(in);
            fontSize = Integer.parseInt(config.getProperty("fontsize", "15"));
        } catch (IOException e) {
            fontSize = 15;
            saveConfig(config);
        }

        undoManager = new UndoManager();

	    JFrame frame = new JFrame("lvEditor");
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
        textwrite.getDocument().addUndoableEditListener(undoManager);
        undoManager.maxBufferSize = 10;

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
        JMenu view = new JMenu("View");
        JMenu edit = new JMenu("Edit");
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

        JMenuItem undo = new JMenuItem("Undo", null);
        undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        JMenuItem redo = new JMenuItem("Redo", null);
        redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));

        JMenuItem report = new JMenuItem("Report issue", null);
        JMenuItem about = new JMenuItem("About", null);

        file.add(save);
        file.add(open);
        file.addSeparator();
        file.add(exit);

        view.add(zoomin);
        view.add(zoomout);

        edit.add(undo);
        edit.add(redo);

        help.add(report);
        help.addSeparator();
        help.add(about);
        
        open.addActionListener(e -> {
            File openedFile = openFile(frame);

            if (openedFile.exists()) {
                try {
                    String content = Files.readString(openedFile.toPath());
                    textwrite.setText(content);
                    undoManager.reset();
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
            JOptionPane.showMessageDialog(frame, "lvEditor 1.1.0\nSimple, modern look and small text editor.\n    [ developed by: hvdev ]", "lvEditor", JOptionPane.INFORMATION_MESSAGE);
        });

        undo.addActionListener(e -> {
            if (undoManager.canUndo()) {
                undoManager.undo();
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        });

        redo.addActionListener(e -> {
            if (undoManager.canRedo()) {
                undoManager.redo();
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        });

        menubar.add(file);
        menubar.add(view);
        menubar.add(edit);
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

    public static class UndoManager extends AbstractUndoableEdit implements UndoableEditListener {
        private String lastEditName = null;
        private List<MergeComponentEdit> edits = new ArrayList<MergeComponentEdit>(32);
        private MergeComponentEdit current;
        private int pointer = -1;
        private int maxBufferSize = 50;

        private List<ChangeListener> changeListeners = new ArrayList<>(8);

        public void reset() {
            edits.clear();
            pointer = -1;
            lastEditName = null;
            current = null;
            fireStateChanged();
        }

        public void addChangeListener(ChangeListener changeListener) {
            changeListeners.add(changeListener);
        }

        public void removeChangeListener(ChangeListener changeListener) {
            changeListeners.remove(changeListener);
        }

        public void undoableEditHappened(UndoableEditEvent e) {
            UndoableEdit edit = e.getEdit();
            if (edit instanceof AbstractDocument.DefaultDocumentEvent) {
                try {
                    AbstractDocument.DefaultDocumentEvent event = (AbstractDocument.DefaultDocumentEvent) edit;
                    int start = event.getOffset();
                    int len = event.getLength();
                    if (start + len > event.getDocument().getLength()) {
                        createCompoundEdit();
                        current.addEdit(edit);
                        lastEditName = edit.getPresentationName();
                    } else {

                        String text = event.getDocument().getText(start, len);
                        boolean isNeedStart = false;
                        if (current == null) {
                            isNeedStart = true;
                        } else if (text.contains(" ")) {
                            isNeedStart = true;
                        } else if (lastEditName == null || !lastEditName.equals(edit.getPresentationName())) {
                            isNeedStart = true;
                        }

                        while (pointer < edits.size() - 1) {
                            edits.remove(edits.size() - 1);
                            isNeedStart = true;
                        }
                        if (isNeedStart) {
                            createCompoundEdit();
                        }

                        current.addEdit(edit);
                        lastEditName = edit.getPresentationName();
                    }
                    fireStateChanged();
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
        }

        public void createCompoundEdit() {
            if (current == null || current.getLength() > 0) {
                current = new MergeComponentEdit();
                if (edits.size() >= maxBufferSize) {
                    edits.remove(0);
                    if (pointer > 0) {
                        pointer--;
                    } else {
                        pointer = -1;
                    }
                }
                edits.add(current);
                pointer = edits.size() - 1;
            }
        }

        public void undo() throws CannotUndoException {
            if (!canUndo()) {
                throw new CannotUndoException();
            }

            MergeComponentEdit u = edits.get(pointer);
            u.undo();
            pointer--;

            fireStateChanged();
        }

        public void redo() throws CannotUndoException {
            if (!canRedo()) {
                throw new CannotUndoException();
            }

            pointer++;
            MergeComponentEdit u = edits.get(pointer);
            u.redo();

            fireStateChanged();
        }

        public boolean canUndo() {
            return pointer >= 0;
        }

        public boolean canRedo() {
            return edits.size() > 0 && pointer < edits.size() - 1;
        }

        protected void fireStateChanged() {
            if (changeListeners.isEmpty()) {
                return;
            }
            ChangeEvent evt = new ChangeEvent(this);
            for (ChangeListener listener : changeListeners) {
                listener.stateChanged(evt);
            }
        }

        protected class MergeComponentEdit extends CompoundEdit {
            boolean isUnDone = false;

            public int getLength() {
                return edits.size();
            }

            public void undo() throws CannotUndoException {
                super.undo();
                isUnDone = true;
            }

            public void redo() throws CannotUndoException {
                super.redo();
                isUnDone = false;
            }

            public boolean canUndo() {
                return edits.size() > 0 && !isUnDone;
            }

            public boolean canRedo() {
                return edits.size() > 0 && isUnDone;
            }

        }
    }
}
