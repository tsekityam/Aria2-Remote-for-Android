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

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Aria2 {

    public enum ListType {ACTIVE, WAITING, STOPPED}

    private final XmlRpcClient mClient;

    private String mSecret;

    private List<Download> mActiveList;
    private List<Download> mWaitingList;
    private List<Download> mStoppedList;

    private List<OnAria2ListUpdatedListener> mListenerList;

    protected Aria2(String url, String secret) throws MalformedURLException {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(String.format("%s/rpc", url)));
        mClient = new XmlRpcClient();
        mClient.setConfig(config);

        mActiveList = new ArrayList<>();
        mWaitingList = new ArrayList<>();
        mStoppedList = new ArrayList<>();

        mListenerList = new ArrayList<>();

        mSecret = secret == null ? null : String.format("token:%s", secret);
    }

    public void addListener(OnAria2ListUpdatedListener listener) {
        mListenerList.add(listener);
    }

    public void removeListener(OnAria2ListUpdatedListener listener) {
        mListenerList.remove(listener);
    }

    public List<Download> getList(ListType type) {
        switch (type) {
            case ACTIVE:
                return mActiveList;
            case WAITING:
                return mWaitingList;
            case STOPPED:
                return mStoppedList;
        }

        return new ArrayList<>();
    }

    public void updateList(ListType type) throws XmlRpcException {
        List<Download> targetList;
        switch (type) {
            case ACTIVE:
                targetList = this.tellActive();
                break;
            case WAITING: {
                Map globalStat = this.getGlobalStat();
                int numWaiting = Integer.parseInt((String) globalStat.get("numWaiting"));
                targetList = this.tellWaiting(0, numWaiting);
                break;
            }
            case STOPPED: {
                Map globalStat = this.getGlobalStat();
                int numStopped = Integer.parseInt((String) globalStat.get("numStopped"));
                targetList = this.tellStopped(0, numStopped);
                break;
            }
            default:
                targetList = new ArrayList<>();
        }

        this.synchroniseList(type, targetList);
    }

    public String addUri(String uri) throws XmlRpcException {
        Object[] params;
        Object[] uris = new Object[] {uri};

        if (mSecret != null) {
            params = new Object[]{mSecret, uris};
        } else {
            params = new Object[] {uris};
        }

        return (String) mClient.execute("aria2.addUri", params);
    }

    public String remove(String gid) throws XmlRpcException {
        Object[] params;

        if (mSecret != null) {
            params = new Object[] {mSecret, gid};
        } else {
            params = new Object[] {gid};
        }

        return (String) mClient.execute("aria2.remove", params);
    }

    public String forceRemove(String gid) throws XmlRpcException {
        Object[] params;

        if (mSecret != null) {
            params = new Object[] {mSecret, gid};
        } else {
            params = new Object[] {gid};
        }

        return (String) mClient.execute("aria2.forceRemove", params);
    }

    public String pause(String gid) throws XmlRpcException {
        Object[] params;

        if (mSecret != null) {
            params = new Object[] {mSecret, gid};
        } else {
            params = new Object[] {gid};
        }

        return (String) mClient.execute("aria2.pause", params);
    }

    public void pauseAll() throws XmlRpcException {
        Object[] params;

        if (mSecret != null) {
            params = new Object[] {mSecret};
        } else {
            params = new Object[] {};
        }

        mClient.execute("aria2.pauseAll", params);
    }

    public String forcePause(String gid) throws XmlRpcException {
        Object[] params;

        if (mSecret != null) {
            params = new Object[] {mSecret, gid};
        } else {
            params = new Object[] {gid};
        }

        return (String) mClient.execute("aria2.forcePause", params);
    }

    public void forcePauseAll() throws XmlRpcException {
        Object[] params;

        if (mSecret != null) {
            params = new Object[] {mSecret};
        } else {
            params = new Object[] {};
        }

        mClient.execute("aria2.forcePauseAll", params);
    }

    public String unpause(String gid) throws XmlRpcException {
        Object[] params;

        if (mSecret != null) {
            params = new Object[] {mSecret, gid};
        } else {
            params = new Object[] {gid};
        }

        return (String) mClient.execute("aria2.unpause", params);
    }

    public void unpauseAll() throws XmlRpcException {
        Object[] params;

        if (mSecret != null) {
            params = new Object[] {mSecret};
        } else {
            params = new Object[] {};
        }

        mClient.execute("aria2.unpauseAll", params);
    }

    public Download tellStatus(String gid) throws XmlRpcException {
        Object[] params;

        if (mSecret != null) {
            params = new Object[] {mSecret, gid};
        } else {
            params = new Object[] {gid};
        }

        return new Download((Map<String, Object>) mClient.execute("aria2.tellStatus", params));
    }

    private List<Download> tellActive() throws XmlRpcException {
        Object[] params;

        if (mSecret != null) {
            params = new Object[] {mSecret};
        } else {
            params = new Object[] {};
        }

        Object[] status = (Object[]) mClient.execute("aria2.tellActive", params);
        List<Download> result = new ArrayList<>(status.length);
        for (Object object :
                status) {
            if (object instanceof Map) {
                result.add(new Download((Map<String, Object>) object));
            }
        }

        return result;
    }

    private List<Download> tellWaiting(int offset, int num) throws XmlRpcException {
        Object[] params;

        if (mSecret != null) {
            params = new Object[] {mSecret, Integer.valueOf(offset), Integer.valueOf(num)};
        } else {
            params = new Object[] {Integer.valueOf(offset), Integer.valueOf(num)};
        }

        Object[] status = (Object[]) mClient.execute("aria2.tellWaiting", params);
        List<Download> result = new ArrayList<>(status.length);
        for (Object object :
                status) {
            if (object instanceof Map) {
                result.add(new Download((Map<String, Object>) object));
            }
        }

        return result;
    }

    private List<Download> tellStopped(int offset, int num) throws XmlRpcException {
        Object[] params;

        if (mSecret != null) {
            params = new Object[] {mSecret, Integer.valueOf(offset), Integer.valueOf(num)};
        } else {
            params = new Object[] {Integer.valueOf(offset), Integer.valueOf(num)};
        }

        Object[] status = (Object[]) mClient.execute("aria2.tellStopped", params);
        List<Download> result = new ArrayList<>(status.length);
        for (Object object :
                status) {
            if (object instanceof Map) {
                result.add(new Download((Map<String, Object>) object));
            }
        }

        return result;
    }

    public Map getGlobalStat() throws XmlRpcException {
        Object[] params;

        if (mSecret != null) {
            params = new Object[] {mSecret};
        } else {
            params = new Object[] {};
        }

        return (Map) mClient.execute("aria2.getGlobalStat", params);
    }

    public int changePosition(int pos) throws XmlRpcException {
        Object[] params;

        if (mSecret != null) {
            params = new Object[] {mSecret, Integer.valueOf(pos), "POS_SET"};
        } else {
            params = new Object[] {Integer.valueOf(pos), "POS_SET"};
        }

        return Integer.getInteger((String) mClient.execute("aria2.changePosition", params));
    }

    public List<String> listMethod() throws XmlRpcException {
        Object[] params = new Object[] {};
        Object[] methodArray = (Object[]) mClient.execute("system.listMethods", params);

        List<String> methodList = new ArrayList<>(methodArray.length);

        for (Object method :
                methodArray) {
            methodList.add((String) method);
        }

        return methodList;
    }

