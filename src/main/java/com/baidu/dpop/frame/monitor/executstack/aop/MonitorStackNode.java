package com.baidu.dpop.frame.monitor.executstack.aop;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * 监控执行栈中节点
 * 
 * @author huhailiang <br/>
 * @date: 2014-11-28 20:28:56 <br/>
 * 
 */
public class MonitorStackNode {

    /**
     * 
     */
    protected static final String BREAK_LINE = "\r\n";

    /**
     * 
     */
    protected static final String PREFIX_SEPERATE = "|-";

    /**
     * 
     */
    protected StackNodeTypeEnum stackNodetype;

    /**
     * 签名
     */
    protected String signature;

    /**
     * 起始时间
     */
    protected long startTime;
    /**
     * 结束时间
     */
    protected long endTime;

    /**
     * 栈的层数
     */
    protected int stackLevel;

    /**
     * 执行期间抛出的异常，如果没抛出则为空
     */
    protected Throwable exception;

    /**
     * 方法的调用者方法
     */
    protected MonitorStackNode parentStackNode;

    /**
     * 方法内部调用的方法
     */
    protected List<MonitorStackNode> childStackNodes;

    public MonitorStackNode(String methodSignature, StackNodeTypeEnum nodeType) {
        this.signature = methodSignature;
        this.stackNodetype = nodeType;
        this.setStartTime(System.currentTimeMillis());
    }

    /**
     * 新增方法调用的子方法
     * 
     * @param executMethodStack
     */
    public void addChlid(MonitorStackNode executMethodStack) {
        if (childStackNodes == null) {
            childStackNodes = new ArrayList<MonitorStackNode>();
        }
        childStackNodes.add(executMethodStack);
    }

    /**
     * 获取方法抛出的所有异常
     * 
     * @return
     */
    public List<Throwable> getThrowables() {
        List<Throwable> throwables = new LinkedList<Throwable>();
        if (null != this.exception) {
            throwables.add(this.exception);
        }
        if (null != childStackNodes && !childStackNodes.isEmpty()) {
            for (MonitorStackNode executMethodStack : childStackNodes) {
                throwables.addAll(executMethodStack.getThrowables());
            }
        }
        return throwables;
    }

    /**
     * 
     * @return
     */
    public String toFullTreeString() {
        StringBuilder methodDes = new StringBuilder();
        methodDes.append(toSelfTreeString());
        methodDes.append(BREAK_LINE);
        methodDes.append(toChildTreeString());
        return methodDes.toString();
    }

    /**
     * 
     * @return
     */
    public String toChildTreeString() {
        if (null == childStackNodes || childStackNodes.isEmpty()) {
            return "";
        }
        StringBuilder childMethodSignatureDes = new StringBuilder();
        for (MonitorStackNode methodStack : childStackNodes) {
            childMethodSignatureDes.append(methodStack.toFullTreeString());
        }
        return childMethodSignatureDes.toString();
    }

    /**
     * 方法的方法签名描述
     * 
     * @return
     */
    public String toSelfTreeString() {

        int initDesLength = (PREFIX_SEPERATE.length() * stackLevel);
        initDesLength += (null == signature) ? 0 : signature.length() + 56;

        final boolean hasException = null != exception;
        String exceptionTreeString = "";
        if (hasException) {
            initDesLength += (PREFIX_SEPERATE.length() * (stackLevel + 1));
            StackTraceElement[] error = exception.getStackTrace();
            exceptionTreeString =
                    String.format("Exception(%s):%s[%s]", exception.getClass().getName(), error[0].toString(),
                            exception.getMessage());
        }

        StringBuilder methodSignatureDes = new StringBuilder(initDesLength);
        for (int i = 0; i < stackLevel; i++) {
            methodSignatureDes.append(PREFIX_SEPERATE);
        }
        methodSignatureDes.append(signature);
        methodSignatureDes.append(String.format("%d(ms)", endTime - startTime));

        if (hasException) {
            methodSignatureDes.append(BREAK_LINE);
            for (int i = 0; i < stackLevel + 1; i++) {
                methodSignatureDes.append(PREFIX_SEPERATE);
            }
            methodSignatureDes.append(exceptionTreeString);
        }

        return methodSignatureDes.toString();
    }

    public String toString() {
        String str =
                String.format("{stackNodetype:%s;signature:%s;startTime:%d;endTime:%d}", stackNodetype.getDesc(),
                        signature, startTime, endTime);
        return str;
    }

    public StackNodeTypeEnum getStackNodetype() {
        return stackNodetype;
    }

    public void setStackNodetype(StackNodeTypeEnum stackNodetype) {
        this.stackNodetype = stackNodetype;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getStackLevel() {
        return stackLevel;
    }

    public void setStackLevel(int stackLevel) {
        this.stackLevel = stackLevel;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public MonitorStackNode getParentStackNode() {
        return parentStackNode;
    }

    public void setParentStackNode(MonitorStackNode parentStackNode) {
        this.parentStackNode = parentStackNode;
    }

    public List<MonitorStackNode> getChildStackNodes() {
        return childStackNodes;
    }

    public void setChildStackNodes(List<MonitorStackNode> childStackNodes) {
        this.childStackNodes = childStackNodes;
    }
}
