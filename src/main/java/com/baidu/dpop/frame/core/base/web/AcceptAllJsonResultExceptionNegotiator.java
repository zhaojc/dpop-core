package com.baidu.dpop.frame.core.base.web;

import javax.servlet.http.HttpServletRequest;

public class AcceptAllJsonResultExceptionNegotiator implements JsonResultExceptionNegotiator {

    @Override
    public boolean isAcceptable(HttpServletRequest request) {
        return true;
    }
}
