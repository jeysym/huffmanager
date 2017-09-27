package huffManager.ui;

import huffManager.archive.*;
import huffManager.archive.exceptions.*;
import huffManager.generator.*;
import huffManager.codecs.*;

import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * The class that represents the main window of the program, which is used to create/browse/modify the archive
 * files.
 *
 * @author Jan Bryda
 */
public class ArchiveExplorerFrame extends JFrame {
    ArchiveExplorerFrame thisFrame;

    ArchiveLoader archiveLoader = new ArchiveLoader();
    ArchiveSaver archiveSaver = new ArchiveSaver();
    Collection<Codec> knownCodecs = Codecs.getKnownCodecs();

    /**
     * Describes the currently opened archive.
     */
    class OpenedArchive {
        Archive archive;
        String archivePath;
        ArchiveDirectory currentDirectory;

        /**
         * Changes the current directory to its parent directory if the directory is not the root itself.
         */
        private void upALevel() {
            if (currentDirectory == null)
                return;

            ArchiveDirectory parent = currentDirectory.getParent();
            if (parent != null)
                currentDirectory = parent;

            archivePanel.repaint();
        }

        /**
         * Sets the opened archive to given archive, and given path to archive file on disk.
         * @param archive archive
         * @param archivePath path to archive file on disk
         */
        private void set(Archive archive, String archivePath) {
            this.archive = archive;
            this.archivePath = archivePath;
            this.currentDirectory = archive.getRootDirectory();

            enableArchiveOperations();
            statusBar.reset();
            archivePanel.repaint();
        }

        /**
         * Resets the opened archive so no archive is opened.
         */
        private void reset() {
            archive = null;
            archivePath = null;
            currentDirectory = null;

            disableArchiveOperations();
            statusBar.reset();
            archivePanel.repaint();
        }

        /**
         * Enables the archive explorer menu items that can change (add, remove, ...) the current archive.
         */
        private void enableArchiveOperations() {
            menuBar.fileMenu.save.setEnabled((archivePath != null));
            menuBar.fileMenu.saveAs.setEnabled(archive != null);
            menuBar.archiveMenu.createDirectory.setEnabled(true);
            menuBar.archiveMenu.add.setEnabled(true);
            menuBar.archiveMenu.removeSelected.setEnabled(true);
            menuBar.extractMenu.extractArchive.setEnabled(true);
            menuBar.extractMenu.extractSelected.setEnabled(true);

        }

        /**
         * Enables the archive explorer menu items that can change (add, remove, ...) the current archive and
         * also enables the new, open, save, save as menu items.
         */
        private void enableAllArchiveOperations() {
            enableArchiveOperations();
            menuBar.fileMenu._new.setEnabled(true);
            menuBar.fileMenu.open.setEnabled(true);
            menuBar.fileMenu.close.setEnabled(true);
        }

        /**
         * Disables the archive explorer menu items that can change (add, remove, ...) the current archive and
         * also disables the new, open, save, save as menu items.
         */
        private void disableAllArchiveOperations() {
            disableArchiveOperations();
            menuBar.fileMenu._new.setEnabled(false);
            menuBar.fileMenu.open.setEnabled(false);
            menuBar.fileMenu.close.setEnabled(false);
        }

        /**
         * Disables the archive explorer menu items that can change (add, remove, ...) the current archive.
         */
        private void disableArchiveOperations() {
            menuBar.fileMenu.save.setEnabled(false);
            menuBar.fileMenu.saveAs.setEnabled(false);
            menuBar.archiveMenu.createDirectory.setEnabled(false);
            menuBar.archiveMenu.add.setEnabled(false);
            menuBar.archiveMenu.removeSelected.setEnabled(false);
            menuBar.extractMenu.extractArchive.setEnabled(false);
            menuBar.extractMenu.extractSelected.setEnabled(false);
        }
    }

    /** Currently opened archive */
    OpenedArchive openedArchive = new OpenedArchive();

