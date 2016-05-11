package HuffManager.ui;
import javax.swing.*;

/**
 * The class that represents the main window of the program, which is used to create/browse/modify the archive
 * files.
 * @author Jan Bryda
 */
public class ArchiveExplorerFrame extends JFrame {

    public ArchiveExplorerFrame(String name) {
        super(name);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().add(new JLabel("This is a really nice label, and btw. Hello World!"));
        pack();
        setVisible(true);
    }
}
