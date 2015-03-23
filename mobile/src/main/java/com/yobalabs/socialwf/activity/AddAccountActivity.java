package com.yobalabs.socialwf.activity;

import com.facebook.widget.LoginButton;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.yobalabs.socialwf.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AddAccountActivity extends ActionBarActivity {

    @InjectView(R.id.insta)
    View mInstaGroup;

//    @InjectView(R.id.facebook)
//    View mFbGroup;
//    @InjectView(R.id.twitter)
//    View mTwGroup;

    @InjectView(R.id.tw_login)
    TwitterLoginButton mTwLogin;

    @InjectView(R.id.fb_login)
    LoginButton mFbLogin;

    @InjectView(R.id.insta_done)
    ImageView mInstaDone;

    @InjectView(R.id.fb_done)
    ImageView mFbDone;

    @InjectView(R.id.tw_done)
    ImageView mTwDone;

    EditText mInstaLogin;

    EditText mInstaPass;

    Button mInstaSignIn;

//    EditText mFbLogin;
//
//    EditText mFbPass;
//
//    Button mFbSignIn;

//    EditText mTwLogin;
//    EditText mTwPass;
//    Button mTwSignIn;

    View.OnClickListener mInstaSignInClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    View.OnClickListener mFbSignInClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    View.OnClickListener mTwSignInClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    public static void startAddAccountActivity(Context context) {
        Intent i = new Intent(context, AddAccountActivity.class);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);
        ButterKnife.inject(this);

        mInstaLogin = (EditText) mInstaGroup.findViewById(R.id.login);
        mInstaPass = (EditText) mInstaGroup.findViewById(R.id.pass);
        mInstaSignIn = (Button) mInstaGroup.findViewById(R.id.sign_in);
        mInstaSignIn.setOnClickListener(mInstaSignInClickListener);

//        mFbLogin = (EditText) mFbGroup.findViewById(R.id.login);
//        mFbPass = (EditText) mFbGroup.findViewById(R.id.pass);
//        mFbSignIn = (Button) mFbGroup.findViewById(R.id.sign_in);
//        mFbSignIn.setOnClickListener(mFbSignInClickListener);

//        mTwLogin = (EditText) mTwGroup.findViewById(R.id.login);
//        mTwPass = (EditText) mTwGroup.findViewById(R.id.pass);
//        mTwSignIn = (Button) mTwGroup.findViewById(R.id.sign_in);
//        mTwSignIn.setOnClickListener(mTwSignInClickListener);
        mTwLogin.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
//                TwitterSession session =
//                        result.getSessionManager().getActiveSession();
                TwitterAuthToken authToken = result.data.getAuthToken();
                String token = authToken.token;
                String secret = authToken.secret;
            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
            }
        });
    }

    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_add_account, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the login button.
        mTwLogin.onActivityResult(requestCode, resultCode,
                data);
    }
}
