package com.baidu.dpop.frame.monitor.executstack;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;

import com.baidu.dpop.frame.monitor.executstack.aop.MonitorStackNode;
import com.baidu.dpop.frame.monitor.executstack.aop.StackNodeTypeEnum;

/**
 * 
 * 执行栈日志的格式化，包括如下几部分数据：<br>
 * （1）Http请求信息    <br>
 * （2）J2EE执行栈、耗时等<br>
 * （3）JVM内存分布、GC执行情况<br>
 * （4）问题线程状态，如死锁、长时间等待线程等 <br>
 * （5）环境、应用启动参数，<br>
 * 
 * @author huhailiang
 * 
 */
public class ExcecutLogFormat {

    public static final String BREAK_LINE = "\r\n";

    private String requestId;
    private HttpServletRequest httpRequest;
    private MonitorStackNode executMethodStack;

    private StringBuilder httpRequestInfos = new StringBuilder(1024);

    private StringBuilder executMethodStackInfos = new StringBuilder(1024 * 2);

    private StringBuilder executJVMInfos = new StringBuilder(1024);

    /**
     * 格式化请求URL、首部、请求参数等
     * 
     * @param requestId
     * @param httpRequest
     * @return
     */
    public ExcecutLogFormat bulid(String requestId, HttpServletRequest httpRequest) {
        this.httpRequest = httpRequest;
        this.requestId = requestId;

        httpRequestInfos.append(String.format("requestId:\r\n%s\r\n", requestId));
        httpRequestInfos.append(BREAK_LINE);
        httpRequestInfos.append(String.format("Request URL:\r\n%s\r\n", httpRequest.getRequestURI()));
        httpRequestInfos.append(BREAK_LINE);

        httpRequestInfos.append(String.format("Request Method:%s\r\n", httpRequest.getMethod()));
        httpRequestInfos.append(BREAK_LINE);

        httpRequestInfos.append("Request Header:");
        httpRequestInfos.append(BREAK_LINE);
        Enumeration headerNames = httpRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            Object key = headerNames.nextElement();
            String headerStr = httpRequest.getHeader(key.toString());
            httpRequestInfos.append(String.format("%s:%s\r\n", key.toString(), headerStr));
        }
        httpRequestInfos.append(BREAK_LINE);

