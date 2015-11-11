/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.dpop.frame.core.uniqueid.impl;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.baidu.dpop.frame.core.uniqueid.StateService;
import com.baidu.dpop.frame.core.uniqueid.SysBizService;
import com.baidu.dpop.frame.core.uniqueid.UniqueElement;
import com.baidu.dpop.frame.core.uniqueid.UniqueidService;

/**
 * @author huhailiang
 * @date 2015年3月11日
 */
public final class DpopUniqueidService implements UniqueidService {

    private static final Logger LOGGER = Logger.getLogger(DpopUniqueidService.class);

    /**
     * 默认起始时间,2015年
     */
    private static final int DEFAULT_YEAR = 2015;

    /**
     * 总字节数目
     */
    private static final int TOTAL_BIT = 64;

    /**
     * 时间差值存储长度
     */
    private static final int MAX_TIME_TAMP_BIT = 41;

    /**
     * 时间差值左移位数
     */
    private static final int IME_TAMP_LEFT_BIT = TOTAL_BIT - MAX_TIME_TAMP_BIT;

    /**
     * 业务排他值存储长度
     */
    private static final int MAX_BIZ_BIT = 7;

    /**
     * 业务排他值左移位数
     */
    private static final int BIZ_LEFT_BIT = IME_TAMP_LEFT_BIT - MAX_BIZ_BIT;

    /**
     * 机器排他值存储长度
     */
    private static final int MAX_MAC_BIT = 4;

    /**
     * 机器排他值左移位数
     */
    private static final int MAC_LEFT_BIT = BIZ_LEFT_BIT - MAX_MAC_BIT;

    /**
     * 状态排他值存储长度
     */
    private static final int MAX_STATE_BIT = 4;

    /**
     * 状态排他值左移位数
     * 
     */
    private static final int STATE_LEFT_BIT = MAC_LEFT_BIT - MAX_STATE_BIT;

    /**
     * 自增值存储长度
     */
    private static final int MAX_SEQ_BIT = 8;

    /**
     * 自增值最大长度
     */
    private static final int MAX_SEQ_NUM = 255;

    /**
     * 自增值复位时，系统处理线程休眠时间，单位：毫秒
     */
    private static final long SEQ_RESET_SLEEP_TIME = 1L;

    /**
     * MAC地址后3位扩展字段
     */
    private int macExtId;

    /**
     * 初始化时间
     */
    private Date startDate;

    /**
     * 自增序列
     */
    private AtomicInteger sequence = new AtomicInteger(0);

    private long crrentTime = 0L;

    /**
     * 业务ID获取器
     */
    private SysBizService sysBizService;

    /**
     * 业务状态ID获取器
     */
    private StateService stateService;

    public DpopUniqueidService() {
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        initStartDate();
        if (null == sysBizService) {
            sysBizService = new DefaultSysBizService();
        }
        if (null == stateService) {
            stateService = new DefaultStateService();
        }
        try {
            macExtId = getMacExtId();
        } catch (Exception e) {
            LOGGER.error("InetAddressHepler.getMacExtId() has error:", e);
            macExtId = 0;
        }
    }

    /**
     * 初始化初始时间,默认初始化时间为 2015年1月1日 零时
     */
    private void initStartDate() {
        if (null == startDate) {
            Calendar startDateCalendar = Calendar.getInstance();
            startDateCalendar.set(DEFAULT_YEAR, 0, 1, 0, 0, 0);
            startDate = startDateCalendar.getTime();
        }
    }

    /**
     * 生成唯一编号
     * 
     * @see com.baidu.unbiz.monitor.requestid.IdService#generateId()
     */
    @Override
    public final long generateId() {
        long timestamp = getTimestamp();
        long bizId = Math.abs(sysBizService.getBiz());
        long stateId = Math.abs(stateService.getState());

        long generateId = timestamp << IME_TAMP_LEFT_BIT;
        generateId |= (bizId % 128) << BIZ_LEFT_BIT;
        generateId |= (macExtId % 16) << MAC_LEFT_BIT;
        generateId |= (stateId % 16) << STATE_LEFT_BIT;
        generateId |= sequence.incrementAndGet() % MAX_SEQ_NUM;

        return generateId;
    };

