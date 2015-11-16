/*
 * Copyright 2014 Google Inc. All Rights Reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.vrtoolkit.cardboard.samples.treasurehunt;

import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import android.content.Context;
import android.content.Intent;
import android.graphics.AvoidXfermode;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.params.MeteringRectangle;
import android.media.MediaActionSound;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
/**
 * A Cardboard sample application.
 */
public class MainActivity extends CardboardActivity implements CardboardView.StereoRenderer {

  private static final String TAG = "MainActivity";

  private static final float Z_NEAR = 0.1f;
  private static final float Z_FAR = 100.0f;

  private static float CAMERA_Y = 0.0f;
  private static float Z_info = 0.01f;
  private static float CAMERA_X = 0.01f;
  private static final float TIME_DELTA = 0.3f;
  private static float CAMERA_Z = 1.0f;
  private static final float YAW_LIMIT = 0.12f;
  private static final float PITCH_LIMIT = 0.12f;

  private static final int COORDS_PER_VERTEX = 3;

  // We keep the light always position just above the user.
  // 光源を常にユーザーの頭上に位置付ける。
  private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[] { 0.0f, 2.0f, 0.0f, 1.0f };
  private final float[] lightPosInEyeSpace = new float[4];

  private FloatBuffer floorVertices;
  private FloatBuffer floorColors;
  private FloatBuffer floorNormals;

  private FloatBuffer cubeVertices;
  private FloatBuffer cubeColors;
  private FloatBuffer cubeFoundColors;
  private FloatBuffer cubeNormals;

  private FloatBuffer RectangleVertices;
  private FloatBuffer RectangleColors;
  private FloatBuffer RectangleFoundColors;
  private FloatBuffer RectangleNormals;

  private int cubeProgram;
  private int floorProgram;

  private int rectangleProgram;

  private int cubePositionParam;
  private int cubeNormalParam;
  private int cubeColorParam;
  private int cubeModelParam;
  private int cubeModelViewParam;
  private int cubeModelViewProjectionParam;
  private int cubeLightPosParam;

  private int cubeColorParam1;

  private int rectanglePositionParam;
  private int rectangleNormalParam;
  private int rectangleColorParam;
  private int rectangleModelParam;
  private int rectangleModelViewParam;
  private int rectangleModelViewProjectionParam;
  private int rectangleLightPosParam;

  private int floorPositionParam;
  private int floorNormalParam;
  private int floorColorParam;
  private int floorModelParam;
  private int floorModelViewParam;
  private int floorModelViewProjectionParam;
  private int floorLightPosParam;

  private float[] modelCube;
  private float[] modelCube1;
  private float[] modelCube2;
  private float[] modelCube3;
  private float[] camera;
  private float[] view;
  private float[] headView;
  private float[] modelViewProjection;
  private float[] modelView;
  private float[] modelFloor;
  private float[] mAtEye;
  private float[] modelRectangle;

  private int score = 0;
  private int item;
  private float objectDistance = -10.0f;
  private float floorDepth = 20f;
  private float mAddLook = 0.0f;
  private float mAddCube = 0.0f;
  private Vibrator vibrator;
  private CardboardOverlayView overlayView;
  private int mNumVertices;
  private int mNumber;
  private boolean mFlag = false;
  private float EyePointerZ = 0.0f;
  private float EyePointerX = 0.0f;
  private float Model_X = 1.0f;
  private float Model_Z = 1.0f;
  private float MODEL_X,MODEL_Y,MODEL_Z;
  private float PositiveCatchObjectEye_X,PositiveCatchObjectEye_Z,NegativeCatchObjectEye_X,NegativeCatchObjectEye_Z;

  private RigidBody mRigidBody;
  /*
   * Converts a raw text file, saved as a resource, into an OpenGL ES shader.
   *
   * @param type The type of shader we will be creating.
   * @param resId The resource ID of the raw text file about to be turned into a shader.
   * @return The shader object handler.
   */
	//Shader定義を記述したテキストファイルを読み込む
	
