package com.baidu.dpop.frame.monitor.executstack;

import java.io.File;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.baidu.dpop.frame.monitor.executstack.aop.MonitorStackNode;
import com.baidu.dpop.frame.monitor.executstack.context.ExecutContextUtils;

/**
 *  2EE执行器监控的Filter工具类
 *  
 *  
 * @author huhailiang
 * 
 */
public abstract class ExecutInfoFilterUtils {

    // 日志信息
    private static Logger LOGGER = Logger.getLogger(ExecutInfoFilterUtils.class);

    private static final String OPEN_REQUEST_KEY = ";openMoniterExecut";
    private static final String QUERY_REQUEST_KEY = "query.execut.stack";
    private static final String QUERY_REQUEST_ID_KEY = "requestId";

    private static String serverHostName = "";
    private static String serverIp = "";

    private static final String HTTP_RESPONSE_SERVER = "X-Dpop-MonitorTools-Execut-ServerInfo";
    private static final String HTTP_RESPONSE_REQUID = "X-Dpop-MonitorTools-Execut-RequestId";

    /**
     * 查询程序的执行栈、异常情况
     * 
     * @param servletContext
     * @param httpRequest
     * @param httpResponse
     */
    protected static void doProcessQueryExecutStackInfo(ServletContext servletContext, String traceLogPath,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        String requestId = httpRequest.getParameter(QUERY_REQUEST_ID_KEY);
        File stackInfoLogFile = getSaveExecutStackInfoLogFile(servletContext, traceLogPath, requestId);
        if (null == stackInfoLogFile) {
            try {
                httpResponse.setContentType("text/html;charset=UTF-8");
                PrintWriter out = httpResponse.getWriter();
                out.write(String.format("no stack[%s] info", requestId));
            } catch (Exception e) {
                LOGGER.error("ExecutInfoFilterUtils read stackInfoLogFile has error:", e);
            }
            return;
        }

        try {
            List<String> responseContentLines = FileUtils.readLines(stackInfoLogFile, "UTF-8");
            httpResponse.setContentType("text/html;charset=UTF-8");
            PrintWriter out = httpResponse.getWriter();
            for (String line : responseContentLines) {
                out.write(String.format("<nobr>%s</nobr><br/>", line));
            }

        } catch (Exception e) {
            LOGGER.error("ExecutInfoFilterUtils read stackInfoLogFile has error:", e);
        }
    }

    /**
     * 
     * @param servletContext
     * @param httpRequest
     * @param httpResponse
     */
    protected static void prepareExecutStackInfo(ServletContext servletContext, HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        serverHostName = ExecutInfoFilterUtils.getServerHostName();
        serverIp = ExecutInfoFilterUtils.getServerIp();

        ExecutContextUtils.openExecutMoniter();
        httpResponse.addHeader(HTTP_RESPONSE_SERVER,
                String.format("%s(%s:%d)", serverHostName, serverIp, httpRequest.getServerPort()));
        final String requestId = ExecutInfoFilterUtils.getRequestUUID();
        ExecutContextUtils.setRequestId(requestId);
        httpResponse.addHeader(HTTP_RESPONSE_REQUID, requestId);
    }

    /**
     * 保存程序执行的栈和异常情况
     * 
     * @param servletContext
     * @param httpRequest
     * @param httpResponse
     */
    protected static void saveExecutStackInfo(ServletContext servletContext, String traceLogPath,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        final String requestId = ExecutContextUtils.getRequestId();
        File stackInfoLogFile = getSaveExecutStackInfoLogFile(servletContext, traceLogPath, requestId);

        String stackInfoLogInfo = getExecutStackInfo(httpRequest, httpResponse);
        try {
            File parent = stackInfoLogFile.getParentFile();
            parent.mkdirs();
            parent.createNewFile();
            FileUtils.writeByteArrayToFile(stackInfoLogFile, stackInfoLogInfo.getBytes());
        } catch (Exception e) {
            LOGGER.error("ExecutInfoFilterUtils writestackInfoLogFile has error:", e);
        }
    }

    /**
     * 获取保存程序执行栈的日志文件
     * 
     * @param servletContext
     * @param traceLogPath
     * @return
     */
    private static File getSaveExecutStackInfoLogFile(ServletContext servletContext, final String traceLogPath,
            final String requestId) {

        final String webRootPath = servletContext.getRealPath("/");

        // 1.默认保存文件
        if (null == traceLogPath || traceLogPath.isEmpty()) {// 默认保存文件
            String saveExecutStackInfoLogFile = String.format("%s/tracelog/%s.request.log", webRootPath, requestId);
            return new File(saveExecutStackInfoLogFile);
        }

        // 2.保持在NFS目录下面
        final String traceLogPathUpper = traceLogPath.toUpperCase();
        if (traceLogPathUpper.startsWith("NFS:")) {
            String traceLogNFSRootPath = traceLogPath.substring(traceLogPathUpper.indexOf(":") + 1);
            String saveExecutStackInfoLogFile = String.format("%s/%s.request.log", traceLogNFSRootPath, requestId);
            return new File(saveExecutStackInfoLogFile);
        }

        // 3.保存在webRoot某个文件夹下面：
        String saveExecutStackInfoLogFile = String.format("%s/%s/%s.request.log", webRootPath, traceLogPath, requestId);
        return new File(saveExecutStackInfoLogFile);
    }

    /**
     * 把Http请求响应和服务器内部执行的栈、异常格式化
     * 
     * @param httpRequest
     * @param httpResponse
     * @return
     */
    private static String getExecutStackInfo(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        final String requestId = ExecutContextUtils.getRequestId();
        MonitorStackNode executMethodStack = ExecutContextUtils.getExecutStackTrace().getTrace();

        ExcecutLogFormat excecutLogFormat = new ExcecutLogFormat();
        excecutLogFormat.bulid(requestId, httpRequest);
        excecutLogFormat.bulid(executMethodStack);
        excecutLogFormat.bulid(true);
        return excecutLogFormat.toString();
    }

    /**
     * 
     * @param httpRequest
     * @return
     */
    protected static boolean requestOpenMoniter(HttpServletRequest httpRequest) {
        String url = httpRequest.getRequestURI();
        return url.contains(OPEN_REQUEST_KEY);
    }

    protected static boolean requestIsQueryExecutStack(HttpServletRequest httpRequest) {
        String url = httpRequest.getRequestURI();
        return url.contains(QUERY_REQUEST_KEY);
    }

    public static String getRequestUUID() {
        Random random = new Random();
        String ip = getServerIp();
        SimpleDateFormat fomatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String currentDate = fomatter.format(new Date());
        return String.format("%s-%s-%d", ip, currentDate, random.nextInt(1024));
    }

    public static String getServerHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e1) {
        }
        return "UnknownHost";
    }

    public static String getServerIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e1) {
        }
        return "UnknownHost";
    }
}