//    public void getVersion() throws XmlRpcException {
//        Object[] params;
//
//        if (mSecret != null) {
//            Object secret = String.format("token:%s", mSecret);
//            params = new Object[]{secret};
//        } else {
//                params = new Object[] {};
//        }
//
//        Object versionMap = mClient.execute("aria2.getVersion", params);
//        if (versionMap instanceof HashMap) {
//            for (Object key :
//                    ((HashMap) versionMap).keySet()) {
//                Object version = ((HashMap) versionMap).get(key);
//                if (version instanceof String) {
//                    System.out.printf("%s -> %s\n", key, version);
//                } else if (version instanceof Object[]) {
//                    System.out.printf("%s -> %s\n", key, getStringFromObjectArray((Object[]) version));
//                }
//            }
//        }
//    }

    public interface OnAria2ListUpdatedListener {

        void onAria2ListItemChanged(ListType type, int position);

        void onAria2ListItemInserted(ListType type, int position);

        void onAria2ListItemRemoved(ListType type, int position);

        void onAria2ListItemMoved(ListType type, int oldPosition, int newPosition);

    }

    private void synchroniseList(ListType type, List<Download> targetList) {

        List<Download> sourceList;

        switch (type) {
            case ACTIVE:
                sourceList = this.mActiveList;
                break;
            case WAITING:
                sourceList = this.mWaitingList;
                break;
            case STOPPED:
                sourceList = this.mStoppedList;
                break;
            default:
                return;
        }

        for (int newPos = 0; newPos < targetList.size(); newPos++) {
            Download newObj = targetList.get(newPos);
            int pos = indexOfObjectWithGID(sourceList, newObj.getGID());

            if (pos == newPos) {
                sourceList.get(newPos).updateDataFrom(targetList.get(newPos));

                for (OnAria2ListUpdatedListener listener :
                        mListenerList) {
                    listener.onAria2ListItemChanged(type, pos);
                }
            } else if (pos == -1) {
                sourceList.add(newPos, newObj);

                for (OnAria2ListUpdatedListener listener :
                        mListenerList) {
                    listener.onAria2ListItemInserted(type, newPos);
                }
            } else {
                Download obj = sourceList.get(pos);
                sourceList.remove(obj);
                sourceList.add(newPos, obj);
                sourceList.get(newPos).updateDataFrom(targetList.get(newPos));

                for (OnAria2ListUpdatedListener listener :
                        mListenerList) {
                    listener.onAria2ListItemMoved(type, pos, newPos);
                }
            }

        }

        for (int i = targetList.size(); i < sourceList.size(); i++) {
            sourceList.remove(i);

            for (OnAria2ListUpdatedListener listener :
                    mListenerList) {
                listener.onAria2ListItemRemoved(type, i);
            }
        }

    }

    private int indexOfObjectWithGID(List<Download> list, String targetGID) {
        for (int i = 0; i < list.size(); i++) {
            String gid = list.get(i).getGID();
            if (targetGID.contentEquals(gid)) {
                return i;
            }
        }

        return -1;
    }
}
