package com.hmct.rotatepointer;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Administrator on 2016/11/17.
 * 陈朝勇
 * 仿支付宝芝麻信用圆形仪表盘
 */

public class RoundIndicatorView extends View {

    private Paint paint;
    private Paint paint_2;
    private Paint paint_3;
    private Paint paint_4;
    private Paint paint_5;

    private Paint paint_6;

    private Context context;
    private int maxNum;
    private int startAngle;
    private int sweepAngle;
    private int radius;
    private int mWidth;
    private int mHeight;
    private int sweepInWidth;//内圆的宽度
    private int sweepOutWidth;//外圆的宽度
    private int currentNum=0;//需设置setter、getter 供属性动画使用
    private int[] indicatorColor = {0xffffffff,0x00ffffff,0x99ffffff,0xffffffff};

    private ValueAnimator outerProgressAnim;
    private float value;
    private int min = 0;
    private int max = 100;//最大进度值
    private float outerProgressValue = min;
    private float plusAngle = 0;//经过的角度


    public int getCurrentNum() {
        return currentNum;
    }

    public void setCurrentNum(int currentNum) {
        this.currentNum = currentNum;
        invalidate();
    }

    public RoundIndicatorView(Context context) {
        this(context,null);
    }

    public RoundIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RoundIndicatorView(final Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        setBackgroundColor(0xFFFF6347);
        initAttr(attrs);
        initPaint();

        initAnim();
    }

    public void setCurrentNumAnim(int num) {
        float duration = (float)Math.abs(num-currentNum)/maxNum *1500+500; //根据进度差计算动画时间
        ObjectAnimator anim = ObjectAnimator.ofInt(this,"currentNum",num);
        anim.setDuration((long) Math.min(duration,2000));
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                int color = calculateColor(value);
                setBackgroundColor(color);
            }
        });
        anim.start();



    }
    private int calculateColor(int value){
        ArgbEvaluator evealuator = new ArgbEvaluator();
        float fraction = 0;
        int color = 0;
        if(value <= maxNum/2){
            fraction = (float)value/(maxNum/2);
            color = (int) evealuator.evaluate(fraction,0xFFFF6347,0xFFFF8C00); //由红到橙
        }else {
            fraction = ( (float)value-maxNum/2 ) / (maxNum/2);
            color = (int) evealuator.evaluate(fraction,0xFFFF8C00,0xFF00CED1); //由橙到蓝
        }
        return color;
    }

    private void initPaint() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //防抖动
        paint.setDither(true);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xffffffff);
        paint_2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint_3 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint_4 = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint_5 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint_6 = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    private void initAttr(AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs,R.styleable.RoundIndicatorView);
        maxNum = array.getInt(R.styleable.RoundIndicatorView_maxNum,100);
        startAngle = array.getInt(R.styleable.RoundIndicatorView_startAngle,180);
        sweepAngle = array.getInt(R.styleable.RoundIndicatorView_sweepAngle,180);
        //内外圆的宽度
        sweepInWidth = 8;
        sweepOutWidth = 3;
        array.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth=300;
        mHeight=110;

        setMeasuredDimension(mWidth,mHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        radius = getMeasuredWidth()/4; //不要在构造方法里初始化，那时还没测量宽高
        canvas.save();
        canvas.translate(150,100);//把当前画布的原点移到(mWidth/2,mWidth/2),后面的操作都以(mWidth/2,mWidth/2)作为参照点，默认原点为(0,0)
        drawRound(canvas);  //画内外圆
        drawScale(canvas);//画刻度
        canvas.restore();
    }

    private void drawScale(Canvas canvas) {
        canvas.save();
        float angle = (float)sweepAngle/30;//刻度间隔
        canvas.rotate(-270+startAngle); //将起始刻度点旋转到正上方（270)
        for (int i = 0; i <= 30; i++) {
            if(i%5 == 0){   //画粗刻度和刻度值
                paint.setStrokeWidth(2);
                paint.setAlpha(0x70);
                canvas.drawLine(0, -radius-sweepInWidth/2,0, -radius+sweepInWidth/2+1, paint);
//                drawText(canvas,i*maxNum/30+"",paint);
            }else {         //画细刻度
                paint.setStrokeWidth(1);
                paint.setAlpha(0x50);
                canvas.drawLine(0,-radius-sweepInWidth/2,0, -radius+sweepInWidth/2, paint);
            }
//            if(i==3 || i==9 || i==15 || i==21 || i==27){  //画刻度区间文字
//                paint.setStrokeWidth(dp2px(2));
//                paint.setAlpha(0x50);
//                drawText(canvas,text[(i-3)/6], paint);
//            }
            canvas.rotate(angle); //逆时针
        }
        canvas.restore();
    }



    private void drawRound(Canvas canvas) {
        canvas.save();
        //内圆
        paint.setAlpha(0x40);
        paint.setStrokeWidth(sweepInWidth);
        RectF rectf = new RectF(-radius,-radius,radius,radius);
        canvas.drawArc(rectf,startAngle,sweepAngle,false,paint);

        paint_5.setColor(Color.BLUE);
        canvas.drawCircle( 0, 0, 5,paint_5);


        paint_6.setStrokeWidth(1);
        paint_6.setColor(Color.BLUE);

        float toX =  (float) Math.cos(Math.toRadians(plusAngle+180)) * 75;
        float toY =  (float) Math.sin(Math.toRadians(plusAngle+180)) * 75;

        canvas.drawLine(0,0, toX, toY, paint_6);

    }





    /**
     * 初始化属性动画
     */
    private void initAnim() {
        outerProgressAnim = new ValueAnimator();
        outerProgressAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                updateOuterProgressValue(value);
                outerProgressValue =  value;
            }
        });

    }

    private void updateOuterProgressValue(float value) {
        setOuterAnimAngle(value);
    }

    public void setOuterAnimAngle(float value){
        this.plusAngle = (180 * value) / max;
        invalidate();//重新绘制图形
    }

    public void startAnim(float value) {
        this.value = value;
        if (value <= max && value >= min) {
            updateAnimValue();
        }
    }
    private void updateAnimValue() {

        outerProgressAnim.setFloatValues(outerProgressValue, value);
        outerProgressAnim.setDuration(1350);
        outerProgressAnim.start();

    }
}
