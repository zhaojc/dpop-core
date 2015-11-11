package com.baidu.dpop.frame.core.base.web;

import java.util.Comparator;

import org.springframework.http.MediaType;

/**
 * 根据权重排序MediaType，权重大的排在权重小的前面。
 */
class MediaTypeQualityComparator implements Comparator<MediaType> {

    @Override
    public int compare(MediaType mediaType1, MediaType mediaType2) {
        return -Double.valueOf(mediaType1.getQualityValue()).compareTo(mediaType2.getQualityValue());
    }
}
