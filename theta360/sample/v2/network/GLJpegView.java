package com.theta360.sample.v2.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import com.theta360.sample.v2.glview.GLPhotoView;
import com.theta360.sample.v2.model.Photo;
import com.theta360.sample.v2.view.MJpegInputStream;

import java.io.IOException;

public class GLJpegView extends GLPhotoView {
    private GLJpegViewThread mGLJpegViewThread = null;
    private MJpegInputStream mMJpegInputStream = null;
    private boolean existSurface = false;

    public GLJpegView(Context context) {
        super(context);
    }

    public GLJpegView(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    /**
     * Start playback
     */
    public void play() {
        if (mGLJpegViewThread != null) {
            stopPlay();
        }

        if(mMJpegInputStream != null) {
            if (mGLJpegViewThread != null) {
                if (mGLJpegViewThread.getState() == Thread.State.NEW) {
                    mGLJpegViewThread.start();
                }
            } else {
                mGLJpegViewThread = new GLJpegViewThread();
                mGLJpegViewThread.start();
            }
        }
    }

    /**
     * Stop playback
     */
    public void stopPlay() {
        if (mGLJpegViewThread != null) {
            mGLJpegViewThread.cancel();
            boolean retry = true;
            while (retry) {
                try {
                    mGLJpegViewThread.join();
                    retry = false;
                    mGLJpegViewThread = null;
                } catch (InterruptedException e) {
                    e.getStackTrace();
                }
            }
        }
    }

    /**
     * Set source stream for receiving motion JPEG
     * @param source Source stream
     */
    public void setSource(MJpegInputStream source) {
        mMJpegInputStream = source;
        play();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        existSurface = false;
        stopPlay();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
        existSurface = true;
    }

    /**
     * Thread class for receiving motion JPEG
     */
    private class GLJpegViewThread extends Thread {
        private boolean keepRunning = true;

        public GLJpegViewThread() {}

        public void cancel() {
            keepRunning = false;
        }

        @Override
        public void run(){
            Bitmap bitmap;

            while(keepRunning) {
                if (existSurface) {
                    try{
                        if (mMJpegInputStream != null){
                            bitmap = mMJpegInputStream.readMJpegFrame();
                            setTexture(new Photo(bitmap));
                            bitmap.recycle();
                        }
                    }catch (IOException io){
                        io.printStackTrace();
                        keepRunning = false;
                    }
                }


            }

            if (mMJpegInputStream != null) {
                try {
                    mMJpegInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
