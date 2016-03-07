package data;

import java.util.ArrayList;

/**
 * Created by derek on 05/11/2015.
 */
public class Polygon {

    ArrayList<Vertex> Vertices;
    float[] scale = new float[3];
    float[] rotation = new float[3];
    float[] translation = new float[3];
    ArrayList<Surface> Surfaces;
    String comment;

    public Polygon(ArrayList<Vertex> vertices){
        //stores all vertices of a polygon
        setVertices(vertices);
    }






    public ArrayList<Vertex> getVertices(){
        return Vertices;
    }

    public void setVertices(ArrayList<Vertex> vertices){
        this.Vertices = vertices;
    }

    public void setScale(float[] scale){
        this.scale = scale;
    }

    public void setRotation(float[] rotation){
        this.rotation = rotation;
    }

    public void setTranslation(float[] translation){
        this.translation = translation;
    }

    public void setComment(String s){ comment = s; }

    public String getComment() { return comment; }

    public float[] getScale(){
        return scale;
    }

    public float[] getRotation(){
        return rotation;
    }

    public float[] getTranslation(){
        return translation;
    }

    public void setSurfaces(ArrayList<Surface> s){
        Surfaces = s;
    }

    public ArrayList<Surface> getSurfaces(){
        return Surfaces;
    }

}
