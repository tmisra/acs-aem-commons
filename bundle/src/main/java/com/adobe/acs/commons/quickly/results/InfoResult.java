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

package com.adobe.acs.commons.quickly.results;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfoResult extends AbstractResult {
    private static final Logger log = LoggerFactory.getLogger(InfoResult.class);

    public InfoResult(final String title, final String description) {
        this.setTitle(title);
        this.setDescription(description);
        this.setActionMethod(METHOD_NOOP);
    }
}