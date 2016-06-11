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

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.kytse.aria2.Aria2;
import com.kytse.aria2.Aria2Factory;

import org.apache.xmlrpc.XmlRpcException;

import java.net.MalformedURLException;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        LoginFragment.OnLoginSucceededListener {

    private DownloadListFragment mActiveFragment;
    private DownloadListFragment mWaitingFragment;
    private DownloadListFragment mStoppedFragment;
    private LoginFragment mLoginFragment;

    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.setDrawerListener(toggle);
        }
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        if (mNavigationView != null) {
            mNavigationView.setNavigationItemSelectedListener(this);
        }

        mActiveFragment = new DownloadListFragment();
        mWaitingFragment = new DownloadListFragment();
        mStoppedFragment = new DownloadListFragment();
        mLoginFragment = new LoginFragment();

        mLoginFragment.setListener(this);

        loginStateDidChanged();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        if (findViewById(R.id.fragment_container) != null) {

            ActionBar actionBar = getSupportActionBar();
            String actionBarTitle = getResources().getString(R.string.app_name);

            switch (item.getItemId()) {
                case R.id.nav_active:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, mActiveFragment).commit();
                    if (actionBar != null) {
                        actionBar.setTitle(actionBarTitle.concat(getString(R.string.suffix_active)));
                    }
                    break;
                case R.id.nav_waiting:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, mWaitingFragment).commit();
                    if (actionBar != null) {
                        actionBar.setTitle(actionBarTitle.concat(getString(R.string.suffix_waiting)));
                    }
                    break;
                case R.id.nav_stopped:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, mStoppedFragment).commit();
                    if (actionBar != null) {
                        actionBar.setTitle(actionBarTitle.concat(getString(R.string.suffix_stopped)));
                    }
                    break;
                case R.id.nav_login:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, mLoginFragment).commit();
                    if (actionBar != null) {
                        actionBar.setTitle(actionBarTitle);
                    }
                    break;
                case R.id.nav_logout:
                    logout();
                    break;
                default:
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }

        return true;
    }

    @Override
    public void onLoginSucceeded() {
        loginStateDidChanged();
    }

    private void logout() {
        SharedPreferences settings = getSharedPreferences(getString(R.string.login_credentials), 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(getString(R.string.KEY_URL));
        editor.remove(getString(R.string.KEY_SECRET));

        editor.apply();

        String url = settings.getString(getString(R.string.KEY_URL), null);
        Aria2Factory.removeInstance(url);

        loginStateDidChanged();
    }

    private void loginStateDidChanged() {

        SharedPreferences settings = getSharedPreferences(getString(R.string.login_credentials), 0);
        String url = settings.getString(getString(R.string.KEY_URL), null);
        String secret = settings.getString(getString(R.string.KEY_SECRET), null);

        Menu navMenu = mNavigationView.getMenu();
        navMenu.setGroupVisible(R.id.group_list, url != null);
        navMenu.findItem(R.id.nav_login).setVisible(url == null);
        navMenu.findItem(R.id.nav_logout).setVisible(url != null);

        if (url == null) {
            onNavigationItemSelected(mNavigationView.getMenu().findItem(R.id.nav_login));
        } else {
            View headerView = mNavigationView.getHeaderView(0);
            TextView textViewUrl = (TextView) headerView.findViewById(R.id.textView_url);
            textViewUrl.setText(url);

            Bundle sharedBundle = new Bundle();
            sharedBundle.putString(getString(R.string.KEY_URL), url);
            sharedBundle.putString(getString(R.string.KEY_SECRET), secret);

            Bundle activateBundle = new Bundle(sharedBundle);
            activateBundle.putInt(getString(R.string.KEY_LIST_TYPE_INDEX), Aria2.ListType.ACTIVE.ordinal());
            mActiveFragment.setArguments(activateBundle);

            Bundle waitingBundle = new Bundle(sharedBundle);
            waitingBundle.putInt(getString(R.string.KEY_LIST_TYPE_INDEX), Aria2.ListType.WAITING.ordinal());
            mWaitingFragment.setArguments(waitingBundle);

            Bundle stoppedBundle = new Bundle(sharedBundle);
            stoppedBundle.putInt(getString(R.string.KEY_LIST_TYPE_INDEX), Aria2.ListType.STOPPED.ordinal());
            mStoppedFragment.setArguments(stoppedBundle);

            onNavigationItemSelected(mNavigationView.getMenu().findItem(R.id.nav_active));
        }
    }
}
