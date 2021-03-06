package org.telegram.ui.Components;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.support.annotation.Keep;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

public class Switch2 extends View {
    private static Bitmap drawBitmap;
    private boolean attachedToWindow;
    private ObjectAnimator checkAnimator;
    private boolean isChecked;
    private Paint paint;
    private Paint paint2;
    private float progress;
    private RectF rectF = new RectF();
    private Paint shadowPaint;

    public Switch2(Context context) {
        super(context);
        if (drawBitmap == null || drawBitmap.getWidth() != AndroidUtilities.dp(24.0f)) {
            drawBitmap = Bitmap.createBitmap(AndroidUtilities.dp(24.0f), AndroidUtilities.dp(24.0f), Config.ARGB_8888);
            Canvas canvas = new Canvas(drawBitmap);
            Paint paint = new Paint(1);
            paint.setShadowLayer((float) AndroidUtilities.dp(2.0f), 0.0f, 0.0f, Theme.ACTION_BAR_PHOTO_VIEWER_COLOR);
            canvas.drawCircle((float) AndroidUtilities.dp(12.0f), (float) AndroidUtilities.dp(12.0f), (float) AndroidUtilities.dp(9.0f), paint);
            try {
                canvas.setBitmap(null);
            } catch (Exception e) {
            }
        }
        this.shadowPaint = new Paint(2);
        this.paint = new Paint(1);
        this.paint2 = new Paint(1);
        this.paint2.setStyle(Style.STROKE);
        this.paint2.setStrokeCap(Cap.ROUND);
        this.paint2.setStrokeWidth((float) AndroidUtilities.dp(2.0f));
    }

    @Keep
    public void setProgress(float value) {
        if (this.progress != value) {
            this.progress = value;
            invalidate();
        }
    }

    public float getProgress() {
        return this.progress;
    }

    private void cancelCheckAnimator() {
        if (this.checkAnimator != null) {
            this.checkAnimator.cancel();
        }
    }

    private void animateToCheckedState(boolean newCheckedState) {
        String str = NotificationCompat.CATEGORY_PROGRESS;
        float[] fArr = new float[1];
        fArr[0] = newCheckedState ? 1.0f : 0.0f;
        this.checkAnimator = ObjectAnimator.ofFloat(this, str, fArr);
        this.checkAnimator.setDuration(250);
        this.checkAnimator.start();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.attachedToWindow = true;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.attachedToWindow = false;
    }

    public void setChecked(boolean checked, boolean animated) {
        if (checked != this.isChecked) {
            this.isChecked = checked;
            if (this.attachedToWindow && animated) {
                animateToCheckedState(checked);
                return;
            }
            cancelCheckAnimator();
            setProgress(checked ? 1.0f : 0.0f);
        }
    }

    public boolean isChecked() {
        return this.isChecked;
    }

    protected void onDraw(Canvas canvas) {
        if (getVisibility() == 0) {
            int width = AndroidUtilities.dp(36.0f);
            int thumb = AndroidUtilities.dp(20.0f);
            int x = (getMeasuredWidth() - width) / 2;
            int y = (getMeasuredHeight() - AndroidUtilities.dp(14.0f)) / 2;
            int tx = (((int) (((float) (width - AndroidUtilities.dp(14.0f))) * this.progress)) + x) + AndroidUtilities.dp(7.0f);
            int ty = getMeasuredHeight() / 2;
            int color1 = Theme.getColor(Theme.key_switch2Track);
            int color2 = Theme.getColor(Theme.key_switch2TrackChecked);
            int r1 = Color.red(color1);
            int r2 = Color.red(color2);
            int g1 = Color.green(color1);
            int g2 = Color.green(color2);
            int b1 = Color.blue(color1);
            int b2 = Color.blue(color2);
            int a1 = Color.alpha(color1);
            int alpha = (int) (((float) a1) + (((float) (Color.alpha(color2) - a1)) * this.progress));
            this.paint.setColor(((((alpha & 255) << 24) | ((((int) (((float) r1) + (((float) (r2 - r1)) * this.progress))) & 255) << 16)) | ((((int) (((float) g1) + (((float) (g2 - g1)) * this.progress))) & 255) << 8)) | (((int) (((float) b1) + (((float) (b2 - b1)) * this.progress))) & 255));
            this.rectF.set((float) x, (float) y, (float) (x + width), (float) (AndroidUtilities.dp(14.0f) + y));
            canvas.drawRoundRect(this.rectF, (float) AndroidUtilities.dp(7.0f), (float) AndroidUtilities.dp(7.0f), this.paint);
            color1 = Theme.getColor(Theme.key_switch2Thumb);
            color2 = Theme.getColor(Theme.key_switch2ThumbChecked);
            r1 = Color.red(color1);
            r2 = Color.red(color2);
            g1 = Color.green(color1);
            g2 = Color.green(color2);
            b1 = Color.blue(color1);
            b2 = Color.blue(color2);
            a1 = Color.alpha(color1);
            alpha = (int) (((float) a1) + (((float) (Color.alpha(color2) - a1)) * this.progress));
            this.paint.setColor(((((alpha & 255) << 24) | ((((int) (((float) r1) + (((float) (r2 - r1)) * this.progress))) & 255) << 16)) | ((((int) (((float) g1) + (((float) (g2 - g1)) * this.progress))) & 255) << 8)) | (((int) (((float) b1) + (((float) (b2 - b1)) * this.progress))) & 255));
            this.shadowPaint.setAlpha(alpha);
            canvas.drawBitmap(drawBitmap, (float) (tx - AndroidUtilities.dp(12.0f)), (float) (ty - AndroidUtilities.dp(11.0f)), this.shadowPaint);
            canvas.drawCircle((float) tx, (float) ty, (float) AndroidUtilities.dp(10.0f), this.paint);
            this.paint2.setColor(Theme.getColor(Theme.key_switch2Check));
            tx = (int) (((float) tx) - (((float) AndroidUtilities.dp(10.8f)) - (((float) AndroidUtilities.dp(1.3f)) * this.progress)));
            ty = (int) (((float) ty) - (((float) AndroidUtilities.dp(8.5f)) - (((float) AndroidUtilities.dp(0.5f)) * this.progress)));
            int startX2 = ((int) AndroidUtilities.dpf2(4.6f)) + tx;
            int startY2 = (int) (AndroidUtilities.dpf2(9.5f) + ((float) ty));
            int startX = ((int) AndroidUtilities.dpf2(7.5f)) + tx;
            int startY = ((int) AndroidUtilities.dpf2(5.4f)) + ty;
            int endX = startX + AndroidUtilities.dp(7.0f);
            int endY = startY + AndroidUtilities.dp(7.0f);
            Canvas canvas2 = canvas;
            canvas2.drawLine((float) ((int) (((float) startX) + (((float) (startX2 - startX)) * this.progress))), (float) ((int) (((float) startY) + (((float) (startY2 - startY)) * this.progress))), (float) ((int) (((float) endX) + (((float) ((startX2 + AndroidUtilities.dp(2.0f)) - endX)) * this.progress))), (float) ((int) (((float) endY) + (((float) ((startY2 + AndroidUtilities.dp(2.0f)) - endY)) * this.progress))), this.paint2);
            startX = ((int) AndroidUtilities.dpf2(7.5f)) + tx;
            startY = ((int) AndroidUtilities.dpf2(12.5f)) + ty;
            canvas2 = canvas;
            canvas2.drawLine((float) startX, (float) startY, (float) (startX + AndroidUtilities.dp(7.0f)), (float) (startY - AndroidUtilities.dp(7.0f)), this.paint2);
        }
    }
}
