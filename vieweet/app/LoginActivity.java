package com.vieweet.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pixeet.app.camera360.R;
import com.vieweet.app.Network.HttpClient;
import com.vieweet.app.Network.ServerResponse;
import com.vieweet.app.Network.ServiceGenerator;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends Activity {
    Mode mode = Mode.LOGIN;

    @BindView(R.id.bar_progression)
    ProgressBar mProgressBar;

    @BindView(R.id.table_login)
    TableLayout tblLogin;
    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.lbl_login)
    TextView lblLogin;

    @BindView(R.id.btn_signup)
    Button btnSignUp;
    @BindView(R.id.lbl_signup)
    TextView lblSignUp;

    @BindView(R.id.btn_forgot)
    Button btnForgot;
    @BindView(R.id.lbl_forgot)
    TextView lblForgot;

    @BindView(R.id.btn_validate)
    Button btnValidate;

    @BindView(R.id.txt_login)
    EditText txtEmail;
    @BindView(R.id.txt_pwd)
    EditText txtPassword;

    String userID;
    String sessionID;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);

        Typeface tf = Typeface.createFromAsset(getAssets(), Constants.APP_FONT);

        txtEmail.setText("");
        txtEmail.setTypeface(tf);
        txtPassword.setText("");
        txtPassword.setTypeface(tf);

        lblForgot.setTypeface(tf);
        btnForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchScreen(Mode.FORGOT);
            }
        });

        lblLogin.setTypeface(tf);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchScreen(Mode.LOGIN);
            }
        });

        lblSignUp.setTypeface(tf);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });

        btnValidate.setTypeface(tf);
        btnValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mode == Mode.LOGIN) authentication();
                else registration();
            }
        });

        switchScreen(mode);
    }

    private void closeKeyboards() {
        closeKeyboard(txtEmail);
        closeKeyboard(txtPassword);
    }

    private void closeKeyboard(EditText ib) {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        assert in != null;
        in.hideSoftInputFromWindow(ib.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void switchScreen(Mode m) {
        mode = m;
        if (mode == Mode.SIGNUP) {
            btnValidate.setText("SIGN UP");
            btnLogin.setVisibility(View.VISIBLE);
            lblLogin.setVisibility(View.VISIBLE);
            btnSignUp.setVisibility(View.GONE);
            lblSignUp.setVisibility(View.GONE);
            btnForgot.setVisibility(View.GONE);
            lblForgot.setVisibility(View.GONE);
            txtPassword.setVisibility(View.GONE);
            //lblMore.setText(getString(R.string.Sign_MsgEmailPwd));
        } else if (mode == Mode.FORGOT) {
            btnValidate.setText("FORGOT PASSWORD");
            btnLogin.setVisibility(View.VISIBLE);
            lblLogin.setVisibility(View.VISIBLE);
            btnSignUp.setVisibility(View.GONE);
            lblSignUp.setVisibility(View.GONE);
            btnForgot.setVisibility(View.GONE);
            lblForgot.setVisibility(View.GONE);
            txtPassword.setVisibility(View.GONE);
            //lblMore.setText(getString(R.string.Sign_MsgEmailPwd));
        } else {
            btnValidate.setText("LOGIN");
            btnLogin.setVisibility(View.GONE);
            lblLogin.setVisibility(View.GONE);
            btnSignUp.setVisibility(View.VISIBLE);
            lblSignUp.setVisibility(View.VISIBLE);
            btnForgot.setVisibility(View.VISIBLE);
            lblForgot.setVisibility(View.VISIBLE);
            txtPassword.setVisibility(View.VISIBLE);
        }
        resizeTable();
    }

    private void resizeTable() {
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 205,
                getResources().getDisplayMetrics());
        if (mode == Mode.SIGNUP || mode == Mode.FORGOT) {
            height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 140,
                    getResources().getDisplayMetrics());
        }
        tblLogin.getLayoutParams().height = height;
    }

    private void registration() {
        closeKeyboards();
        String errorMessage = "";
        if (txtEmail.getText().toString().length() == 0) {
            errorMessage = getString(R.string.Sign_AlertLogin);
        }

        if (errorMessage.length()==0){
            String url;
            String todo;

            if(mode == Mode.FORGOT) todo = "forgotpwd";
            else todo = "signup";

            url = Constants.prepareInfoUrl(this, Constants.URL_SIGN_IN);

            try{
                RequestBody login = RequestBody.create(MediaType.parse("string"), txtEmail.getText().toString());
                RequestBody password = RequestBody.create(MediaType.parse("string"), txtPassword.getText().toString());
                RequestBody todoBody = RequestBody.create(MediaType.parse("string"), todo);

                HttpClient service = ServiceGenerator.createService(HttpClient.class);
                Call<ServerResponse> call = service.LoginTask(url, login, password, todoBody);

                call.enqueue(new Callback<ServerResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ServerResponse> call, @NonNull Response<ServerResponse> response) {
                        ServerResponse serverResponse = response.body();
                        if (serverResponse != null){
                            int result = serverResponse.result;
                            if (result == 200) {
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.Sign_PwdSent).replace("#EMAIL#", txtEmail.getText().toString()),
                                        Toast.LENGTH_LONG).show();
                                switchScreen(Mode.LOGIN);
                            } else {
                                String errorMessage;
                                if (result == 501) {
                                    errorMessage = getString(R.string.Sign_AlertEmailInvalid);
                                } else if (result == 502) {
                                    errorMessage = getString(R.string.Sign_AlertEmailUsed);
                                } else if (result == 504) {
                                    errorMessage = getString(R.string.Sign_AlertEmailUnknown);
                                } else if (result == 503) {
                                    errorMessage = getString(R.string.Sign_AlertCountDevice);
                                } else if (result == 505) {
                                    errorMessage = getString(R.string.Sign_AlertPwd);
                                } else if (result == 506) {
                                    errorMessage = getString(R.string.Sign_AlertPwd);
                                } else {
                                    errorMessage = getString(R.string.Sign_AlertIssue);
                                }
                                if (errorMessage.length() > 0)
                                    Toast.makeText(getApplicationContext(),
                                            errorMessage, Toast.LENGTH_LONG)
                                            .show();
                            }

                        }
                        else{
                            Toast.makeText(getApplicationContext(),
                                    R.string.General_ErrMsgNetwork, Toast.LENGTH_LONG)
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ServerResponse> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        Toast.makeText(getApplicationContext(),
                                R.string.General_ErrMsgNetwork, Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

    }

    private void authentication(){
        closeKeyboards();
        String errorMessage = "";
            // check default values
        if (txtEmail.getText().toString().length() == 0) {
            errorMessage = getString(R.string.Sign_AlertLogin);
        } else if (txtPassword.getText().toString().length() == 0) {
            errorMessage = getString(R.string.Sign_AlertPwd);
        }

        if (errorMessage.length()==0){
            String url;
            String todo;

            todo = "signin";
            url = Constants.prepareInfoUrl(this, Constants.URL_REGISTRATION);

            try{
                RequestBody login = RequestBody.create(MediaType.parse("string"), txtEmail.getText().toString());
                RequestBody password = RequestBody.create(MediaType.parse("string"), txtPassword.getText().toString());
                RequestBody todoBody = RequestBody.create(MediaType.parse("string"), todo);

                HttpClient service = ServiceGenerator.createService(HttpClient.class);
                Call<ServerResponse> call = service.LoginTask(url, login, password, todoBody);

                call.enqueue(new Callback<ServerResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ServerResponse> call, @NonNull Response<ServerResponse> response) {
                        ServerResponse serverResponse = response.body();
                        if (serverResponse != null){
                            int result = serverResponse.result;
                                if (result== 200) {
                                    ServerResponse.ResponseInfo responseInfo = serverResponse.responseInfo;
                                    if (responseInfo!=null) {
                                        userID = responseInfo.userID;
                                        sessionID = responseInfo.sessionID;

                                        Intent intent = getIntent();
                                        intent.putExtra("source", "SIGN_IN");
                                        intent.putExtra("login", 1);
                                        intent.putExtra("user_id", userID);
                                        intent.putExtra("session_id", sessionID);
                                        setResult(RESULT_OK, intent);
                                    }
                                    finish();
                                } else {
                                        String errorMessage = serverResponse.errMess;
                                        if (errorMessage == null || errorMessage.length() == 0) {
                                            errorMessage = getString(R.string.Sign_AlertCheckLogin);
                                            txtEmail.setText("");
                                            txtPassword.setText("");
                                        }
                                        Toast.makeText(getApplicationContext(),
                                                errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                }
                        else{
                            Toast.makeText(getApplicationContext(),
                                    R.string.General_ErrMsgNetwork, Toast.LENGTH_LONG)
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ServerResponse> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        Toast.makeText(getApplicationContext(),
                                R.string.General_ErrMsgNetwork, Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    public enum Mode{
        LOGIN, FORGOT, SIGNUP
    }

    private void signUp() {
        Intent launcher = new Intent(this, WebActivity.class);
        String url = Constants.prepareInfoUrl(getApplicationContext(),
                Constants.URL_SERVER + Constants.URL_SUBSCRIBE);
        launcher.putExtra("url", url);
        launcher.putExtra("mode", "signup");
    startActivityForResult(launcher, 0);
}
}
