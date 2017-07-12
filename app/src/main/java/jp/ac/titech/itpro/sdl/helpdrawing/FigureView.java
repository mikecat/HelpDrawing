package jp.ac.titech.itpro.sdl.helpdrawing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by kota on 2017/07/13.
 */

public class FigureView extends View {
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
        Paint paint = new Paint();
        paint.setARGB(255, 255, 0, 0);
        paint.setStrokeWidth(5);
        canvas.drawLine(0, 0, 500, 500, paint);
    }
}
