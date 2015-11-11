package com.baidu.dpop.frame.monitor.executstack;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.baidu.dpop.frame.core.filter.DpopFilterConfig;
import com.baidu.dpop.frame.monitor.executstack.context.ExecutContext;

/**
 * 
 * J2EE执行器监控的Filter入口 <br>
 * 主要职责：<br>
 * （1）负责产生全局唯一的请求ID<br>
 * （2）负责开关监控请求开启<br>
 * （3）持久化运行日志：本地 和 NFS<br>
 * @author huhailiang
 * 
 */
public class ExecutInfoFilter implements Filter {

    private ServletContext servletContext;

    /**
     * 保持traceLog的路径
     */
    private String traceLogPath;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        DpopFilterConfig dpopFilterConfig = new DpopFilterConfig(filterConfig);
        traceLogPath = dpopFilterConfig.getInitParameter("traceLogPath");
        servletContext = filterConfig.getServletContext();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // 是否开启程序执行监控
        final boolean isOpenMoniter = ExecutInfoFilterUtils.requestOpenMoniter(httpRequest);
        // 是否是查询执行监控结果
        final boolean isQueryExecutStack = ExecutInfoFilterUtils.requestIsQueryExecutStack(httpRequest);
        try {
            if (isQueryExecutStack) {
                ExecutInfoFilterUtils.doProcessQueryExecutStackInfo(servletContext, traceLogPath, httpRequest,
                        httpResponse);
                return;
            }

            if (isOpenMoniter) {
                ExecutInfoFilterUtils.prepareExecutStackInfo(servletContext, httpRequest, httpResponse);
            }

            chain.doFilter(httpRequest, httpResponse);
        } finally {
            if (isOpenMoniter) {
                ExecutInfoFilterUtils.saveExecutStackInfo(servletContext, traceLogPath, httpRequest, httpResponse);
            }
            ExecutContext.clear();
        }
    }

    @Override
    public void destroy() {
        servletContext = null;
        ExecutContext.clear();
    }

}
