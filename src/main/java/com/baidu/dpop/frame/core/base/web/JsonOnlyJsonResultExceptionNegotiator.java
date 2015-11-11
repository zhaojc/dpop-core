package com.baidu.dpop.frame.core.base.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.util.Assert;

public class JsonOnlyJsonResultExceptionNegotiator implements JsonResultExceptionNegotiator {

    private Set<MediaType> jsonMediaTypes = new HashSet<MediaType>(
            Arrays.asList(MediaType.APPLICATION_JSON,
                          MediaType.valueOf("application/javascript"),
                          MediaType.valueOf("application/x-javascript"),
                          MediaType.valueOf("text/javascript"),
                          MediaType.valueOf("text/x-javascript"),
                          MediaType.valueOf("text/x-json")));
    private Set<MediaType> excludedMediaTypes = new HashSet<MediaType>(
            Arrays.asList(MediaType.TEXT_HTML, MediaType.APPLICATION_XHTML_XML));

    @Override
    public boolean isAcceptable(HttpServletRequest request) {
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader == null) {
            return false;
        }
        acceptHeader = acceptHeader.trim();
        if (acceptHeader.isEmpty()) {
            return false;
        }
        List<MediaType> acceptMediaTypes = MediaType.parseMediaTypes(acceptHeader);
        Collections.sort(acceptMediaTypes, new MediaTypeQualityComparator());
        List<MediaType> mediaTypesWithoutParams = removeParameters(acceptMediaTypes);

        for (MediaType acceptMediaType : mediaTypesWithoutParams) {
            if (excludedMediaTypes.contains(acceptMediaType)) {
                return false;
            }
            if (jsonMediaTypes.contains(acceptMediaType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 设置JSON类的Content-Type列表。
     *
     * @param jsonMediaTypes JSON类的Content-Type列表
     */
    public void setJsonMediaTypes(Collection<String> jsonMediaTypes) {
        Assert.notEmpty(jsonMediaTypes);
        this.jsonMediaTypes = new HashSet<MediaType>();
        for (String jsonMediaType : jsonMediaTypes) {
            this.jsonMediaTypes.add(MediaType.valueOf(jsonMediaType));
        }
    }

    /**
     * 设置排查类型。对于这些类型，如果优先级比JSON类的高，则内容协商失效。
     *
     * @param excludedMediaTypes 排查类型
     */
    public void setExcludedMediaTypes(Set<MediaType> excludedMediaTypes) {
        Assert.notNull(excludedMediaTypes);
        this.excludedMediaTypes = excludedMediaTypes;
    }

    private List<MediaType> removeParameters(List<MediaType> mediaTypes) {
        List<MediaType> mediaTypesWithoutParams = new ArrayList<MediaType>();
        for (MediaType mediaType : mediaTypes) {
            mediaTypesWithoutParams.add(new MediaType(mediaType.getType(), mediaType.getSubtype()));
        }
        return mediaTypesWithoutParams;
    }
}
