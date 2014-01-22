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

package com.adobe.acs.commons.quickly.commands.impl.fallback;

import com.adobe.acs.commons.quickly.Command;
import com.adobe.acs.commons.quickly.Result;
import com.adobe.acs.commons.quickly.commands.AbstractCommandHandler;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component(
        label = "ACS AEM Commons - Quickly - Fallback Command Handler"
)
@Properties({
        @Property(
                name = "cmd",
                value = "fallback",
                propertyPrivate = true
        )
})
@Service
public class FallbackCommandHandlerImpl extends AbstractCommandHandler {
    private static final Logger log = LoggerFactory.getLogger(FallbackCommandHandlerImpl.class);

    @Override
    public boolean accepts(final ResourceResolver resourceResolver, final Command cmd) {
        return true;
    }

    @Override
    protected List<Result> withoutParams(final ResourceResolver resourceResolver,
                                         final Command cmd) {
        return this.withParams(resourceResolver, cmd);
    }

    @Override
    protected List<Result> withParams(final ResourceResolver resourceResolver, final Command cmd) {
        final List<Result> results = new LinkedList<Result>();

        final String param = cmd.getOp() + " " + cmd.getParam();


        return results;
    }


    @Activate
    protected void activate(final Map<String, String> config) {
    }
}