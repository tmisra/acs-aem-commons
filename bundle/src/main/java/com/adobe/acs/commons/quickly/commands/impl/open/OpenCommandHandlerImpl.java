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

package com.adobe.acs.commons.quickly.commands.impl.open;

import com.adobe.acs.commons.quickly.Command;
import com.adobe.acs.commons.quickly.Result;
import com.adobe.acs.commons.quickly.ResultHelper;
import com.adobe.acs.commons.quickly.commands.AbstractCommandHandler;
import com.day.cq.search.QueryBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

@Component(
        label = "ACS AEM Commons - Quickly - Go Command Handler"
)
@Properties({
        @Property(
                name = "cmd",
                value = OpenCommandHandlerImpl.CMD,
                propertyPrivate = true
        )
})
@Service
public class OpenCommandHandlerImpl extends AbstractCommandHandler {
    private static final Logger log = LoggerFactory.getLogger(OpenCommandHandlerImpl.class);

    public static final String CMD = "open";

    @Reference
    private QueryBuilder queryBuilder;

    @Reference
    private ResultHelper resultHelper;

    @Override
    public boolean accepts(final ResourceResolver resourceResolver, final Command cmd) {
        return StringUtils.equalsIgnoreCase(CMD, cmd.getOp());
    }

    @Override
    protected List<Result> withoutParams(final ResourceResolver resourceResolver, final Command cmd) {
        return new LinkedList<Result>();
    }

    @Override
    protected List<Result> withParams(final ResourceResolver resourceResolver,
                                      final Command cmd) {
        final List<Result> results = new LinkedList<Result>();

        final List<Resource> matchedResources = resultHelper.matchNodeName(resourceResolver, cmd.getParam(),
                "cq:Page", 25);

        for(final Resource matchedResource : matchedResources) {
            if(OpenResult.accepts(matchedResource)) {
                results.add(new OpenResult(matchedResource));
            }
        }

        return results;
    }
}