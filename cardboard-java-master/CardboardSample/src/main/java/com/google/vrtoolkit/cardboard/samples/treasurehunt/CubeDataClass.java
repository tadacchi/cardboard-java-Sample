package com.google.vrtoolkit.cardboard.samples.treasurehunt;

/**
 * Created by 2015295 on 2015/11/11.
 */
public class CubeDataClass {
    private float Object_X;
    private float Object_Y;
    private float Object_Z;


    public CubeDataClass(float mObject_X, float mObject_Y, float mObject_Z){
        Object_X = mObject_X;
        Object_Y = mObject_Y;
        Object_Z = mObject_Z;
    }
    public void setObject_X(float mObject_X){
        Object_X = mObject_X;
    }

    public void setObject_Y(float mObject_Y){
        Object_Y = mObject_Y;
    }

    public void setObject_Z(float mObject_Z){
        Object_Z = mObject_Z;
    }

    public float getObject_X(){
        return Object_X;
    }

    public float getObject_Y(){
        return Object_Y;
    }

    public float getObject_Z(){
        return Object_Z;
    }

    public void setCubeData(float mCubeData_X , float mCubeData_Z){

    }
}

