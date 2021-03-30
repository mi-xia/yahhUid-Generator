package com.yahh.uid.buffer;

import com.yahh.uid.utils.NamingThreadFactory;
import com.yahh.uid.utils.PaddedAtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author 邹磊
 * @version 1.0
 * @description:
 * @date 2021/3/21 20:49
 */
public class BufferPaddingExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(BufferPaddingExecutor.class);

    /**
     * 线程名称
     */
    private static final String WORKER_NAME = "RingBuffer-Padding-Worker";
    /**
     * 定时线程名称
     */
    private static final String SCHEDULE_NAME = "RingBuffer-Padding-Schedule";
    /**
     * 默认的定时时间
     */
    private static final long DEFAULT_SCHEDULE_INTERVAL = 5 * 60L; // 5 minutes


    /**
     * 判断当前是否正在进行填充
     */
    private final AtomicBoolean running;

    /**
     * 存储消费过的最后的时间
     * 借用未来的时间
     * 缓存填充
     */
    private final PaddedAtomicLong lastSecond;

    private final RingBuffer ringBuffer;

    private final BufferedUidProvider bufferedUidProvider;

    /**
     * 通过该线程池立即对ringbuffer进行填充
     */
    private final ExecutorService bufferPadExecutors;
    /**
     * 通过定时线程城填充
     */
    private final ScheduledExecutorService bufferPadSchedule;

    /** Schedule interval Unit as seconds */
    private long scheduleInterval = DEFAULT_SCHEDULE_INTERVAL;


    public BufferPaddingExecutor(RingBuffer ringBuffer, BufferedUidProvider bufferedUidProvider) {
        this(ringBuffer,bufferedUidProvider,true);
    }


    public BufferPaddingExecutor(RingBuffer ringBuffer, BufferedUidProvider bufferedUidProvider, Boolean usingSchedule) {
        this.running = new AtomicBoolean(false);
        this.lastSecond = new PaddedAtomicLong(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        this.ringBuffer = ringBuffer;
        this.bufferedUidProvider = bufferedUidProvider;

        /**
         * 初始化线程池
         */
        int coreCount = Runtime.getRuntime().availableProcessors();
        bufferPadExecutors = Executors.newFixedThreadPool(coreCount * 2, new NamingThreadFactory(WORKER_NAME));

        /**
         * 初始化定时线程池
         */
        if (usingSchedule){
            bufferPadSchedule = Executors.newSingleThreadScheduledExecutor(new NamingThreadFactory(SCHEDULE_NAME));
        } else {
            bufferPadSchedule = null;
        }

    }


    public void paddingBuffer(){

        LOGGER.info("Ready to padding buffer lastSecond:{}. {}",lastSecond.get(),ringBuffer);

        //校验是否正在填充中
        if (!running.compareAndSet(false,true)){
            LOGGER.info("Padding buffer is still running. {}",ringBuffer);
            return;
        }

        boolean isFullRingBuffer = false;

        while (!isFullRingBuffer) {
            List<Long> uidList = bufferedUidProvider.proide(lastSecond.incrementAndGet());
            for (Long uid : uidList) {
                isFullRingBuffer = !ringBuffer.put(uid);
                if (isFullRingBuffer) {
                    break;
                }
            }
        }

        // 填充结束
        running.compareAndSet(true, false);
        LOGGER.info("End to padding buffer lastSecond:{}. {}", lastSecond.get(), ringBuffer);

    }


    /**
     * 启用一个定时线程进行填充
     */
    public void start() {
        if (null != bufferPadSchedule) {
            bufferPadSchedule.scheduleWithFixedDelay(() -> paddingBuffer(), scheduleInterval, scheduleInterval, TimeUnit.SECONDS);
        }
    }


    /**
     * 通过线程池异步填充slot
     */
    public void asyncPadding() {
        bufferPadExecutors.submit(this::paddingBuffer);
    }

    /**
     * 关闭线程池
     */
    public void shutdown() {
        if (!bufferPadExecutors.isShutdown()) {
            bufferPadExecutors.shutdown();
        }
        if (null != bufferPadSchedule && !bufferPadSchedule.isShutdown()) {
            bufferPadSchedule.shutdown();
        }
    }

    public boolean isRunning(){
        return running.get();
    }


    /**
     * Setters
     */
    public void setScheduleInterval(long scheduleInterval) {
        Assert.isTrue(scheduleInterval > 0, "Schedule interval must positive!");
        this.scheduleInterval = scheduleInterval;
    }



}
