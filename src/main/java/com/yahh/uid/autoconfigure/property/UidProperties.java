package com.yahh.uid.autoconfigure.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 邹磊
 * @version 1.0
 * @description:
 * @date 2021/3/15 12:25
 */
@ConfigurationProperties(prefix = "uid")
public class UidProperties {

    /**
     * delta seconds所占位数
     */
    private int timeBits = 28;

    /**
     * workid所占位数
     */
    private int workerBits = 22;

    /**
     * 每秒下的并发序列所占位数
     */
    private int seqBits = 13;

    /**
     * 时间基点
     */
    private String epochStr = "2021-03-15";

    /**
     * 缓存uid配置
     */
    private CachedUidProperties cached;

    /**
     * UidGenerator类型，standard表示标准版，cached表示使用了ringbuffer
     */
    private String type = "standard";

    public int getTimeBits() {
        return timeBits;
    }

    public void setTimeBits(int timeBits) {
        this.timeBits = timeBits;
    }

    public int getWorkerBits() {
        return workerBits;
    }

    public void setWorkerBits(int workerBits) {
        this.workerBits = workerBits;
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

    public CachedUidProperties getCached() {
        return cached;
    }

    public void setCached(CachedUidProperties cached) {
        this.cached = cached;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
