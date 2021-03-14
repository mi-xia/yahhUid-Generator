package com.yahh.uid.impl;

import com.yahh.uid.UidGenerator;
import com.yahh.uid.exception.YahhUIDException;
import com.yahh.uid.worker.BitsAllocator;
import com.yahh.uid.worker.WorkerIdAssigner;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.InitializingBean;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author 邹磊
 * @version 1.0
 * @description:
 * @date 2021/3/14 18:57
 */
@Slf4j
public class DefaultUidGenerator implements UidGenerator, InitializingBean {

    private int timeBits = 28;
    private int workerIdBits = 22;
    private int seqBits = 13;

    private String epochStr = "2021-03-01";
    private Long epochSeconds = TimeUnit.MILLISECONDS.toSeconds(1614528000000L);

    private BitsAllocator bitsAllocator;
    private Long workerId;

    protected long sequence = 0L;
    protected long lastSecond = -1L;

    private WorkerIdAssigner workerIdAssigner;

    @Override
    public long getUID() throws YahhUIDException {

        try {
            return this.nextId();
        } catch (Exception e){
            log.error("Generate unique id exception. ", e);
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

        log.info("Initialized bits(1, {}, {}, {}) for workerID:{}", timeBits, workerIdBits, seqBits, workerId);
    }
}
