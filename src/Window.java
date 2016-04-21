import data.*;
import data.Polygon;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
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
    JPanel northPanelSouth = new JPanel();
    JPanel northPanelNorth = new JPanel();
    public static JPanel southPanel = new JPanel();

    public static boolean newFile = true;
    JLabel valueLabel;
    JSlider slider;
    JButton fileChooserButton;
    JButton nextPass;
    JFileChooser fileChooser;
    JLabel label;
    DrawingPanel dp;
    int hemiRes = 128;

    public static long startTime;
    public static long duration;
    public static JLabel durationLabel;

    public static void main(String args[]) {
        new Window();
    }

    public Window() {
        super("Radiosity Demonstrator");

        northPanel.setLayout(new GridLayout(2, 1));
        southPanel.setLayout(new BorderLayout());

        northPanelSouth.setLayout(new FlowLayout());
        northPanelSouth.add(new JLabel("Hemicube Resolution?"));
        slider = new JSlider(JSlider.HORIZONTAL, 4, 256, 128);
        slider.addChangeListener(new SliderListener());
        northPanelSouth.add(slider);
        valueLabel = new JLabel(hemiRes + "x" + hemiRes);
        northPanelSouth.add(valueLabel);

        label = new JLabel("Select a File: ");
        northPanelNorth.add(label);

        fileChooserButton = new JButton("Open File Chooser");
        fileChooserButton.addActionListener(this);
        northPanelNorth.add(fileChooserButton);

        northPanel.add(northPanelNorth);
        northPanel.add(northPanelSouth);

        nextPass = new JButton("Continue");
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

        setSize(800, 800);
        setResizable(false);
        setVisible(true);
    }

    class SliderListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider) e.getSource();
                hemiRes = source.getValue();
                northPanelSouth.remove(valueLabel);
                valueLabel = new JLabel(hemiRes + "x" + hemiRes);
                northPanelSouth.add(valueLabel);
                northPanelSouth.setVisible(false);
                northPanelSouth.setVisible(true);
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object widget = e.getSource();

        if (widget == fileChooserButton) {
            fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(".wld Files", "wld");
            fileChooser.setFileFilter(filter);

            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            File workingDirectory = new File(System.getProperty("user.dir")); //makes starting directory where the program is run from
            fileChooser.setCurrentDirectory(workingDirectory);
            int returnVal = fileChooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                startTime = System.currentTimeMillis();
                northPanel.setVisible(false);
                File file = fileChooser.getSelectedFile();
                new Parser(file);
                newFile = true;
                dp = new DrawingPanel(hemiRes);
                add(dp, BorderLayout.CENTER);
            }

        } else if (widget == nextPass) {
            //calculate next iteration, update pass count and refresh display
            remove(dp);
            dp = new DrawingPanel(hemiRes);
            dp.calculateRadiosities();
            add(dp, BorderLayout.CENTER);

            setVisible(false);
            setVisible(true);
        }
    }

}

