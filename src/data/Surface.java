package data;

import java.util.ArrayList;

/**
 * Created by derek on 06/11/2015.
 */
public class Surface {

    ArrayList<Patch> patches = new ArrayList<>();
    float[] reflectance = new float[3];
    float[] exitance = new float[3];


    public Surface(float[] reflectance, float[] exitance){
        this.reflectance = reflectance;
        this.exitance = exitance;
    }

    public float[] getReflectance(){
        return reflectance;
    }

    public float[] getExitance(){
        return exitance;
    }

    public void addPatch(Patch p){
        patches.add(p);
    }

    public ArrayList<Patch> getPatches(){
        return patches;
    }

}
