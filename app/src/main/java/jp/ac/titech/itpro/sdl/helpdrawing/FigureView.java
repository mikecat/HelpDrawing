package jp.ac.titech.itpro.sdl.helpdrawing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class FigureView extends View {
    private int radius = 0;
    private int[] figureX = new int[4], figureY = new int[4];
    private Paint paint = new Paint();

    public FigureView(Context context) {
        super(context);
    }

    public FigureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FigureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setARGB(255, 255, 0, 0);
        paint.setStrokeWidth(5);
        canvas.drawLine(figureX[0], figureY[0], figureX[1], figureY[1], paint);
        canvas.drawLine(figureX[1], figureY[1], figureX[2], figureY[2], paint);
        canvas.drawLine(figureX[2], figureY[2], figureX[3], figureY[3], paint);
        paint.setARGB(255, 0, 255, 0);
        canvas.drawCircle(figureX[0], figureY[0], radius, paint);
        canvas.drawCircle(figureX[3], figureY[3], radius, paint);
        paint.setARGB(255, 0, 128, 0);
        canvas.drawCircle(figureX[1], figureY[1], radius, paint);
        canvas.drawCircle(figureX[2], figureY[2], radius, paint);
    }

    void setRadius(int radius) {
        this.radius = radius;
    }

    void setCoord(int x, int y, int index) {
        figureX[index] = x;
        figureY[index] = y;
    }
}
