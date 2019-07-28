package com.menthoven.arduinoandroid;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;

public class Rudder extends SurfaceView implements Runnable, Callback {

    private SurfaceHolder mHolder;
    private boolean isStop = false;
    private Thread mThread;
    private Paint mPaint;
    private Point mRockerPosition; //摇杆位置

    private int mRudderRadius = 40;//摇杆半径
    private int mWheelRadius = 120;//摇杆活动范围半径
    private volatile boolean needPaint = true;

    private Point mCtrlPoint ;
    private RudderListener listener = null; //事件回调接口
    public static final int ACTION_RUDDER = 1, ACTION_ATTACK = 2; // 1：摇杆事件 2：按钮事件（未实现）

    public Rudder(Context context) {
        super(context);
    }

    public Rudder(Context context, AttributeSet as) {
        super(context, as);
        float des = this.getResources().getDisplayMetrics().density;
        mCtrlPoint = new Point(
                (int)(this.getResources().getDimensionPixelSize(R.dimen.rudder_width)/des  + mWheelRadius / 2.0),
                (int)(this.getResources().getDimensionPixelSize(R.dimen.rudder_height)/des  + mWheelRadius / 2.0));//摇杆起始位置
        this.setKeepScreenOn(true);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mThread = new Thread(this);
        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mPaint.setAntiAlias(true);//抗锯齿
        mRockerPosition = new Point(mCtrlPoint);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setZOrderOnTop(true);
        mHolder.setFormat(PixelFormat.TRANSPARENT);//设置背景透明
    }

    //设置回调接口
    public void setRudderListener(RudderListener rockerListener) {
        listener = rockerListener;
    }

    @Override
    public void run() {
        Canvas canvas = null;
        while (!isStop) {
            if(needPaint) {
                try {
                    canvas = mHolder.lockCanvas();
                    canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);//清除屏幕
                    mPaint.setColor(Color.CYAN);
//                mPaint.setStyle(Paint.Style.STROKE);
                    canvas.drawCircle(mCtrlPoint.x, mCtrlPoint.y, mWheelRadius, mPaint);//绘制范围
                    mPaint.setColor(Color.RED);
//                mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                    canvas.drawCircle(mRockerPosition.x, mRockerPosition.y, mRudderRadius, mPaint);//绘制摇杆
                    needPaint = false;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (canvas != null) {
                        mHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isStop = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(isEnabled()) {
            int len = MathUtils.getLength(mCtrlPoint.x, mCtrlPoint.y, event.getX(), event.getY());
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                //如果屏幕接触点不在摇杆挥动范围内,则不处理
                if (len > mWheelRadius) {
                    return true;
                }
            }
            needPaint=true;
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (len <= mWheelRadius) {
                    //如果手指在摇杆活动范围内，则摇杆处于手指触摸位置
                    mRockerPosition.set((int) event.getX(), (int) event.getY());

                } else {
                    //设置摇杆位置，使其处于手指触摸方向的 摇杆活动范围边缘
                    mRockerPosition = MathUtils.getBorderPoint(mCtrlPoint, new Point((int) event.getX(), (int) event.getY()), mWheelRadius);
                }
                if (listener != null) {
//                float radian = MathUtils.getRadian(mCtrlPoint, new Point((int)event.getX(), (int)event.getY()));
//                listener.onSteeringWheelChanged(ACTION_RUDDER,Rudder.this.getAngleCouvert(radian));
                    listener.onSteeringWheelChanged(ACTION_RUDDER,
                            (mRockerPosition.x - mCtrlPoint.x) / (float) mWheelRadius,
                            (mCtrlPoint.y - mRockerPosition.y) / (float) mWheelRadius);
                }
            }
            //如果手指离开屏幕，则摇杆返回初始位置
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mRockerPosition = new Point(mCtrlPoint);
                listener.onSteeringWheelChanged(ACTION_RUDDER, 0, 0);
            }
        }
        return true;
    }

    //获取摇杆偏移角度 0-360°
//    private int getAngleCouvert(float radian) {
//        int tmp = (int)Math.round(radian/Math.PI*180);
//        if(tmp < 0) {
//            return -tmp;
//        }else{
//            return 180 + (180 - tmp);
//        }
//    }

    //回调接口
    public interface RudderListener {
        //        void onSteeringWheelChanged(int action,int angle);
        void onSteeringWheelChanged(int action, float x, float y);
    }
}
class MathUtils {
    //获取两点间直线距离
    public static int getLength(float x1,float y1,float x2,float y2) {
        return (int)Math.sqrt(Math.pow(x1-x2, 2) + Math.pow(y1-y2, 2));
    }
    /**
     * 获取线段上某个点的坐标，长度为a.x - cutRadius
     * @param a 点A
     * @param b 点B
     * @param cutRadius 截断距离
     * @return 截断点
     */
    @NonNull
    public static Point getBorderPoint(Point a, Point b, int cutRadius) {
        float radian = getRadian(a, b);
        return new Point(a.x + (int)(cutRadius * Math.cos(radian)), a.x + (int)(cutRadius * Math.sin(radian)));
    }

    //获取水平线夹角弧度
    public static float getRadian (Point a, Point b) {
        float lenA = b.x-a.x;
        float lenB = b.y-a.y;
        float lenC = (float)Math.sqrt(lenA*lenA+lenB*lenB);
        float ang = (float)Math.acos(lenA/lenC);
        ang = ang * (b.y < a.y ? -1 : 1);
        return ang;
    }
}