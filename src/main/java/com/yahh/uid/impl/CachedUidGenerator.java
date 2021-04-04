package com.yahh.uid.impl;

import com.yahh.uid.buffer.BufferPaddingExecutor;
import com.yahh.uid.buffer.RejectedPutBufferHandler;
import com.yahh.uid.buffer.RejectedTakeBufferHandler;
import com.yahh.uid.buffer.RingBuffer;
import com.yahh.uid.exception.YahhUIDException;
import jdk.nashorn.internal.runtime.linker.LinkerCallSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 邹磊
 * @version 1.0
 * @description:
 * @date 2021/3/21 17:36
 *
 * 参数说明：
 * boostPower：决定了RingBuffer buffer size的一个参数，bufferSize = (maxSequence + 1) << boostPower
 * paddingFactor: 决定向RingBuffer中填充UID的时机，是个百分比，默认50。当环上可用UID小于bufferSize * paddingFactor。对应即时填充机制。
 * scheduleInterval：也是决定向RingBuffer中填充UID的时机，单位秒。对应周期填充机制。
 * rejectedPutBufferHandler：拒绝策略: 当环已满, 无法继续填充时，需要实现RejectedPutBufferHandler接口
 * rejectedTakeBufferHandler：拒绝策略: 当环已空, 无法继续获取时，需要实现RejectedTakeBufferHandler接口
 *
 */
public class CachedUidGenerator extends DefaultUidGenerator implements DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachedUidGenerator.class);

    /**
     * 计算bufferSize使用
     */
    private static final int DEFAULT_BOOST_POWER = 3;

    private int boostPower = DEFAULT_BOOST_POWER;
    private int paddingFactor = RingBuffer.DEFAULT_PADDING_PERCENT;
    private Long scheduleInterval;

    private RejectedTakeBufferHandler rejectedTakeBufferHandler;
    private RejectedPutBufferHandler rejectedPutBufferHandler;

    private RingBuffer ringBuffer;
    private BufferPaddingExecutor bufferPaddingExecutor;


    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        this.initRingbuffer();
        LOGGER.info("Initialized RingBuffer successfully");
    }


    @Override
    public long getUID() throws YahhUIDException {
        try {
            return ringBuffer.take();
        } catch (Exception e) {
            LOGGER.error("Generate unique id exception: ", e);
            throw new YahhUIDException(e);
        }
    }

    @Override
    public String pareUID(long uid) {
        return super.pareUID(uid);
    }


    /**
     * 初始化
     */
    private void initRingbuffer() {
        // 初始化bufferSize
        int bufferSize = ((int) bitsAllocator.getMaxSequence() + 1) << boostPower;
        this.ringBuffer = new RingBuffer(bufferSize,paddingFactor);
        LOGGER.info("Initialized ring buffer size:{}, paddingFactor:{}", bufferSize, paddingFactor);

        // 初始化bufferpaddingExecutor
        boolean usingSchedule = (scheduleInterval != null);
        this.bufferPaddingExecutor = new BufferPaddingExecutor(ringBuffer,this::nextIdsForOneSecond, usingSchedule);
        if (usingSchedule) {
            bufferPaddingExecutor.setScheduleInterval(scheduleInterval);
        }
        LOGGER.info("Initialized BufferPaddingExecutor. Using schdule:{}, interval:{}", usingSchedule, scheduleInterval);

        this.ringBuffer.setBufferPaddingExecutor(bufferPaddingExecutor);
        if (rejectedPutBufferHandler != null) {
            this.ringBuffer.setRejectedPutHandler(rejectedPutBufferHandler);
        }
        if (rejectedTakeBufferHandler != null) {
            this.ringBuffer.setRejectedTakeHandler(rejectedTakeBufferHandler);
        }

        // 填充
        bufferPaddingExecutor.paddingBuffer();

        // 开启填充线程
        bufferPaddingExecutor.start();

    }


    protected List<Long> nextIdsForOneSecond(long currentSecond) {
        // 初始化结果集大小
        int listSize = (int) bitsAllocator.getMaxSequence() + 1;
        List<Long> uidList = new ArrayList<>(listSize);

        //获取到当前时间的第一个id,然后依次累加
        long firstSeqUid = bitsAllocator.allocte(currentSecond-epochSeconds,workerId,0L);
        for (int offset = 0; offset < listSize; offset++) {
            uidList.add(firstSeqUid + offset);
        }

        return uidList;
    }


    @Override
    public void destroy() throws Exception {
        bufferPaddingExecutor.shutdown();
    }


    public void setBoostPower(int boostPower) {
        Assert.isTrue(boostPower > 0, "Boost power must be positive!");
        this.boostPower = boostPower;
    }

    public void setRejectedPutBufferHandler(RejectedPutBufferHandler rejectedPutBufferHandler) {
        Assert.notNull(rejectedPutBufferHandler, "RejectedPutBufferHandler can't be null!");
        this.rejectedPutBufferHandler = rejectedPutBufferHandler;
    }

    public void setRejectedTakeBufferHandler(RejectedTakeBufferHandler rejectedTakeBufferHandler) {
        Assert.notNull(rejectedTakeBufferHandler, "RejectedTakeBufferHandler can't be null!");
        this.rejectedTakeBufferHandler = rejectedTakeBufferHandler;
    }

    public void setScheduleInterval(long scheduleInterval) {
        Assert.isTrue(scheduleInterval > 0, "Schedule interval must positive!");
        this.scheduleInterval = scheduleInterval;
    }

    public void setPaddingFactor(int paddingFactor) {
        Assert.isTrue(paddingFactor > 0 && paddingFactor < 100, "padding factor must be in (0, 100)!");
        this.paddingFactor = paddingFactor;
    }

}
