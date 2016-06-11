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

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class Aria2Factory {

    private static Map<String, Aria2> mMap = new HashMap<>();

    public static Aria2 getInstance(String url) throws MalformedURLException {
        return getInstance(url, null);
    }

    public static Aria2 getInstance(String url, String secret) throws MalformedURLException {
        Aria2 aria2 = mMap.get(url);

        if (aria2 == null) {
            aria2 = new Aria2(url, secret);
            mMap.put(url, aria2);
        }

        return aria2;
    }

    public static void removeInstance(String url) {
        mMap.remove(url);
    }
}
