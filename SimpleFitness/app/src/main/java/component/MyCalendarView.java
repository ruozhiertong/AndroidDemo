package component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.Calendar;


public class MyCalendarView extends View {
    private Paint paint;
    public Calendar calendar;
    private int dayWidth;
    private int selectedDay = -1;

    public MyCalendarView(Context context) {
        super(context);
        init();
    }

    public MyCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.CENTER);
        calendar = Calendar.getInstance();
    }

    public void setSelectedDay(int day) {
        selectedDay = day;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);

        // Calculate day width based on the total width and number of days in a week
        dayWidth = width / 7;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int dayHeight = getHeight() / 7; // Divide height by 7 days

        for (int day = 1; day <= 31; day++) {
            int x = (day - 1) % 7 * dayWidth;
            int y = ((day - 1) / 7 + 1) * dayHeight;

            // Check if the day is selected
            boolean isSelected = selectedDay == day;
            paint.setColor(isSelected ? 0xFF03A9F4 : 0xFF000000); // Blue for selected, black for others
            paint.setStyle(isSelected ? Paint.Style.FILL : Paint.Style.STROKE);

            // Draw the day number
            canvas.drawCircle(x + dayWidth / 2, y + dayHeight / 2, dayWidth / 2 - 10, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFFFFFFFF); // White text color
            canvas.drawText(String.valueOf(day), x + dayWidth / 2, y + dayHeight / 2, paint);
        }
    }
}