    ArchiveExplorerMenuBar menuBar = new ArchiveExplorerMenuBar();
    ButtonControls buttonControls = new ButtonControls();
    ArchiveExplorerPanel archivePanel = new ArchiveExplorerPanel();
    StatusBar statusBar = new StatusBar();

    // segments of the ui

    /**
     * Menu bar of the archive explorer window.
     */
    class ArchiveExplorerMenuBar extends JMenuBar {
        FileMenu fileMenu = new FileMenu();
        ArchiveMenu archiveMenu = new ArchiveMenu();
        ExtractMenu extractMenu = new ExtractMenu();
        HelpMenu helpMenu = new HelpMenu();

        /**
         * Contains menu items: New, Open, Save, Save as
         */
        class FileMenu extends JMenu  {
            JMenuItem _new = new JMenuItem("New");
            JMenuItem open = new JMenuItem("Open");
            JMenuItem save = new JMenuItem("Save");
            JMenuItem saveAs = new JMenuItem("Save as");
            JMenuItem close = new JMenuItem("Close");

            public FileMenu() {
                super("File");

                _new.addActionListener((e) -> newArchive());
                open.addActionListener((e) -> openArchive());
                save.addActionListener((e) -> saveArchive());
                saveAs.addActionListener((e) -> saveArchiveAs());
                close.addActionListener((e) -> closeArchive());

                add(_new);
                add(open);
                add(save);
                add(saveAs);
                add(close);
            }
        }

        /**
         * Contains menu items: Create new directory, Add to archive, Remove selected
         */
        class ArchiveMenu extends JMenu  {
            JMenuItem createDirectory = new JMenuItem("Create directory");
            JMenuItem add = new JMenuItem("Add");
            JMenuItem removeSelected = new JMenuItem("Remove Selected");

            public ArchiveMenu() {
                super("Archive");

                createDirectory.addActionListener((e) -> createDirectory());
                add.addActionListener((e) -> addToArchive());
                removeSelected.addActionListener((e) -> removeSelected());

                add(createDirectory);
                add(add);
                add(removeSelected);
            }

        }

        /**
         * Contains menu items: Extract selected, Extract whole archive
         */
        class ExtractMenu extends JMenu  {
            JMenuItem extractSelected = new JMenuItem("Extract Selected");
            JMenuItem extractArchive = new JMenuItem("Extract Archive");

            public ExtractMenu() {
                super("Extract");

                extractSelected.addActionListener((e) -> extractSelected());
                extractArchive.addActionListener((e) -> extractArchive());

                add(extractSelected);
                add(extractArchive);
            }
        }

        /**
         * Contains menu items: About
         */
        class HelpMenu extends JMenu  {
            JMenuItem about = new JMenuItem("About");

            public HelpMenu() {
                super("Help");

                about.addActionListener((e) ->
                        JOptionPane.showMessageDialog(thisFrame, "HuffManager v1.0\ncreated by Jan Bryda"));

                add(about);
            }
        }

        public ArchiveExplorerMenuBar() {
            add(fileMenu);
            add(archiveMenu);
            add(extractMenu);
            add(helpMenu);
        }
    }

    /**
     * Panel with table that shows the currently opened directory in archive.
     */
    class ArchiveExplorerPanel extends JPanel {

        class ArchiveTableModel extends AbstractTableModel {
            String[] columnNames = {"Type", "Name", "Size", "Codec"};

            @Override
            public String getColumnName(int column) {
                return columnNames[column];
            }

            @Override
            public int getRowCount() {
                try {
                    ArchiveDirectory currentDir = openedArchive.currentDirectory;
                    int subdirCount = currentDir.getSubdirectories().size();
                    int fileCount = currentDir.getFiles().size();
                    return subdirCount + fileCount;
                } catch (Exception e) {
                    return 0;
                }
            }

