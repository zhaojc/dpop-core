package com.baidu.dpop.frame.core.util;

import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author huhailiang
 */
public class UrlUtilsTest {

    
    @Test
    public  void testUrlMatch() {
        String path = "/dpop-rmp/sysMonitor.action";

        SortedSet<String> urls = new TreeSet<String>();
        urls.add("/dpop-rmp/sysMonitor*");
        urls.add("/dpop-rmp/upload/dev/upload.do");
        boolean b = UrlUtils.urlMatch(urls, path);
        Assert.assertTrue(b);
        path = "/dpop-rmp/upload/dev/upload.do";
        b = UrlUtils.urlMatch(urls, path);
        Assert.assertTrue(b);
    }
    
    
    @Test
    public  void testUrlMatch02() {
        String path = "/asset-0.1.12/external/esl.js";
        SortedSet<String> urls = new TreeSet<String>();
        urls.add("/asset-**/**");
        urls.add("/dep-**/**");
        boolean b = UrlUtils.urlMatch(urls, path);
        Assert.assertTrue(b);
        
        path = "/dep-0.1.12/esui/3.1.0-beta.6/asset-0.1.12/css/main.css";
        b = UrlUtils.urlMatch(urls, path);
        Assert.assertTrue(b);
    }
    
    
    
}
