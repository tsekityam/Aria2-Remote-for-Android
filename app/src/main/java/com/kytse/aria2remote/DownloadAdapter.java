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

import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kytse.aria2.Aria2;
import com.kytse.aria2.Download;

import org.apache.xmlrpc.XmlRpcException;

import java.util.List;
import java.util.Locale;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.ViewHolder> implements View.OnClickListener {


    private Context mContext;
    private Aria2 mAria2;
    private List<Download> mDownloads;

    public DownloadAdapter(Context context, Aria2 aria2, List<Download> downloads) {

        mContext = context;
        mAria2 = aria2;
        mDownloads = downloads;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Download download = mDownloads.get(position);

        holder.imageButtonAction.setOnClickListener(this);
        holder.imageButtonAction.setTag(R.string.KEY_GID, download.getGID());
        holder.imageButtonAction.setTag(R.string.KEY_STATUS, download.getStatus());
        switch (download.getStatus()){
            case ACTIVE:
            case WAITING:
                holder.imageButtonAction.setImageResource(R.drawable.ic_pause_black_48dp);
                break;
            case PAUSED:
                holder.imageButtonAction.setImageResource(R.drawable.ic_play_arrow_black_48dp);
                break;
            default:
                holder.imageButtonAction.setImageResource(android.R.color.transparent);
        }

        holder.textViewName.setText(download.getName());
        holder.textViewStatus.setText(download.getStatus().toString());

        switch (download.getStatus()) {
            case ACTIVE:
                holder.textViewDownloadSpeed.setText(String.format(Locale.getDefault(), "%s/sec", toReadableFormat(Integer.parseInt(download.getDownloadSpeed()))));
                holder.textViewUploadSpeed.setText(String.format(Locale.getDefault(), "%s/sec", toReadableFormat(Integer.parseInt(download.getUploadSpeed()))));
                holder.linearLayoutUploadSpeed.setVisibility(View.VISIBLE);
                holder.linearLayoutDownloadSpeed.setVisibility(View.VISIBLE);
                break;
            case WAITING:
            case PAUSED:
            case COMPLETE:
            case REMOVED:
            case ERROR:
                holder.linearLayoutUploadSpeed.setVisibility(View.INVISIBLE);
                holder.linearLayoutDownloadSpeed.setVisibility(View.INVISIBLE);
                break;
            default:
                break;
        }

        holder.textViewTotalLength.setText(toReadableFormat(download.getTotalLength()));
        holder.progressBarLinear.setMax(download.getTotalLength());
        holder.progressBarLinear.setProgress(download.getCompletedLength());
    }

    @Override
    public int getItemCount() {
        return mDownloads.size();
    }

    @Override
    public void onClick(View v) {

        if (v instanceof ImageButton) {
            String gid = (String) v.getTag(R.string.KEY_GID);
            Download.Status status = (Download.Status) v.getTag(R.string.KEY_STATUS);

            switch (status) {
                case ACTIVE:
                case WAITING:
                    new PauseDownloadAsyncTask(v, gid, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    break;
                case PAUSED:
                    new UnpauseDownloadAsyncTask(v, gid).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    break;
                default:
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private CardView mCardView;

        public ImageButton imageButtonAction;
        public LinearLayout linearLayoutUploadSpeed;
        public LinearLayout linearLayoutDownloadSpeed;
        public TextView textViewName;
        public TextView textViewStatus;
        public TextView textViewTotalLength;
        public TextView textViewDownloadSpeed;
        public TextView textViewUploadSpeed;
        public ProgressBar progressBarLinear;

        public ViewHolder(View itemView) {
            super(itemView);

            mCardView = (CardView) itemView.findViewById(R.id.card_view);
            imageButtonAction = (ImageButton) mCardView.findViewById(R.id.imageButton_action);
            linearLayoutUploadSpeed = (LinearLayout) mCardView.findViewById(R.id.linearLayout_uploadSpeed);
            linearLayoutDownloadSpeed = (LinearLayout) mCardView.findViewById(R.id.linearLayout_downloadSpeed);
            textViewName = (TextView) mCardView.findViewById(R.id.textView_name);
            textViewStatus = (TextView) mCardView.findViewById(R.id.textView_status);
            textViewTotalLength = (TextView) mCardView.findViewById(R.id.textView_totalLength);
            textViewDownloadSpeed = (TextView) mCardView.findViewById(R.id.textView_downloadSpeed);
            textViewUploadSpeed = (TextView) mCardView.findViewById(R.id.textView_uploadSpeed);
            progressBarLinear = (ProgressBar) mCardView.findViewById(R.id.progressBar_linear);
        }
    }

    private String toReadableFormat(int sizeInB) {

        double size = sizeInB;
        String unit = ("B");

        double sizeInKB = sizeInB / 1000.00;
        if (sizeInKB > 1) {
            size = sizeInKB;
            unit = "KB";
        }

        double sizeInMB = sizeInKB / 1000.00;
        if (sizeInMB > 1) {
            size = sizeInMB;
            unit = "MB";
        }

        double sizeInGB = sizeInMB / 1000.00;
        if (sizeInGB > 1) {
            size = sizeInGB;
            unit = "GB";
        }

        return String.format(Locale.getDefault(), "%.2f %s", size, unit);
    }

    private class UnpauseDownloadAsyncTask extends AsyncTask<Void, Void, Void> {

        private XmlRpcException mException;

        private View mView;
        private String mGID;

        UnpauseDownloadAsyncTask(View view, String gid) {
            mView = view;
            mGID = gid;
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                mAria2.unpause(mGID);
            } catch (XmlRpcException e) {
                e.printStackTrace();
                mException = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            String message = String.format(mContext.getString(R.string.gid_unpausing), mGID);

            if (mException != null) {
                message = mContext.getString(R.string.error_occurred);

                if (mException.code != 0) {
                    message = mException.getLocalizedMessage();
                }
            }

            Snackbar.make(mView, message, Snackbar.LENGTH_SHORT).show();
        }
    }

    private class PauseDownloadAsyncTask extends AsyncTask<Void, Void, Void> {

        private XmlRpcException mException;

        private View mView;
        private String mGID;
        private boolean mForce;

        public PauseDownloadAsyncTask(View view, String gid, boolean force) {
            mView = view;
            mGID = gid;
            mForce = force;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            String message = String.format(mContext.getString(R.string.gid_pausing), mGID);

            if (mException != null) {
                message = mContext.getString(R.string.error_occurred);

                if (mException.code != 0) {
                    message = mException.getLocalizedMessage();
                }
            }

            Snackbar.make(mView, message, Snackbar.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                if (mForce) {
                    mAria2.forcePause(mGID);
                } else {
                    mAria2.pause(mGID);
                }
            } catch (XmlRpcException e) {
                e.printStackTrace();
                mException = e;
            }

            return null;
        }
    }
}
