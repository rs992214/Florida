package com.thinkware.florida.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thinkware.florida.R;

/**
 * Created by Mihoe on 2016-09-22.
 */
public class NumberPadView extends LinearLayout implements View.OnClickListener {
    private final int ThreeByFour = 0;
    private final int FourByThree = 1;

    private View num1, num2, num3, num4, num5, num6, num7, num8, num9, num0, numAC, numC;
    private int padType;
    private TextView textView = null;

    public NumberPadView(Context context) {
        super(context);
        initView();
    }

    public NumberPadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getAttrs(attrs);
        initView();
    }

    public NumberPadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getAttrs(attrs, defStyleAttr);
        initView();
    }

    public void setFocusedTextView(TextView textView) {
        this.textView = textView;
    }

    private void getAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.NumberPadView);
        setTypeArray(typedArray);
    }

    private void getAttrs(AttributeSet attrs, int defStyle) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.NumberPadView, defStyle, 0);
        setTypeArray(typedArray);
    }

    private void setTypeArray(TypedArray typedArray) {
        padType = typedArray.getInt(R.styleable.NumberPadView_numberPadType, 0);
        typedArray.recycle();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (padType == ThreeByFour) {
            addView(inflater.inflate(R.layout.view_pad_three_four, this, false));
        } else if (padType == FourByThree) {
            addView(inflater.inflate(R.layout.view_pad_four_three, this, false));
        }

        num1 = findViewById(R.id.num_1);
        num2 = findViewById(R.id.num_2);
        num3 = findViewById(R.id.num_3);
        num4 = findViewById(R.id.num_4);
        num5 = findViewById(R.id.num_5);
        num6 = findViewById(R.id.num_6);
        num7 = findViewById(R.id.num_7);
        num8 = findViewById(R.id.num_8);
        num9 = findViewById(R.id.num_9);
        num0 = findViewById(R.id.num_0);
        numAC = findViewById(R.id.num_ac);
        numC = findViewById(R.id.num_c);


        num1.setOnClickListener(this);
        num2.setOnClickListener(this);
        num3.setOnClickListener(this);
        num4.setOnClickListener(this);
        num5.setOnClickListener(this);
        num6.setOnClickListener(this);
        num7.setOnClickListener(this);
        num8.setOnClickListener(this);
        num9.setOnClickListener(this);
        num0.setOnClickListener(this);
        numAC.setOnClickListener(this);
        numC.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (textView != null) {
            switch(view.getId()) {
                case R.id.num_1:
                    textView.append("1");
                    break;
                case R.id.num_2:
                    textView.append("2");
                    break;
                case R.id.num_3:
                    textView.append("3");
                    break;
                case R.id.num_4:
                    textView.append("4");
                    break;
                case R.id.num_5:
                    textView.append("5");
                    break;
                case R.id.num_6:
                    textView.append("6");
                    break;
                case R.id.num_7:
                    textView.append("7");
                    break;
                case R.id.num_8:
                    textView.append("8");
                    break;
                case R.id.num_9:
                    textView.append("9");
                    break;
                case R.id.num_0:
                    textView.append("0");
                    break;
                case R.id.num_c: {
                    int length = textView.length();
                    if (length > 0) {
                        CharSequence subText = textView.getText().subSequence(0, length - 1);
                        textView.setText(subText);
                    } else {
                        textView.setText("");
                    }
                }
                break;
                case R.id.num_ac:
                    textView.setText("");
                    break;
            }
        }
    }

}
