package com.baidu.dpop.frame.core.servlet;

/**
 * HTTP分段请求信息
 */
class Range {

    private long start;
    private long end;

    /**
     * 构造一个资源范围对象。
     *
     * @param start 起始下标（包括）
     * @param end 结束下标（不包括）
     */
    public Range(long start, long end) {
        if (start < 0 || end < 0) {
            throw new IllegalArgumentException("argument must not be negative");
        }
        if (start > end) {
            throw new IllegalArgumentException("start index must not be greater than end index");
        }
        this.start = start;
        this.end = end;
    }

    /**
     * @return 起始下标（包括）
     */
    public long getStart() {
        return start;
    }

    /**
     * @return 结束下标（不包括）
     */
    public long getEnd() {
        return end;
    }

    /**
     * @return 范围的长度
     */
    public long getLength() {
        return end - start;
    }
}
