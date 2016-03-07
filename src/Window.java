import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
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
    JPanel northPanel = new JPanel();
    JPanel southPanel = new JPanel();

    JButton fileChooserButton;
    JButton nextPass;
    JFileChooser fileChooser;
    JLabel label;
    DrawingPanel dp;
    int pass = 0;
    JLabel passText;

    public static void main(String args[]) {
        //Gui gui = new Gui();

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

        setSize(1000,1000);
        setResizable(false);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e){
        Object widget = e.getSource();

        if (widget == fileChooserButton) {
//            fileChooser = new JFileChooser();
//            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//            File workingDirectory = new File(System.getProperty("user.dir")); //makes starting directory where the program is run from
//            fileChooser.setCurrentDirectory(workingDirectory);
//            int returnVal = fileChooser.showOpenDialog(this);
//            if (returnVal == JFileChooser.APPROVE_OPTION){
                northPanel.setVisible(false);
                //File file = fileChooser.getSelectedFile();
                File file = new File("room.wld");
                new Parser(file);
                dp = new DrawingPanel();
                add(dp, BorderLayout.CENTER);

                southPanel.setVisible(true);
            //}

        } else if(widget == nextPass){
            southPanel.remove(passText);
            pass++;
            passText.setText("Pass: " + pass);

            calculateRadiosity();
            redraw();

            southPanel.add(passText, BorderLayout.WEST);
            setVisible(false);
            setVisible(true);
        }
    }

    private void redraw(){
        remove(dp);
        dp = new DrawingPanel();
        add(dp, BorderLayout.CENTER);
    }

    private void calculateRadiosity(){

        //TODO radiosity calculations

        //first pass all exitance = [0,0,0] except lights
        for (Polygon aPolyArrayList : polyArrayList) {
            for (int j = 0; j < aPolyArrayList.getSurfaces().size(); j++) {
                for (int k = 0; k < aPolyArrayList.getSurfaces().get(j).getPatches().size(); k++) {
                    for (int l = 0; l < aPolyArrayList.getSurfaces().get(j).getPatches().get(k).getElements().size(); l++) {
                        float[] e = aPolyArrayList.getSurfaces().get(j).getReflectance();
                        aPolyArrayList.getSurfaces().get(j).getPatches().get(k).getElements().get(l).setEmission(e);
                    }
                }
            }
        }
    }

}

