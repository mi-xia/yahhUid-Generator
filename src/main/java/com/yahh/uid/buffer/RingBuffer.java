package com.yahh.uid.buffer;

import com.yahh.uid.utils.PaddedAtomicLong;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 邹磊
 * @version 1.0
 * @description:
 * @date 2021/3/21 17:48
 */
public class RingBuffer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RingBuffer.class);

    private static final int START_POINT = -1;
    private static final long CAN_PUT_FLAG = 0L;
    private static final long CAN_TAKE_FLAG = 1L;
    public static final int DEFAULT_PADDING_PERCENT = 50;

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


    public long take() {
        // 获取当前消费的节点
        long currentCursor = cursor.get();
        long nextCursor = cursor.updateAndGet(old -> old == tail.get() ? old : old + 1);

        // 判断下一个要消费的节点要大于等于已经消费过的节点
        Assert.isTrue(nextCursor >= currentCursor, "Curosr can't move back");

        // 如果剩余可消费 slot 小于 paddingThreshold，则进行异步填充
        long currentTail = tail.get();
        if (currentTail - currentCursor < paddingThreshold) {
            LOGGER.info("Reach the padding threshold:{}. tail:{}, cursor:{}, rest:{}", paddingThreshold, currentTail,
                    nextCursor, currentTail - nextCursor);
            bufferPaddingExecutor.asyncPadding();
        }

        if (nextCursor == currentCursor) {
            // 说明已经消费到了最后
            rejectedTakeBufferHandler.rejectTakeBuffer(this);
        }

        int nextCursorIndex = calSlotIndex(nextCursor);
        Assert.isTrue(flags[nextCursorIndex].get() == CAN_TAKE_FLAG, "Curosr not in can take status");

        /**
         * 到这里就可以真正的取出id了
         */
        long uid = slots[nextCursorIndex];
        flags[nextCursorIndex].set(CAN_PUT_FLAG);

        return uid;
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
        LOGGER.warn("Rejected putting buffer for uid:{}. {}", uid, ringBuffer);
    }

    /**
     * Policy for {@link RejectedTakeBufferHandler}, throws {@link RuntimeException} after logging
     */
    protected void exceptionRejectedTakeBuffer(RingBuffer ringBuffer) {
        LOGGER.warn("Rejected take buffer. {}", ringBuffer);
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
