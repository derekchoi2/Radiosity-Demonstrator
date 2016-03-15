package data;

/**
 * Created by derek on 08/11/2015.
 */
public class Element {

    Vertex[] vertices = new Vertex[4]; //max 4 vertices per Element
    float[] reflectance = new float[3];
    float[] exitance;
    float[] emission;
    float[] incident;
    float[] normal = new float[3];
    float[] translation;
    float[] rotation;
    float[] scale;
    String comment;

    public Element(Vertex[] vertices, float[] reflectance, float[] exitance, float[] emission, float[] incident){

        this.vertices = vertices;
        this.reflectance = reflectance;
        this.exitance = exitance;
        this.emission = emission;
        this.incident = incident;

        //calculate surface normal
        //https://www.opengl.org/wiki/Calculating_a_Surface_Normal
        Vertex u = vertices[1].minus(vertices[0]);
        Vertex v = vertices[2].minus(vertices[0]);
        normal[0] = (u.getY() * v.getZ()) - (u.getZ() * v.getY());
        normal[1] = (u.getZ() * v.getX()) - (u.getX() * v.getZ());
        normal[2] = (u.getX() * v.getY()) - (u.getY() * v.getX());

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

    public float[] getIncident() { return incident; }

    public void setIncident(float[] incident) { this.incident = incident; }

    public float[] getNormal() { return normal; }

    public void setNormal(float[] normal) { this.normal = normal; }

    public float[] getTranslation() { return translation; }

    public void setTranslation(float[] translation) { this.translation = translation; }

    public float[] getRotation() { return rotation; }

    public void setRotation(float[] rotation) { this.rotation = rotation; }

    public float[] getScale() { return scale; }

    public void setScale(float[] scale) { this.scale = scale; }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }
}
