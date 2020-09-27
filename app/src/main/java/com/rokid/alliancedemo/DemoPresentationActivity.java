package com.rokid.alliancedemo;

import android.app.Presentation;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rokid.glass.m_glass.RKRecogPresentation;
import com.rokid.mcui.utils.Logger;

import java.util.LinkedList;

/**
 * 双屏异显demo
 */
public class DemoPresentationActivity extends AppCompatActivity {

    private DemoPresentation demoPresentation;
    private LinkedList<DemoPresentation> mShowList = new LinkedList<>();
    private int currentDisplayId;
    private DisplayManager.DisplayListener displayListener = new DisplayManager.DisplayListener() {
        @Override
        public void onDisplayAdded(int displayId) {
            if (currentDisplayId != displayId) {
                showCurrentDisplay();
            }
        }

        @Override
        public void onDisplayRemoved(int displayId) {

        }

        @Override
        public void onDisplayChanged(int displayId) {

        }
    };

    private void showCurrentDisplay() {
        Display[] presentationDisplays = ((DisplayManager)getSystemService(Context.DISPLAY_SERVICE)).getDisplays("android.hardware.display.category.PRESENTATION");
        if (presentationDisplays.length == 0) {
            if (null != demoPresentation && demoPresentation.isShowing()) demoPresentation.dismiss();
            return;
        }
        if (presentationDisplays[0].getDisplayId() == this.currentDisplayId) {

            if (null != demoPresentation) demoPresentation.show();
        } else {
            if (null != demoPresentation&& demoPresentation.isShowing()) {
                demoPresentation.dismiss();
                String text = demoPresentation.getText();
                demoPresentation = new DemoPresentation(this, presentationDisplays[0]);
                if (!TextUtils.isEmpty(text)) {
                    demoPresentation.setText(text);
                }
                demoPresentation.show();
            }


        }

        currentDisplayId= presentationDisplays[0].getDisplayId();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_presentation);

        findViewById(R.id.bt_first).setOnClickListener(v -> {
            showPresentation("第一个");
        });

        findViewById(R.id.bt_second).setOnClickListener(v -> {

            showPresentation("第二个");
        });

        findViewById(R.id.bt_dismiss).setOnClickListener(v -> {
            if (mShowList.size() > 0) {
                DemoPresentation demoPresentation = mShowList.removeLast();
                if (null != demoPresentation && demoPresentation.isShowing()) {
                    demoPresentation.dismiss();
                }
            }
        });
    }


    /**
     * 这里仅仅简单的在眼镜上显示一个Button
     * 关于Presentation的使用方式请参考Android开发者网站(https://developer.android.com/reference/android/app/Presentation)学习
     * TODO 注意  这里由于主题是透明的，因此多个Presentation同时显示会重叠显示
     */
    public static class DemoPresentation extends Presentation {

        private String text;

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

            ((TextView) findViewById(R.id.tv_pre)).setText(text);

        }

        public String getText() {
            return text;
        }



        public void setText(String text) {
            this.text = text;
        }
    }


    private Display display;

    private void showPresentation(String text) {
        if (null == display) {
            display = getDisplay();
        }
        if (display == null) {
            Toast.makeText(this, "没有找到外接显示屏", Toast.LENGTH_LONG).show();
            return;
        }

        demoPresentation = new DemoPresentation(this, display);
        demoPresentation.setText(text);
        demoPresentation.show();
        mShowList.add(demoPresentation);
    }

    private Display getDisplay() {
        DisplayManager dm = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        if (null == dm) throw new RuntimeException("Request DisplayManager failed");
        Display[] displays = dm.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
        dm.registerDisplayListener(displayListener, new Handler());
        return null != displays && displays.length > 0 ? displays[0] : null;
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
