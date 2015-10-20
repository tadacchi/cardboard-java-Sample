package com.google.vrtoolkit.cardboard.samples.treasurehunt;

/**
 * Created by 2015295 on 2015/10/20.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

import javax.microedition.khronos.opengles.GL10;

public class Texture {
    int mTextureNo;

    public int returnTex(GL10 gl, Context context){
        float[] lightAmbient = new float[] { 0.2f, 0.2f, 0.2f, 1.0f };
        float[] lightDiffuse = new float[] { 1, 1, 1, 1 };
        float[] lightPos = new float[] { 1, 1, 1, 1 };

        float[] matAmbient = new float[] { 1f, 1f, 1f, 1.0f };
        float[] matDiffuse = new float[] { 1f, 1f, 1f, 1.0f };

        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.wood_box);
        int[] textureNo = new int[1];

        gl.glGenTextures(1, textureNo, 0);
        mTextureNo = textureNo[0];

        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureNo);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bmp, 0);

        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);

        gl.glEnable(GL10.GL_LIGHTING);
        gl.glEnable(GL10.GL_LIGHT0);

        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbient, 0);
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuse, 0);
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPos, 0);

        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, matAmbient, 0);
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, matDiffuse, 0);

        return mTextureNo;
    }


}
