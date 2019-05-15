package org.editor;

import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Editor extends JFrame implements ActionListener {
    private final static Logger logger = Logger.getLogger(Editor.class);
    private SimpleDateFormat lastModificationDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private JTextArea textArea;
    private JFrame frame;

    Editor() {
        frame = new JFrame("editor");
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        textArea = new JTextArea();

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            MetalLookAndFeel.setCurrentTheme(new OceanTheme());
        } catch (Exception e) {
            logger.fatal("\tNOT FOUND \'javax.swing.plaf.metal.MetalLookAndFeel\'");
        }

        // Элемент меню File
        JMenu menuFile = new JMenu("File");

        JMenuItem menuItemNew = new JMenuItem("New");
        JMenuItem menuItemOpen = new JMenuItem("Open");
        JMenuItem menuItemSave = new JMenuItem("Save");
        JMenuItem menuItemDelete = new JMenuItem("Delete");

        menuItemNew.addActionListener(this);
        menuItemOpen.addActionListener(this);
        menuItemSave.addActionListener(this);
        menuItemDelete.addActionListener(this);

        menuFile.add(menuItemNew);
        menuFile.add(menuItemOpen);
        menuFile.add(menuItemSave);
        menuFile.add(menuItemDelete);

        // Элемент меню Edit
        JMenu menuEmulate = new JMenu("Emulate");

        JMenuItem menuItemInterrupt = new JMenuItem("Interrupt");

        menuItemInterrupt.addActionListener(this);

        menuEmulate.add(menuItemInterrupt);

        // Меню
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(menuFile);
        menuBar.add(menuEmulate);

        frame.setJMenuBar(menuBar);
        frame.add(textArea);
        frame.setSize(500, 500);
        frame.setVisible(true);
    }

    // Обработчик нажатия
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();

        if (actionCommand.equals("Save")) {
            logger.debug("\tSTART \'save\'");

            JFileChooser fileChooser = new JFileChooser("Save:");
            int repo = fileChooser.showSaveDialog(null);

            // Выбран файл
            if (repo == JFileChooser.APPROVE_OPTION) {
                // Присвоить заголовоку пусть выбранного каталога
                File file = new File(fileChooser.getSelectedFile().getAbsolutePath());

                try {
                    fixFileLog(file);
                } catch (FileNotFoundException ex) {
                    logger.error("-- File was not found --");
                }

                try {
                    // Создать файл для записи
                    FileWriter fileWriter = new FileWriter(file, false);

                    // Создать буфер для записи
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                    // Запись
                    bufferedWriter.write(textArea.getText());

                    bufferedWriter.flush();
                    bufferedWriter.close();

                    logger.debug("\tFINISH \'save\':" + file.getAbsolutePath());
                } catch (Exception evt) {
                    JOptionPane.showMessageDialog(frame, evt.getMessage());

                    logger.warn("\tFAILED \'save\':" + file.getAbsolutePath());
                }
            } else {
                // Отмена операции
                JOptionPane.showMessageDialog(frame, "Отмена save");

                logger.warn("\tCANCEL \'save\'");
            }
        } else if (actionCommand.equals("Open")) {
            logger.debug("\tSTART \'open\'");

            JFileChooser fileChooser = new JFileChooser("Open:");
            int repo = fileChooser.showOpenDialog(null);

            // Выбран файл
            if (repo == JFileChooser.APPROVE_OPTION) {
                // Присвоить заголовоку пусть выбранного каталога
                File file = new File(fileChooser.getSelectedFile().getAbsolutePath());

                try {
                    String string1, string2;
                    FileReader fileReader = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);

                    string2 = bufferedReader.readLine();

                    // Конкатинация строк
                    while ((string1 = bufferedReader.readLine()) != null) {
                        string2 = string2 + "\n" + string1;
                    }

                    textArea.setText(string2);

                    logger.debug("\tFINISH \'open\':" + file.getAbsolutePath());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage());

                    logger.warn("\tFAILED \'open\':" + file.getAbsolutePath());
                }
            } else {
                // Отмена операции
                JOptionPane.showMessageDialog(frame, "Отмена open");

                logger.warn("\tCANCEL \'open\'");
            }
        } else if (actionCommand.equals("Delete")) {
            logger.debug("\tSTART \'delete\'");

            JFileChooser fileChooser = new JFileChooser("Delete:");
            int repo = fileChooser.showOpenDialog(null);

            // Выбран файл
            if (repo == JFileChooser.APPROVE_OPTION) {
                // Присвоить заголовоку пусть выбранного каталога
                File file = new File(fileChooser.getSelectedFile().getAbsolutePath());

                try {
                    file.delete();

                    logger.debug("\tFINISH \'delete\':" + file.getAbsolutePath());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage());

                    logger.warn("\tFAILED \'delete\':" + file.getAbsolutePath());
                }
            } else {
                // отмена операции
                JOptionPane.showMessageDialog(frame, "Отмена delete");
                logger.warn("\tCANCEL \'delete\'");
            }
        } else if (actionCommand.equals("New")) {
            logger.debug("\tSTART \'new\'");

            try {
                textArea.setText("");
            } catch (Exception ex) {
                logger.warn("\tFAILED \'new\'");
            }

            logger.debug("\tFINISH \'new\'");
        }
    }

    // Корректировка файла логов
    private static void fixFileLog(File file) throws FileNotFoundException {
        /**********************************************
         *  Чтение файла логов
         */
        Scanner s = new Scanner(new File("D:\\Dump\\JDump\\file-system-service\\log4j-application.log"));
        List<String> list = new ArrayList<String>();

        while (s.hasNext()) {
            list.add(s.next());
        }

        s.close();

        /**********************************************
         *  Чтение файлов из каталога
         */
        String expDir = file.getParent();
        List<File> expFiles = Arrays.asList(listFiles(expDir));

        // Проверка на соответсвие логов и каталога
        for (File expFile : expFiles) {
            boolean mustExist = true;
            Pattern pattern = Pattern.compile(expFile.getAbsolutePath());

            for (String line : list) {
                Matcher matcherDelete = pattern.matcher("FINISH 'delete':" + line);
                Matcher matcherSave = pattern.matcher("FINISH 'save':" + line);

                if (matcherDelete.find()) {
                    mustExist = false;
                } else if (matcherSave.find()) {
                    mustExist = true;
                }
            }

            if (mustExist) {
                logger.warn("\tFORCE \'save\':" + expFile.getAbsolutePath());
                File file1 = new File(expFile.getAbsolutePath());
            } else {
                logger.warn("\tFORCE \'delete\':" + expFile.getAbsolutePath());
                expFile.delete();
            }
        }
    }

    // Чтение файлов из каталога
    private static File[] listFiles(String dirName) {
        File dir = new File(dirName);

        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".txt");
            }
        });
    }

    // Главный метод
    public static void main(String[] args) {
        Editor editor = new Editor();
    }
}
