package com.yahh.uid.worker;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.util.Assert;

/**
 * @author 邹磊
 * @version 1.0
 * @description: 构建一个64位的比特
 * @date 2021/3/14 17:05
 */
public class BitsAllocator {


    /**
     * 总长度为64位
     */
    public static final int TOTAL_BITS =1 << 6;

    /**
     * 一个id的构成由一下组成（默认配置）
     * sign 1位 保证生成的id为正数
     * second 28位 代表当前的时间戳（距离某一个时间点，秒级）
     * worker_node_id 22位 机器id 最多记录400w次机器启动
     * sequence 13位 每秒下的并发序列号，最多支持每秒8192次并发
     */
    private int signBits = 1;
    private final int timestampBits;
    private final int workerIdBits;
    private final int sequenceBits;

    /**
     * 各个结构的最大值
     */
    private final long maxDeltaSeconds;
    private final long maxWorkerId;
    private final long maxSequence;

    /**
     * 计算时间戳的位置
     */
    private final int timestampShift;
    /**
     * 计算workid的偏移位置
     */
    private final int workerIdShift;


    public BitsAllocator(int timestampBits, int workerIdBits, int sequenceBits) {

        int allcotolTotal = signBits + timestampBits + workerIdBits + sequenceBits;
        Assert.isTrue(allcotolTotal == TOTAL_BITS,"not enough 64 bits");

        /**
         * 初始化
         */
        this.timestampBits = timestampBits;
        this.workerIdBits = workerIdBits;
        this.sequenceBits = sequenceBits;

        /**
         * 计算各结构的最大值
         * -1 = 11111111
         */
        this.maxDeltaSeconds = ~(-1L << timestampBits);
        this.maxWorkerId = ~(-1L << workerIdBits);
        this.maxSequence = ~(-1L << sequenceBits);

        this.timestampShift = workerIdBits + sequenceBits;
        this.workerIdShift = sequenceBits;
    }

    public long allocte(long deltaSeconds, long workerId, long sequence){
        return (deltaSeconds << timestampShift) | (workerId << workerIdShift) | sequence;
    }

    public static int getTotalBits() {
        return TOTAL_BITS;
    }

    public int getSignBits() {
        return signBits;
    }

    public void setSignBits(int signBits) {
        this.signBits = signBits;
    }

    public int getTimestampBits() {
        return timestampBits;
    }

    public int getWorkerIdBits() {
        return workerIdBits;
    }

    public int getSequenceBits() {
        return sequenceBits;
    }

    public long getMaxDeltaSeconds() {
        return maxDeltaSeconds;
    }

    public long getMaxWorkerId() {
        return maxWorkerId;
    }

    public long getMaxSequence() {
        return maxSequence;
    }

    public int getTimestampShift() {
        return timestampShift;
    }

    public int getWorkerIdShift() {
        return workerIdShift;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
