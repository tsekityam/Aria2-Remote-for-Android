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

package com.kytse.aria2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Download {

    public enum Status {ACTIVE, WAITING, PAUSED, ERROR, COMPLETE, REMOVED}

    private boolean mChangingStatus;

    private String mGID;
    private Status mStatus;
    private int mTotalLength;
    private int mCompletedLength;
    private String mUploadLength;
    private String mBitfield;
    private String mDownloadSpeed;
    private String mUploadSpeed;
    private String mInfoHash;
    private String mNumSeeders;
    private String mSeeder;
    private String mPieceLength;
    private String mNumPieces;
    private String mConnections;
    private String mErrorCode;
    private String mErrorMessage;
    private List<String> mFollowedBy;
    private String mFollowing;
    private String mBelongsTo;
    private String mDir;
    private List<File> mFiles;

    private String mName;

    public Download(Map<String, Object> object) {
        if (object instanceof HashMap) {
            mGID = (String) object.get("gid");

            if (((String) object.get("status")).contentEquals("active")) {
                mStatus = Status.ACTIVE;
            } else if (((String) object.get("status")).contentEquals("waiting")) {
                mStatus = Status.WAITING;
            } else if (((String) object.get("status")).contentEquals("paused")) {
                mStatus = Status.PAUSED;
            } else if (((String) object.get("status")).contentEquals("error")) {
                mStatus = Status.ERROR;
            } else if (((String) object.get("status")).contentEquals("complete")) {
                mStatus = Status.COMPLETE;
            } else if (((String) object.get("status")).contentEquals("removed")) {
                mStatus = Status.REMOVED;
            }

            mTotalLength = Integer.parseInt((String) object.get("totalLength"));
            mCompletedLength = Integer.parseInt((String) object.get("completedLength"));
            mUploadLength = (String) object.get("uploadLength");
            mBitfield = (String) object.get("bitfield");
            mDownloadSpeed = (String) object.get("downloadSpeed");
            mUploadSpeed = (String) object.get("uploadSpeed");
            mInfoHash = (String) object.get("infoHash");
            mNumSeeders = (String) object.get("numSeeders");
            mSeeder = (String) object.get("seeder");
            mPieceLength = (String) object.get("pieceLength");
            mNumPieces = (String) object.get("numPieces");
            mConnections = (String) object.get("connections");
            mErrorCode = (String) object.get("errorCode");
            mErrorMessage = (String) object.get("errorMessage");

            Object[] followBy = (Object[]) object.get("followedBy");
            if (followBy != null) {
                mFollowedBy = new ArrayList<>(followBy.length);
                for (Object gid :
                        followBy) {
                    mFollowedBy.add((String) gid);
                }
            }

            mFollowing = (String) object.get("following");
            mBelongsTo = (String) object.get("belongsTo");
            mDir = (String) object.get("dir");

            Object[] files = (Object[]) object.get("files");
            if (files != null) {
                mFiles = new ArrayList<>(files.length);
                for (Object file :
                        files) {
                    mFiles.add(new File((Map) file));
                }
            }

            Object bittorrent = object.get("bittorrent");
            if (bittorrent instanceof HashMap) {
                Object info = ((HashMap) bittorrent).get("info");
                if (info instanceof HashMap) {
                    mName = (String) ((HashMap) info).get("name");
                } else {
                    if (mFiles.size() == 1) {
                        mName = mFiles.get(0).getPath();
                    }
                }
            } else {
                if (mFiles.size() == 1) {
                    String path = mFiles.get(0).getPath();
                    if (path.contentEquals("[METADATA]")) {
                        mName = path;
                    } else {
                        mName = path.substring(mDir.length() + 1);
                    }
                } else {
                    mName = "Multiple Files";
                }
            }
        }
    }

    public void updateDataFrom(Download download) {

        mStatus = download.getStatus();
        mTotalLength = download.getTotalLength();
        mCompletedLength = download.getCompletedLength();
        mUploadLength = download.getUploadLength();
        mBitfield = download.getBitfield();
        mDownloadSpeed = download.getDownloadSpeed();
        mUploadSpeed = download.getUploadSpeed();
        mInfoHash = download.getInfoHash();
        mNumSeeders = download.getNumSeeders();
        mSeeder = download.getSeeder();
        mPieceLength = download.getPieceLength();
        mNumPieces = download.getNumPieces();
        mConnections = download.getConnections();
        mErrorCode = download.getErrorCode();
        mErrorMessage = download.getErrorCode();
        mFollowedBy = download.getFollowedBy();
        mFollowing = download.getFollowing();
        mBelongsTo = download.getBelongsTo();
        mDir = download.getDir();
        mFiles = download.getFiles();
    }

    public boolean isChangingStatus() {
        return mChangingStatus;
    }

    public void setChangingStatus(boolean changingStatus) {
        mChangingStatus = changingStatus;
    }

    public String getGID() {
        return mGID;
    }

    public Status getStatus() {
        return mStatus;
    }

    public int getTotalLength() {
        return mTotalLength;
    }

    public int getCompletedLength() {
        return mCompletedLength;
    }

    public String getUploadLength() {
        return mUploadLength;
    }

    public String getBitfield() {
        return mBitfield;
    }

    public String getDownloadSpeed() {
        return mDownloadSpeed;
    }

    public String getUploadSpeed() {
        return mUploadSpeed;
    }

    public String getInfoHash() {
        return mInfoHash;
    }

    public String getNumSeeders() {
        return mNumSeeders;
    }

    public String getSeeder() {
        return mSeeder;
    }

    public String getPieceLength() {
        return mPieceLength;
    }

    public String getNumPieces() {
        return mNumPieces;
    }

    public String getConnections() {
        return mConnections;
    }

    public String getErrorCode() {
        return mErrorCode;
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }

    public List<String> getFollowedBy() {
        return mFollowedBy;
    }

    public String getFollowing() {
        return mFollowing;
    }

    public String getBelongsTo() {
        return mBelongsTo;
    }

    public String getDir() {
        return mDir;
    }

    public List<File> getFiles() {
        return mFiles;
    }

    public String getName() {
        return mName;
    }
}
