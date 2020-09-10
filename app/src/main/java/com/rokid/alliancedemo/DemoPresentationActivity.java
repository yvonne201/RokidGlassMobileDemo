package com.rokid.alliancedemo;

import android.app.Presentation;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.view.Display;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 双屏异显demo
 */
public class DemoPresentationActivity extends AppCompatActivity {

    private DemoPresentation demoPresentation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_presentation);

        showPresentation();
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