  private int loadGLShader(int type, int resId) {
    String code = readRawTextFile(resId);
    int shader = GLES20.glCreateShader(type);
    GLES20.glShaderSource(shader, code);
    GLES20.glCompileShader(shader);

    // Get the compilation status.
    final int[] compileStatus = new int[1];
    GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

    // If the compilation failed, delete the shader.
    if (compileStatus[0] == 0) {
      Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
      GLES20.glDeleteShader(shader);
      shader = 0;
    }

    if (shader == 0) {
      throw new RuntimeException("Error creating shader.");
    }

    return shader;
  }

  /**
   * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
   *
   * @param label Label to report in case of error.
   */
	//OpenGLESのエラーチェック
  private static void checkGLError(String label) {
    int error;
    while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
      Log.e(TAG, label + ": glError " + error);
      throw new RuntimeException(label + ": glError " + error);
    }
  }

  /**
   * Sets the view to our CardboardView and initializes the transformation matrices we will use
   * to render our scene.
   */
	//CardboardView を設定し、描画のための各種パラメータを初期化する
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent intent = getIntent();
    mNumber = intent.getIntExtra("Number", 0);

    setContentView(R.layout.common_ui);
    CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
    cardboardView.setRestoreGLStateEnabled(false);
    cardboardView.setRenderer(this);
    setCardboardView(cardboardView);


    modelCube = new float[16];
    modelCube1 = new float[16];
    modelCube2 = new float[16];
    modelCube3 = new float[16];
    camera = new float[16];
    view = new float[16];
    modelViewProjection = new float[16];
    modelView = new float[16];
    modelFloor = new float[16];
    headView = new float[16];
    mAtEye = new float[16];

    vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


    overlayView = (CardboardOverlayView) findViewById(R.id.overlay);
    overlayView.show3DToast("Pull the magnet when you find an object.");
  }

  public void setNumber(int item){
    item = mNumber + 1;
  }
  public int getNumber(){
    return item;
  }
  @Override
  public void onRendererShutdown() {
    Log.i(TAG, "onRendererShutdown");
  }

  @Override
  public void onSurfaceChanged(int width, int height) {
    Log.i(TAG, "onSurfaceChanged");
  }

  /**
   * Creates the buffers we use to store information about the 3D world.
   *
   * <p>OpenGL doesn't use Java arrays, but rather needs data in a format it can understand.
   * Hence we use ByteBuffers.
   *
   * @param config The EGL configuration used when creating the surface.
   */
	//3D空間で利用する情報を保持するためのバッファ領域を確保する。
	// OpenGL では、Java の配列ではなく、ByteBuffer を利用する。
  @Override
  public void onSurfaceCreated(EGLConfig config) {
    Log.i(TAG, "onSurfaceCreated");
    GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text shows up well.
    ByteBuffer bbVertices = ByteBuffer.allocateDirect(WorldLayoutData.CUBE_COORDS.length * 4);
    bbVertices.order(ByteOrder.nativeOrder());
    cubeVertices = bbVertices.asFloatBuffer();
    cubeVertices.put(WorldLayoutData.CUBE_COORDS);
    cubeVertices.position(0);


    ByteBuffer bbColors = ByteBuffer.allocateDirect(WorldLayoutData.CUBE_COLORS.length * 4);
    bbColors.order(ByteOrder.nativeOrder());
    cubeColors = bbColors.asFloatBuffer();
    cubeColors.put(WorldLayoutData.CUBE_COLORS);
    cubeColors.position(0);



    ByteBuffer bbFoundColors = ByteBuffer.allocateDirect(WorldLayoutData.CUBE_FOUND_COLORS.length * 4);
    bbFoundColors.order(ByteOrder.nativeOrder());
    cubeFoundColors = bbFoundColors.asFloatBuffer();
    cubeFoundColors.put(WorldLayoutData.CUBE_FOUND_COLORS);
    cubeFoundColors.position(0);

    ByteBuffer bbNormals = ByteBuffer.allocateDirect(WorldLayoutData.CUBE_NORMALS.length * 4);
    bbNormals.order(ByteOrder.nativeOrder());
    cubeNormals = bbNormals.asFloatBuffer();
    cubeNormals.put(WorldLayoutData.CUBE_NORMALS);
    cubeNormals.position(0);
    // make a floor
  	//床
    ByteBuffer bbFloorVertices = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_COORDS.length * 4);
    bbFloorVertices.order(ByteOrder.nativeOrder());
    floorVertices = bbFloorVertices.asFloatBuffer();
    floorVertices.put(WorldLayoutData.FLOOR_COORDS);
    floorVertices.position(0);

    ByteBuffer bbFloorNormals = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_NORMALS.length * 4);
    bbFloorNormals.order(ByteOrder.nativeOrder());
    floorNormals = bbFloorNormals.asFloatBuffer();
    floorNormals.put(WorldLayoutData.FLOOR_NORMALS);
    floorNormals.position(0);

    ByteBuffer bbFloorColors = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_COLORS.length * 4);
    bbFloorColors.order(ByteOrder.nativeOrder());
    floorColors = bbFloorColors.asFloatBuffer();
    floorColors.put(WorldLayoutData.FLOOR_COLORS);
    floorColors.position(0);

    int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.light_vertex);
    int gridShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.grid_fragment);
    int passthroughShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.passthrough_fragment);

    cubeProgram = GLES20.glCreateProgram();
    GLES20.glAttachShader(cubeProgram, vertexShader);
    GLES20.glAttachShader(cubeProgram, passthroughShader);
    GLES20.glLinkProgram(cubeProgram);
    GLES20.glUseProgram(cubeProgram);

    checkGLError("Cube program");

    cubePositionParam = GLES20.glGetAttribLocation(cubeProgram, "a_Position");
    cubeNormalParam = GLES20.glGetAttribLocation(cubeProgram, "a_Normal");
    cubeColorParam = GLES20.glGetAttribLocation(cubeProgram, "a_Color");
    cubeColorParam1 = GLES20.glGetAttribLocation(cubeProgram, "a_Color");

    cubeModelParam = GLES20.glGetUniformLocation(cubeProgram, "u_Model");
    cubeModelViewParam = GLES20.glGetUniformLocation(cubeProgram, "u_MVMatrix");
    cubeModelViewProjectionParam = GLES20.glGetUniformLocation(cubeProgram, "u_MVP");
    cubeLightPosParam = GLES20.glGetUniformLocation(cubeProgram, "u_LightPos");

    GLES20.glEnableVertexAttribArray(cubePositionParam);
    GLES20.glEnableVertexAttribArray(cubeNormalParam);
    GLES20.glEnableVertexAttribArray(cubeColorParam);

    checkGLError("Cube program params");

    floorProgram = GLES20.glCreateProgram();
    GLES20.glAttachShader(floorProgram, vertexShader);
    GLES20.glAttachShader(floorProgram, gridShader);
    GLES20.glLinkProgram(floorProgram);
    GLES20.glUseProgram(floorProgram);

    checkGLError("Floor program");

    floorModelParam = GLES20.glGetUniformLocation(floorProgram, "u_Model");
    floorModelViewParam = GLES20.glGetUniformLocation(floorProgram, "u_MVMatrix");
    floorModelViewProjectionParam = GLES20.glGetUniformLocation(floorProgram, "u_MVP");
    floorLightPosParam = GLES20.glGetUniformLocation(floorProgram, "u_LightPos");

    floorPositionParam = GLES20.glGetAttribLocation(floorProgram, "a_Position");
    floorNormalParam = GLES20.glGetAttribLocation(floorProgram, "a_Normal");
    floorColorParam = GLES20.glGetAttribLocation(floorProgram, "a_Color");

    GLES20.glEnableVertexAttribArray(floorPositionParam);
    GLES20.glEnableVertexAttribArray(floorNormalParam);
    GLES20.glEnableVertexAttribArray(floorColorParam);

    checkGLError("Floor program params");

    // Object first appears directly in front of user.
    MODEL_X = (float) Math.random() * 10 + 20;
    MODEL_Z = (float) Math.random() * 10 + 20;
    Matrix.setIdentityM(modelCube, 0);
    Matrix.translateM(modelCube, 0, 0, 0, -objectDistance);

    Matrix.setIdentityM(modelFloor, 0);
    Matrix.translateM(modelFloor, 0, 0, -floorDepth, 0); // Floor appears below user.

    checkGLError("onSurfaceCreated");
  }

  /**
   * Converts a raw text file into a string.
   *
   * @param resId The resource ID of the raw text file about to be turned into a shader.
   * @return The context of the text file, or null in case of error.
   */
	//テキストファイルの内容を String に変換する。loadGLShader メソッドから呼び出される。
  private String readRawTextFile(int resId) {
    InputStream inputStream = getResources().openRawResource(resId);
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line).append("\n");
      }
      reader.close();
      return sb.toString();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
  /**
   * Prepares OpenGL ES before we draw a frame.
   *
   * @param headTransform The head transformation in the new frame.
   */
	//描画前に必ず呼び出されるメソッド。OpenGL ES の準備を行う。
  @Override
  public void onNewFrame(HeadTransform headTransform) {
    // Build the Model part of the ModelView matrix.
    //回転っぽい
    Matrix.rotateM(modelCube, 0, TIME_DELTA, 0.5f, 0.5f, 1.0f);
    /*
    Build the camera matrix and apply it to the ModelView.
    CAMERA_Zで動く。＋が後ろ向き
    ビュー変換行列を作成？
    */
    EyePointerX = headView[8] / 10;
    EyePointerZ = headView[10] / 10;
    PositiveCatchObjectEye_X = CAMERA_X + 3.0f;
    NegativeCatchObjectEye_X = CAMERA_X - 3.0f;
    PositiveCatchObjectEye_Z = CAMERA_Z + 3.0f;
    NegativeCatchObjectEye_Z = CAMERA_Z - 3.0f;

    float Wall_Z = 198.0f;
    float Wall_X = 198.0f;

    boolean isObjectInView_X = NegativeCatchObjectEye_X < modelCube[12] && modelCube[12] < PositiveCatchObjectEye_X;
    boolean PositiveObjectInView_Z = CAMERA_Z < modelCube[14] && modelCube[14] < PositiveCatchObjectEye_Z;
    boolean NegativeObjectInView_Z = NegativeCatchObjectEye_Z < modelCube[14] && modelCube[14] < CAMERA_Z;
    boolean PositiveWall_Z = CAMERA_Z < Wall_Z && Wall_Z < PositiveCatchObjectEye_Z;
    boolean NegativeWall_Z = NegativeCatchObjectEye_Z < -Wall_Z && -Wall_Z < CAMERA_Z;
    boolean PositiveWall_X =  NegativeCatchObjectEye_X < Wall_X  && Wall_X < PositiveCatchObjectEye_X;
    boolean NegativeWall_X =  NegativeCatchObjectEye_X < Wall_X  && Wall_X < PositiveCatchObjectEye_X;
    boolean culcZ = (EyePointerZ > 0) ? PositiveObjectInView_Z : NegativeObjectInView_Z;
    boolean BumpWall = (EyePointerZ > 0) ? PositiveWall_Z : NegativeWall_Z;

    if (culcZ && isObjectInView_X){
      CAMERA_Z = Z_info;
      CAMERA_X = CAMERA_X;
     }
    else if(BumpWall && ){

    }
    else {
      CAMERA_Y = -floorDepth + 5.0f;
      CAMERA_X = CAMERA_X - EyePointerX;
      CAMERA_Z = Z_info + EyePointerZ;
      Z_info = Z_info + EyePointerZ;
      }

    /*if (CAMERA_ZZ < -199.0f) {
      CAMERA_ZZ = -199.0f;
      Matrix.setLookAtM(camera, 0, CAMERA_X, CAMERA_Y, CAMERA_Z, 0.0f, 0.0f, 200.0f, 0.0f, 1.0f, 0.0f);
    }*/
    
    Matrix.setLookAtM(camera, 0, CAMERA_X, CAMERA_Y , CAMERA_Z, 0.0f, 0.0f, 200.0f, 0.0f, 1.0f, 0.0f);


    headTransform.getHeadView(headView, 0);

    checkGLError("onReadyToDraw");
  }

  /**
   * Draws a frame for an eye.
   *
   * @param eye The eye to render. Includes all required transformations.
   */
	//右目・左目の各視点用のフレームを描画する
	
  @Override
  public void onDrawEye(Eye eye) {
    GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    checkGLError("colorParam");


    // Apply the eye transformation to the camera.
  	//ユーザが見ている方向にあわせて視点を移動する
    Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

    // Set the position of the light
    Matrix.multiplyMV(lightPosInEyeSpace, 0, view, 0, LIGHT_POS_IN_WORLD_SPACE, 0);
    // Build the ModelView and ModelViewProjection matrices
    // for calculating cube position and light.
  	//キューブの位置や光を計算するためのModelViewとModelViewProjection行列を構築します。



    DrawHuman(eye,MODEL_X,MODEL_Z);
    System.out.println("X1="+modelCube[0]+"Y="+ modelCube[1]+"Z="+ modelCube[2]+"0="+modelCube[3]);
    System.out.println("X2="+modelCube[4]+"Y="+ modelCube[5]+"Z="+ modelCube[6]+"0="+modelCube[7]);
    System.out.println("X3="+modelCube[8]+"Y="+ modelCube[9]+"Z="+ modelCube[10]+"0="+modelCube[11]);
    System.out.println("X4="+modelCube[12]+"Y="+ modelCube[13]+"Z="+ modelCube[14]+"0="+modelCube[15]);

    // Set modelView for the floor, so we draw floor in the correct location
  	//正しい場所にモデルビューをセット！
  }

  @Override
  public void onFinishFrame(Viewport viewport) {

  }


    
    public void DrawHuman(Eye eye, float MODEL_X, float MODEL_Z){
    float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
    //車体
    Matrix.setIdentityM(modelCube, 0);
    MODEL_Y = -floorDepth;
    Matrix.translateM(modelCube, 0, MODEL_X, MODEL_Y, -MODEL_Z);
    Matrix.scaleM(modelCube, 0, 1.5f, 0.5f, 1.5f);
    Matrix.multiplyMM(modelView, 0, view, 0, modelCube, 0);
    Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
    drawCube();

    MODEL_Y = MODEL_Y + 0.5f;

    //胴体
    Matrix.setIdentityM(modelCube1, 0);
    Matrix.translateM(modelCube1, 0, MODEL_X, MODEL_Y, -MODEL_Z);
    Matrix.scaleM(modelCube1, 0, 0.75f, 1.0f, 0.75f);
    Matrix.multiplyMM(modelView, 0, view, 0, modelCube1, 0);
    Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
    drawCube1();
        

    MODEL_Y = MODEL_Y + 1.0f;
    //頭
    Matrix.setIdentityM(modelCube2, 0);
    Matrix.translateM(modelCube2, 0, MODEL_X, MODEL_Y, -MODEL_Z);
    Matrix.scaleM(modelCube2, 0, 0.1f, 0.1f, 0.1f);
    Matrix.multiplyMM(modelView, 0, view, 0, modelCube2, 0);
    Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
    drawCube2();
        

    MODEL_Y = MODEL_Y + 0.1f;
    Matrix.setIdentityM(modelCube3, 0);
    Matrix.translateM(modelCube3, 0, MODEL_X, MODEL_Y, -MODEL_Z);
    Matrix.scaleM(modelCube3, 0, 0.5f, 1.0f, 0.5f);
    Matrix.multiplyMM(modelView, 0, view, 0, modelCube3, 0);
    Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
    drawCube3();
    Matrix.multiplyMM(modelView, 0, view, 0, modelFloor, 0);
    Matrix.multiplyMM(modelViewProjection, 0, perspective, 0,
              modelView, 0);
    drawFloor();
    }
  /**
   * Draw the cube.
   *
   * <p>We've set all of our transformation matrices. Now we simply pass them into the shader.
   */
  public void drawCube() {

    GLES20.glUseProgram(cubeProgram);

    GLES20.glUniform3fv(cubeLightPosParam, 1, lightPosInEyeSpace, 0);

    // Set the Model in the shader, used to calculate lighting
    GLES20.glUniformMatrix4fv(cubeModelParam, 1, false, modelCube, 0);

    // Set the ModelView in the shader, used to calculate lighting
    GLES20.glUniformMatrix4fv(cubeModelViewParam, 1, false, modelView, 0);

    // Set the position of the cube
    GLES20.glVertexAttribPointer(cubePositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
            false, 0, cubeVertices);

    // Set the ModelViewProjection matrix in the shader.
    GLES20.glUniformMatrix4fv(cubeModelViewProjectionParam, 1, false, modelViewProjection, 0);

    Transform transform = new Transform();
    transform.setIdentity();

    DefaultMotionState motionState = new DefaultMotionState(transform);

    createRigidBody(motionState);
    // Set the normal positions of the cube, again for shading
    //シェーダってやつ？
    GLES20.glVertexAttribPointer(cubeNormalParam, 3, GLES20.GL_FLOAT, false, 0, cubeNormals);
    GLES20.glVertexAttribPointer(cubeColorParam, 4, GLES20.GL_FLOAT, false, 0,
            isTouchingAtObject() ? cubeFoundColors : cubeColors);

    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
    checkGLError("Drawing cube");

  }
  public void drawCube3() {

    GLES20.glUseProgram(cubeProgram);

    GLES20.glUniform3fv(cubeLightPosParam, 1, lightPosInEyeSpace, 0);

    // Set the Model in the shader, used to calculate lighting
    GLES20.glUniformMatrix4fv(cubeModelParam, 1, false, modelCube3, 0);

    // Set the ModelView in the shader, used to calculate lighting
    GLES20.glUniformMatrix4fv(cubeModelViewParam, 1, false, modelView, 0);

    // Set the position of the cube
    GLES20.glVertexAttribPointer(cubePositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
            false, 0, cubeVertices);

    // Set the ModelViewProjection matrix in the shader.
    GLES20.glUniformMatrix4fv(cubeModelViewProjectionParam, 1, false, modelViewProjection, 0);

    Transform transform = new Transform();
    transform.setIdentity();

    DefaultMotionState motionState = new DefaultMotionState(transform);

    createRigidBody(motionState);
    // Set the normal positions of the cube, again for shading
    //シェーダってやつ？
    GLES20.glVertexAttribPointer(cubeNormalParam, 3, GLES20.GL_FLOAT, false, 0, cubeNormals);
    GLES20.glVertexAttribPointer(cubeColorParam, 4, GLES20.GL_FLOAT, false, 0,
            isTouchingAtObject() ? cubeFoundColors : cubeColors);

    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
    checkGLError("Drawing cube");

  }
  public void drawCube1() {

    GLES20.glUseProgram(cubeProgram);

    GLES20.glUniform3fv(cubeLightPosParam, 1, lightPosInEyeSpace, 0);

    // Set the Model in the shader, used to calculate lighting
    GLES20.glUniformMatrix4fv(cubeModelParam, 1, false, modelCube1, 0);

    // Set the ModelView in the shader, used to calculate lighting
    GLES20.glUniformMatrix4fv(cubeModelViewParam, 1, false, modelView, 0);

    // Set the position of the cube
    GLES20.glVertexAttribPointer(cubePositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
            false, 0, cubeVertices);

    // Set the ModelViewProjection matrix in the shader.
    GLES20.glUniformMatrix4fv(cubeModelViewProjectionParam, 1, false, modelViewProjection, 0);

    Transform transform = new Transform();
    transform.setIdentity();

    DefaultMotionState motionState = new DefaultMotionState(transform);

    createRigidBody(motionState);
    // Set the normal positions of the cube, again for shading
    //シェーダってやつ？
    GLES20.glVertexAttribPointer(cubeNormalParam, 3, GLES20.GL_FLOAT, false, 0, cubeNormals);
    //色変化のポインタ？
    GLES20.glVertexAttribPointer(cubeColorParam1, 4, GLES20.GL_FLOAT, false, 0,
            isTouchingAtObject2() ? cubeFoundColors : cubeColors);

    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
    checkGLError("Drawing cube");

  }
  public void drawCube2() {

    GLES20.glUseProgram(cubeProgram);

    GLES20.glUniform3fv(cubeLightPosParam, 1, lightPosInEyeSpace, 0);

    // Set the Model in the shader, used to calculate lighting
    GLES20.glUniformMatrix4fv(cubeModelParam, 1, false, modelCube2, 0);

    // Set the ModelView in the shader, used to calculate lighting
    GLES20.glUniformMatrix4fv(cubeModelViewParam, 1, false, modelView, 0);

    // Set the position of the cube
    GLES20.glVertexAttribPointer(cubePositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
            false, 0, cubeVertices);

    // Set the ModelViewProjection matrix in the shader.
    GLES20.glUniformMatrix4fv(cubeModelViewProjectionParam, 1, false, modelViewProjection, 0);

    Transform transform = new Transform();
    transform.setIdentity();

    DefaultMotionState motionState = new DefaultMotionState(transform);

    createRigidBody(motionState);
    // Set the normal positions of the cube, again for shading
    //シェーダってやつ？
    GLES20.glVertexAttribPointer(cubeNormalParam, 3, GLES20.GL_FLOAT, false, 0, cubeNormals);
    //色変化のポインタ？
    GLES20.glVertexAttribPointer(cubeColorParam1, 4, GLES20.GL_FLOAT, false, 0,
            isTouchingAtObject2() ? cubeFoundColors : cubeColors);

    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
    checkGLError("Drawing cube");

  }
  private void createRigidBody(DefaultMotionState motionState){
    CollisionShape shape = new BoxShape(new Vector3f(1.0f, 1.0f, 1.0f));
    RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(10.0f, motionState, shape, new Vector3f(10.0f, 10.0f,10.0f));
    mRigidBody = new RigidBody(rbInfo);

  }
  private void initTextures(int program) {
    //画像ファイルを読み込む
    Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.blue_sky);

    int[] textures = new int[1];
    GLES20.glGenTextures(1, textures, 0);

    int u_Sampler = GLES20.glGetUniformLocation(program, "u_Sampler");

    GLES20.glActiveTexture(GLES20.GL_TEXTURE);

    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);

    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, image, 0);

    GLES20.glUniform1f(u_Sampler, 0);
  }
 /**
   * Draw the floor.
   *三角形に床を描画していく。
   * <p>This feeds in data for the floor into the shader. Note that this doesn't feed in data about
   * position of the light, so if we rewrite our code to draw the floor first, the lighting might
   * look strange.
   */
  public void drawFloor() {
    GLES20.glUseProgram(floorProgram);

    // Set ModelView, MVP, position, normals, and color.
    GLES20.glUniform3fv(floorLightPosParam, 1, lightPosInEyeSpace, 0);
    GLES20.glUniformMatrix4fv(floorModelParam, 1, false, modelFloor, 0);
    GLES20.glUniformMatrix4fv(floorModelViewParam, 1, false, modelView, 0);
    GLES20.glUniformMatrix4fv(floorModelViewProjectionParam, 1, false,
            modelViewProjection, 0);
    GLES20.glVertexAttribPointer(floorPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
            false, 0, floorVertices);
    GLES20.glVertexAttribPointer(floorNormalParam, 3, GLES20.GL_FLOAT, false, 0,
            floorNormals);
    GLES20.glVertexAttribPointer(floorColorParam, 4, GLES20.GL_FLOAT, false, 0, floorColors);

    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

    checkGLError("drawing floor");
  }

  /**
   * Called when the Cardboard trigger is pulled.
   */
  @Override
  public void onCardboardTrigger() {
    Log.i(TAG, "onCardboardTrigger");

    if (isTouchingAtObject()) {
      score++;
    } else {
    }
  }

  /**
   * Find a new random position for the object.
   *
   * <p>We'll rotate it around the Y-axis so it's out of sight, and then up or down by a little bit.
   */
	//立方体を再配置する
  private void hideObject() {
    float[] rotationMatrix = new float[16];
    float[] posVec = new float[4];

    // First rotate in XZ plane, between 90 and 270 deg away, and scale so that we vary
    // the object's distance from the user.
  	//見つけた後同じ視界内には出現しないようにする。
  	//横90°縦270°の範囲から飛ばす
  	
    float angleXZ = (float) Math.random() * 180 + 90;
    Matrix.setRotateM(rotationMatrix, 0, angleXZ, 0f, 1f, 0f);
    float oldObjectDistance = objectDistance;
    objectDistance = (float) Math.random() * 15 + 5;
    float objectScalingFactor = objectDistance / oldObjectDistance;
    Matrix.scaleM(rotationMatrix, 0, objectScalingFactor, objectScalingFactor,
        objectScalingFactor);
    Matrix.multiplyMV(posVec, 0, rotationMatrix, 0, modelCube, 12);

    // Now get the up or down angle, between -20 and 20 degrees.
    float angleY = (float) Math.random() * 80 - 40; // Angle in Y plane, between -40 and 40.
    angleY = (float) Math.toRadians(angleY);
    float newY = (float) Math.tan(angleY) * objectDistance;

    Matrix.setIdentityM(modelCube, 0);
    Matrix.translateM(modelCube, 0, posVec[0], newY, posVec[2]);
  }
  private void RamdamXZ() {
    float[] rotationMatrix = new float[16];
    float[] posVec = new float[4];

    // First rotate in XZ plane, between 90 and 270 deg away, and scale so that we vary
    // the object's distance from the user.
    //見つけた後同じ視界内には出現しないようにする。
    //横90°縦270°の範囲から飛ばす

    float angleXZ = (float) Math.random() * 180 + 90;
    Matrix.setRotateM(rotationMatrix, 0, angleXZ, 0f, 1f, 0f);
    float oldObjectDistance = objectDistance;
    objectDistance = (float) Math.random() * 15 + 5;
    float objectScalingFactor = objectDistance / oldObjectDistance;
    Matrix.scaleM(rotationMatrix, 0, objectScalingFactor, objectScalingFactor,
            objectScalingFactor);
    Matrix.multiplyMV(posVec, 0, rotationMatrix, 0, modelCube, 12);

    // Now get the up or down angle, between -20 and 20 degrees.
    float angleY = (float) Math.random() * 80 - 40; // Angle in Y plane, between -40 and 40.
    angleY = (float) Math.toRadians(angleY);
    float newY = (float) Math.tan(angleY) * objectDistance;

  }
  /**
   * Check if user is looking at object by calculating where the object is in eye-space.
   *
   * @return true if the user is looking at the object.
   */
	//ユーザの視点に立方体が入っているかどうかをチェックする
  private boolean isTouchingAtObject() {

    float[] initVec = { 0, 0, 0, 1.0f };
    float[] objPositionVec = new float[4];
    boolean CheckTouchX,CheckTouchZ,CheckTouch;
    CheckTouchX = CAMERA_X-3.0f < modelCube[12] && modelCube[12]  < CAMERA_X+3.0f;
    CheckTouchZ = CAMERA_ZZ-3.0f < modelCube[14] && modelCube[14]  < CAMERA_ZZ+3.0f;
    CheckTouch = CheckTouchX&&CheckTouchZ;
    return CheckTouch;
  }

  /**
   * Check if user is looking at object by calculating where the object is in eye-space.
   *
   * @return true if the user is looking at the object.
   */
  //ユーザの視点に立方体が入っているかどうかをチェックする
  private boolean isTouchingAtObject2() {

    float[] initVec = { 0, 0, 0, 1.0f };
    float[] objPositionVec = new float[4];
    boolean CheckTouchX,CheckTouchZ,CheckTouch;
    CheckTouchX = CAMERA_X-3.0f < modelCube1[12] && modelCube1[12]  < CAMERA_X+3.0f;
    CheckTouchZ = CAMERA_ZZ-3.0f < modelCube1[14] && modelCube1[14]  < CAMERA_ZZ+3.0f;
    CheckTouch = CheckTouchX&&CheckTouchZ;
    return CheckTouch;
  }
}
