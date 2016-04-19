package data;

/**
 * Created by derek on 05/11/2015.
 */
public class Vertex {

    Float x;
    Float y;
    Float z;

    public Vertex(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX(){
        return x;
    }

    public float getY(){
        return y;
    }

    public float getZ(){
        return z;
    }

    public String toString(){
        return x + " " + y + " " + z;
    }
}
