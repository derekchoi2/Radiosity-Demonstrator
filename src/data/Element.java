package data;

/**
 * Created by derek on 08/11/2015.
 */
public class Element {

    private Vertex[] vertices = new Vertex[4]; //max 4 vertices per Element
    private Vertex[] world = new Vertex[4];
    private float[] reflectance;
    private float[] exitance;
    private float[] emission;
    private float[] normal = new float[3];
    private float[] translation;
    private float[] rotation;
    private float[] scale;
    private String comment;

    public Element(Vertex[] vertices, float[] reflectance, float[] exitance, float[] emission){

        this.vertices = vertices;
        this.reflectance = reflectance;
        this.exitance = exitance;
        this.emission = emission;

        //calculate surface normal
        //https://www.opengl.org/wiki/Calculating_a_Surface_Normal
        for(int i = 0; i < vertices.length; i++){
            Vertex current = vertices[i];
            Vertex next = vertices[(i+1) % vertices.length];

            normal[0] += ((current.getY() - next.getY()) * (current.getZ() + next.getZ()));
            normal[1] += ((current.getZ() - next.getZ()) * (current.getX() + next.getX()));
            normal[2] += ((current.getX() - next.getX()) * (current.getY() + next.getY()));

        }
        double magnitude = Math.sqrt((double)(normal[0] * normal[0] + normal[1] * normal[1] + normal[2] * normal[2]));
        normal[0] /= magnitude;
        normal[1] /= magnitude;
        normal[2] /= magnitude;
    }

    public Vertex[] getVertices(){
        return vertices;
    }

    public Vertex[] getWorld() { return world; }

    public float[] getReflectance() { return reflectance; }

    public float[] getExitance() { return exitance; }

    public void setExitance(float[] exitance) { this.exitance = exitance; }

    public float[] getEmission() { return emission; }

    public float[] getNormal() { return normal; }

    public float[] getTranslation() { return translation; }

    public void setTranslation(float[] translation) { this.translation = translation; }

    public float[] getRotation() { return rotation; }

    public void setRotation(float[] rotation) { this.rotation = rotation; }

    public float[] getScale() { return scale; }

    public void setScale(float[] scale) { this.scale = scale; }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }

    public void calculateWorldCoordinates(){
        //scale -> rotate -> translate
        float[][] finalTransformation;
        //scale
        finalTransformation = new float[][]{{scale[0], 0, 0, 0}, {0, scale[1], 0, 0}, {0, 0, scale[2], 0}, {0, 0, 0, 1}};
        //rotate x
        double angle = rotation[0] * (Math.PI / 180);
        float[][] rotatex = new float[][]{{1, 0, 0, 0}, {0, (float) Math.cos(angle), (float) -Math.sin(angle), 0}, {0, (float) Math.sin(angle), (float) Math.cos(angle), 0}, {0, 0, 0, 1}};
        finalTransformation = matrixMultiply(finalTransformation, rotatex);
        //rotate y
        angle = rotation[1] * (Math.PI/180);
        float[][] rotatey = new float[][]{{(float) Math.cos(angle), 0, (float) Math.sin(angle), 0}, {0, 1, 0, 0}, {(float) -Math.sin(angle), 0, (float) Math.cos(angle), 0}, {0, 0, 0, 1}};
        finalTransformation = matrixMultiply(finalTransformation, rotatey);
        //rotate z
        angle = rotation[2] * (Math.PI/180);
        float[][] rotatez = new float[][]{{(float) Math.cos(angle), (float) -Math.sin(angle), 0, 0}, {(float) Math.sin(angle), (float) Math.cos(angle), 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}};
        finalTransformation = matrixMultiply(finalTransformation, rotatez);
        //translate matrix
        float[][] translate = new float[][]{{1, 0, 0, translation[0]}, {0, 1, 0, translation[1]}, {0, 0, 1, translation[2]}, {0, 0, 0, 1}};
        finalTransformation = matrixMultiply(finalTransformation, translate);

        for (int i = 0; i < vertices.length; i++) {
            Vertex v = vertices[i];
            float[][] transformedVertices = matrixMultiply(finalTransformation, new float[][]{{v.getX()}, {v.getY()}, {v.getZ()}, {1}});
            //System.out.println(transformedVertices[0][0] + " " + transformedVertices[1][0] + " " + transformedVertices[2][0]);
            world[i] = new Vertex(transformedVertices[0][0], transformedVertices[1][0], transformedVertices[2][0]);
        }

    }

    public float[][] matrixMultiply(float[][] a, float[][] b){
        float[][] result = new float[a.length][b[0].length];
        for (int i = 0; i < a.length; i++){
            for (int j = 0; j < b[0].length; j++){
                for (int k = 0; k < a[0].length; k++){
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return result;

    }

}
