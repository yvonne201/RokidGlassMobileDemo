package com.rokid.alliancedemo;

import android.app.Presentation;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.rokid.mcui.utils.Logger;

/**
 * 双屏异显demo
 */
public class DemoPresentationActivity extends AppCompatActivity {

    private DemoPresentation demoPresentation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_presentation);

        FrameLayout decorView = (FrameLayout) getWindow().getDecorView();
        showViewInfo(decorView);

        showPresentation();
    }

    private void showViewInfo(View parent) {
        if (parent instanceof ViewParent) {
            for (int i = 0; i < ((ViewGroup) parent).getChildCount(); i++) {
                View childAt = ((ViewGroup) parent).getChildAt(i);
                showViewInfo(childAt);
            }
        } else {
            if (parent instanceof TextView) {
                Logger.i("instance of textview:", ((TextView) parent).getText().toString());

                if (parent.hasOnClickListeners()) {
                    Logger.i("has onclick listener");
                }
            }
        }
    }


    /**
     * 这里仅仅简单的在眼镜上显示一个Button
     * 关于Presentation的使用方式请参考Android开发者网站(https://developer.android.com/reference/android/app/Presentation)学习
     */
    public static class DemoPresentation extends Presentation {

        public DemoPresentation(Context outerContext, Display display) {
            this(outerContext, display, R.style.GlassTheme);
        }

        public DemoPresentation(Context outerContext, Display display, int theme) {
            super(outerContext, display, theme);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            // Be sure to call the super class.
            super.onCreate(savedInstanceState);

            // Inflate the layout.
            setContentView(R.layout.presentation_demo_layout);
            findViewById(R.id.bt_pre).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Logger.i("Presentation button click");
                }
            });

            findViewById(R.id.bt_first).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Logger.i("Presentation button click first");
                }
            });
            findViewById(R.id.bt_fourth).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Logger.i("Presentation button click fourth");
                }
            });
            findViewById(R.id.bt_second).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Logger.i("Presentation button click second");
                }
            });
            FrameLayout decorView = (FrameLayout) getWindow().getDecorView();
            showViewInfo(decorView);

            View focus = decorView.findFocus();
            if (null != focus) {
                if (focus instanceof TextView) {
                    Logger.i("Presentation focus view is textview:", ((TextView) focus).getText().toString());
                }
            }
        }

        private int count = 1;

        private void showViewInfo(View parent) {
            if (parent instanceof ViewParent) {
                for (int i = 0; i < ((ViewGroup) parent).getChildCount(); i++) {
                    View childAt = ((ViewGroup) parent).getChildAt(i);
                    showViewInfo(childAt);
                }
            } else {
                if (parent instanceof TextView) {
                    Logger.i("Presentation instance of textview:", ((TextView) parent).getText().toString());

                    if (parent.hasOnClickListeners()) {
                        Logger.i("Presentation has onclick listener");
                        parent.callOnClick();
                        final ViewTreeObserver observer = parent.getViewTreeObserver();

                        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                            @Override
                            public boolean onPreDraw() {
                                float x = parent.getX();
                                float y = parent.getY();
                                Logger.i("onPreDraw view " + x + " y " + y);
                                if (observer.isAlive()) {
                                    observer.removeOnPreDrawListener(this);
                                }
                                ViewTreeObserver newObserver = parent.getViewTreeObserver();
                                newObserver.addOnDrawListener(new ViewTreeObserver.OnDrawListener() {
                                    @Override
                                    public void onDraw() {
                                        if (newObserver.isAlive()) {
                                            newObserver.removeOnDrawListener(this);
                                        }
                                        TextView textView = new TextView(getContext());
                                        textView.setText(String.valueOf(count++));
                                        textView.setX(x);
                                        textView.setY(y);
                                        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(20, 20);
                                        getWindow().addContentView(textView, params);
                                    }
                                });
                                return false;
                            }
                        });
                    }
                }
            }
        }
    }


    private void showPresentation() {
        DisplayManager dm = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        if (null == dm) throw new RuntimeException("Request DisplayManager failed");
        Display[] displays = dm.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
        if (displays.length > 0) {
            Display display = displays[0];
            demoPresentation = new DemoPresentation(this, display);
            demoPresentation.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != demoPresentation && demoPresentation.isShowing()) {
            demoPresentation.dismiss();
            demoPresentation = null;
        }
    }
}
