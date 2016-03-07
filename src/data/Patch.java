package data;

import java.util.ArrayList;

/**
 * Created by derek on 08/11/2015.
 */
public class Patch {

    ArrayList<Element> elements = new ArrayList<>();
    Vertex[] vertices = new Vertex[4]; //4 vertices max for each patch
    int surfaceIndex;

    public Patch(Vertex[] vertices, int index){
        this.vertices = vertices;
        surfaceIndex = index;
    }

    public void addElement(Element e){
        elements.add(e);
    }

    public Vertex[] getVertices(){
        return vertices;
    }

    public ArrayList<Element> getElements(){
        return elements;
    }

    public int getSurfaceIndex(){
        return surfaceIndex;
    }

}
