package com.baidu.dpop.frame.monitor.executstack.aop;

import org.junit.Test;

/**
 * 
 * @author huhailiang
 */
public class ExecutStackTraceTest {

    @Test
    public void testStackTrace() throws Exception {
        String clazz = "com.baidu.dpop.rmp.user.service.impl";

        ExecutStackTrace executStackTrace = new ExecutStackTrace();

        MonitorStackNode methodStack01 =
                new MonitorStackNode(String.format("%s.%s", clazz, "UserServiceImpl.get(Long)"),
                        StackNodeTypeEnum.METHOD);
        executStackTrace.entry(methodStack01);
        Thread.sleep(50L);

        MonitorStackNode methodStack011 =
                new MonitorStackNode(String.format("%s.%s", clazz, "TestServiceImpl.check(Long)"),
                        StackNodeTypeEnum.METHOD);
        executStackTrace.entry(methodStack011);
        Thread.sleep(10L);

        MonitorStackNode methodStack0111 =
                new MonitorStackNode(String.format("%s.%s", clazz, "AuthDaoImpl.doLogin(String,Long)"),
                        StackNodeTypeEnum.METHOD);
        executStackTrace.entry(methodStack0111);
        Thread.sleep(10L);
        executStackTrace.entrySql("delete FROM tb_document_category ", 110L, 10002L);
        executStackTrace.leave();

        executStackTrace.entrySql("delete FROM tb_document_category ", 110L, 10002L);
        executStackTrace.leave();

        Thread.sleep(50L);
        executStackTrace.leave();

        MonitorStackNode methodStack0112 =
                new MonitorStackNode(String.format("%s.%s", clazz, "PanguDaoImpl.doFinish(String)"),
                        StackNodeTypeEnum.METHOD);
        executStackTrace.entry(methodStack0112);

        executStackTrace.entrySql("SELECT * FROM tb_document_category ", 110L, 10002L);
        executStackTrace.leave();

        executStackTrace.entrySql("SELECT * FROM tb_document_category ", 110L, 10002L);
        executStackTrace.leave();

        Thread.sleep(50L);
        executStackTrace.leave();

        Thread.sleep(10L);
        executStackTrace.leave();

        MonitorStackNode methodStack012 =
                new MonitorStackNode(String.format("%s.%s", clazz, "CheckServiceImpl.doFinish(String)"),
                        StackNodeTypeEnum.METHOD);
        executStackTrace.entry(methodStack012);
        Thread.sleep(10L);

        MonitorStackNode methodStack0121 =
                new MonitorStackNode(String.format("%s.%s", clazz, "P1ServiceImpl.do2nish(String)"),
                        StackNodeTypeEnum.METHOD);
        executStackTrace.entry(methodStack0121);

        executStackTrace.entrySql(
                "delete 1212,12,21,12,12,21,,21,21,21,21,21,21,21,1,1,21,21,21,21,21 FROM tb_document_category ", 110L,
                10002L);
        executStackTrace.leave();

        Thread.sleep(10L);
        methodStack0121.setException(new Exception("user has an error"));
        executStackTrace.leave();

        Thread.sleep(10L);
        executStackTrace.leave();

        Thread.sleep(10L);
        executStackTrace.leave();

        System.out.println(executStackTrace.getTrace().toFullTreeString());

    }
}
