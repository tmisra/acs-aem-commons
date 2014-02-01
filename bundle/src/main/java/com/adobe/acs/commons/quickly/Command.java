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

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;

public class Command {
    private final String raw;
    private final String operation;
    private final String param;
    private final String[] options;

    public Command(final SlingHttpServletRequest request) {
        this(request.getParameter("cmd"));
    }

    public Command(final String raw) {
        this.raw = StringUtils.stripToEmpty(raw);
        this.operation = StringUtils.lowerCase(StringUtils.substringBefore(this.raw, " "));

        if(StringUtils.substringAfter(this.raw, " ").contains(" ")) {
            this.options = StringUtils.substringBeforeLast(StringUtils.substringAfter(this.raw, " "), " ").split(" ");
        } else {
            this.options = new String[]{};
        }

        this.param = StringUtils.substringAfterLast(raw, " ");
    }

    public String getOp() {
        return this.operation;
    }

    public String getParam() {
        return this.param;
    }

    public String toString() { return this.raw; }

    public String[] getOptions() { return this.options; }
}