            @Override
            public int getColumnCount() {
                return columnNames.length;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                if (rowIndex < 0 || rowIndex >= getRowCount() || columnIndex < 0 || columnIndex >= getColumnCount())
                    return null;
                try {
                    ArchiveDirectory currentDir = openedArchive.currentDirectory;
                    List<ArchiveDirectory> subDirs = currentDir.getSubdirectories();
                    List<ArchiveFile> files = currentDir.getFiles();

                    if (rowIndex < subDirs.size()) {
                        // we want to get the subdirectory
                        ArchiveDirectory directory = subDirs.get(rowIndex);
                        switch (columnIndex) {
                            case 0:
                                return "D";
                            case 1:
                                return directory.getName();
                            case 2:
                                return directory.getSize();
                            case 3:
                                return "-";
                        }
                    } else {
                        // we want to get the file
                        ArchiveFile file = files.get(rowIndex - subDirs.size());
                        switch (columnIndex) {
                            case 0:
                                return "F";
                            case 1:
                                return file.getName();
                            case 2:
                                return file.getSize();
                            case 3:
                                return file.getCodec().getCodecName();
                        }
                    }

                    return null;
                } catch (Exception e) {
                    return null;
                }
            }
        }

        JTable table = new JTable(new ArchiveTableModel());

