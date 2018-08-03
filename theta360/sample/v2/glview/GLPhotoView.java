package com.theta360.sample.v2.glview;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.Scroller;

import com.theta360.sample.v2.model.Constants;
import com.theta360.sample.v2.model.Photo;
import com.theta360.sample.v2.model.RotateInertia;
import com.vieweet.app.Database.Hotspot;
import com.vieweet.app.GLViewActivity;

import java.util.List;

/**
 * View class for photo display
 */
public class GLPhotoView extends GLSurfaceView {

    private static final int ANIMATION_INTERVAL = 10;

    private GLRenderer mRenderer = null;

    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;

    private Scroller mScroller = null;

    private float mPrevX, mPrevY;

    private List<Hotspot> hotspots;

    Double theta, phi;

    private RotateInertia mRotateInertia = RotateInertia.INERTIA_0;

    /**
     * Constructor
     * @param context Context
     */

    public GLPhotoView(Context context) {
        this(context, null);

    }

    /**
     * Constructor
     * @param context Context
     * @param attrs Argument for resource
     */
    public GLPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }


    /**
     * onTouchEvent Event listener
     * @param event Event object
     * @return Process continuation judgment value
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        boolean ret = false;
        mScaleGestureDetector.onTouchEvent(event);
        if (!mScaleGestureDetector.isInProgress()) {
            ret = mGestureDetector.onTouchEvent(event);
            if (!ret) {
                super.onTouchEvent(event);
            }
        }
        return ret;
    }


    private void initialize(final Context context) {

        setEGLContextClientVersion(2);

        mRenderer = new GLRenderer();
        setRenderer(mRenderer);

        setLongClickable(true);

        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            private final int SWIPE_MAX_OF_PATH_X = 100;
            private final int SWIPE_MAX_OF_PATH_Y = 100;

            final Handler handler = new Handler();

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                float x = e.getX();
                float y = e.getY();
                float width = mRenderer.getWidth();
                float height = mRenderer.getHeight();
                float fovDegree = mRenderer.getFovDegree();
                double mRotationAngleXZ = mRenderer.getRotationAngleXZ();
                double mRotationAngleY = mRenderer.getmRotationAngleY();

                theta = (double) ((x - width / 2) / width
                        * fovDegree * 3 / 4);
                phi = - (double) ((y - height / 2) / height
                        * fovDegree * 4 / 3);

                theta += (mRotationAngleXZ * 180 / Math.PI);
                phi += (mRotationAngleY * 180 / Math.PI);

                theta = theta % 360;
                phi = phi < - 90 ? -90 : (phi > 90 ? 90 : phi);

                Hotspot hotspot = getHotspot(theta, phi);

                if (hotspot != null){
                    GLViewActivity parentActivity = GLViewActivity.getMyActivity();
                    parentActivity.loadHotspot(hotspot);
                    return true;
                }

                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e){
                float x = e.getX();
                float y = e.getY();
                float width = mRenderer.getWidth();
                float height = mRenderer.getHeight();
                float fovDegree = mRenderer.getFovDegree();
                double mRotationAngleXZ = mRenderer.getRotationAngleXZ();
                double mRotationAngleY = mRenderer.getmRotationAngleY();

                theta = (double) ((x - width / 2) / width
                        * fovDegree * 3 / 4);
                phi = - (double) ((y - height / 2) / height
                        * fovDegree * 4 / 3);

                theta += (mRotationAngleXZ * 180 / Math.PI);
                phi += (mRotationAngleY * 180 / Math.PI);

                theta = theta % 360;
                phi = phi < - 90 ? -90 : (phi > 90 ? 90 : phi);

                Hotspot hotspot = getHotspot(theta, phi);

                if (hotspot == null) {
                    GLViewActivity parentActivity = GLViewActivity.getMyActivity();
                    parentActivity.selectionPicture(theta, phi);
                    return true;
                }
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {
                return;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                return;
            }

            @Override
            public boolean onDown(MotionEvent e) {

                if (null != mScroller && !mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    handler.removeCallbacksAndMessages(null);
                }

                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

                boolean ret;

                if (null != mScroller && !mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                if ((Math.abs(distanceX) > SWIPE_MAX_OF_PATH_X ) || (Math.abs(distanceY) > SWIPE_MAX_OF_PATH_Y )) {
                    ret = false;
                } else {
                    float diffX = distanceX / Constants.ON_SCROLL_DIVIDER_X;
                    float diffY = distanceY / Constants.ON_SCROLL_DIVIDER_Y;

                    if (Math.abs(diffX) < Constants.THRESHOLD_SCROLL_X) {
                        diffX = 0.0f;
                    }
                    if (Math.abs(diffY) < Constants.THRESHOLD_SCROLL_Y) {
                        diffY = 0.0f;
                    }

                    if (null != mRenderer) {
                        mRenderer.rotate(diffX, -diffY);
                    }
                    ret = true;
                }

                return ret;
            }


            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                mScroller.fling((int)e2.getX(), (int)e2.getY(), (int)velocityX, (int)velocityY, 0, getWidth(), 0, getHeight());
                mPrevX = e2.getX();
                mPrevY = e2.getY();

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mRotateInertia == RotateInertia.INERTIA_0) {
                            // do nothing
                        }
                        else {
                            mScroller.computeScrollOffset();
                            float diffX = mScroller.getCurrX() - mPrevX;
                            float diffY = mScroller.getCurrY() - mPrevY;
                            mPrevX = mScroller.getCurrX();
                            mPrevY = mScroller.getCurrY();

                            if (mRotateInertia == RotateInertia.INERTIA_50) {
                                diffX = diffX / Constants.ON_FLING_DIVIDER_X_FOR_INERTIA_50;
                                diffY = diffY / Constants.ON_FLING_DIVIDER_Y_FOR_INERTIA_50;
                            }
                            else {
                                diffX = diffX / Constants.ON_FLING_DIVIDER_X_FOR_INERTIA_100;
                                diffY = diffY / Constants.ON_FLING_DIVIDER_Y_FOR_INERTIA_100;
                            }
                            mRenderer.rotate(-diffX, diffY);

                            if (!mScroller.isFinished()) {
                                handler.postDelayed(this, ANIMATION_INTERVAL);
                            }
                        }
                    }
                });

                return true;
            }
        });

        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {

                float scale = detector.getScaleFactor();

                if (null != mRenderer) {
                    mRenderer.scale(scale);
                }

                return true;
            }
        });

        mScroller = new Scroller(context);

        return;
    }


    /**
     * Texture setting method
     * @param thumbnail Photo object for texture
     */
    public void setTexture(Photo thumbnail) {
        mRenderer.setTexture(thumbnail);
        return;
    }


    /**
     * Inertia setting method
     * @param mRotateInertia Setting inertia value
     */
    public void setmRotateInertia(RotateInertia mRotateInertia) {
        this.mRotateInertia = mRotateInertia;
        return;
    }

    public void setHotspots(List<Hotspot> hotspots){
        this.hotspots = hotspots;
    }

    private Hotspot getHotspot(Double theta, Double phi){
        Hotspot result = null;
        double tolerance = 10.0;

        for (Hotspot hotspot : hotspots) {
            if (((hotspot.theta <= (theta + tolerance) && hotspot.theta >= theta - tolerance)
                || (hotspot.theta <= theta + 360 + tolerance && hotspot.theta >= theta + 360 - tolerance))
                &&
                    ((hotspot.phi <= (phi + tolerance) && hotspot.phi >= phi - tolerance)
                            || (hotspot.phi <= phi + 360 + tolerance && hotspot.phi >= phi + 360 - tolerance))){
                result = hotspot;
                break;
            }
        }
        return result;
    }
}