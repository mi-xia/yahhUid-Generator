package com.yahh.uid.impl;

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
public class CachedUidGenerator extends DefaultUidGenerator {
}
