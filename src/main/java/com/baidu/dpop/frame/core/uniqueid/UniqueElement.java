/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.dpop.frame.core.uniqueid;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author huhailiang
 * @date 2015年3月11日
 */
public class UniqueElement {

    private long timestamp;

    private Date timestampDate;

    private long bizId;

    private long machineId;

    private long stateId;

    private long sequence;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getBizId() {
        return bizId;
    }

    public void setBizId(long bizId) {
        this.bizId = bizId;
    }

    public long getMachineId() {
        return machineId;
    }

    public void setMachineId(long machineId) {
        this.machineId = machineId;
    }

    public long getStateId() {
        return stateId;
    }

    public void setStateId(long stateId) {
        this.stateId = stateId;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    public Date getTimestampDate() {
        return timestampDate;
    }

    public void setTimestampDate(Date timestampDate) {
        this.timestampDate = timestampDate;
    }

    @Override
    public String toString() {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        return "uniqueid {timestampDate=" + format.format(timestampDate) + ", timestamp=" + timestamp + ", bizId="
                + bizId + ", machineId=" + machineId + ", stateId=" + stateId + ", sequence=" + sequence + "}";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (bizId ^ (bizId >>> 32));
        result = prime * result + (int) (machineId ^ (machineId >>> 32));
        result = prime * result + (int) (sequence ^ (sequence >>> 32));
        result = prime * result + (int) (stateId ^ (stateId >>> 32));
        result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UniqueElement other = (UniqueElement) obj;
        if (bizId != other.bizId)
            return false;
        if (machineId != other.machineId)
            return false;
        if (sequence != other.sequence)
            return false;
        if (stateId != other.stateId)
            return false;
        if (timestamp != other.timestamp)
            return false;
        return true;
    }
}
