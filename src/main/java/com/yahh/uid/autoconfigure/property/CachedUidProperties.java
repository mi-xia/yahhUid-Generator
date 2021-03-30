package com.yahh.uid.autoconfigure.property;

/**
 * @author 邹磊
 * @version 1.0
 * @description: cached ui 的属性配置
 * @date 2021/3/15 12:21
 */
public class CachedUidProperties {

    /**
     * RingBuffer size扩容参数, 可提高UID生成的吞吐量
     */
    private int boostPower = 3;

    /**
     * 在Schedule线程中, 周期性检查填充
     * 默认:不配置此项, 即不使用Schedule线程. 如需使用, 请指定Schedule线程时间间隔, 单位:秒
     */
    private Long scheduleInterval;

    /**
     * RingBuffer 填充阈值
     */
    private int paddingFactor = 50;

    public int getBoostPower() {
        return boostPower;
    }

    public void setBoostPower(int boostPower) {
        this.boostPower = boostPower;
    }

    public Long getScheduleInterval() {
        return scheduleInterval;
    }

    public void setScheduleInterval(Long scheduleInterval) {
        this.scheduleInterval = scheduleInterval;
    }

    public int getPaddingFactor() {
        return paddingFactor;
    }

    public void setPaddingFactor(int paddingFactor) {
        this.paddingFactor = paddingFactor;
    }

}
