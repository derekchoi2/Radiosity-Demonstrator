import data.*;
import data.Polygon;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class Window extends JFrame implements ActionListener {

    public static ArrayList<Polygon> polyArrayList = new ArrayList<>();
    public static ArrayList<Element> faces = new ArrayList<>();
    public static ArrayList<int[]> facesColorCode = new ArrayList<>();
    public static float[][] formFactors;
    public static ArrayList<float[]> radiosities = new ArrayList<>();

    JPanel northPanel = new JPanel();
    JPanel southPanel = new JPanel();

    JButton fileChooserButton;
    JButton nextPass;
    JFileChooser fileChooser;
    JLabel label;
    DrawingPanel dp;
    public static int pass = 0;
    JLabel passText;

    JDialog dialog;

    public static void main(String args[]) {
        new Window();
    }

    public Window(){
        super("Radiosity Demonstrator");

        northPanel.setLayout(new BorderLayout());
        southPanel.setLayout(new BorderLayout());

        label = new JLabel("Select a File: ");
        northPanel.add(label);

        fileChooserButton = new JButton("Open File Chooser");
        fileChooserButton.addActionListener(this);
        northPanel.add(fileChooserButton);

        passText = new JLabel("Pass: 0");
        southPanel.add(passText, BorderLayout.WEST);

        nextPass = new JButton("Next Pass");
        nextPass.addActionListener(this);
        southPanel.add(nextPass, BorderLayout.CENTER);

        add(southPanel, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        southPanel.setVisible(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        setSize(800,800);
        setResizable(false);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e){
        Object widget = e.getSource();

        if (widget == fileChooserButton) {
            fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            File workingDirectory = new File(System.getProperty("user.dir")); //makes starting directory where the program is run from
            fileChooser.setCurrentDirectory(workingDirectory);
            int returnVal = fileChooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION){
                northPanel.setVisible(false);
                File file = fileChooser.getSelectedFile();
                new Parser(file);
                dp = new DrawingPanel();
                add(dp, BorderLayout.CENTER);
                southPanel.setVisible(true);
            }

        } else if(widget == nextPass){
            //calculate next iteration, update pass count and refresh display
            southPanel.remove(passText);
            pass++;
            passText.setText("Pass: " + pass);

            remove(dp);
            dp = new DrawingPanel();
            dp.calculateRadiosities();
            add(dp, BorderLayout.CENTER);

            southPanel.add(passText, BorderLayout.WEST);
            setVisible(false);
            setVisible(true);
        }
    }

}

