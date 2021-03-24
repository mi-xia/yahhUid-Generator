package com.yahh.uid.buffer;

/**
 * @author 邹磊
 * @version 1.0
 * @description:
 * @date 2021/3/21 20:37
 */
@FunctionalInterface
public interface RejectedPutBufferHandler {

    void rejectPutBuffer(RingBuffer ringBuffer, long uid);

}
