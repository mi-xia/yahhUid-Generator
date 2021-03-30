package com.yahh.uid.impl;

import com.yahh.uid.UidGenerator;
import com.yahh.uid.buffer.RingBuffer;
import com.yahh.uid.exception.YahhUIDException;
import com.yahh.uid.utils.DateUtils;
import com.yahh.uid.BitsAllocator;
import com.yahh.uid.worker.WorkerIdAssigner;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author 邹磊
 * @version 1.0
 * @description:
 * @date 2021/3/14 18:57
 */
public class DefaultUidGenerator implements UidGenerator, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUidGenerator.class);

    protected int timeBits = 28;
    protected int workerIdBits = 22;
    protected int seqBits = 13;

    protected String epochStr = "2021-03-01";
    protected Long epochSeconds = TimeUnit.MILLISECONDS.toSeconds(1614528000000L);

    protected BitsAllocator bitsAllocator;
    protected Long workerId;

    protected long sequence = 0L;
    protected long lastSecond = -1L;

    protected WorkerIdAssigner workerIdAssigner;

    @Override
    public long getUID() throws YahhUIDException {

        try {
            return this.nextId();
        } catch (Exception e){
            LOGGER.error("Generate unique id exception. ", e);
            throw new YahhUIDException(e);
        }
    }

    @Override
    public String pareUID(long uid) {
        long totalBits = BitsAllocator.TOTAL_BITS;
        long signBits = bitsAllocator.getSignBits();
        long timestampBits = bitsAllocator.getTimestampBits();
        long workerIdBits = bitsAllocator.getWorkerIdBits();
        long sequenceBits = bitsAllocator.getSequenceBits();

        // parse UID
        long sequence = (uid << (totalBits - sequenceBits)) >>> (totalBits - sequenceBits);
        long workerId = (uid << (timestampBits + signBits)) >>> (totalBits - workerIdBits);
        long deltaSeconds = uid >>> (workerIdBits + sequenceBits);

        Date thatTime = new Date(TimeUnit.SECONDS.toMillis(epochSeconds + deltaSeconds));
        String thatTimeStr = DateUtils.formatByDateTimePattern(thatTime);

        // format as string
        return String.format("{\"UID\":\"%d\",\"timestamp\":\"%s\",\"workerId\":\"%d\",\"sequence\":\"%d\"}",
                uid, thatTimeStr, workerId, sequence);
    }


    private synchronized long nextId(){
        long currentSecond = this.getCurrentSecond();


        if (currentSecond < lastSecond) {
            long refusedSeconds = lastSecond - currentSecond;
            throw new YahhUIDException("Clock moved backwards. Refusing for %d seconds", refusedSeconds);
        }

        if (currentSecond == lastSecond){
            /**
             * 处于同一秒内，则+1
             */
            sequence = (sequence + 1) & bitsAllocator.getMaxSequence();

            if (sequence == 0){
                /**
                 * 说明当前秒内8192个id已经用完，需要等待到下一秒重新生成id
                 */
                currentSecond = this.getNextSecond(lastSecond);
            }

        } else {
            /**
             * 不处于同一秒内，则从0开始
             */
            sequence = 0L;
        }

        lastSecond = currentSecond;
        return bitsAllocator.allocte(currentSecond - epochSeconds,workerId,sequence);
    }


    private long getCurrentSecond(){
        long currentSecond = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

        if (currentSecond - epochSeconds > bitsAllocator.getMaxDeltaSeconds()) {
            throw new YahhUIDException("Timestamp bits is exhausted. Refusing UID generate. Now: " + currentSecond);
        }
        return currentSecond;
    }

    private long getNextSecond(long lastTimestamp) {
        long timestamp = getCurrentSecond();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentSecond();
        }

        return timestamp;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        bitsAllocator = new BitsAllocator(timeBits,workerIdBits,seqBits);

        // 初始化workid
        workerId = workerIdAssigner.assignWorkerId();
        if (workerId > bitsAllocator.getMaxWorkerId()){
            throw new RuntimeException("Worker id " + workerId + " exceeds the max " + bitsAllocator.getMaxWorkerId());
        }

        LOGGER.info("Initialized bits(1, {}, {}, {}) for workerID:{}", timeBits, workerIdBits, seqBits, workerId);
    }

    public int getTimeBits() {
        return timeBits;
    }

    public void setTimeBits(int timeBits) {
        this.timeBits = timeBits;
    }

    public int getWorkerIdBits() {
        return workerIdBits;
    }

    public void setWorkerIdBits(int workerIdBits) {
        this.workerIdBits = workerIdBits;
    }

    public int getSeqBits() {
        return seqBits;
    }

    public void setSeqBits(int seqBits) {
        this.seqBits = seqBits;
    }

    public String getEpochStr() {
        return epochStr;
    }

    public void setEpochStr(String epochStr) {
        this.epochStr = epochStr;
    }

    public Long getEpochSeconds() {
        return epochSeconds;
    }

    public void setEpochSeconds(Long epochSeconds) {
        this.epochSeconds = epochSeconds;
    }

    public BitsAllocator getBitsAllocator() {
        return bitsAllocator;
    }

    public void setBitsAllocator(BitsAllocator bitsAllocator) {
        this.bitsAllocator = bitsAllocator;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    public long getLastSecond() {
        return lastSecond;
    }

    public void setLastSecond(long lastSecond) {
        this.lastSecond = lastSecond;
    }

    public WorkerIdAssigner getWorkerIdAssigner() {
        return workerIdAssigner;
    }

    public void setWorkerIdAssigner(WorkerIdAssigner workerIdAssigner) {
        this.workerIdAssigner = workerIdAssigner;
    }
}
