package com.ljz.circlebar.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TextView;

import com.ljz.circlebar.R;
import com.ljz.circlebar.util.DpOrPxUtils;

/**
 * Created by lenovo on 2017/11/6.
 */

public class CircleBarView extends View {

    private RectF mRectF;//绘制圆弧的矩形区域

    //    private Paint rPaint;
    private Paint progressPaint;
    private Paint bgPaint;
    private float sweepAngle;//背景圆弧扫过的角度
    private float startAngle;//背景圆弧的起始角度
    private float progressSweepAngle;//进度条圆弧扫过的角度
    private CircleBarAnim anim;

    private float progressNum;//可以更新的数值
    private float maxNum; //进度条最大数值

    private float barWidth;//圆弧进度条宽度
    private int defaultSize;//自定义View默认的宽高

    private int progressColor;//进度条圆弧颜色
    private int bgColor;//背景圆弧颜色

    private TextView textView;
    private OnAnimationListener onAnimationListener;

    public CircleBarView(Context context) {
        this(context, null);
    }

    public CircleBarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleBarView);

        progressColor = typedArray.getColor(R.styleable.CircleBarView_progress_color,Color.GREEN);
        bgColor = typedArray.getColor(R.styleable.CircleBarView_bg_color,Color.GRAY);//默认为灰色
        startAngle = typedArray.getFloat(R.styleable.CircleBarView_start_angle,0);//默认为0
        sweepAngle = typedArray.getFloat(R.styleable.CircleBarView_sweep_angle,360);//默认为360
        barWidth = typedArray.getDimension(R.styleable.CircleBarView_bar_width,DpOrPxUtils.dip2px(context,10));//默认为10dp
        typedArray.recycle();

        mRectF = new RectF();

//        rPaint = new Paint();
//        rPaint.setStyle(Paint.Style.STROKE);//只描边 不填充
//        rPaint.setColor(Color.RED);
        barWidth = DpOrPxUtils.dip2px(context, 5);
        defaultSize = DpOrPxUtils.dip2px(context, 100);

        progressPaint = new Paint();
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setColor(progressColor);
        progressPaint.setAntiAlias(true);
        progressPaint.setStrokeWidth(barWidth);

        bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.STROKE);//只描边，不填充
        bgPaint.setColor(bgColor);
        bgPaint.setAntiAlias(true);//设置抗锯齿
        bgPaint.setStrokeWidth(barWidth);

        anim = new CircleBarAnim();

        progressNum = 0;
        maxNum = 100;//也是随便设的

//        startAngle = 0;
//        sweepAngle = 360;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float x = 50;
        float y = 50;
//        RectF rectF = new RectF(x, y, x + 300, y + 300);

        Log.i("zhou", sweepAngle + "!");

        canvas.drawArc(mRectF, startAngle, sweepAngle, false, bgPaint); //recf 画板的背景一般用矩形 false不连中点
        canvas.drawArc(mRectF, startAngle, progressSweepAngle, false, progressPaint);
//        canvas.drawRect(rectF, rPaint);
    }

    public class CircleBarAnim extends Animation {

        public CircleBarAnim() {
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) { //在执行动画的时候调用
            super.applyTransformation(interpolatedTime, t);
            //实现颜色变动
            onAnimationListener.howTiChangeProgressColor(progressPaint,interpolatedTime,progressNum,maxNum);
            //实现文字
            if(textView !=null && onAnimationListener!=null){
                textView.setText(onAnimationListener.howToChangeText(interpolatedTime,progressNum,maxNum));
            }
            //实现进度条
            progressSweepAngle = interpolatedTime * sweepAngle * progressNum / maxNum;
            ; //回调更新sweepAngle的值
            postInvalidate(); //更新ondraw
        }
    }

    public void setProgressNum(float progressNum, int time) {
        //省略部分代码...
        this.progressNum = progressNum;
        anim.setDuration(time);
        this.startAnimation(anim);
    }

    public void setOnAnimationListener(OnAnimationListener animationListener){
        this.onAnimationListener = animationListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = measureSize(defaultSize, heightMeasureSpec);
        int width = measureSize(defaultSize, widthMeasureSpec);
        int min = Math.min(width, height);// 获取View最短边的长度
        setMeasuredDimension(min, min);// 强制改View为以最短边为长度的正方形

        if (min >= barWidth * 2) {//这里简单限制了圆弧的最大宽度
            mRectF.set(barWidth / 2, barWidth / 2, min - barWidth / 2, min - barWidth / 2);
        }

    }

    private int measureSize(int defaultSize, int measureSpec) {
        int result = defaultSize;
        int specMode = View.MeasureSpec.getMode(measureSpec); //父控件传来的模式
        int specSize = View.MeasureSpec.getSize(measureSpec); //父控件传来的值

        if (specMode == View.MeasureSpec.EXACTLY) { //MATCH_PARENT 其他情况 就是具体赋值
            result = specSize;  //match是传来的父控件最大值 否则是具体的值
        } else if (specMode == View.MeasureSpec.AT_MOST) {
            result = Math.min(result, specSize); //WRAP_CONTENT result是默认的大小 wrap的 specsize是传来的父控件 俩者取小
        }
        return result;
    }

    public interface OnAnimationListener {
        /**
         * 如何处理要显示的文字内容
         * @param interpolatedTime 从0渐变成1,到1时结束动画
         * @param progressNum 进度条数值
         * @param maxNum 进度条最大值
         * @return
         */
        String howToChangeText(float interpolatedTime, float progressNum, float maxNum);

        /**
         * 如何处理进度条的颜色
         * @param paint 进度条画笔
         * @param interpolatedTime 从0渐变成1,到1时结束动画
         * @param progressNum 进度条数值
         * @param maxNum 进度条最大值
         */
        void howTiChangeProgressColor(Paint paint, float interpolatedTime, float progressNum, float maxNum);

    }

    /**
     * 设置显示文字的TextView
     * @param textView
     */
    public void setTextView(TextView textView) {
        this.textView = textView;
    }

}
