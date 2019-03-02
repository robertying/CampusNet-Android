package io.robertying.campusnet;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static com.github.mikephil.charting.components.XAxis.XAxisPosition;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setSwipeToRefreshIndicator();
        setChart();

        System.loadLibrary("tunet");
    }

    private void setSwipeToRefreshIndicator() {
        // set indicator color
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));

        // set indicator position
        swipeRefreshLayout.setProgressViewOffset(false,
                swipeRefreshLayout.getProgressViewStartOffset(),
                swipeRefreshLayout.getProgressViewEndOffset() - 48);
    }

    private void setChart() {
        int primaryColor = getResources().getColor(R.color.colorPrimary);
        int dividerColor = getResources().getColor(R.color.divider);

        LineChart chart = findViewById(R.id.chart);

        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);

        Description emptyDescription = new Description();
        emptyDescription.setText("");
        chart.setDescription(emptyDescription);
        chart.setNoDataText(getString(R.string.no_data_text));

        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMaximum(31);
        xAxis.setAxisMinimum(1);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(primaryColor);

        YAxis yAxis = chart.getAxisLeft();
        // yAxis.addLimitLine();
        yAxis.setDrawAxisLine(false);
        yAxis.setAxisMinimum(0);
        yAxis.setAxisLineWidth(1f);
        yAxis.setGridLineWidth(1f);
        yAxis.setTextColor(primaryColor);
        yAxis.setGridColor(dividerColor);
        yAxis.setAxisLineColor(dividerColor);

        List<Entry> entries = new ArrayList<Entry>();
        for (int i = 1; i <= 31; i += 5) {
            entries.add(new Entry(i, i * i));
        }
        LineDataSet dataSet = new LineDataSet(entries, "Usage");

        dataSet.setFillDrawable(getDrawable(R.drawable.linear_gradient_primary_color));
        dataSet.setDrawFilled(true);
        dataSet.setValueTextColor(primaryColor);
        dataSet.setValueTextSize(14);
        dataSet.setCircleColor(primaryColor);
        dataSet.setColor(primaryColor);

        chart.setData(new LineData(dataSet));
        chart.animateX(500);
    }
}
