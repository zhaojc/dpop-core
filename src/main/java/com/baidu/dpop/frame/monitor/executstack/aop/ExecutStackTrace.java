package com.baidu.dpop.frame.monitor.executstack.aop;

import java.util.List;
import java.util.Stack;

/**
 * J2EE监控执行栈
 * 
 * @author huhailiang
 * 
 */
public class ExecutStackTrace {

    private Stack<MonitorStackNode> executStack = new Stack<MonitorStackNode>();

    private MonitorStackNode topStackTrace;

    /**
     * 
     * |top | ------ | m4 | ------ | m3 | ------ | m2 | ------ | m1 | ------
     * 
     * 栈顶的层次:
     */
    private int topStackLevel = 0;

    public void entryJavaMethod(String methodSignature, Object[] methodArgs) {
        MonitorStackNode javaMethod = new MonitorStackNode(methodSignature, StackNodeTypeEnum.METHOD);
        entry(javaMethod);
    }

    public void entrySql(String sql, long startTime, long endTime) {
        MonitorStackNode sqlStackNode = new MonitorStackNode(sql, StackNodeTypeEnum.SQL);
        sqlStackNode.setStartTime(startTime);
        sqlStackNode.setEndTime(endTime);
        entry(sqlStackNode);
    }

    public void entryTransaction(String transactionDes) {
        MonitorStackNode sqlStackNode = new MonitorStackNode(transactionDes, StackNodeTypeEnum.TRANSCTION);
        sqlStackNode.setStartTime(System.currentTimeMillis());
        entry(sqlStackNode);
    }

    public void entry(MonitorStackNode monitorStackNode) {
        if (topStackLevel == 0) {
            topStackTrace = monitorStackNode;
        }

        if (topStackLevel > 0) { // add child
            executStack.peek().addChlid(monitorStackNode);
            monitorStackNode.setParentStackNode(executStack.peek());
        }
        monitorStackNode.setStackLevel(topStackLevel);
        executStack.push(monitorStackNode);
        topStackLevel++;
    }

    /**
     * 
     * @return
     */
    public MonitorStackNode leave() {
        topStackLevel--;
        if (topStackLevel < 0) {
            throw new RuntimeException("invalid level: current level is zero");
        }
        MonitorStackNode methodStack = executStack.pop();
        if (methodStack.getEndTime() <= 0) {
            methodStack.setEndTime(System.currentTimeMillis());
        }
        return methodStack;
    }

    /**
     * afterAdvice after afterThrowingAdvice
     * 
     * @param exception
     * @return
     */
    public MonitorStackNode setLeaveException(Throwable exception) {
        if (executStack.size() <= 0) {
            topStackTrace.setException(exception);
            return topStackTrace;
        }
        MonitorStackNode prExecutStackNode = executStack.peek();
        List<MonitorStackNode> childMethodStacks = prExecutStackNode.getChildStackNodes();
        if (null == childMethodStacks || childMethodStacks.isEmpty()) {
            return null;
        }
        MonitorStackNode lastExecutMethodStack = childMethodStacks.get(childMethodStacks.size() - 1);
        lastExecutMethodStack.setException(exception);
        return lastExecutMethodStack;
    }

    public MonitorStackNode getTrace() {
        return topStackTrace;
    }

}
