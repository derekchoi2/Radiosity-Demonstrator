import data.*;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Created by derek on 05/11/2015.
 */
public class Parser {

    static int noOfPolygons = 0; //count how many polygons entered so far

    public Parser(File file) {

        try {
            FileReader input = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(input);

            String line = bufferedReader.readLine(); //line currently being read

            ArrayList<Vertex> vertices;
            ArrayList<Patch> patches;
            ArrayList<Surface> surfaces;
            float[] scale = new float[3];
            float[] rotation = new float[3];
            float[] translation = new float[3];
            int faceindex = 0;

            String comment = "";

            if (line.contains("WORLD")){
                int counter = 0;
                while (!(line = bufferedReader.readLine()).equals("END_FILE")){

                    if (line.startsWith("<")) {
                        counter++;
                        float[] result = createArray(line, 2);
                        Polygon polygon = Window.polyArrayList.get(noOfPolygons - 1);

                        switch (counter) {
                            case 1: polygon.setScale(result);
                                    scale = result;
                                    break;
                            case 2: polygon.setRotation(result);
                                    rotation = result;
                                    break;
                            case 3: polygon.setTranslation(result);
                                    translation = result;
                                    for (int i = faceindex; i < Window.faces.size(); i++){
                                        Window.faces.get(i).setScale(scale);
                                        Window.faces.get(i).setRotation(rotation);
                                        Window.faces.get(i).setTranslation(translation);
                                        Window.faces.get(i).setComment(comment);
                                    }
                                    polygon.setComment(comment);
                                    break;
                        }
                    } else if (line.contains(".ent")){
                        //new parser for each entity file
                        counter = 0;
                        faceindex = Window.faces.size();
                        new Parser(new File(System.getProperty("user.dir"),line));

                    } else if (line.contains("COMMENT")){
                            String[] s = line.split(" ");
                            comment = s[1];
                    }
                }

            } else if (line.contains("ENTITY")) {
                vertices = new ArrayList<>();
                patches = new ArrayList<>();
                surfaces = new ArrayList<>();

                boolean vertexFlag = false, surfaceFlag = false, patchFlag = false, elementFlag = false; //flags for import
                while (!(line = bufferedReader.readLine()).equals("END_ENTITY")) {

                    //vertices
                    if (line.startsWith("<") && vertexFlag) {
                        float[] result = createArray(line, 2);
                        vertices.add(new Vertex(result[0], result[1], result[2]));
                    } else if (line.contentEquals("VERTEX")) {
                        vertexFlag = true;
                    } else if (line.contentEquals("END_VERT")) {
                        vertexFlag = false;
                        noOfPolygons++;
                        Window.polyArrayList.add(new Polygon(vertices));
                    }

                    //check and enable / disable flags
                    else if (line.contentEquals("SURFACE")) {
                        surfaceFlag = true;
                    } else if (line.contentEquals("END_SURF")){
                        surfaceFlag = false;
                    } else if (line.contentEquals("PATCH")){
                        patchFlag = true;
                    } else if (line.contentEquals("END_PATCH")){
                        patchFlag = false;
                    } else if (line.contentEquals("ELEMENT")){
                        elementFlag = true;
                    } else if (line.contentEquals("END_ELEM")) {
                        elementFlag = false;
                    }
                    //parse surface
                    else if (line.startsWith("[") && surfaceFlag){
                        surfaces.add(new Surface(createArray(line, 2), createArray(line, line.lastIndexOf("[") + 2)));
                    }
                    //parse patch
                    else if (!line.contains("COMMENT")  && patchFlag && hasInt(line, line.indexOf("{"))){
                        int surfaceIndex = startInt(line);
                        patches.add(new Patch(getVertices(line), surfaceIndex));
                    }
                    //parse element
                    else if (!line.contains("COMMENT") && elementFlag && hasInt(line, line.indexOf("{"))){
                        int patchIndex = startInt(line);
                        float[] exitance = surfaces.get(patches.get(patchIndex).getSurfaceIndex()).getExitance();
                        float[] reflectance = surfaces.get(patches.get(patchIndex).getSurfaceIndex()).getReflectance();
                        float[] emission;
                        //set emission to exitance if light, otherwise 0
                        if (equals(exitance, new float[]{0,0,0})){
                            emission = new float[]{0,0,0};
                        } else {
                            emission = exitance;
                        }
                        //set initial reflectance and exitance of elements to surface reflectance and exitance
                        Window.faces.add(new Element(getVertices(line), reflectance, exitance, emission));
                        Window.facesColorCode.add(encodeColor(Window.faces.size()-1));
                        Window.radiosities.add(new float[]{0f, 0f, 0f});
                    }
                }

                //after parse, put everything together
                for (int i = 0; i < patches.size(); i++){
                    int surfaceIndex = patches.get(i).getSurfaceIndex();
                    surfaces.get(surfaceIndex).addPatch(patches.get(i));

                }


            }

        } catch (FileNotFoundException e){
            System.out.println("File not Found");
            JOptionPane.showMessageDialog(null, "File not found", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);

        } catch (IOException e){
            System.out.println("IO Exception");
            JOptionPane.showMessageDialog(null, "IO Exception", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (Exception e){
            JOptionPane.showMessageDialog(null, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    //Method to create a float[3] array from a string
    private float[] createArray(String s, int startIndex){
        Scanner scanner = new Scanner(s.substring(startIndex));
        float[] result = new float[3];
        for (int i = 0; i < 3; i++){
            result[i] = scanner.nextFloat();
        }
        return result;
    }

    //Method to check if a string starts with an int
    private boolean hasInt(String s, int index){
        Scanner scanner = new Scanner(s.substring(0, index));
        return scanner.hasNextInt();
    }

    //Method to return starting int
    private int startInt(String s){
        Scanner scanner = new Scanner(s);
        return scanner.nextInt();
    }

    //Method to get vertices. Used in Patch and Element Parsing
    private Vertex[] getVertices(String s){

        int startIndex = s.indexOf("{");
        Vertex[] v = new Vertex[4];
        Scanner scanner = new Scanner(s.substring(startIndex+2));
        for (int i = 0; i < 4; i++){
            int nextInt = scanner.nextInt();
            v[i] = Window.polyArrayList.get(noOfPolygons - 1).getVertices().get(nextInt);
        }
        return v;
    }

    public static boolean equals(float[] a, float[] b){
        if (a.length != b.length){ return false; }
        else{
            for (int i = 0; i < a.length; i++){
                if (a[i] != b[i]){ return false; }
            }
            return true;
        }
    }

    private int[] encodeColor(int f){
        int[] code = new int[3];
        f++; //take care of 0 based indexing of faces
        code[0] = f % 256;
        code[1] = (f >> 8) % 256;
        code[2] = (f >> 16) % 256;
        return code;
    }

}
