package io.robertying.campusnet.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import io.robertying.campusnet.R;
import io.robertying.campusnet.custom.MySnackbar;
import io.robertying.campusnet.helper.CredentialHelper;
import io.robertying.campusnet.helper.TunetHelper;
import io.robertying.campusnet.helper.UseregHelper;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {

    private final String CLASS_NAME = getClass().getSimpleName();

    private SwipeRefreshLayout swipeRefreshLayout;
    private BottomNavigationView navigationView;

    private Context context;
    private FragmentActivity activity;
    private View view;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        activity = getActivity();

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        navigationView = activity.findViewById(R.id.navigation);

        setSwipeToRefreshIndicator();
        setUsageText();
        setChart();

        TunetHelper.init(context);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateChart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        TunetHelper.cleanup();
    }

    private void setSwipeToRefreshIndicator() {
        // set indicator color
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));

        // set indicator position
        swipeRefreshLayout.setProgressViewOffset(false,
                swipeRefreshLayout.getProgressViewStartOffset(),
                swipeRefreshLayout.getProgressViewEndOffset() - 150);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateChart();
            }
        });
    }

    private void setUsageText() {
        TextView usageTextView = view.findViewById(R.id.usageNumberTextView);

        SharedPreferences preferences = context
                .getApplicationContext()
                .getSharedPreferences("AppInfo", MODE_PRIVATE);

        usageTextView.setText(preferences.getString("Usage", "0.00"));
    }

    private void setChart() {
        int primaryColor = context.getApplicationContext()
                .getResources().getColor(R.color.colorPrimary);
        int dividerColor = context.getApplicationContext()
                .getResources().getColor(R.color.divider);

        LineChart chart = view.findViewById(R.id.chart);

        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);

        Description emptyDescription = new Description();
        emptyDescription.setText("");
        chart.setDescription(emptyDescription);
        chart.setNoDataTextColor(primaryColor);
        chart.setNoDataText(getString(R.string.no_data_text));

        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMaximum(31);
        xAxis.setAxisMinimum(1);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(primaryColor);

        YAxis yAxis = chart.getAxisLeft();
        // TODO: yAxis.addLimitLine();
        yAxis.setDrawAxisLine(false);
        yAxis.setAxisMinimum(0);
        yAxis.setAxisLineWidth(1f);
        yAxis.setGridLineWidth(1f);
        yAxis.setTextColor(primaryColor);
        yAxis.setGridColor(dividerColor);
        yAxis.setAxisLineColor(dividerColor);

        List<Entry> loadedData = loadUsageDetail();
        if (loadedData != null) {
            setChartData(loadedData);
        }
    }

    private void updateChart() {
        Log.i(CLASS_NAME, "Updating chart");
        swipeRefreshLayout.setRefreshing(true);

        String[] credentials = CredentialHelper.getCredentials(context);
        new LoginTask().execute(credentials);
        new GetUsageDetailTask().execute(credentials);
    }

    @NonNull
    private List<Entry> getUsageDetail(String username, String password) {
        Calendar now = Calendar.getInstance();
        int y = now.get(Calendar.YEAR);
        int m = now.get(Calendar.MONTH) + 1;
        int d = now.get(Calendar.DAY_OF_MONTH);
        String year = Integer.toString(y);
        String month = m < 10 ? "0" + Integer.toString(m) : Integer.toString(m);
        float sum = 0;
        int i;

        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 0.0f));

        for (i = 5; i <= d; i += 5) {
            String startDay = i - 4 < 10 ? "0" + Integer.toString(i - 4) : Integer.toString(i - 4);
            String endDay = i < 10 ? "0" + Integer.toString(i) : Integer.toString(i);
            String startTime = year + "-" + month + "-" + startDay;
            String endTime = year + "-" + month + "-" + endDay;

            float usageInGB = UseregHelper.getUsageDetail(username,
                    password,
                    startTime,
                    endTime)
                    / 10e8f;
            sum += usageInGB;

            entries.add(new Entry(i, sum));
        }

        if (i - 4 <= d) {
            String startDay = i - 4 < 10 ? "0" + Integer.toString(i - 4) : Integer.toString(i - 4);
            String endDay = d < 10 ? "0" + Integer.toString(d) : Integer.toString(d);
            String startTime = year + "-" + month + "-" + startDay;
            String endTime = year + "-" + month + "-" + endDay;

            float usageInGB = UseregHelper.getUsageDetail(username,
                    password,
                    startTime,
                    endTime)
                    / 10e8f;
            sum += usageInGB;

            entries.add(new Entry(d, sum));
        }

        saveUsageDetail(entries);

        return entries;
    }

    private void saveUsageDetail(List<Entry> entries) {
        String json = new Gson().toJson(entries);
        SharedPreferences preferences = context
                .getApplicationContext()
                .getSharedPreferences("AppInfo", MODE_PRIVATE);
        preferences.edit()
                .putString("UsageDetail", json)
                .apply();
    }

    private List<Entry> loadUsageDetail() {
        SharedPreferences preferences = context
                .getApplicationContext()
                .getSharedPreferences("AppInfo", MODE_PRIVATE);
        String json = preferences.getString("UsageDetail", null);
        return new Gson().fromJson(json, new TypeToken<List<Entry>>() {
        }.getType());
    }

    private void setChartData(List<Entry> entries) {
        int primaryColor = context
                .getApplicationContext()
                .getResources()
                .getColor(R.color.colorPrimary);

        LineDataSet dataSet = new LineDataSet(entries, "Usage");
        dataSet.setFillDrawable(context
                .getApplicationContext()
                .getDrawable(R.drawable.linear_gradient_primary_color));
        dataSet.setDrawFilled(true);
        dataSet.setDrawValues(false);
        dataSet.setValueTextColor(primaryColor);
        dataSet.setValueTextSize(12);
        dataSet.setCircleColor(primaryColor);
        dataSet.setColor(primaryColor);

        LineChart chart = view.findViewById(R.id.chart);

        chart.clear();
        chart.setData(new LineData(dataSet));
        chart.invalidate();
    }

    private void makeSnackbar(CharSequence text) {
        MySnackbar.make(context,
                swipeRefreshLayout,
                navigationView,
                text,
                Snackbar.LENGTH_LONG)
                .show();
    }

    private class LoginResponse {
        TunetHelper.ResponseType response;
        float usage;
    }

    private class LoginTask extends AsyncTask<String, Void, LoginResponse> {
        @NonNull
        @Override
        protected LoginResponse doInBackground(String... credentials) {
            Log.i(CLASS_NAME, "Logging in");
            LoginResponse loginResponse = new LoginResponse();
            String username = credentials[0];
            String password = credentials[1];

            if (username == null || password == null) {
                loginResponse.response = TunetHelper.ResponseType.WRONG_CREDENTIAL;
                loginResponse.usage = 0;
            } else {
                TunetHelper.auth4Login(username, password);
                loginResponse.response = TunetHelper.netLogin(username, password);
                loginResponse.usage = UseregHelper.getUsage();
            }

            return loginResponse;
        }

        @Override
        protected void onPostExecute(@NonNull LoginResponse loginResponse) {
            String text = String.format("%.2f", loginResponse.usage / 10e8f);
            TextView usageTextView = view.findViewById(R.id.usageNumberTextView);
            usageTextView.setText(text);

            SharedPreferences preferences = context
                    .getApplicationContext()
                    .getSharedPreferences("AppInfo", MODE_PRIVATE);
            preferences.edit()
                    .putString("Usage", text)
                    .apply();

            switch (loginResponse.response) {
                case OUT_OF_BALANCE:
                    makeSnackbar(getResources().getString(R.string.out_of_balance));
                    break;
                case ALREADY_ONLINE:
                case SUCCESS:
                    makeSnackbar(getResources().getString(R.string.wrong_credentials));
                    break;
                case WRONG_CREDENTIAL:
                    makeSnackbar(getResources().getString(R.string.wrong_credentials));
                    break;
                case UNKNOWN_ERR:
                    makeSnackbar(getResources().getString(R.string.unknown_error));
                    break;
            }
        }
    }

    private class GetUsageDetailTask extends AsyncTask<String, Void, List<Entry>> {
        @NonNull
        @Override
        protected List<Entry> doInBackground(String... credentials) {
            return getUsageDetail(credentials[0], credentials[1]);
        }

        @Override
        protected void onPostExecute(List<Entry> entries) {
            setChartData(entries);
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
