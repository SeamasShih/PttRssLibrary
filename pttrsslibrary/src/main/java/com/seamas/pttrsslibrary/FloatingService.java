package com.seamas.pttrsslibrary;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

public class FloatingService extends Service {

    private WindowManager.LayoutParams wmParams;
    private WindowManager mWindowManager;
    private View mWindowView;
    private ViewGroup view;
    private RecyclerView recyclerView;

    private WindowManager.LayoutParams deleteParams;
    private View deleteView;
    private FloatingImage delete;

    private Intent intent;

    private float startX;
    private float startY;
    private float endX;
    private float endY;
    private final float deleteX = Resources.getSystem().getDisplayMetrics().widthPixels >> 1;
    private final float deleteY = (Resources.getSystem().getDisplayMetrics().heightPixels * 7 >> 3);

    private boolean isAddDeleteView = false;
    private enum MODE{
        DELETE,
        PREVIEW,
        MOVING,
        NORMAL,
        CLOSING
    }
    private MODE mode = MODE.NORMAL;

    private ValueAnimator animator;
    private ValueAnimator deleteAnimator;

    @Override
    public void onCreate() {
        super.onCreate();

        initWindowParams();
        initView();
        initClick();
        addWindowView2Window();
        initAnimator();
    }

    private void initAnimator() {
        animator = new ValueAnimator();
        animator.setDuration(500);
        animator.setInterpolator(new OvershootInterpolator());

        deleteAnimator = new ValueAnimator();
        deleteAnimator.setDuration(500);
        deleteAnimator.setInterpolator(new OvershootInterpolator());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initClick() {
        view.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    animator.cancel();
                    deleteAnimator.cancel();
                    startX = event.getRawX();
                    startY = event.getRawY();
                    endX = event.getRawX();
                    endY = event.getRawY();
                    view.setScaleX(.8f);
                    view.setScaleY(.8f);
                    break;
                case MotionEvent.ACTION_MOVE:
                    endX = event.getRawX();
                    endY = event.getRawY();
                    if (readyToDelete()) {
                        if (deleteAnimator.isRunning() || mode == MODE.DELETE)
                            return true;
                        else {
                            startDeleteAnimator();
                            return true;
                        }
                    } else {
                        if (mode == MODE.PREVIEW) {
                            removeRecyclerView();
                            mode = MODE.CLOSING;
                        }
                        wmParams.x = (int) endX - (delete.getW() >> 1);
                        wmParams.y = (int) endY - (delete.getH());
                        if (needIntercept())
                            mode = MODE.MOVING;
                        if (!isAddDeleteView)
                            addDeleteView2Window();
                        else if (mode == MODE.DELETE)
                            cancelDeleteAnimator();
                        else
                            mWindowManager.updateViewLayout(mWindowView, wmParams);
                        return true;
                    }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    view.setScaleX(1);
                    view.setScaleY(1);
                    switch (mode){
                        case DELETE:
                            stop();
                            return true;
                        case MOVING:
                            startAnimatorToEdge();
                            if (isAddDeleteView)
                                removeDeleteView();
                            mode = MODE.NORMAL;
                            return true;
                        case CLOSING:
                            mode = MODE.NORMAL;
                            if (isAddDeleteView) {
                                removeDeleteView();
                            }
                            return true;
                    }
                    if (isAddDeleteView) {
                        removeDeleteView();
                    }
                    break;
            }
            return false;
        });

        view.setOnClickListener(v -> {
            if (mode == MODE.NORMAL) {
                addRecyclerView();
            } else {
                removeRecyclerView();
            }
        });
    }

    private void removeRecyclerView() {
        mode = MODE.NORMAL;
        view.removeView(recyclerView);
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowManager.updateViewLayout(mWindowView, wmParams);
    }

    private void addRecyclerView() {
        mode = MODE.PREVIEW;
        wmParams.width = getResources().getDisplayMetrics().widthPixels;
        mWindowManager.updateViewLayout(mWindowView, wmParams);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new GridLayoutManager(getApplication(), 2));
        recyclerView.setAdapter(new Adapter(intent.getParcelableArrayListExtra("Articles"), view));
        view.addView(recyclerView);
    }

    private void stop() {
        view.setVisibility(View.GONE);
        delete.setVisibility(View.GONE);

        animator.cancel();
        deleteAnimator.cancel();

        mWindowManager.removeView(mWindowView);
        mWindowManager.removeView(deleteView);

        stopSelf();
    }

    private void startDeleteAnimator() {
        deleteAnimator.cancel();
        deleteAnimator.setFloatValues(0, 100);
        deleteAnimator.setDuration(500);
        deleteAnimator.setInterpolator(new OvershootInterpolator());
        deleteAnimator.removeAllUpdateListeners();
        deleteAnimator.addUpdateListener(animation -> {
            float r = (float) animation.getAnimatedValue() / 100;
            wmParams.x = (int) (r * deleteX + (1 - r) * endX - (delete.getW() >> 1));
            wmParams.y = (int) (r * deleteY + (1 - r) * endY - (delete.getH() >> 1));
            mWindowManager.updateViewLayout(mWindowView, wmParams);

            delete.setScaleX(.7f + r * 0.3f);
            delete.setScaleY(.7f + r * 0.3f);
        });
        mode = MODE.DELETE;
        deleteAnimator.start();
    }

    private void cancelDeleteAnimator() {
        deleteAnimator.cancel();
        deleteAnimator.setFloatValues(0, 100);
        deleteAnimator.setDuration(300);
        deleteAnimator.setInterpolator(new DecelerateInterpolator());
        deleteAnimator.removeAllUpdateListeners();
        deleteAnimator.addUpdateListener(animation -> {
            float r = (float) animation.getAnimatedValue() / 100;
            wmParams.x = (int) (r * endX + (1 - r) * deleteX - (delete.getW() >> 1));
            wmParams.y = (int) (r * endY + (1 - r) * deleteY - (delete.getH() >> 1));
            mWindowManager.updateViewLayout(mWindowView, wmParams);

            delete.setScaleX(1f - r * 0.3f);
            delete.setScaleY(1f - r * 0.3f);
        });
        mode = MODE.MOVING;
        deleteAnimator.start();
    }

    private void startAnimatorToEdge() {
        if (endX < Resources.getSystem().getDisplayMetrics().widthPixels >> 1)
            animator.setIntValues((int) endX - (delete.getW() >> 1), 25);
        else
            animator.setIntValues((int) endX - (delete.getH() >> 1), Resources.getSystem().getDisplayMetrics().widthPixels - (delete.getW()) - 25);

        animator.removeAllUpdateListeners();
        animator.addUpdateListener(animation -> {
            wmParams.x = (int) animation.getAnimatedValue();
            mWindowManager.updateViewLayout(mWindowView, wmParams);
        });

        animator.start();
    }

    private float convertDpToPixel(float dp) {
        return dp * Resources.getSystem().getDisplayMetrics().density;
    }

    private boolean needIntercept() {
        return Math.abs(startX - endX) > 200 || Math.abs(startY - endY) > 200;
    }

    private boolean readyToDelete() {
        return Math.abs(deleteX - endX) < 200 && Math.abs(deleteY - endY) < 200;
    }

    private void initWindowParams() {
        mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);

        wmParams = new WindowManager.LayoutParams();
        //type
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        else
            wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        wmParams.format = PixelFormat.TRANSLUCENT;
        //flags
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        deleteParams = new WindowManager.LayoutParams();
        //type
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            deleteParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        else
            deleteParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        deleteParams.format = PixelFormat.TRANSLUCENT;
        //flags
        deleteParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        deleteParams.gravity = Gravity.LEFT | Gravity.TOP;
        deleteParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        deleteParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
    }

    private void initView() {
        mWindowView = LayoutInflater.from(getApplication()).inflate(R.layout.floatint_service, null);
        view = mWindowView.findViewById(R.id.percentTv);
        recyclerView = view.findViewById(R.id.recycler);
        view.removeView(recyclerView);
        view.setClickable(true);

        deleteView = LayoutInflater.from(getApplication()).inflate(R.layout.floatint_service_delete, null);
        delete = deleteView.findViewById(R.id.delete);
        delete.setScaleX(.7f);
        delete.setScaleY(.7f);
    }

    private void addWindowView2Window() {
        mWindowManager.addView(mWindowView, wmParams);
        wmParams.x = Resources.getSystem().getDisplayMetrics().widthPixels - (delete.getW()) - 25;
        mWindowManager.updateViewLayout(mWindowView, wmParams);
    }

    private void addDeleteView2Window() {
        isAddDeleteView = true;
        mWindowManager.addView(deleteView, deleteParams);
        deleteParams.x = (Resources.getSystem().getDisplayMetrics().widthPixels / 2) - (delete.getW() >> 1);
        deleteParams.y = (Resources.getSystem().getDisplayMetrics().heightPixels * 7 / 8) - (delete.getH() >> 1);
        mWindowManager.updateViewLayout(deleteView, deleteParams);
    }

    private void removeDeleteView() {
        isAddDeleteView = false;
        mWindowManager.removeViewImmediate(deleteView);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.intent = intent;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}
