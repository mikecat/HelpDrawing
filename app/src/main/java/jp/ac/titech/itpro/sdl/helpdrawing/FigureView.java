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
        paint.setARGB(255, 0, 128, 0);
        paint.setStrokeWidth(2);
        canvas.drawLine(figureX[0], figureY[0], figureX[1], figureY[1], paint);
        canvas.drawLine(figureX[2], figureY[2], figureX[3], figureY[3], paint);
        paint.setARGB(255, 255, 0, 0);
        paint.setStrokeWidth(5);
        float px = figureX[0], py = figureY[0];
        final int DIV_NUM = 200;
        for (int i = 1; i <= DIV_NUM; i++) {
            float d = (float)(DIV_NUM - i) / DIV_NUM;
            float id = (float)i / DIV_NUM;
            float x4, x5, x6, x7, x8, x9;
            float y4, y5, y6, y7, y8, y9;
            x4 = d * figureX[0] + id * figureX[1]; y4 = d * figureY[0] + id * figureY[1];
            x5 = d * figureX[1] + id * figureX[2]; y5 = d * figureY[1] + id * figureY[2];
            x6 = d * figureX[2] + id * figureX[3]; y6 = d * figureY[2] + id * figureY[3];
            x7 = d * x4 + id * x5; y7 = d * y4 + id * y5;
            x8 = d * x5 + id * x6; y8 = d * y5 + id * y6;
            x9 = d * x7 + id * x8; y9 = d * y7 + id * y8;
            canvas.drawLine(px, py, x9, y9, paint);
            px = x9;
            py = y9;
        }
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
