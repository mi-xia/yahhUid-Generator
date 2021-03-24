package com.yahh.uid.buffer;

import com.yahh.uid.utils.NamingThreadFactory;
import com.yahh.uid.utils.PaddedAtomicLong;
import lombok.extern.slf4j.Slf4j;

import java.rmi.Naming;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 邹磊
 * @version 1.0
 * @description:
 * @date 2021/3/21 20:49
 */
@Slf4j
public class BufferPaddingExecutor {

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

        log.info("Ready to padding buffer lastSecond:{}. {}",lastSecond.get(),ringBuffer);

        //校验是否正在填充中
        if (!running.compareAndSet(false,true)){
            log.info("Padding buffer is still running. {}",ringBuffer);
            return;
        }




    }


    public void shutDown(){
        if (!bufferPadExecutors.isShutdown()){
            bufferPadExecutors.shutdown();
        }

        if (bufferPadSchedule != null && !bufferPadSchedule.isShutdown()){
            bufferPadSchedule.shutdown();
        }
    }


    public boolean isRunning(){
        return running.get();
    }






}
