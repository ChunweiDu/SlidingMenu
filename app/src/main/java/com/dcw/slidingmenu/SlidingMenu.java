package com.dcw.slidingmenu;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

/**
 * @Author： duchunwei
 * @Date: 2017/6/28   09:53
 * @Email： duchunwei_it@163.com
 * 自定义侧滑菜单
 */

public class SlidingMenu extends HorizontalScrollView {
    //打印TAG
    private static final String TAG = "HorizontalScrollView";
    //上下文
    private Context mContext;
    // 给菜单和内容View指定宽高 - 左边菜单View
    private View mMenuView;
    // 给菜单和内容View指定宽高 - 菜单的宽度
    private int mMenuWidth;
    // 手势处理类 主要用来处理手势快速滑动
    private GestureDetector mGestureDetector;
    // 菜单是否打开
    private boolean mMenuIsOpen = false;
    // 主页面内容View的布局包括阴影ImageView
    private ViewGroup mContentView;
    // 给内容添加阴影效果 - 阴影的ImageView
    private ImageView mShadowIv;

    public SlidingMenu(Context context) {
        this(context, null);
    }

    public SlidingMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        //计算左边菜单的宽度
        //获取自定义的右边留出的宽度
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SlidingMenu);
        float rightPadding = array.getDimension(
                R.styleable.SlidingMenu_SlidingMenu_rightPadding, dip2px(60));
        //计算菜单的宽度 = 屏幕的宽度 - 自定义右边留出的宽度
        mMenuWidth = (int) (getScreenWidth() - rightPadding);
        array.recycle();
        //实例化手势处理类
        mGestureDetector = new GestureDetector(context, new GestureListener());
    }

    /**
     * 把dip 转成像素
     */
    private float dip2px(int dip) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dip, getResources().getDisplayMetrics());
    }


    /**
     * View填充完成时执行
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // 指定菜单和内容View的宽度
        // 获取根View也就是外层的LinearLayout
        ViewGroup container = (ViewGroup) this.getChildAt(0);
        int containerChildCount = container.getChildCount();
        if (containerChildCount > 2) {
            // 里面只允许放置两个布局  一个是Menu(菜单布局) 一个是Content（主页内容布局）
            throw new IllegalStateException("SlidingMenu 根布局LinearLayout下面只允许两个布局,菜单布局和主页内容布局");
        }
        // 获取菜单和内容布局
        mMenuView = container.getChildAt(0);
        //给内容添加阴影效果
        //先new一个主内容布局用来放  阴影和LinearLayout原来的内容布局
        mContentView = new FrameLayout(mContext);
        ViewGroup.LayoutParams contentParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //获取原来的内容布局，并把原来的内容布局从LinearLayout中异常
        View oldContentView = container.getChildAt(1);
        container.removeView(oldContentView);
        //把原来的内容View 和 阴影加到我们新创建的内容布局中
        mContentView.addView(oldContentView);
        //创建阴影ImageView
        mShadowIv = new ImageView(mContext);
        mShadowIv.setBackgroundColor(Color.parseColor("#99000000"));
        mContentView.addView(mShadowIv);
        //把包含阴影的新的内容View 添加到 LinearLayout中
        container.addView(mContentView);

        //指定内容和菜单布局的宽度
        //菜单的宽度 = 屏幕的宽度 - 自定义的右边留出的宽度
        mMenuView.getLayoutParams().width = mMenuWidth;
        //内容的宽度 = 屏幕的宽度
        mContentView.getLayoutParams().width = getScreenWidth();
        //内容的高度=屏幕的高度
        mContentView.getLayoutParams().height = getScreenHeight();
        //当点击阴影关闭菜单
        mShadowIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMenu();
            }
        });
    }

    /**
     * 获取屏幕的高度
     *
     * @return
     */
    private int getScreenHeight() {
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.heightPixels;
    }

    /**
     * 处理手指抬起和快速滑动切换菜单
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // 5.3 处理手指快速滑动
        if (mGestureDetector.onTouchEvent(ev)) {
            return mGestureDetector.onTouchEvent(ev);
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                // 手指抬起获取滚动的位置
                int currentScrollX = getScrollX();
                if (currentScrollX > mMenuWidth / 2) {
                    // 关闭菜单
                    closeMenu();
                } else {
                    // 打开菜单
                    openMenu();
                }
                return false;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        // 是 当前滚动的x距离  在滚动的时候会不断反复的回调这个方法
        Log.e(TAG, l + "");
        // 实现菜单左边抽屉样式的动画效果
        mMenuView.setTranslationX(l * 0.8f);

        // 给内容添加阴影效果 - 计算梯度值
        float gradientValue = l * 1f / mMenuWidth;// 这是  1 - 0 变化的值

        // 给内容添加阴影效果 - 给阴影的View指定透明度   0 - 1 变化的值
        float shadowAlpha = 1 - gradientValue;
        mShadowIv.setAlpha(shadowAlpha);
    }

    /**
     * 5.1.2 打开菜单,
     */
    private void openMenu() {
        mShadowIv.setVisibility(VISIBLE);
        smoothScrollTo(0, 0);
        mMenuIsOpen = true;
    }

    /**
     * 5.1.1 关闭菜单,隐藏阴影View
     */
    private void closeMenu() {
        mShadowIv.setVisibility(GONE);
        smoothScrollTo(mMenuWidth, 0);
        mMenuIsOpen = false;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // 布局指定后会从新摆放子布局，当其摆放完毕后，让菜单滚动到不可见状态
        if (changed) {
            scrollTo(mMenuWidth, 0);
        }
    }

    /**
     * 获取屏幕的宽度
     */
    public int getScreenWidth() {
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.widthPixels;
    }


    /**
     * 5.3 处理手指快速滑动
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // 当手指快速滑动时候回调的方法
            Log.e(TAG, velocityX + "");
            // 如果菜单打开 并且是向左快速滑动 切换菜单的状态
            if (mMenuIsOpen) {
                if (velocityX < 0) {
                    toggleMenu();
                    return true;
                }
            } else {
                // 如果菜单关闭 并且是向右快速滑动 切换菜单的状态
                if (velocityX > 0) {
                    toggleMenu();
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 切换菜单的状态
     */
    private void toggleMenu() {
        if (mMenuIsOpen) {
            closeMenu();
        } else {
            openMenu();
        }
    }
}