        httpRequestInfos.append("Request Parameter:");
        httpRequestInfos.append(BREAK_LINE);
        Enumeration parameterNames = httpRequest.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            Object key = parameterNames.nextElement();
            String paramStr = httpRequest.getParameter(key.toString());
            httpRequestInfos.append(String.format("%s:%s\r\n", key.toString(), paramStr));
        }
        httpRequestInfos.append(BREAK_LINE);
        return this;
    }

    /**
     * 格式化执行栈和执行期间的异常信息
     * 
     * @param executMethodStack
     * @return
     */
    public ExcecutLogFormat bulid(MonitorStackNode executMethodStack) {
        this.executMethodStack = executMethodStack;

        executMethodStackInfos.append(String.format("JAVA STACK:\r\n%s\r\n", executMethodStack.toFullTreeString()));
        executMethodStackInfos.append(BREAK_LINE);
        executMethodStackInfos.append(BREAK_LINE);

        List<Throwable> throwables = executMethodStack.getThrowables();
        if (null != throwables && !throwables.isEmpty()) {
            StringBuilder throwablesInfos = new StringBuilder(throwables.size() * 256);
            for (Throwable t : throwables) {
                ByteArrayOutputStream throwableOut = new ByteArrayOutputStream();
                PrintStream throwableStream = new PrintStream(throwableOut);
                t.printStackTrace(throwableStream);
                throwablesInfos.append(new String(throwableOut.toByteArray()));
                throwablesInfos.append(BREAK_LINE);
            }
            executMethodStackInfos.append(String.format("EXCEPTIONS:\r\n%s\r\n", throwablesInfos.toString()));
        }

        List<String> sqlList = getAllSql(executMethodStack);
        if (null != sqlList && !sqlList.isEmpty()) {
            executMethodStackInfos.append(BREAK_LINE);
            executMethodStackInfos.append(BREAK_LINE);
            StringBuilder sqlStrInfos = new StringBuilder(throwables.size() * 256);
            for (String sql : sqlList) {
                sqlStrInfos.append(sql);
                sqlStrInfos.append(BREAK_LINE);
            }
            executMethodStackInfos.append(String.format("EXECUT SQL:\r\n%s\r\n", sqlStrInfos.toString()));
        }

        return this;
    }

    private List<String> getAllSql(MonitorStackNode executMethodStack) {
        List<String> sqlList = new LinkedList<String>();

        List<MonitorStackNode> childs = executMethodStack.getChildStackNodes();
        if (CollectionUtils.isNotEmpty(childs)) {
            for (MonitorStackNode stackNode : childs) {
                if (StackNodeTypeEnum.SQL.equals(executMethodStack.getStackNodetype())) {
                    sqlList.add(executMethodStack.getSignature());
                }
            }
        }

        return sqlList;
    }

    /**
     * 格式化当前JVM的信息
     * 
     * @param logJVMInfo
     * @return
     */
    public ExcecutLogFormat bulid(boolean logJVMInfo) {
        if (!logJVMInfo) {
            return this;
        }

        executJVMInfos.append("\r\nJVM INFO:\r\n\r\n");

        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        long maxMemory = Runtime.getRuntime().maxMemory();

        executJVMInfos.append(String.format("JVM MAX MEMORY:%d(MB)\r\n", maxMemory / (1024 * 1204)));
        executJVMInfos.append(String.format("JVM TOTAL MEMORY:%d(MB)\r\n", totalMemory / (1024 * 1204)));
        executJVMInfos.append(String.format("JVM FREE MEMORY:%d(MB)\r\n", freeMemory / (1024 * 1204)));

        MemoryMXBean mm = (MemoryMXBean) ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = mm.getHeapMemoryUsage();
        if (null != heapMemoryUsage) {
            executJVMInfos.append(String.format(
                    "JVM HeapMemory : Init[%d(MB)],Max[%d(MB)],Used[%d(MB)],Committed[%d(MB)]\r\n",
                    heapMemoryUsage.getInit() / (1024 * 1204), heapMemoryUsage.getMax() / (1024 * 1204),
                    heapMemoryUsage.getUsed() / (1024 * 1204), heapMemoryUsage.getCommitted() / (1024 * 1204)));
        }

        MemoryUsage nonHeapMemoryUsage = mm.getNonHeapMemoryUsage();
        if (null != nonHeapMemoryUsage) {
            executJVMInfos.append(String.format(
                    "JVM NonHeapMemory : Init[%d(MB)],Max[%d(MB)],Used[%d(MB)],Committed[%d(MB)]\r\n",
                    nonHeapMemoryUsage.getInit() / (1024 * 1204), nonHeapMemoryUsage.getMax() / (1024 * 1204),
                    nonHeapMemoryUsage.getUsed() / (1024 * 1204), nonHeapMemoryUsage.getCommitted() / (1024 * 1204)));
        }

        List<GarbageCollectorMXBean> gcmList = ManagementFactory.getGarbageCollectorMXBeans();
        if (null != gcmList || !gcmList.isEmpty()) {
            for (GarbageCollectorMXBean gcm : gcmList) {
                executJVMInfos.append(String.format("JVM GC[%s] : GCTims[%d(ms)],GCCount[%d],MemoryNames[%s]\r\n",
                        gcm.getName(), gcm.getCollectionTime(), gcm.getCollectionCount(),
                        join(gcm.getMemoryPoolNames(), ",")));
            }
        }
        executJVMInfos.append(BREAK_LINE);
        executJVMInfos.append(BREAK_LINE);

        // 获取运行时信息
        RuntimeMXBean rmb = (RuntimeMXBean) ManagementFactory.getRuntimeMXBean();
        executJVMInfos.append(String.format("ClassPath:%s", rmb.getClassPath()));
        executJVMInfos.append(BREAK_LINE);
        executJVMInfos.append(String.format("LibraryPath:%s", rmb.getLibraryPath()));
        executJVMInfos.append(BREAK_LINE);
        executJVMInfos.append(String.format("JVmVersion:%s", rmb.getVmVersion()));
        executJVMInfos.append(BREAK_LINE);

        // 死锁测试注释掉
        // SynchronizedDeadLockMock.startMock();
        // try {
        // Thread.sleep(200);
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }
        //
        // ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        // threadMXBean.setThreadContentionMonitoringEnabled(true);
        // threadMXBean.setThreadCpuTimeEnabled(true);
        // Thread.currentThread().getId();
        // ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(false, false);
        // ThreadInfo currentThreadInfo = threadMXBean.getThreadInfo(Thread.currentThread().getId());
        //
        // executJVMInfos.append(BREAK_LINE);
        // executJVMInfos.append(BREAK_LINE);
        // executJVMInfos.append(BREAK_LINE);
        //
        // executJVMInfos.append(BREAK_LINE + "------currentThreadInfo--------");
        // executJVMInfos.append(BREAK_LINE + currentThreadInfo.toString());
        // executJVMInfos.append(BREAK_LINE + "------currentThreadInfo--------");
        //
        // for(ThreadInfo threadInfo : threadInfos){
        // executJVMInfos.append(BREAK_LINE + threadInfo.toString());
        // executJVMInfos.append(BREAK_LINE + "--------------");
        // }

        return this;
    }

    private String join(String[] arr, String separator) {
        if (null == arr || arr.length == 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            stringBuilder.append(arr[i]);
            if (i < arr.length - 1) {
                stringBuilder.append(separator);
            }
        }
        return stringBuilder.toString();
    }

    public String toString() {

        StringBuilder infos =
                new StringBuilder(httpRequestInfos.length() + executMethodStackInfos.length() + executJVMInfos.length()
                        + 64);
        infos.append(httpRequestInfos);
        infos.append(BREAK_LINE);
        infos.append(executMethodStackInfos);
        infos.append(BREAK_LINE);
        infos.append(executJVMInfos);
        infos.append(BREAK_LINE);

        return infos.toString();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public HttpServletRequest getHttpRequest() {
        return httpRequest;
    }

    public void setHttpRequest(HttpServletRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

}
