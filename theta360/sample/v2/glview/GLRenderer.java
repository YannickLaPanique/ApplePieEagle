package com.theta360.sample.v2.glview;

import android.graphics.Bitmap;
import android.opengl.GLES31;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.theta360.sample.v2.glview.model.UVSphere;
import com.theta360.sample.v2.model.Constants;
import com.theta360.sample.v2.model.Photo;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * Renderer class for photo display
 */
public class GLRenderer implements Renderer {

    private final String VSHADER_SRC =
            "attribute vec4 aPosition;\n" +
            "attribute vec2 aUV;\n" +
            "uniform mat4 uProjection;\n" +
            "uniform mat4 uView;\n" +
            "uniform mat4 uModel;\n" +
            "varying vec2 vUV;\n" +
            "void main() {\n" +
            "  gl_Position = uProjection * uView * uModel * aPosition;\n" +
            "  vUV = aUV;\n" +
            "}\n";

    private final String FSHADER_SRC =
            "precision mediump float;\n" +
            "varying vec2 vUV;\n" +
            "uniform sampler2D uTex;\n" +
            "void main() {\n" +
            "  gl_FragColor = texture2D(uTex, vUV);\n" +
            "}\n";


    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;

    private UVSphere mEastShell;
    private UVSphere mWestShell;

    private double mRotationAngleY;
    private double mRotationAngleXZ;

    private Photo mTexture;
    private boolean mTextureUpdate = false;

    private float mScreenAspectRatio;

    private float mCameraPosX = 0.0f;
    private float mCameraPosY = 0.0f;
    private float mCameraPosZ = 0.0f;

    private float mCameraDirectionX = 0.0f;
    private float mCameraDirectionY = 0.0f;
    private float mCameraDirectionZ = 1.0f;

    private float mCameraFovDegree = 100;

    private int[] mTextures = new int[2];

    private int mPositionHandle;
    private int mProjectionMatrixHandle;
    private int mViewMatrixHandle;
    private int mUVHandle;
    private int mTexHandle;
    private int mModelMatrixHandle;

    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mModelMatrix = new float[16];

    private float width, height;

    /**
     * Constructor
     */
    GLRenderer() {
        mEastShell = new UVSphere(Constants.TEXTURE_SHELL_RADIUS, Constants.SHELL_DIVIDES, true);
        mWestShell = new UVSphere(Constants.TEXTURE_SHELL_RADIUS, Constants.SHELL_DIVIDES, false);

        mRotationAngleY = 0.0f;
        mRotationAngleXZ = 0.0f;
    }


