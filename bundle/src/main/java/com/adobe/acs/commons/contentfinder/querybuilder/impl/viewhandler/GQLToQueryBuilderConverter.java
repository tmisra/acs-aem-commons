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
package com.adobe.acs.commons.contentfinder.querybuilder.impl.viewhandler;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.search.Predicate;
import com.day.cq.search.eval.FulltextPredicateEvaluator;
import com.day.cq.search.eval.JcrPropertyPredicateEvaluator;
import com.day.cq.wcm.api.NameConstants;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class GQLToQueryBuilderConverter {
    private static final Logger log = LoggerFactory.getLogger(GQLToQueryBuilderConverter.class);

    public static final String DELIMITER = ContentFinderConstants.DELIMITER;

    public static final String CF_TYPE = ContentFinderConstants.CF_TYPE;
    public static final String CF_PATH = ContentFinderConstants.CF_PATH;
    public static final String CF_FULLTEXT = ContentFinderConstants.CF_FULLTEXT;
    public static final String CF_MIMETYPE = ContentFinderConstants.CF_MIMETYPE;
    public static final String CF_ORDER = ContentFinderConstants.CF_ORDER;
    public static final String CF_LIMIT = ContentFinderConstants.CF_LIMIT;
    public static final String CF_OFFSET = ContentFinderConstants.CF_OFFSET;
    public static final String CF_NAME = ContentFinderConstants.CF_NAME;
    public static final String CF_TAGS = ContentFinderConstants.CF_TAGS;

    public static final int GROUP_PATH = ContentFinderConstants.GROUP_PATH;
    public static final int GROUP_TYPE = ContentFinderConstants.GROUP_TYPE;
    public static final int GROUP_NAME = ContentFinderConstants.GROUP_NAME;
    public static final int GROUP_MIMETYPE = ContentFinderConstants.GROUP_MIMETYPE;
    public static final int GROUP_TAGS = ContentFinderConstants.GROUP_TAGS;
    public static final int GROUP_FULLTEXT = ContentFinderConstants.GROUP_FULLTEXT;

    public static final int GROUP_PROPERTY_USERDEFINED = ContentFinderConstants.GROUP_PROPERTY_USERDEFINED;

    public static final int GROUP_ORDERBY_USERDEFINED = ContentFinderConstants.GROUP_ORDERBY_USERDEFINED;
    public static final int GROUP_ORDERBY_SCORE = ContentFinderConstants.GROUP_ORDERBY_SCORE;
    public static final int GROUP_ORDERBY_MODIFIED = ContentFinderConstants.GROUP_ORDERBY_MODIFIED;

    public static final int DEFAULT_OFFSET = ContentFinderConstants.DEFAULT_OFFSET;
    public static final int DEFAULT_LIMIT = ContentFinderConstants.DEFAULT_LIMIT;

    /**
     * Private constructor.
     */
    private GQLToQueryBuilderConverter() {
        // Prevent instantiation
    }

    /**
     * Checks if request forces QueryBuilder mode.
     *
     * @param request the HTTP Request initiating the query
     * @return true is the request should be converted form GQL to QueryBuilder syntax
     */
    public static boolean convertToQueryBuilder(final SlingHttpServletRequest request) {
        return (has(request, ContentFinderConstants.CONVERT_TO_QUERYBUILDER_KEY)
                && ContentFinderConstants.CONVERT_TO_QUERYBUILDER_VALUE.equals(get(request,
                ContentFinderConstants.CONVERT_TO_QUERYBUILDER_KEY)));
    }

    /**
     * Adds a Path predicate to the QueryBuilder query definition .
     *
     * @param request the HTTP Request initiating the query
     * @param map map with previous QueryBuilder query definitions
     * @return a QueryBuild query map
     */
    public static Map<String, String> addPath(final SlingHttpServletRequest request, Map<String, String> map) {
        if (has(request, CF_PATH)) {
            map = put(request, map, CF_PATH, GROUP_PATH, true);
        } else {
            map.put(CF_PATH, request.getRequestPathInfo().getSuffix());
        }

        return map;
    }

    /**
     * Adds a nodeType predicate to the QueryBuilder query definition.
     *
     * @param request the HTTP Request initiating the query
     * @param map map with previous QueryBuilder query definitions
     * @return a QueryBuild query map
     */
    public static Map<String, String> addType(final SlingHttpServletRequest request, Map<String, String> map) {
        if (has(request, CF_TYPE)) {
            map = put(request, map, CF_TYPE, GROUP_TYPE, true);
        }

        return map;
    }

    /**
     * Adds a nodename predicate to the QueryBuilder query definition.
     *
     * @param request the HTTP Request initiating the query
     * @param map map with previous QueryBuilder query definitions
     * @return a QueryBuild query map
     */
    public static Map<String, String> addName(final SlingHttpServletRequest request, Map<String, String> map) {
        if (has(request, CF_NAME)) {
            map = put(request, map, CF_NAME, "nodename", GROUP_NAME, true);
        }

        return map;
    }

    /**
     * Adds a order predicate to the QueryBuilder query definition.
     *
     * @param request the HTTP Request initiating the query
     * @param map map with previous QueryBuilder query definitions
     * @param queryString
     * @return a QueryBuild query map
     */
    public static Map<String, String> addOrder(final SlingHttpServletRequest request, Map<String, String> map,
                                               final String queryString) {
        if (has(request, CF_ORDER)) {

            int count = 1;
            for (String value : getAll(request, CF_ORDER)) {
                value = StringUtils.trim(value);
                final String orderGroupId = String.valueOf(GROUP_ORDERBY_USERDEFINED + count) + "_group";
                boolean sortAsc = false;

                if (StringUtils.startsWith(value, "-")) {
                    sortAsc = false;
                    value = StringUtils.removeStart(value, "-");
                } else if (StringUtils.startsWith(value, "+")) {
                    sortAsc = true;
                    value = StringUtils.removeStart(value, "+");
                }

                map.put(orderGroupId, StringUtils.trim(value));
                map.put(orderGroupId + ".sort", sortAsc ? Predicate.SORT_ASCENDING : Predicate.SORT_DESCENDING);

                count++;
            }

        } else {

            final boolean isPage = isPage(get(request, CF_TYPE));
            final boolean isAsset = isAsset(get(request, CF_TYPE));
            final String prefix = getPropertyPrefix(request);

            if (StringUtils.isNotBlank(queryString)) {
                map.put(GROUP_ORDERBY_SCORE + "_orderby", "@" + JcrConstants.JCR_SCORE);
                map.put(GROUP_ORDERBY_SCORE + "_orderby.sort", Predicate.SORT_DESCENDING);
            }

            String modifiedOrderProperty = "@" + JcrConstants.JCR_LASTMODIFIED;
            if (isPage) {
                modifiedOrderProperty = "@" + prefix + NameConstants.PN_PAGE_LAST_MOD;
            } else if (isAsset) {
                modifiedOrderProperty = "@" + prefix + JcrConstants.JCR_LASTMODIFIED;
            }

            map.put(GROUP_ORDERBY_MODIFIED + "_orderby", modifiedOrderProperty);
            map.put(GROUP_ORDERBY_MODIFIED + "_orderby.sort", Predicate.SORT_DESCENDING);
        }

        return map;
    }


    /**
     * Adds a mimeType-based property predicate to the QueryBuilder query definition.
     *
     * @param request the HTTP Request initiating the query
     * @param map map with previous QueryBuilder query definitions
     * @return a QueryBuild query map
     */
    public static Map<String, String> addMimeType(final SlingHttpServletRequest request, Map<String, String> map) {
        final boolean isAsset = isAsset(get(request, CF_TYPE));
        final String prefix = getPropertyPrefix(request);

        if (isAsset && has(request, CF_MIMETYPE)) {
            map.put(GROUP_MIMETYPE + "_group.1_property.operation", "like");
            map.put(GROUP_MIMETYPE + "_group.1_property", prefix + DamConstants.DC_FORMAT);
            map.put(GROUP_MIMETYPE + "_group.1_property.value", "%" + get(request, CF_MIMETYPE) + "%");
        }

        return map;
    }


    /**
     * Adds a tag predicate to the QueryBuilder query definition.
     *
     * @param request the HTTP Request initiating the query
     * @param map map with previous QueryBuilder query definitions
     * @return a QueryBuild query map
     */
    public static Map<String, String> addTags(final SlingHttpServletRequest request, Map<String, String> map) {
        if (has(request, CF_TAGS)) {
            final String prefix = getPropertyPrefix(request);

            final String groupId = GROUP_TAGS + "_group";
            final String tagProperty = prefix + NameConstants.PN_TAGS;

            map.put(groupId + ".p.or", "true");

            if (hasMany(request, CF_TAGS)) {
                final String[] tags = getAll(request, CF_TAGS);

                int i = 1;
                for (final String tag : tags) {
                    map.put(groupId + "." + i + "_property", tagProperty);
                    map.put(groupId + "." + i + "_tagid", tag);

                    i++;
                }
            } else {
                map.put(groupId + ".1_property", tagProperty);
                map.put(groupId + ".1_tagid", get(request, CF_TAGS));
            }
        }

        return map;
    }

    /**
     * Adds fulltext predicate to the QueryBuilder query definition.
     *
     * @param request the HTTP Request initiating the query
     * @param map map with previous QueryBuilder query definitions
     * @param queryString value to fulltext search for
     * @return a QueryBuild query map
     */
    public static Map<String, String> addFulltext(final SlingHttpServletRequest request,
                                                  Map<String, String> map, final String queryString) {
        if (StringUtils.isNotBlank(queryString)) {
            final String groupId = GROUP_FULLTEXT + "_group";

            map.put(groupId + "." + FulltextPredicateEvaluator.FULLTEXT, queryString);
            map.put(groupId + ".p.or", "true");
        }
        return map;
    }

    /**
     * Adds limit and offset predicates to the QueryBuilder query definition.
     *
     * @param request the HTTP Request initiating the query
     * @param map map with previous QueryBuilder query definitions
     * @return a QueryBuild query map
     */
    public static Map<String, String> addLimitAndOffset(final SlingHttpServletRequest request,
                                                        final Map<String, String> map) {
        if (has(request, CF_LIMIT)) {
            // Both limits and offsets are computed from CF's limit field X..Y
            final String offset = String.valueOf(getOffset(request));
            final String limit = String.valueOf(getLimit(request));

            map.put("p.offset", String.valueOf(offset));
            map.put("p.limit", limit);
        } else {
            map.put("p.limit", String.valueOf(DEFAULT_LIMIT));
        }

        return map;
    }


    /**
     * Adds property predicate to the QueryBuilder query definition.
     *
     * @param request the HTTP Request initiating the query
     * @param map map with previous QueryBuilder query definitions
     * @param requestKey key of request attribute whose value it to be used
     * @param count count used to create unique GroupIDs; typically incremented by caller
     * @return
     */
    public static Map<String, String> addProperty(final SlingHttpServletRequest request, Map<String, String> map,
                                                  final String requestKey, final int count) {
        if (!ArrayUtils.contains(ContentFinderConstants.PROPERTY_BLACKLIST, requestKey)) {
            map = putProperty(request, map, requestKey, JcrPropertyPredicateEvaluator.PROPERTY,
                    (GROUP_PROPERTY_USERDEFINED + count), true);
        } else {
            log.debug("Rejecting property [ {} ] due to blacklist match", requestKey);
        }
        return map;
    }


    /**
     * Used to determine if a request parameter key should be ignored (system properties).
     *
     * @param key the key to evaluate against the blacklist
     * @return true is value is in the property blacklist
     */
    public static boolean isValidProperty(final String key) {
        return (!ArrayUtils.contains(ContentFinderConstants.PROPERTY_BLACKLIST, key));
    }

    /**
     * Checks if the provided key has more than 1 values (comma delimited).
     *
     * @param request the HTTP Request initiating the query
     * @param key the request parameter key
     * @return true is key has more than 1 value
     */
    public static boolean hasMany(SlingHttpServletRequest request, String key) {
        final RequestParameter rp = request.getRequestParameter(key);
        if (rp == null) {
            return false;
        }
        return getAll(request, key).length > 1;
    }

    /**
     * Checks if the provided key has ANY values (1 or more).
     *
     * @param request the HTTP Request initiating the query
     * @param key the request parameter key
     * @return true is key has 1 or more values
     */
    public static boolean has(SlingHttpServletRequest request, String key) {
        return request.getParameterValues(key) != null;
    }

    /**
     * Returns a single value for a query parameter key.
     *
     * @param request the HTTP Request initiating the query
     * @param key the request parameter key
     * @return the value trimmed of white-space
     */
    public static String get(SlingHttpServletRequest request, String key) {
        return StringUtils.trim(request.getRequestParameter(key).toString());
    }

    /**
     * Returns a String array from a comma delimited list of values.
     *
     * @param request the HTTP Request initiating the query
     * @param key the request parameter key
     * @return String array from a comma delimited list of values
     */
    public static String[] getAll(SlingHttpServletRequest request, String key) {
        final RequestParameter rp = request.getRequestParameter(key);
        if (rp == null) {
            return new String[0];
        }
        return StringUtils.split(rp.getString(), DELIMITER);
    }

    /**
     * Convenience wrapper.
     *
     * @param request the HTTP Request initiating the query
     * @param map             => map with previous QueryBuilder query definitions
     * @param predicate       => property
     * @param group           => ID
     * @param or              => true/false
     * @return a QueryBuild query map
     */
    public static Map<String, String> put(final SlingHttpServletRequest request, final Map<String, String> map,
                                          final String predicate, final int group, final boolean or) {
        return putAll(map, predicate, getAll(request, predicate), group, or);
    }

    /**
     *
     * @param request the HTTP Request initiating the query
     * @param map             => map with previous QueryBuilder query definitions
     * @param requestKey      => The key used to look up value in request
     * @param predicate       => property
     * @param group           => ID
     * @param or              => true/false
     * @return a QueryBuild query map
     */
    public static Map<String, String> put(final SlingHttpServletRequest request, final Map<String, String> map,
                                          final String requestKey, final String predicate, final int group,
                                          final boolean or) {
        return putAll(map, predicate, getAll(request, requestKey), group, or);
    }

    /**
     * Used when the request key is different from the Predicate.
     *
     * @param request the HTTP Request initiating the query
     * @param map             => map with previous QueryBuilder query definitions
     * @param requestKey      => The key used to look up value in request
     * @param predicate       => property
     * @param group           => ID
     * @param or              => true/false
     * @return a QueryBuild query map
     */
    public static Map<String, String> putProperty(final SlingHttpServletRequest request, final Map<String,
            String> map, final String requestKey, final String predicate, final int group, final boolean or) {
        // putAll(map, "property", "jcr:title", "value", [x,y,z], 10, true)
        return putAll(map, predicate, requestKey, JcrPropertyPredicateEvaluator.VALUE,
                getAll(request, requestKey), group, or);
    }

    /**
     * Helper method for adding comma delimited values into a Query Builder predicate.
     *
     * @param map             => map with previous QueryBuilder query definitions
     * @param predicate       => property
     * @param values          => [Square, Triangle]
     * @param group           => ID
     * @param or              => true/false
     * @return a QueryBuild query map
     */
    public static Map<String, String> putAll(final Map<String, String> map, final String predicate,
                                             final String[] values, final int group, final boolean or) {
        final String groupId = String.valueOf(group) + "_group";
        int count = 1;

        for (final String value : values) {
            final String predicateId = count + "_" + predicate;

            map.put(groupId + "." + predicateId, StringUtils.trim(value));
            count++;
        }

        map.put(groupId + ".p.or", String.valueOf(or));

        return map;
    }

    /**
     * Creates map representing a QueryBuilder query.
     *
     * @param map             => map with previous QueryBuilder query definitions
     * @param predicateValue  => jcr:title
     * @param predicate       => property
     * @param predicateSuffix => value
     * @param values          => [Square, Triangle]
     * @param group           => ID
     * @param or              => true/false
     * @return a QueryBuild query map
     */
    public static Map<String, String> putAll(final Map<String, String> map, final String predicate,
                                             final String predicateValue, final String predicateSuffix,
                                             final String[] values, final int group, boolean or) {
        final String groupId = String.valueOf(group) + "_group";

        map.put(groupId + "." + predicate, predicateValue);

        int count = 1;
        for (final String value : values) {
            final String predicateId = predicate;
            final String predicateSuffixId = count + "_" + predicateSuffix;
            map.put(groupId + "." + predicateId + "." + predicateSuffixId, StringUtils.trim(value));
            count++;
        }

        map.put(groupId + ".p.or", String.valueOf(or));

        return map;
    }

    /**
     * Checks of the query param node type is that of a CQ Page.
     *
     * @param nodeType value of the node type (jcr:PrimaryType)
     * @return true is the nodeType is that of a CQ Page
     */
    public static boolean isPage(final String nodeType) {
        return StringUtils.equals(nodeType, "cq:Page");
    }

    /**
     * Checks of the query param node type is that of a DAM Asset.
     *
     * @param nodeType value of the node type (jcr:PrimaryType)
     * @return true is the nodeType is that of an Asset
     */
    public static boolean isAsset(final String nodeType) {
        return StringUtils.equals(nodeType, "dam:Asset");
    }

    /**
     * Determines path prefix for the resource depending on if its a CQ Page or DAM Asset.
     *
     * @param request the HTTP Request initiating the query
     * @return the path prefix for the resource
     */
    public static String getPropertyPrefix(final SlingHttpServletRequest request) {
        final boolean isPage = isPage(get(request, CF_TYPE));
        final boolean isAsset = isAsset(get(request, CF_TYPE));

        String prefix = "";
        if (isPage) {
            prefix = JcrConstants.JCR_CONTENT + "/";
        } else if (isAsset) {
            prefix = JcrConstants.JCR_CONTENT + "/" + DamConstants.METADATA_FOLDER + "/";
        }

        return prefix;
    }

    /**
     * Extract the query limit from the ContentFinder Query Parameter notation.
     *
     * @param request the HTTP Request initiating the query
     * @return the query limit
     */
    public static int getLimit(final SlingHttpServletRequest request) {
        if (has(request, CF_LIMIT)) {
            final String value = get(request, CF_LIMIT);
            final String[] limits = StringUtils.split(value, "..");

            if (value.matches("^(\\d)+\\.\\.(\\d)+$")) {
                // 10..20
                return Integer.parseInt(limits[1]) - Integer.parseInt(limits[0]);
            } else if (value.matches("^\\.\\.(\\d)+$")) {
                // ..20
                return Integer.parseInt(limits[0]);
            } else if (value.matches("^(\\d)+\\.\\.$")) {
                // 20..
                // Not upper limit
                return DEFAULT_LIMIT;
            }
            log.info("Could not find valid LIMIT for QueryBuilder-based ContentFinder: {}", value);
        } else {
            log.info("Could not find any LIMIT for QueryBuilder-based ContentFinder");
        }

        return DEFAULT_LIMIT;
    }

    /**
     * Extract the query offset from the ContentFinder Query Parameter notation.
     *
     * @param request the HTTP Request initiating the query
     * @return the query offset
     */
    public static int getOffset(final SlingHttpServletRequest request) {
        if (has(request, CF_LIMIT)) {
            final String value = get(request, CF_LIMIT);
            final String[] offsets = StringUtils.split(value, "..");

            if (value.matches("^(\\d)+\\.\\.(\\d)+$")) {
                // 10..20
                return Integer.parseInt(offsets[0]);
            } else if (value.matches("^\\.\\.(\\d)+$")) {
                // ..20
                return Integer.parseInt(offsets[0]);
            } else if (value.matches("^(\\d)+\\.\\.$")) {
                // 20..
                return Integer.parseInt(offsets[0]);
            }
            log.info("Could not find valid OFFSET for QueryBuilder-based ContentFinder: {}", value);
        } else {
            log.info("Could not find any OFFSET for QueryBuilder-based ContentFinder");
        }

        return DEFAULT_OFFSET;
    }
}
