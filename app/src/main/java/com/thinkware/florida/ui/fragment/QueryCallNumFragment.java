package com.thinkware.florida.ui.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thinkware.florida.R;

import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class QueryCallNumFragment extends BaseFragment {
    View root, btnInqDay;
    View btnStartDayUp, btnStartDayDown, btnStartMonthUp, btnStartMonthDown;
    View btnEndDayUp, btnEndDayDown, btnEndMonthUp, btnEndMonthDown;
    TextView txtStartMonth, txtStartDay, txtEndMonth, txtEndDay;
    TextView txtStatus;
    int year, startMonth, startDay, endMonth, endDay;

    View.OnClickListener onInquiryButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            processInqueryButton(view.getId());
        }
    };

    View.OnClickListener onUpDownButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            processUpDownButton(view);
        }
    };


    public QueryCallNumFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_query_call_num, container, false);

        btnInqDay = root.findViewById(R.id.btn_day);
        btnInqDay.setOnClickListener(onInquiryButtonClick);

        View startLayout = root.findViewById(R.id.layout_start);
        View endLayout = root.findViewById(R.id.layout_end);
        txtStartMonth = (TextView)startLayout.findViewById(R.id.txt_month);
        txtStartDay = (TextView)startLayout.findViewById(R.id.txt_day);
        txtEndMonth = (TextView)endLayout.findViewById(R.id.txt_month);
        txtEndDay = (TextView)endLayout.findViewById(R.id.txt_day);
        btnStartDayDown = startLayout.findViewById(R.id.btn_daydown);
        btnStartDayUp = startLayout.findViewById(R.id.btn_dayup);
        btnStartMonthDown = startLayout.findViewById(R.id.btn_monthdown);
        btnStartMonthUp = startLayout.findViewById(R.id.btn_monthup);
        btnEndDayDown = endLayout.findViewById(R.id.btn_daydown);
        btnEndDayUp = endLayout.findViewById(R.id.btn_dayup);
        btnEndMonthDown = endLayout.findViewById(R.id.btn_monthdown);
        btnEndMonthUp = endLayout.findViewById(R.id.btn_monthup);
        btnStartDayDown.setOnClickListener(onUpDownButtonClick);
        btnStartDayUp.setOnClickListener(onUpDownButtonClick);
        btnStartMonthDown.setOnClickListener(onUpDownButtonClick);
        btnStartMonthUp.setOnClickListener(onUpDownButtonClick);
        btnEndDayDown.setOnClickListener(onUpDownButtonClick);
        btnEndDayUp.setOnClickListener(onUpDownButtonClick);
        btnEndMonthDown.setOnClickListener(onUpDownButtonClick);
        btnEndMonthUp.setOnClickListener(onUpDownButtonClick);

        txtStatus = (TextView)root.findViewById(R.id.txt_status);

        setTodaysDate();
        return root;
    }

    private void setTodaysDate() {
        Calendar calendar = Calendar.getInstance();
        startDay= endDay = calendar.get(Calendar.DAY_OF_MONTH);
        startMonth = endMonth = calendar.get(Calendar.MONTH) + 1;
        year = calendar.get(Calendar.YEAR);
        
        updateDate();
    }

    private void processInqueryButton(int resId) {
        String start = String.format("%02d%02d%02d", year % 100, startMonth, startDay);
        String end = String.format("%02d%02d%02d", year % 100, endMonth, endDay);

        if (start.compareTo(end) > 0) {
            txtStatus.setText("종료일이 시작일보다 빠릅니다");
        } else {
            txtStatus.setText("");
            FragmentUtil.add(getFragmentManager(), QueryCallDetailFragment.newInstance(start, end));
        }
    }

    private void processUpDownButton(View view) {
        int resId = view.getId();
        int parentId = ((View)view.getParent().getParent()).getId();

        processDate(parentId, resId);
        updateDate();
    }

    private void processDate(int parentId, int resId) {
        int month, day;

        if (parentId == R.id.layout_start) {
            month = startMonth;
            day = startDay;
        } else {
            month = endMonth;
            day = endDay;
        }

        switch (resId) {
            case R.id.btn_monthup : {
                month = month >= 12 ? 1 : ++month;
                int maximum = getMaximumDay(month);
                if (day > maximum) day = maximum;
                break;
            }
            case R.id.btn_monthdown : {
                month = month <= 1 ? 12 : --month;
                int maximum = getMaximumDay(month);
                if (day > maximum) day = maximum;
                break;
            }
            case R.id.btn_dayup : {
                int maximum = getMaximumDay(month);
                day = day >= maximum ? 1 : ++day;
                break;
            }
            case R.id.btn_daydown : {
                int maximum = getMaximumDay(month);
                day = day <= 1 ? maximum : --day;
                break;
            }
        }

        if (parentId == R.id.layout_start) {
            startMonth = month;
            startDay = day;
        } else {
            endMonth = month;
            endDay = day;
        }
    }

    private int getMaximumDay(int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month - 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private void updateDate() {
        txtStartMonth.setText(getString(R.string.month, startMonth));
        txtStartDay.setText(getString(R.string.day, startDay));
        txtEndMonth.setText(getString(R.string.month, endMonth));
        txtEndDay.setText(getString(R.string.day, endDay));
    }
}