    /**
     * 
     * 解释唯一ID
     * 
     * @see com.baidu.unbiz.monitor.requestid.IdService#explainId(long)
     * @param id：分布式唯一ID
     * @return 分布式ID的组成元素，定义于 @see IdElement
     */
    @Override
    public final UniqueElement explainId(long id) {
        UniqueElement uniqueElement = new UniqueElement();
        long generateStartNum = 1L << IME_TAMP_LEFT_BIT;

        // 如果小于其实时间的，说明是不符号id生成策略，返回一个空对象
        
        if (id <= generateStartNum) {
            throw new RuntimeException("illegal id");
        }
        // id向右移动 64 - 41 位
        long timestamp = id >> IME_TAMP_LEFT_BIT;
        uniqueElement.setTimestamp(timestamp);

        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTimeInMillis(startDate.getTime() + timestamp);
        uniqueElement.setTimestampDate(dateCalendar.getTime());

        // id向先右位移(64 - 41 - 7)位，再向左位移(64 - 7) ，最后无符号右位移(64 - 7)
        long bizId = id >> BIZ_LEFT_BIT << (TOTAL_BIT - MAX_BIZ_BIT) >>> (TOTAL_BIT - MAX_BIZ_BIT);
        uniqueElement.setBizId(bizId);

        // id向先右位移(64 - 41 - 7 - 4)位，再向左位移64 - 4 ，最后无符号右位移64 - 4
        long macExt = id >> MAC_LEFT_BIT << (TOTAL_BIT - MAX_MAC_BIT) >>> (TOTAL_BIT - MAX_MAC_BIT);
        uniqueElement.setMachineId(macExt);

        // id向先右位移(64 - 41 - 7 - 4 -4 )位，再向左位移64 - 4 ，最后无符号右位移64 - 4
        long stateId = id >> STATE_LEFT_BIT << (TOTAL_BIT - MAX_STATE_BIT) >>> (TOTAL_BIT - MAX_STATE_BIT);
        uniqueElement.setStateId(stateId);

        // id向先右位移(64 - 41 - 7 - 4 -4 -8)位，再向左位移64 - 8 ，最后无符号右位移64 - 8
        long sequenceId = id << (TOTAL_BIT - MAX_SEQ_BIT) >>> (TOTAL_BIT - MAX_SEQ_BIT);
        uniqueElement.setSequence(sequenceId);

        return uniqueElement;
    }

    private long getTimestamp() {
        if (null == startDate) {
            initStartDate();
        }
        if (sequence.compareAndSet(MAX_SEQ_NUM, 0)) {
            // 当随机自增号复位时，当前时间需等一毫秒，尽量保证ID是随时间序列递增
            while (System.currentTimeMillis() == crrentTime) {
                try {
                    Thread.sleep(SEQ_RESET_SLEEP_TIME);
                } catch (InterruptedException e) {
                    LOGGER.error(String.format("sequence is [%d] reset Thread.sleep(%d) has error", MAX_SEQ_NUM,
                            SEQ_RESET_SLEEP_TIME), e);
                }
            }
        }
        crrentTime = System.currentTimeMillis();
        return crrentTime - startDate.getTime();
    }

    public static int getMacExtId() throws Exception {
        InetAddress inetAddress = InetAddress.getLocalHost();
        NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
        int macExtId = 0;
        byte[] mac = networkInterface.getHardwareAddress();
        for (int index = 3; index < mac.length; index++) {
            // 字节转换为整数
            int temp = mac[index] & 0xff;
            macExtId |= temp << (8 * (5 - index));
        }
        return macExtId;
    }

    public StateService getStateService() {
        return stateService;
    }

    public void setStateService(StateService stateService) {
        this.stateService = stateService;
    }

    public SysBizService getSysBizService() {
        return sysBizService;
    }

    public void setSysBizService(SysBizService sysBizService) {
        this.sysBizService = sysBizService;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}
