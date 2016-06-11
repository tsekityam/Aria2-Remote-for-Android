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

import java.util.HashMap;
import java.util.Map;

public class File {

    private String mIndex;
    private String mPath;
    private String mLength;
    private String mCompleteLength;
    private String mSelected;
    private Map<String, Object> mUrls;

    public File(Map object) {
        if (object instanceof HashMap) {
            mIndex = (String) object.get("index");
            mPath = (String) object.get("path");
            mLength = (String) object.get("length");
            mCompleteLength = (String) object.get("completedLength");
            mSelected = (String) object.get("selected");

            Object urls = object.get("urls");
            if (urls instanceof HashMap) {
                mUrls = new HashMap<>(((HashMap) urls).size());
                for (Object key :
                        ((HashMap) urls).keySet()) {
                    mUrls.put((String) key, ((HashMap) urls).get(key));
                }
            }
        }
    }

    public String getIndex() {
        return mIndex;
    }

    public String getPath() {
        return mPath;
    }

    public String getLength() {
        return mLength;
    }

    public String getCompleteLength() {
        return mCompleteLength;
    }

    public String getSelected() {
        return mSelected;
    }
}