        public ArchiveExplorerPanel() {
            setLayout(new BorderLayout());
            table.setFillsViewportHeight(true);

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        ArchiveDirectory newDirectory = getDirectoryAtIndex(table.getSelectedRow());
                        if (newDirectory != null)
                            openedArchive.currentDirectory = newDirectory;

                        archivePanel.repaint();
                    }
                }
            });

            JScrollPane scrollPane = new JScrollPane(table);
            add(scrollPane, BorderLayout.CENTER);
        }
    }

    /**
     * Button controls of the archive explorer frame.
     */
    class ButtonControls extends JPanel {

        class UpLevelButton extends JButton {

            public UpLevelButton() {
                super("Up");
                addActionListener((e) -> openedArchive.upALevel());
            }
        }

        class AddButton extends JButton {

            public AddButton() {
                super("+");
                addActionListener((e) -> {
                    addToArchive();
                });
            }
        }

        class RemoveButton extends JButton {

            public RemoveButton() {
                super("-");
                addActionListener((e) -> {
                    removeSelected();
                });
            }
        }

        public ButtonControls() {
            setLayout(new GridLayout(3, 1));

            add(new UpLevelButton());
            add(new AddButton());
            add(new RemoveButton());
        }
    }

    /**
     * Status bar showing the state of the archive explorer window.
     */
    class StatusBar extends JPanel {
        JLabel statusLabel = new JLabel("no archive opened");

        private StatusBar() {
            setLayout(new BorderLayout());
            add(statusLabel, BorderLayout.WEST);
        }

        private void reset() {
            if (openedArchive.archive == null)
                statusLabel.setText("no archive opened");
            else
                statusLabel.setText("ready");
        }
    }

    // workers used to load / save / extract from archives

    /**
     * Is used to load the current archive from file.
     */
    class ArchiveLoaderWorker extends SwingWorker<Archive, Integer> {
        File fileToLoad;

        public ArchiveLoaderWorker(String filePath) {
            openedArchive.disableAllArchiveOperations();
            this.fileToLoad = new File(filePath);
        }

        @Override
        protected Archive doInBackground() throws Exception {
            statusBar.statusLabel.setText("loading...");
            return archiveLoader.load(new FileInputStreamGenerator(fileToLoad));
        }

        @Override
        protected void done() {
            try {
                Archive archive = get();
                openedArchive.set(archive, fileToLoad.getPath());
            } catch (Exception e) {
                openedArchive.reset();
                JOptionPane.showMessageDialog(thisFrame, "Error while loading archive");
            }
            openedArchive.enableAllArchiveOperations();
            statusBar.reset();
        }
    }

    /**
     * Is used to save the current archive to file.
     */
    class ArchiveSaverWorker extends SwingWorker<Void, Integer> {
        File fileToSave;
        Archive archive;

        public ArchiveSaverWorker(String filePath, Archive archive) {
            openedArchive.disableAllArchiveOperations();
            this.fileToSave = new File(filePath);
            this.archive = archive;
        }

        @Override
        protected Void doInBackground() throws Exception {
            statusBar.statusLabel.setText("saving...");
            archiveSaver.save(archive, new FileOutputStreamGenerator(fileToSave));
            return null;
        }

        @Override
        protected void done() {
            try {
                Void v = get();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(thisFrame, "Error while saving archive");
            }
            openedArchive.enableAllArchiveOperations();
            statusBar.reset();
        }
    }

    /**
     * Is used to extract given files and directories from archive, in a given directory on disk.
     */
    class ExtractWorker extends SwingWorker<Void, Integer> {
        List<ArchiveDirectory> directoriesToExtract;
        List<ArchiveFile> filesToExtract;
        File extractTo;

        public ExtractWorker(List<ArchiveDirectory> directoriesToExtract, List<ArchiveFile> filesToExtract, File extractTo) {
            openedArchive.disableAllArchiveOperations();
            this.directoriesToExtract = directoriesToExtract;
            this.filesToExtract = filesToExtract;
            this.extractTo = extractTo;
        }

        @Override
        protected Void doInBackground() throws Exception {
            statusBar.statusLabel.setText("extracting...");
            try {
                for (ArchiveDirectory subdir : directoriesToExtract)
                    extractDirectory(subdir, extractTo);

                for (ArchiveFile file : filesToExtract)
                    extractFile(file, extractTo);
            } catch (IOException | UnableToGetStreamException e) {
                JOptionPane.showMessageDialog(thisFrame, "Error : Unable to extract the selection!");
            }
            return null;
        }

        @Override
        protected void done() {
            openedArchive.enableAllArchiveOperations();
            statusBar.reset();
        }
    }

    // file choosers

    /** Regular file chooser used for choosing files/directories when opening/saving/exctracting. */
    final JFileChooser myFileChooser = new JFileChooser(System.getProperty("user.dir"));

    /**
     * Gives user the ability to choose a file from disk and also select one of known codecs to be used
     * on this file.
     */
    class AddFileToArchiveChooser extends JFileChooser {
        JComboBox<Codec> codecJComboBox;

        class CodecSelection extends JPanel {

            public CodecSelection() {
                setLayout(new FlowLayout());

                add(new JLabel("Codec: "));
                codecJComboBox = new JComboBox<>(knownCodecs.toArray(new Codec[1]));
                add(codecJComboBox);
            }
        }

        public AddFileToArchiveChooser() {
            super(System.getProperty("user.dir"));
            setDialogType(JFileChooser.CUSTOM_DIALOG);
            setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            setAccessory(new CodecSelection());
            setDialogTitle("Choose file or directory to be added into archive");
            setApproveButtonText("Add");
        }
    }

    // private methods used to manipulate the archive

    /**
     * Sets the opened archive to be a new empty archive.
     */
    private void newArchive() {
        openedArchive.reset();
        openedArchive.set(new Archive(), null);
    }

    /**
     * Prompts user to choose an archive file on disk, and then opens that archive.
     */
    private void openArchive() {
        openedArchive.reset();

        myFileChooser.setDialogTitle("Open archive");
        myFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = myFileChooser.showOpenDialog(thisFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            ArchiveLoaderWorker worker =
                    new ArchiveLoaderWorker(myFileChooser.getSelectedFile().getAbsolutePath());
            worker.execute();
        }
    }

    /**
     * Closes currently opened archive.
     */
    private void closeArchive() {
        openedArchive.reset();
    }

    /**
     * Saves the currently opened archive.
     */
    private void saveArchive() {
        ArchiveSaverWorker worker = new ArchiveSaverWorker(
                openedArchive.archivePath, openedArchive.archive);
        worker.execute();
    }

    /**
     * Prompts user to choose a target the archive the archive should be saved as, and then saves it as that target.
     */
    private void saveArchiveAs() {
        myFileChooser.setApproveButtonText("Save as");
        int result = myFileChooser.showSaveDialog(thisFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            ArchiveSaverWorker worker = new ArchiveSaverWorker(
                    myFileChooser.getSelectedFile().getAbsolutePath(),
                    openedArchive.archive
            );
            worker.execute();
        }
    }

    /**
     * Prompts user for the name of new directory, and creates it in the currently opened directory.
     */
    private void createDirectory() {
        String dirName = JOptionPane.showInputDialog(thisFrame, "Insert the name of the directory", "New directory");
        if (openedArchive.currentDirectory == null)
            openedArchive.currentDirectory = new ArchiveDirectory(dirName);
        else
            openedArchive.currentDirectory.addSubdirectory(new ArchiveDirectory(dirName));

        archivePanel.repaint();
    }

    /**
     * Prompts the user to choose a file or directory that should be added into the currently opened archive
     * and the adds it.
     */
    private void addToArchive() {
        AddFileToArchiveChooser fileChooser = new AddFileToArchiveChooser();
        int result = fileChooser.showOpenDialog(thisFrame);

        if (result == JFileChooser.APPROVE_OPTION) {
            File chosenFile = fileChooser.getSelectedFile();
            Codec chosenCodec = (Codec)fileChooser.codecJComboBox.getSelectedItem();
            if (chosenFile.isFile())
                addFileToArchive(chosenFile, openedArchive.currentDirectory, chosenCodec);
            else if (chosenFile.isDirectory())
                addDirectoryToArchive(chosenFile, openedArchive.currentDirectory, chosenCodec);

            archivePanel.repaint();
        }
    }

    /**
     * Adds the file from disk with specified codec to the archive.
     * @param fileToAdd file to be added
     * @param directory directory it will be put in
     * @param codec codec that will be used on that file
     */
    private void addFileToArchive(File fileToAdd, ArchiveDirectory directory, Codec codec) {
        directory.addFile(new FileToCode(
                fileToAdd.getPath(),
                codec
        ));
    }

    /**
     * Adds the directory from the disk (and all its subdirectories and files) to the archive.
     * @param directory directory to be added
     * @param archiveDirectory directory in archive it will be put in
     * @param codec codec that will be used on that directory
     */
    private void addDirectoryToArchive(File directory, ArchiveDirectory archiveDirectory, Codec codec) {
        ArchiveDirectory newDirectory = new ArchiveDirectory(directory.getName());

        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                addDirectoryToArchive(file, newDirectory, codec);
            } else if (file.isFile()) {
                addFileToArchive(file, newDirectory, codec);
            }
        }

        archiveDirectory.addSubdirectory(newDirectory);
    }

    /**
     * Removes the selected files and directories from the currently opened archive.
     */
    private void removeSelected() {
        List<ArchiveDirectory> directoriesToDelete = new ArrayList<ArchiveDirectory>();
        List<ArchiveFile> filesToDelete = new ArrayList<ArchiveFile>();
        getSelected(directoriesToDelete, filesToDelete);

        ArchiveDirectory directory = openedArchive.currentDirectory;
        for (ArchiveDirectory subdir : directoriesToDelete)
            directory.removeSubdirectory(subdir);

        for (ArchiveFile file : filesToDelete)
            directory.removeFile(file);

        archivePanel.repaint();
    }

    /**
     * Is used to get the files and directories that are selected.
     * @param selectedDirectories selected directories will be put in this list
     * @param selectedFiles selected files will be put in this list
     */
    private void getSelected(List<ArchiveDirectory> selectedDirectories, List<ArchiveFile> selectedFiles) {
        selectedDirectories.clear();
        selectedFiles.clear();

        int[] selectedRows = archivePanel.table.getSelectedRows();
        Arrays.sort(selectedRows);
        int rowsPos = 0;

        if (selectedRows.length == 0)
            return;

        ArchiveDirectory directory = openedArchive.currentDirectory;
        int position = 0;
        for (ArchiveDirectory subdir : directory.getSubdirectories()) {
            if (selectedRows[rowsPos] == position) {
                selectedDirectories.add(subdir);

                if (++rowsPos == selectedRows.length)
                    return;
            }
            position++;
        }

        for (ArchiveFile file : directory.getFiles()) {
            if (selectedRows[rowsPos] == position) {
                selectedFiles.add(file);

                if (++rowsPos == selectedRows.length)
                    return;
            }
            position++;
        }
    }

    /**
     * Gets the directory that is shown at specific index in table. If there is none null is returned.
     * @param index index of table row
     * @return archive directory on that row
     */
    private ArchiveDirectory getDirectoryAtIndex(int index) {
        List<ArchiveDirectory> directories = openedArchive.currentDirectory.getSubdirectories();

        if (index < directories.size())
            return directories.get(index);
        else
            return null;
    }

    /**
     * Prompts the user to choose a directory on the disk, where to extract the selected files and directories from
     * the archive.
     */
    private void extractSelected() {
        myFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        myFileChooser.setDialogTitle("Extract into");

        int result = myFileChooser.showDialog(thisFrame, "Extract");
        if (result == JFileChooser.APPROVE_OPTION) {
            List<ArchiveDirectory> selectedDirectories = new ArrayList<>();
            List<ArchiveFile> selectedFiles = new ArrayList<>();
            getSelected(selectedDirectories, selectedFiles);
            File extractTo = myFileChooser.getSelectedFile();
            ExtractWorker worker = new ExtractWorker(selectedDirectories, selectedFiles, extractTo);
            worker.execute();
        }
    }

    /**
     * Prompts the user to choose a directory on the disk, where to extract the whole archive.
     */
    private void extractArchive() {
        myFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        myFileChooser.setDialogTitle("Extract into");

        int result = myFileChooser.showDialog(thisFrame, "Extract");
        if (result == JFileChooser.APPROVE_OPTION) {
            File extractTo = myFileChooser.getSelectedFile();
            List<ArchiveDirectory> directoriesToExtract = openedArchive.archive.getRootDirectory().getSubdirectories();
            List<ArchiveFile> filesToExtract = openedArchive.archive.getRootDirectory().getFiles();
            ExtractWorker worker = new ExtractWorker(directoriesToExtract, filesToExtract, extractTo);
            worker.execute();
        }
    }

    /**
     * Extracts the given archive directory into given directory on the disk.
     * @param directory archive directory to be extracted
     * @param extractTo where to extract into
     * @throws IOException
     * @throws UnableToGetStreamException
     */
    private void extractDirectory(ArchiveDirectory directory, File extractTo) throws IOException, UnableToGetStreamException {
        File newDirectory = new File(extractTo, directory.getName());
        newDirectory.mkdir();

        for (ArchiveDirectory subdirectory : directory.getSubdirectories())
            extractDirectory(subdirectory, newDirectory);

        for (ArchiveFile file : directory.getFiles())
            extractFile(file, newDirectory);
    }

    /**
     * Extracts the given archive file into given directory on the disk.
     * @param file archive file to be extracted
     * @param extractTo where to extract into
     * @throws UnableToGetStreamException
     * @throws IOException
     */
    private void extractFile(ArchiveFile file, File extractTo) throws UnableToGetStreamException, IOException {
        File target = new File(extractTo, file.getName());
        Files.copy(file.getDecodedStream(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public ArchiveExplorerFrame() {
        super("HuffManager");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        thisFrame = this;
        openedArchive.reset();
        setJMenuBar(menuBar);

        contentPane.setLayout(new BorderLayout());
        contentPane.add(buttonControls, BorderLayout.EAST);
        contentPane.add(archivePanel, BorderLayout.CENTER);
        contentPane.add(statusBar, BorderLayout.SOUTH);

        pack();
        setVisible(true);
    }
}

