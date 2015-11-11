package com.google.vrtoolkit.cardboard.samples.treasurehunt;


import android.opengl.Matrix;

/**
 * Created by 2015295 on 2015/11/11.
 */
public class DrawCubeClass {
    private float Object_X = 0.0f;
    private float Object_Y = 0.0f;
    private float Object_Z = 0.0f;
    private int item = 2;

    private float[] ModelCube;
    private float[][] ModelCubeInfo;
    public DrawCubeClass() {
        CubeDataClass[] DrawCube = new CubeDataClass[item];

        for (int i = 0; i < item; i++) {
            DrawCube[i] = new CubeDataClass(Object_X, Object_Y, Object_Z);
        }

        for (int i = 0; i < item; i++) {
            DrawCube[i].setObject_X((float) Math.random() * 10 + 90);
        }
        for (int i = 0; i < item; i++) {
            DrawCube[i].setObject_Z((float) Math.random() * 10 + 90);
        }
        ModelCube = new float[16];
        ModelCubeInfo = new float [item][16];
        for (int i = 0; i < item; i++) {
            for (int j=0;j<16;j++) {
                Matrix.setIdentityM(ModelCube, 0);
                Matrix.translateM(ModelCube, 0, DrawCube[i].getObject_X(), 0, DrawCube[i].getObject_Z());
                ModelCubeInfo[i][j] = ModelCube[j];
            }
        }
    }

    public float[][] getModelCubeInfo(){
        return ModelCubeInfo;
    }
}