    /**
     * onDrawFrame Method
     * @param gl GLObject (not used)
     */
    @Override
    public void onDrawFrame(final GL10 gl) {

        if (mTexture == null) return;

        mCameraDirectionX = (float) (Math.cos(mRotationAngleXZ)*Math.cos(mRotationAngleY));
        mCameraDirectionZ = (float) (Math.sin(mRotationAngleXZ)*Math.cos(mRotationAngleY));
        mCameraDirectionY = (float) Math.sin(mRotationAngleY);

        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT | GLES31.GL_DEPTH_BUFFER_BIT);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mProjectionMatrix, 0);

        if (mTextureUpdate && null != mTexture && !mTexture.getPhoto().isRecycled()) {
            Log.d("", "load texture1");
            loadTexture(mTexture.getPhoto());
            mTextureUpdate = false;
        }

        Matrix.setLookAtM(mViewMatrix, 0, mCameraPosX, mCameraPosY, mCameraPosZ, mCameraDirectionX, mCameraDirectionY, mCameraDirectionZ, 0.0f, 1.0f, 0.0f);
        Matrix.perspectiveM(mProjectionMatrix, 0, mCameraFovDegree, mScreenAspectRatio, Z_NEAR, Z_FAR);

        if (null != mTexture.getElevetionAngle()) {
            float elevationAngle = mTexture.getElevetionAngle().floatValue();
            Matrix.rotateM(mModelMatrix, 0, elevationAngle, 0, 0, 1);
        }
        if (null != mTexture.getHorizontalAngle()) {
            float horizontalAngle = mTexture.getHorizontalAngle().floatValue();
            Matrix.rotateM(mModelMatrix, 0, horizontalAngle, 1, 0, 0);
        }

        GLES31.glUniformMatrix4fv(mModelMatrixHandle, 1, false, mModelMatrix, 0);
        GLES31.glUniformMatrix4fv(mProjectionMatrixHandle, 1, false, mProjectionMatrix, 0);
        GLES31.glUniformMatrix4fv(mViewMatrixHandle, 1, false, mViewMatrix, 0);

        GLES31.glActiveTexture(GLES31.GL_TEXTURE0);

        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, mTextures[0]);
        GLES31.glUniform1i(mTexHandle, 0);

        mEastShell.draw(mPositionHandle, mUVHandle);

        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, mTextures[1]);
        GLES31.glUniform1i(mTexHandle, 0);

        mWestShell.draw(mPositionHandle, mUVHandle);

    }

    /**
     * onSurfaceChanged Method
     * @param gl GLObject (not used)
     * @param width Screen width
     * @param height Screen height
     */
    @Override
    public void onSurfaceChanged(final GL10 gl, final int width, final int height) {

        this.width = width;
        this.height = height;

        mScreenAspectRatio = (float) width / (float) (height == 0 ? 1 : height);

        GLES31.glViewport(0, 0, width, height);

        Matrix.setLookAtM(mViewMatrix, 0, mCameraPosX, mCameraPosY, mCameraPosZ, mCameraDirectionX, mCameraDirectionY, mCameraDirectionZ, 0.0f, 1.0f, 0.0f);
        Matrix.perspectiveM(mProjectionMatrix, 0, mCameraFovDegree, mScreenAspectRatio, Z_NEAR, Z_FAR);

    }

    /**
     * onSurfaceCreated Method
     * @param gl GLObject (not used)
     * @param config EGL Setting Object
     */
    @Override
    public void onSurfaceCreated(final GL10 gl, final EGLConfig config) {

        int vShader;
        int fShader;
        int program;

        vShader = loadShader(GLES31.GL_VERTEX_SHADER, VSHADER_SRC);
        fShader = loadShader(GLES31.GL_FRAGMENT_SHADER, FSHADER_SRC);

        program = GLES31.glCreateProgram();
        GLES31.glAttachShader(program, vShader);
        GLES31.glAttachShader(program, fShader);
        GLES31.glLinkProgram(program);

        GLES31.glUseProgram(program);

        mPositionHandle = GLES31.glGetAttribLocation(program, "aPosition");
        mUVHandle = GLES31.glGetAttribLocation(program, "aUV");
        mProjectionMatrixHandle = GLES31.glGetUniformLocation(program, "uProjection");
        mViewMatrixHandle = GLES31.glGetUniformLocation(program, "uView");
        mTexHandle = GLES31.glGetUniformLocation(program, "uTex");
        mModelMatrixHandle = GLES31.glGetUniformLocation(program, "uModel");

        GLES31.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

    }


    /**
     * Rotation process method
     * @param xz X axis direction rotation value
     * @param y Y axis direction rotation value
     */
    public void rotate(float xz, float y) {
        mRotationAngleXZ += xz;
        mRotationAngleY += y;
        if (mRotationAngleY > (Math.PI/2)) {
            mRotationAngleY = (Math.PI/2);
        }
        if (mRotationAngleY < -(Math.PI/2)) {
            mRotationAngleY = -(Math.PI/2);
        }
    }


    /**
     * Zoom in/Zoom out method
     * @param ratio Scale value: Zoom in process performed if the value is 1.0 or more; zoom out process is performed if the value is less than 1.0
     */
    public void scale(float ratio) {

        if (ratio < 1.0) {
            mCameraFovDegree = mCameraFovDegree * (Constants.SCALE_RATIO_TICK_EXPANSION);
            if (mCameraFovDegree > Constants.CAMERA_FOV_DEGREE_MAX) {
                mCameraFovDegree = Constants.CAMERA_FOV_DEGREE_MAX;
            }
        }
        else {
            mCameraFovDegree = mCameraFovDegree * (Constants.SCALE_RATIO_TICK_REDUCTION);
            if (mCameraFovDegree < Constants.CAMERA_FOV_DEGREE_MIN) {
                mCameraFovDegree = Constants.CAMERA_FOV_DEGREE_MIN;
            }
        }

    }


    /**
     * Sets the texture for the sphere
     * @param texture Photo object for texture
     */
    public void setTexture(Photo texture) {
        mTexture = texture;
        mTextureUpdate = true;
    }

    /**
     * Acquires the set texture
     * @return Photo object for texture
     */
    public Photo getTexture() {
        return mTexture;
    }


    /**
     * GL error judgment method for debugging
     * @param TAG TAG output character string
     * @param glOperation Message output character string
     */
    public static void checkGlError(String TAG, String glOperation) {
        int error;
        while ((error = GLES31.glGetError()) != GLES31.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }


    /**
     * Texture setting method
     * @param texture Setting texture
     */
    private void loadTexture(final Bitmap texture) {

        int dividedWidth = texture.getWidth() / 2;

        GLES31.glGenTextures(2, mTextures, 0);

        for (int textureIndex = 0; textureIndex < 2; textureIndex++) {
            GLES31.glActiveTexture(GLES31.GL_TEXTURE0);
            GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, mTextures[textureIndex]);

            GLES31.glTexParameterf(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MIN_FILTER, GLES31.GL_NEAREST);
            GLES31.glTexParameterf(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MAG_FILTER, GLES31.GL_LINEAR);
            GLES31.glTexParameterf(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_WRAP_S, GLES31.GL_CLAMP_TO_EDGE);
            GLES31.glTexParameterf(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_WRAP_T, GLES31.GL_CLAMP_TO_EDGE);

            Bitmap dividedBitmap = Bitmap.createBitmap(texture, (dividedWidth * textureIndex), 0, dividedWidth, texture.getHeight());

            GLUtils.texImage2D(GLES31.GL_TEXTURE_2D, 0, dividedBitmap, 0);
            dividedBitmap.recycle();
        }

    }


    private int loadShader(int type, String shaderCode){

        int shader = GLES31.glCreateShader(type);

        GLES31.glShaderSource(shader, shaderCode);
        GLES31.glCompileShader(shader);

        return shader;
    }

    float getWidth() {return width;}
    float getHeight() {return height;}
    float getFovDegree() {return mCameraFovDegree;}
    double getRotationAngleXZ() {return mRotationAngleXZ;}
    double getmRotationAngleY() {return  mRotationAngleY;}
}