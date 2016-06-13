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

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.kytse.aria2.Aria2;
import com.kytse.aria2.Aria2Factory;

import org.apache.xmlrpc.XmlRpcException;

import java.net.MalformedURLException;


public class DownloadListFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener,
        Aria2.OnAria2ListUpdatedListener, View.OnClickListener {

    private static final int LOOP_INTERVAL = 1000;

    private Aria2 mAria2;
    private Aria2.ListType mType;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private GetDownloadListAsyncTask mGetDownloadListAsyncTask;

    public DownloadListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        RecyclerView.ItemAnimator animator = mRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        FloatingActionButton mActionButton = (FloatingActionButton) view.findViewById(R.id.fab);
        if (mActionButton != null) {
            mActionButton.setOnClickListener(this);
        }

        String url = getArguments().getString(getString(R.string.KEY_URL));
        String secret = getArguments().getString(getString(R.string.KEY_SECRET));
        int typeIndex = getArguments().getInt(getString(R.string.KEY_LIST_TYPE_INDEX));
        try {
            mAria2 = Aria2Factory.getInstance(url, secret);

            mType = Aria2.ListType.values()[typeIndex];

            mAdapter = new DownloadAdapter(getContext(), mAria2, mAria2.getList(mType));
            mRecyclerView.setAdapter(mAdapter);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        mAria2.addListener(this);

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                onRefresh();
            }
        });

        mGetDownloadListAsyncTask = new GetDownloadListAsyncTask();
        mGetDownloadListAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGetDownloadListAsyncTask != null) {
            mGetDownloadListAsyncTask.cancel(true);
        }

        mAria2.removeListener(this);
    }

    @Override
    public void onRefresh() {
        if (mGetDownloadListAsyncTask == null ||
                mGetDownloadListAsyncTask.getStatus() != AsyncTask.Status.RUNNING) {
            mGetDownloadListAsyncTask = new GetDownloadListAsyncTask();
            mGetDownloadListAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onAria2ListItemChanged(Aria2.ListType type, int position) {
        if (type == mType) {
            mAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public void onAria2ListItemInserted(Aria2.ListType type, int position) {
        if (type == mType) {
            mAdapter.notifyItemInserted(position);
        }
    }

    @Override
    public void onAria2ListItemRemoved(Aria2.ListType type, int position) {
        if (type == mType) {
            mAdapter.notifyItemRemoved(position);
        }
    }

    @Override
    public void onAria2ListItemMoved(Aria2.ListType type, int oldPosition, int newPosition) {
        if (type == mType) {
            mAdapter.notifyItemMoved(oldPosition, newPosition);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab) {
            final EditText editText = new EditText(v.getContext());

            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle(R.string.add_url_tittle)
                    .setView(editText)
                    .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String url = editText.getEditableText().toString();
                            new AddDownloadAsyncTask(url)
                                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

    private class GetDownloadListAsyncTask extends AsyncTask<Void, Void, Void> {

        private Exception mException;

        @Override
        protected Void doInBackground(Void... params) {

            try {
                while (true) {
                    mAria2.updateList(mType);
                    publishProgress();
                    Thread.sleep(LOOP_INTERVAL);
                }
            } catch (XmlRpcException | InterruptedException e) {
                e.printStackTrace();
                mException = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (mException != null) {
                String message = getString(R.string.error_occurred);

                if (mException instanceof XmlRpcException) {
                    if (((XmlRpcException) mException).code != 0) {
                        message = mException.getLocalizedMessage();
                    }
                }

                Snackbar.make(mSwipeRefreshLayout, message, Snackbar.LENGTH_SHORT).show();
            }

            mSwipeRefreshLayout.setRefreshing(false);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private class AddDownloadAsyncTask extends AsyncTask<Void, Void, String> {

        private Exception mException;

        private String mURL;

        AddDownloadAsyncTask(String url) {
            mURL = url;
        }

        @Override
        protected String doInBackground(Void... params) {
            String gid = null;

            try {
                gid = mAria2.addUri(mURL);
            } catch (XmlRpcException e) {
                e.printStackTrace();
                mException = e;
            }

            return gid;
        }

        @Override
        protected void onPostExecute(String gid) {
            super.onPostExecute(gid);

            String message = getString(R.string.error_occurred);

            if (gid != null) {
                message = String.format(getString(R.string.gid_added), gid);
            } else if (mException != null) {
                if (mException instanceof XmlRpcException) {
                    if (((XmlRpcException) mException).code != 0) {
                        message = mException.getLocalizedMessage();
                    }
                }
            }

            Snackbar.make(mRecyclerView, message, Snackbar.LENGTH_SHORT).show();
        }
    }
}
