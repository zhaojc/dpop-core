package com.baidu.dpop.frame.core.base.web;

import javax.servlet.http.HttpServletRequest;

/**
 * JsonResult数据的内容协商器。只有当协商成功的情况下才会返回对应的JsonResult数据。
 */
public interface JsonResultExceptionNegotiator {

    /**
     * 通过一个请求协商是否可以返回JsonResult类数据。
     *
     * @param request servlet请求
     * @return 是否协商成功
     */
    boolean isAcceptable(HttpServletRequest request);
}
