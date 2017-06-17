package wuxin.enroll.prediction.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

public class CircleProgress extends ImageView {
    private int width,height,r,x1,y1,x2,y2,startAngle=0,i;
    private Paint pPaint;
    private RectF rect1;
    public CircleProgress(Context context) {
        this(context,null);
    }
    public CircleProgress(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }
    public CircleProgress(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        pPaint = new Paint();
        pPaint.setStyle(Paint.Style.STROKE);
        pPaint.setStrokeWidth(5);
        pPaint.setAntiAlias(true);
    }
    @SuppressLint("DrawAllocation")
	@Override
    protected void onDraw(Canvas canvas) {
        width=getWidth();
        height=getHeight();
        r = Math.min(width, height);
        x1=(width-r/2)/2;
        x2=(width+r/2)/2;
        y1=(height-r/2)/2;
        y2=(height+r/2)/2;
        rect1 = new RectF(x1,y1,x2,y2);
        for(i=0;i<255;i+=5) {
            pPaint.setARGB(255 - i, 255, 10, 50);
            canvas.drawArc(rect1, 180 - i + startAngle, 5, false, pPaint);
        }
        startAngle += 5;
        if (startAngle > 360000) startAngle = 0;
        super.onDraw(canvas);
        invalidate();
    }
}
