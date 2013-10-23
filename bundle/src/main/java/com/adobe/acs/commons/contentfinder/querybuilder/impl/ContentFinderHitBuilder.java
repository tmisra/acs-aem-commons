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
package com.adobe.acs.commons.contentfinder.querybuilder.impl;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.commons.util.DamUtil;
import com.day.cq.search.result.Hit;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import javax.jcr.RepositoryException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ContentFinderHitBuilder {
    private static final int ELLIPSE_LENGTH = 3;
    private static final int MAX_EXCERPT_LENGTH = 32;
    private static final String DAM_THUMBNAIL = "cq5dam.thumbnail.48.48.png";

    private ContentFinderHitBuilder() { }

    /**
     * Builds the result object that will representing a CF view record for the provided hit.
     *
     * This method will generate the result object data points based on if the hit is:
     * 1) a Page
     * 2) an Asset
     * 3) Other
     *
     * @param hit the QueryBuilder hit to transform into a CF hit
     * @return a map representing the common/base attributes of all CF Hit results
     * @throws RepositoryException
     */
    public static Map<String, Object> buildGenericResult(final Hit hit) throws RepositoryException {
        Map<String, Object> map = new LinkedHashMap<String, Object>();

        final Resource resource = hit.getResource();
        final boolean isPage = resource.adaptTo(Page.class) != null;
        final boolean isAsset = DamUtil.isAsset(resource);

        /**
         * Common result properties
         */
        map.put("name", resource.getName());
        map.put("path", hit.getPath());

        if (isPage) {
            map = addPageData(hit, map);
        } else if (isAsset) {
            map = addAssetData(hit, map);
        } else {
            map = addOtherData(hit, map);
        }

        return map;
    }

    /**
     * Derives and adds Page related information to the map representing the hit.
     *
     * @param hit the QueryBuilder hit to transform into a CF hit
     * @param map a map of data representing the partially constructed state of the CF Hit
     * @return a map representing a CF hit result of a CQ Page
     * @throws javax.jcr.RepositoryException
     */
    private static Map<String, Object> addPageData(final Hit hit, Map<String, Object> map) throws RepositoryException {
        final Resource resource = hit.getResource();

        final Page page = resource.adaptTo(Page.class);

        // Title
        String title = resource.getName();

        if (StringUtils.isNotBlank(page.getTitle())) {
            title = page.getTitle();
        } else if (StringUtils.isNotBlank(page.getPageTitle())) {
            title = page.getPageTitle();
        } else if (StringUtils.isNotBlank(page.getNavigationTitle())) {
            title = page.getNavigationTitle();
        }

        // Excerpt
        String excerpt = hit.getExcerpt();
        if (StringUtils.isBlank(hit.getExcerpt())) {
            excerpt = StringUtils.stripToEmpty(page.getDescription());
            if (excerpt.length() > MAX_EXCERPT_LENGTH) {
                excerpt = StringUtils.substring(excerpt, 0, (MAX_EXCERPT_LENGTH - ELLIPSE_LENGTH)) + "...";
            }
        }

        map.put("title", title);
        map.put("excerpt", excerpt);
        map.put("ddGroups", "page");
        map.put("type", "Page");
        map.put("lastModified", getLastModified(page));

        return map;
    }

    /**
     * Derives and adds Asset related information to the map representing the hit.
     *
     * @param hit the QueryBuilder hit to transform into a CF hit
     * @param map a map of data representing the partially constructed state of the CF Hit
     * @return a map representing a CF hit result of an Asset
     * @throws javax.jcr.RepositoryException
     */
    private static Map<String, Object> addAssetData(final Hit hit, Map<String, Object> map) throws RepositoryException {
        final Resource resource = hit.getResource();
        final Asset asset = DamUtil.resolveToAsset(resource);

        String title = resource.getName();

        if (StringUtils.isNotBlank(asset.getMetadataValue(DamConstants.DC_TITLE))) {
            title = asset.getMetadataValue(DamConstants.DC_TITLE);
        }

        // Excerpt
        String excerpt = hit.getExcerpt();
        if (StringUtils.isBlank(hit.getExcerpt())) {
            excerpt = StringUtils.stripToEmpty(asset.getMetadataValue(DamConstants.DC_DESCRIPTION));
            if (excerpt.length() > MAX_EXCERPT_LENGTH) {
                excerpt = StringUtils.substring(excerpt, 0, (MAX_EXCERPT_LENGTH - ELLIPSE_LENGTH)) + "...";
            }
        }

        map.put("title", title);
        map.put("excerpt", excerpt);
        map.put("mimeType", asset.getMimeType());
        map.put("size", getSize(asset));
        map.put("ck", getCK(asset));
        map.put("type", "Asset");
        map.put("lastModified", getLastModified(asset));

        return map;
    }


    /**
     * Derives and adds Other (non-Page, non-Asset) related information to the map representing the hit.
     *
     * @param hit the QueryBuilder hit to transform into a CF hit
     * @param map a map of data representing the partially constructed state of the CF Hit
     * @return a map representing a CF hit result of a non-Page and non-Asset
     * @throws javax.jcr.RepositoryException
     */
    private static Map<String, Object> addOtherData(final Hit hit,
                                                    final Map<String, Object> map) throws RepositoryException {
        final Resource resource = hit.getResource();

        map.put("title", resource.getName());
        map.put("excerpt", hit.getExcerpt());
        map.put("lastModified", getLastModified(resource));
        map.put("type", "Data");

        return map;
    }

    /**
     * Get the last modified date for an Asset.
     *
     * @param asset the asset to get the last modified date for
     * @return the last modified date of the asset
     */
    private static long getLastModified(final Asset asset) {
        if (asset.getLastModified() > 0L) {
            return asset.getLastModified();
        } else {
            final Object obj = asset.getMetadata().get(JcrConstants.JCR_LASTMODIFIED);

            if (obj != null && obj instanceof Date) {
                return ((Date) obj).getTime();
            } else {
                return 0L;
            }
        }
    }

    /**
     * Get the last modified date for a Page.
     *
     * @param page the page to get the last modified date for
     * @return the last modified date of the page
     */
    private static long getLastModified(final Page page) {
        if (page.getLastModified() != null) {
            return page.getLastModified().getTimeInMillis();
        } else {
            final ValueMap properties = page.getProperties();
            Date lastModified = properties.get(NameConstants.PN_PAGE_LAST_MOD, Date.class);
            if (lastModified != null) {
                return lastModified.getTime();
            } else {
                return 0L;
            }
        }
    }

    /**
     * Get the last modified date for a generic resource.
     *
     * @param resource the resource to get the last modified date for
     * @return the last modified date of the resource
     */
    private static long getLastModified(final Resource resource) {
        final ValueMap properties = resource.adaptTo(ValueMap.class);

        final Date cqLastModified = properties.get(NameConstants.PN_PAGE_LAST_MOD, Date.class);
        if (cqLastModified != null) {
            return cqLastModified.getTime();
        }

        final Date jcrLastModified = properties.get(JcrConstants.JCR_LASTMODIFIED, Date.class);
        if (jcrLastModified != null) {
            return jcrLastModified.getTime();
        }

        return 0L;
    }

    /**
     * Get the size of the Asset (the original rendition).
     *
     * @param asset the asset to get the size of
     * @return the size of the asset in bytes
     */
    private static long getSize(final Asset asset) {
        final Rendition original = asset.getOriginal();
        if (original == null) {
            return 0;
        }
        return original.getSize();
    }


    /**
     * Get the timestamp for the last change to the thumbnail.
     *
     * @param asset the asset associated w the thumbnail.
     * @return the last modified timestamp
     */
    private static long getCK(final Asset asset) {
        final long ckDivisor = 1000 * 1000;
        try {
            Resource resource = asset.getRendition(DAM_THUMBNAIL);
            Resource contentResource = resource.getChild(JcrConstants.JCR_CONTENT);
            ValueMap properties = contentResource.adaptTo(ValueMap.class);

            return properties.get(JcrConstants.JCR_LASTMODIFIED, 0L) / ckDivisor;
        } catch (Exception ex) {
            return 0L;
        }
    }
}
