package org.telegram.ui.Cells;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import turbogram.Utilities.TurboUtils;

public class TextDetailSettingsCell extends FrameLayout {
    private boolean multiline;
    private boolean needDivider;
    private TextView textView;
    private TextView valueTextView;

    public TextDetailSettingsCell(Context context) {
        int i;
        int i2;
        int i3 = 5;
        super(context);
        this.textView = new TextView(context);
        this.textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        this.textView.setTextSize(1, 16.0f);
        this.textView.setLines(1);
        this.textView.setMaxLines(1);
        this.textView.setSingleLine(true);
        this.textView.setEllipsize(TruncateAt.END);
        this.textView.setGravity((LocaleController.isRTL ? 5 : 3) | 16);
        View view = this.textView;
        if (LocaleController.isRTL) {
            i = 5;
        } else {
            i = 3;
        }
        addView(view, LayoutHelper.createFrame(-2, -2.0f, i | 48, 17.0f, 10.0f, 17.0f, 0.0f));
        this.valueTextView = new TextView(context);
        this.valueTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        this.valueTextView.setTextSize(1, 13.0f);
        TextView textView = this.valueTextView;
        if (LocaleController.isRTL) {
            i2 = 5;
        } else {
            i2 = 3;
        }
        textView.setGravity(i2);
        this.valueTextView.setLines(1);
        this.valueTextView.setMaxLines(1);
        this.valueTextView.setSingleLine(true);
        this.valueTextView.setPadding(0, 0, 0, 0);
        view = this.valueTextView;
        if (!LocaleController.isRTL) {
            i3 = 3;
        }
        addView(view, LayoutHelper.createFrame(-2, -2.0f, i3 | 48, 17.0f, 35.0f, 17.0f, 0.0f));
        this.textView.setTypeface(TurboUtils.getTurboTypeFace());
        this.valueTextView.setTypeface(TurboUtils.getTurboTypeFace());
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i = 0;
        if (this.multiline) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), 1073741824), MeasureSpec.makeMeasureSpec(0, 0));
            return;
        }
        int makeMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), 1073741824);
        int dp = AndroidUtilities.dp(64.0f);
        if (this.needDivider) {
            i = 1;
        }
        super.onMeasure(makeMeasureSpec, MeasureSpec.makeMeasureSpec(i + dp, 1073741824));
    }

    public TextView getTextView() {
        return this.textView;
    }

    public TextView getValueTextView() {
        return this.valueTextView;
    }

    public void setMultilineDetail(boolean value) {
        this.multiline = value;
        if (value) {
            this.valueTextView.setLines(0);
            this.valueTextView.setMaxLines(0);
            this.valueTextView.setSingleLine(false);
            this.valueTextView.setPadding(0, 0, 0, AndroidUtilities.dp(12.0f));
            return;
        }
        this.valueTextView.setLines(1);
        this.valueTextView.setMaxLines(1);
        this.valueTextView.setSingleLine(true);
        this.valueTextView.setPadding(0, 0, 0, 0);
    }

    public void setTextAndValue(String text, CharSequence value, boolean divider) {
        this.textView.setText(text);
        this.valueTextView.setText(value);
        this.needDivider = divider;
        setWillNotDraw(!divider);
    }

    public void setValue(CharSequence value) {
        this.valueTextView.setText(value);
    }

    public void setTextWithEmojiAndValue(String text, CharSequence value, boolean divider) {
        boolean z = false;
        this.textView.setText(Emoji.replaceEmoji(text, this.textView.getPaint().getFontMetricsInt(), AndroidUtilities.dp(14.0f), false));
        this.valueTextView.setText(value);
        this.needDivider = divider;
        if (!divider) {
            z = true;
        }
        setWillNotDraw(z);
    }

    public void invalidate() {
        super.invalidate();
        this.textView.invalidate();
    }

    protected void onDraw(Canvas canvas) {
        if (this.needDivider) {
            canvas.drawLine((float) getPaddingLeft(), (float) (getHeight() - 1), (float) (getWidth() - getPaddingRight()), (float) (getHeight() - 1), Theme.dividerPaint);
        }
    }

    public void setEnabled(boolean value, ArrayList<Animator> animators) {
        float f = 1.0f;
        setEnabled(value);
        TextView textView;
        float f2;
        if (animators != null) {
            textView = this.textView;
            String str = "alpha";
            float[] fArr = new float[1];
            fArr[0] = value ? 1.0f : 0.5f;
            animators.add(ObjectAnimator.ofFloat(textView, str, fArr));
            if (this.textView.getVisibility() == 0) {
                textView = this.textView;
                str = "alpha";
                fArr = new float[1];
                if (value) {
                    f2 = 1.0f;
                } else {
                    f2 = 0.5f;
                }
                fArr[0] = f2;
                animators.add(ObjectAnimator.ofFloat(textView, str, fArr));
            }
            if (this.valueTextView.getVisibility() == 0) {
                TextView textView2 = this.valueTextView;
                String str2 = "alpha";
                float[] fArr2 = new float[1];
                if (!value) {
                    f = 0.5f;
                }
                fArr2[0] = f;
                animators.add(ObjectAnimator.ofFloat(textView2, str2, fArr2));
                return;
            }
            return;
        }
        this.textView.setAlpha(value ? 1.0f : 0.5f);
        if (this.textView.getVisibility() == 0) {
            textView = this.textView;
            if (value) {
                f2 = 1.0f;
            } else {
                f2 = 0.5f;
            }
            textView.setAlpha(f2);
        }
        if (this.valueTextView.getVisibility() == 0) {
            textView2 = this.valueTextView;
            if (!value) {
                f = 0.5f;
            }
            textView2.setAlpha(f);
        }
    }
}
