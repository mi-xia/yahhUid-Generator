package com.yahh.uid.buffer;

import com.yahh.uid.utils.PaddedAtomicLong;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 邹磊
 * @version 1.0
 * @description:
 * @date 2021/3/21 17:48
 */
@Slf4j
public class RingBuffer {


    private static final int START_POINT = -1;
    private static final long CAN_PUT_FLAG = 0L;
    private static final long CAN_TAKE_FLAG = 1L;
    private static final int DEFAULT_PADDING_PERCENT = 50;

    private final int bufferSize;
    private final long indexMask;
    private final long[] slots;
    private final PaddedAtomicLong[] flags;


    /**
     * 最后生产的可以被消费的位置
     */
    private final AtomicLong tail = new PaddedAtomicLong(START_POINT);

    /**
     * 最后消费过的位置
     */
    private final AtomicLong cursor = new PaddedAtomicLong(START_POINT);

    private final int paddingThreshold;

    private RejectedPutBufferHandler rejectedPutBufferHandler = this::discardPutBuffer;
    private RejectedTakeBufferHandler rejectedTakeBufferHandler = this::exceptionRejectedTakeBuffer;

    private BufferPaddingExecutor bufferPaddingExecutor;


    public RingBuffer(int bufferSize) {
        this(bufferSize,DEFAULT_PADDING_PERCENT);
    }


    public RingBuffer(int bufferSize, int paddingFactor) {

        Assert.isTrue(bufferSize > 0L, "RingBuffer size must be positive");
        Assert.isTrue(Integer.bitCount(bufferSize) == 1, "RingBuffer size must be a power of 2");
        Assert.isTrue(paddingFactor > 0 && paddingFactor < 100, "RingBuffer size must be positive");

        this.bufferSize = bufferSize;
        this.indexMask = bufferSize -1;
        this.slots = new long[bufferSize];
        this.flags = initFlags(bufferSize);
        this.paddingThreshold = bufferSize * paddingFactor / 100;
    }


    public synchronized boolean put(long uid) {
        long currentTail = tail.get();
        long currentCursor = cursor.get();

        /**
         * 判断buffer环是否已经填满
         */
        long distance = currentTail - (currentCursor == START_POINT ? 0 : currentCursor);
        if (distance == bufferSize -1){
            rejectedPutBufferHandler.rejectPutBuffer(this,uid);
            return false;
        }

        //计算下一个可放入数据的slot
        int nextTailIndex = calSlotIndex(currentTail + 1);
        //首先判断下这个位置是否可以放入
        if (flags[nextTailIndex].get() != CAN_PUT_FLAG){
            rejectedPutBufferHandler.rejectPutBuffer(this,uid);
            return false;
        }

        /**
         * 首先往slots中放入uid
         */
        slots[nextTailIndex] = uid;
        /**
         * 然后将flags置为可消费
         */
        flags[nextTailIndex].set(CAN_TAKE_FLAG);
        /**
         * tail++  这里有个问题  tail是一直++的吗  不能清除？
         */
        tail.incrementAndGet();

        return true;
    }


    private int calSlotIndex(long sequence) {
        return (int) (sequence & indexMask);
    }

    /**
     * 初始化flags
     */
    private PaddedAtomicLong[] initFlags(int bufferSize){

        PaddedAtomicLong[] flags = new PaddedAtomicLong[bufferSize];
        for (int i = 0; i < bufferSize; i++){
            flags[i] = new PaddedAtomicLong(CAN_PUT_FLAG);
        }

        return flags;
    }


    /**
     * Discard policy for {@link RejectedPutBufferHandler}, we just do logging
     */
    protected void discardPutBuffer(RingBuffer ringBuffer, long uid) {
        log.warn("Rejected putting buffer for uid:{}. {}", uid, ringBuffer);
    }

    /**
     * Policy for {@link RejectedTakeBufferHandler}, throws {@link RuntimeException} after logging
     */
    protected void exceptionRejectedTakeBuffer(RingBuffer ringBuffer) {
        log.warn("Rejected take buffer. {}", ringBuffer);
        throw new RuntimeException("Rejected take buffer. " + ringBuffer);
    }


    /**
     * Getters
     */
    public long getTail() {
        return tail.get();
    }

    public long getCursor() {
        return cursor.get();
    }

    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Setters
     */
    public void setBufferPaddingExecutor(BufferPaddingExecutor bufferPaddingExecutor) {
        this.bufferPaddingExecutor = bufferPaddingExecutor;
    }

    public void setRejectedPutHandler(RejectedPutBufferHandler rejectedPutHandler) {
        this.rejectedPutBufferHandler = rejectedPutHandler;
    }

    public void setRejectedTakeHandler(RejectedTakeBufferHandler rejectedTakeHandler) {
        this.rejectedTakeBufferHandler = rejectedTakeHandler;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RingBuffer [bufferSize=").append(bufferSize)
                .append(", tail=").append(tail)
                .append(", cursor=").append(cursor)
                .append(", paddingThreshold=").append(paddingThreshold).append("]");

        return builder.toString();
    }

}
