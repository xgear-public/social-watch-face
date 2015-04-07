package com.yobalabs.socialwf.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.yobalabs.socialwf.R;
import com.yobalabs.socialwf.model.Credentials;
import com.yobalabs.socialwf.model.LoginResult;
import com.yobalabs.socialwf.settings.MainSettings;
import com.yobalabs.socialwf.util.DebugToastUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginActivity extends BaseActivity {

    @InjectView(R.id.login)
    EditText mLogin;

    @InjectView(R.id.pass)
    EditText mPassword;

    @InjectView(R.id.sign_in)
    Button mSignIn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);

        ButterKnife.inject(this);

        mSignIn.setOnClickListener(mOnClickListener);
    }

    private Credentials mUser;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String login = mLogin.getText().toString();
            String pass = mPassword.getText().toString();

            if (!TextUtils.isEmpty(login) && !TextUtils.isEmpty(pass)) {
                mUser = new Credentials(login, pass, MainSettings.getInstance().getRegId());
                getSocialWatchFaceApi().login(mUser, loginResult);
            }
        }
    };

    Callback<LoginResult> loginResult = new Callback<LoginResult>() {
        @Override
        public void success(LoginResult loginResult, Response response) {
            MainSettings.getInstance().setCredentials(mUser);
        }

        @Override
        public void failure(RetrofitError error) {
            DebugToastUtil.showDebugToast("login failed", LoginActivity.this);
        }
    };

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_login, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void startLoginActivity(Activity activity) {
        Intent i = new Intent(activity, LoginActivity.class);
        activity.startActivity(i);
    }
}
