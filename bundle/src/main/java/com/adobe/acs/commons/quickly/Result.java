/*
 * #%L
 * ACS AEM Commons Bundle
 * %%
 * Copyright (C) 2013 Adobe
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package com.adobe.acs.commons.quickly;

import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

import java.util.Map;

public interface Result {

    public static final String TARGET_BLANK = "_blank";
    public static final String TARGET_TOP = "_top";
    public static final String TARGET_SELF = "_self";

    public static final String METHOD_GET = "get";
    public static final String METHOD_POST = "post";
    public static final String METHOD_NOOP = "noop";

    String getTitle();
    String getDescription();
    String getActionURI();
    String getActionMethod();
    String getActionTarget();
    String getPath();

    // TODO Make MultiMap?
    Map<String, String> getActionParams();

    boolean isValid();

    JSONObject toJSON() throws JSONException;
}