package io.robertying.campusnet.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import io.robertying.campusnet.R;
import io.robertying.campusnet.custom.MySnackbar;
import io.robertying.campusnet.helper.TunetHelper.ResponseType;
import io.robertying.campusnet.helper.UseregHelper;

import static android.content.Context.MODE_PRIVATE;

public class LoginPageFragment extends Fragment {

    private Button nextButton;
    private TextInputEditText usernameTextField;
    private TextInputEditText passwordTextField;
    private ProgressBar progressBar;

    private ViewPager viewPager;
    private Context context;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_page, container, false);

        viewPager = (ViewPager) container;
        nextButton = view.findViewById(R.id.next_button);
        usernameTextField = view.findViewById(R.id.username_edit_text);
        passwordTextField = view.findViewById(R.id.password_edit_text);
        progressBar = view.findViewById(R.id.login_progress_bar);

        UseregHelper.init(context);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextButton.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);

                final Editable usernameText = usernameTextField.getText();
                final Editable passwordText = passwordTextField.getText();
                if (TextUtils.isEmpty(usernameText) || TextUtils.isEmpty(passwordText)) {
                    progressBar.setVisibility(View.GONE);
                    nextButton.setEnabled(true);
                    MySnackbar.make(context,
                            nextButton,
                            nextButton,
                            context.getResources().getString(R.string.missing_credentials),
                            Snackbar.LENGTH_LONG)
                            .show();

                    return;
                }

                new LoginTask().execute(usernameText.toString(), passwordText.toString());
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        UseregHelper.cleanup();
    }

    private void saveCredentials(String username, String password) {
        // there seems to be no proper way to store plain passwords
        // when a rooted device is concerned
        // so be it

        context.getSharedPreferences("Credentials", MODE_PRIVATE)
                .edit()
                .putString("username", username)
                .putString("password", password)
                .apply();
    }

    private class LoginResponse {
        ResponseType response;
        String username;
        String password;
    }

    private class LoginTask extends AsyncTask<String, Void, LoginResponse> {
        @NonNull
        @Override
        protected LoginResponse doInBackground(String... credentials) {
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.response = UseregHelper.
                    useregLogin(credentials[0], credentials[1]);
            loginResponse.username = credentials[0];
            loginResponse.password = credentials[1];
            return loginResponse;
        }

        @Override
        protected void onPostExecute(@NonNull LoginResponse loginResponse) {
            progressBar.setVisibility(View.GONE);
            nextButton.setEnabled(true);

            switch (loginResponse.response) {
                case SUCCESS:
                    saveCredentials(loginResponse.username, loginResponse.password);
                    usernameTextField.clearFocus();
                    passwordTextField.clearFocus();
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                    break;
                case WRONG_CREDENTIAL:
                    MySnackbar.make(context,
                            nextButton,
                            nextButton,
                            context.getResources().getString(R.string.wrong_credentials),
                            Snackbar.LENGTH_LONG)
                            .show();
                    break;
                case UNKNOWN_ERR:
                    MySnackbar.make(context,
                            nextButton,
                            nextButton,
                            context.getResources().getString(R.string.unknown_error),
                            Snackbar.LENGTH_LONG)
                            .show();
                    break;
            }
        }
    }
}
