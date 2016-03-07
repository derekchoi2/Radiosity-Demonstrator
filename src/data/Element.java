package data;

/**
 * Created by derek on 08/11/2015.
 */
public class Element {

    Vertex[] vertices = new Vertex[4]; //max 4 vertices per Element
    float[] reflectance = new float[3];
    float[] exitance;
    float[] emission;

    public Element(Vertex[] vertices, float[] reflectance, float[] exitance, float[] emission){

        this.vertices = vertices;
        this.reflectance = reflectance;
        this.exitance = exitance;
        this.emission = emission;

    }

    public Vertex[] getVertices(){
        return vertices;
    }

    public float[] getReflectance() { return reflectance; }

    public void setReflectance(float[] reflectance) { this.reflectance = reflectance; }

    public float[] getExitance() { return exitance; }

    public void setExitance(float[] exitance) { this.exitance = exitance; }

    public float[] getEmission() { return emission; }

    public void setEmission(float[] emission) { this.emission = emission; }

}
