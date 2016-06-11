/* Copyright 2016 Tse Kit Yam
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kytse.aria2remote;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kytse.aria2.Aria2;
import com.kytse.aria2.Aria2Factory;

import org.apache.xmlrpc.XmlRpcException;

import java.net.MalformedURLException;


public class LoginFragment extends Fragment {

    private UserLoginTask mAuthTask = null;

    private EditText mEditTextUrl;
    private EditText mEditTextSecret;
    private View mProgressView;
    private View mLoginFormView;

    private OnLoginSucceededListener mListener;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        mEditTextUrl = (EditText) getActivity().findViewById(R.id.EditText_url);

        mEditTextSecret = (EditText) getActivity().findViewById(R.id.editText_password);
        mEditTextSecret.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) getActivity().findViewById(R.id.button_sign_in);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = getActivity().findViewById(R.id.login_form);
        mProgressView = getActivity().findViewById(R.id.login_progress);
    }

    public void setListener(OnLoginSucceededListener listener) {
        mListener = listener;
    }

    private void attemptLogin() {

        InputMethodManager inputManager =
                (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

        if (mAuthTask != null) {
            return;
        }

        mEditTextUrl.setError(null);
        mEditTextSecret.setError(null);

        String url = mEditTextUrl.getText().toString();
        String secret = mEditTextSecret.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(url)) {
            mEditTextUrl.setError(getString(R.string.error_url_required));
            focusView = mEditTextUrl;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuthTask = new UserLoginTask(url, secret);
            mAuthTask.execute((Void) null);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUrl;
        private final String mSecret;

        private Exception mException;

        UserLoginTask(String email, String password) {
            mUrl = email;
            mSecret = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                Aria2 aria2 = Aria2Factory.getInstance(mUrl, mSecret);

                aria2.getGlobalStat();

                return true;
            } catch (MalformedURLException | XmlRpcException e) {
                e.printStackTrace();
                mException = e;
            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {

                SharedPreferences settings = getActivity().
                        getSharedPreferences(getString(R.string.login_credentials), 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(getString(R.string.KEY_URL), mUrl);
                editor.putString(getString(R.string.KEY_SECRET), mSecret);

                editor.apply();

                if (mListener != null) {
                    mListener.onLoginSucceeded();
                }

            } else {
                View focusView = null;

                if (mException instanceof MalformedURLException) {
                    mEditTextUrl.setError(mException.getLocalizedMessage());
                    focusView = mEditTextUrl;
                } else if (mException instanceof XmlRpcException) {
                    String error;
                    if (((XmlRpcException) mException).code == 0) {
                        error = "Error occurred";
                    } else {
                        error = mException.getLocalizedMessage();
                    }
                    Snackbar snackbar = Snackbar.make(mLoginFormView, error, Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }

                if (focusView != null) {
                    focusView.requestFocus();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public interface OnLoginSucceededListener {
        void onLoginSucceeded();
    }
}
