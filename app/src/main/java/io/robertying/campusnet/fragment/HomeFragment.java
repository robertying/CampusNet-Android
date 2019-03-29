package io.robertying.campusnet.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import io.robertying.campusnet.R;
import io.robertying.campusnet.custom.MySnackbar;
import io.robertying.campusnet.helper.CredentialHelper;
import io.robertying.campusnet.helper.TunetHelper;
import io.robertying.campusnet.helper.TunetHelper.ResponseType;
import io.robertying.campusnet.helper.UseregHelper;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment implements DeviceFragment.DeviceFragmentListener {

    private final String CLASS_NAME = getClass().getSimpleName();
    private final Fragment thisFragment = this;
    private String sessionsString;
    private String[] credentials;

    private FragmentActivity activity;
    private View view;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BottomNavigationView navigationView;
    private TextView usageTextView;
    private TextView balanceTextView;
    private TextView accountTextView;
    private TextView networkTextView;
    private TextView devicesTextView;
    private AppCompatImageButton devicesDetailButton;
    private LineChart chart;

    private int primaryColor;
    private int dividerColor;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = getActivity();
        credentials = CredentialHelper.getCredentials(activity);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        navigationView = activity.findViewById(R.id.navigation);
        usageTextView = view.findViewById(R.id.usage_number_text_view);
        balanceTextView = view.findViewById(R.id.balance_number_text_view);
        accountTextView = view.findViewById(R.id.account_name_text_view);
        networkTextView = view.findViewById(R.id.network_name_text_view);
        devicesTextView = view.findViewById(R.id.devices_number_textView);
        devicesDetailButton = view.findViewById(R.id.devices_detail_button);
        chart = view.findViewById(R.id.chart);
        primaryColor = getResources().getColor(R.color.colorPrimary);
        dividerColor = getResources().getColor(R.color.divider);

        setSwipeToRefreshIndicator();
        setInfo();
        setChart();

        TunetHelper.init(activity);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        update();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        TunetHelper.cleanup();
    }

    @Override
    public void onDialogClose(int dropped) {
        showSnackbar(getResources().getString(R.string.drop_session_success, dropped));
        new GetSessionsTask().execute(credentials);
    }

    private void setSwipeToRefreshIndicator() {
        // set indicator color
        swipeRefreshLayout.setColorSchemeColors(primaryColor);

        // set indicator position
        swipeRefreshLayout.setProgressViewOffset(false,
                swipeRefreshLayout.getProgressViewStartOffset(),
                swipeRefreshLayout.getProgressViewEndOffset() - 150);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                update();
            }
        });
    }

    private void setInfo() {
        devicesDetailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sessionsString != null && !sessionsString.isEmpty()) {
                    DialogFragment fragment = DeviceFragment.newInstance(sessionsString);
                    fragment.setTargetFragment(thisFragment, 0);
                    fragment.show(getFragmentManager(), "DevicesDetail");
                }
            }
        });

        SharedPreferences preferences = activity
                .getApplicationContext()
                .getSharedPreferences("AppInfo", MODE_PRIVATE);

        usageTextView.setText(preferences.getString("Usage", "0.00"));
        balanceTextView.setText(preferences.getString("Balance", "0.00"));
        if (credentials != null)
            accountTextView.setText(credentials[0]);
    }

    private void setChart() {
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

    private void update() {
        Log.i(CLASS_NAME, "Updating info");
        swipeRefreshLayout.setRefreshing(true);
        devicesDetailButton.animate().alpha(0f);

        new LoginTask().execute(credentials);
        getNetwork();
        new GetSessionsTask().execute(credentials);
        new GetUsageDetailTask().execute(credentials);
    }

    private void getNetwork() {
        networkTextView.post(new Runnable() {
            @Override
            public void run() {
                WifiManager wifiManager = (WifiManager)
                        activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                String ssid = wifiManager.getConnectionInfo().getSSID();
                networkTextView.setText(ssid.replaceAll("^\"|\"$", ""));
            }
        });
    }

    @Nullable
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

            if (Float.compare(usageInGB, 0f) == 0)
                return null;
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
        SharedPreferences preferences = activity
                .getApplicationContext()
                .getSharedPreferences("AppInfo", MODE_PRIVATE);
        preferences.edit()
                .putString("UsageDetail", json)
                .apply();
    }

    private List<Entry> loadUsageDetail() {
        SharedPreferences preferences = activity
                .getApplicationContext()
                .getSharedPreferences("AppInfo", MODE_PRIVATE);
        String json = preferences.getString("UsageDetail", null);
        return new Gson().fromJson(json, new TypeToken<List<Entry>>() {
        }.getType());
    }

    private void setChartData(List<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, "Usage");
        dataSet.setFillDrawable(activity.getDrawable(R.drawable.linear_gradient_primary_color));
        dataSet.setDrawFilled(true);
        dataSet.setDrawValues(false);
        dataSet.setValueTextColor(primaryColor);
        dataSet.setValueTextSize(12);
        dataSet.setCircleColor(primaryColor);
        dataSet.setColor(primaryColor);

        chart.clear();
        chart.setData(new LineData(dataSet));
        chart.invalidate();
    }

    private void showSnackbar(CharSequence text) {
        MySnackbar.make(activity,
                swipeRefreshLayout,
                navigationView,
                text,
                Snackbar.LENGTH_LONG)
                .show();
    }

    private class GetSessionsTask extends AsyncTask<String, Void, JsonArray> {
        @Override
        protected JsonArray doInBackground(String... credentials) {
            JsonArray sessions = null;
            if (credentials != null) {
                sessionsString = UseregHelper.getSessions(credentials[0], credentials[1]);
                try {
                    sessions = new JsonParser().parse(sessionsString).getAsJsonArray();
                } catch (Exception e) {
                    Log.e(CLASS_NAME, e.getMessage());
                }
            }
            return sessions;
        }

        @Override
        protected void onPostExecute(JsonArray sessions) {
            if (sessions != null)
                devicesTextView.setText(Integer.toString(sessions.size()));
            devicesDetailButton.animate().alpha(1f);
        }
    }

    private class LoginResponse {
        ResponseType response;
        float usage;
        float balance;
    }

    private class LoginTask extends AsyncTask<String, Void, LoginResponse> {
        @NonNull
        @Override
        protected LoginResponse doInBackground(String... credentials) {
            LoginResponse loginResponse = new LoginResponse();
            String username = credentials[0];
            String password = credentials[1];

            if (username == null || password == null) {
                loginResponse.response = ResponseType.WRONG_CREDENTIAL;
                loginResponse.usage = 0;
                loginResponse.balance = 0;
            } else {
                WifiManager wifiManager = (WifiManager)
                        activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                String ssid = wifiManager.getConnectionInfo().getSSID();
                ssid = ssid.replaceAll("^\"|\"$", "");

                loginResponse.response = ResponseType.UNKNOWN_ERR;

                if (ssid.equals("Tsinghua-5G") || ssid.equals("Tsinghua")) {
                    Log.i(CLASS_NAME, "Net login to " + ssid);
                    loginResponse.response = TunetHelper.netLogin(username, password);
                }

                if (ssid.equals("Tsinghua-IPv4") || ssid.equals("122A-5G")) {
                    Log.i(CLASS_NAME, "Auth login to " + ssid);
                    loginResponse.response = TunetHelper.auth4Login(username, password);
                    TunetHelper.auth6Login(username, password);
                }

                loginResponse.usage = UseregHelper.getUsage();
                loginResponse.balance = UseregHelper.getBalance();
            }

            return loginResponse;
        }

        @Override
        protected void onPostExecute(@NonNull LoginResponse loginResponse) {
            Activity activity = getActivity();
            if (activity != null && isAdded()) {
                SharedPreferences preferences = activity
                        .getApplicationContext()
                        .getSharedPreferences("AppInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();

                if (loginResponse.usage != 0) {
                    String usageText = String.format("%.2f", loginResponse.usage / 10e8f);
                    TextView usageTextView = view.findViewById(R.id.usage_number_text_view);
                    usageTextView.setText(usageText);

                    editor.putString("Usage", usageText);
                }
                if (loginResponse.balance != 0) {
                    String balanceText = "¥ " + String.format("%.2f", loginResponse.balance);
                    TextView balanceTextView = view.findViewById(R.id.balance_number_text_view);
                    balanceTextView.setText(balanceText);

                    editor.putString("Balance", balanceText);
                }

                editor.apply();

                switch (loginResponse.response) {
                    case OUT_OF_BALANCE:
                        showSnackbar(getResources().getString(R.string.out_of_balance));
                        break;
                    case ALREADY_ONLINE:
                    case SUCCESS:
                        showSnackbar(getResources().getString(R.string.login_success));
                        break;
                    case WRONG_CREDENTIAL:
                        showSnackbar(getResources().getString(R.string.wrong_credentials));
                        break;
                    case UNKNOWN_ERR:
                        showSnackbar(getResources().getString(R.string.unknown_error));
                        break;
                }
            }

            Log.i(CLASS_NAME, loginResponse.response.toString());

            swipeRefreshLayout.setRefreshing(false);
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
            if (entries != null)
                setChartData(entries);
        }
    }
}
