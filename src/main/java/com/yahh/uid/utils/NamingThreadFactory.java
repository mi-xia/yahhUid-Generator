package com.yahh.uid.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 邹磊
 * @version 1.0
 * @description: 自定义一个线程工厂
 * @date 2021/3/21 23:10
 */
public class NamingThreadFactory implements ThreadFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(NamingThreadFactory.class);

    /**
     * 线程名称
     */
    private String name;

    private boolean isDaemon;

    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    private final ConcurrentHashMap<String, AtomicLong> sequences;

    public NamingThreadFactory() {
        this(null, false, null);
    }

    public NamingThreadFactory(String name) {
        this(name, false, null);
    }

    public NamingThreadFactory(String name, boolean isDaemon, Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.name = name;
        this.isDaemon = isDaemon;
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
        this.sequences = new ConcurrentHashMap<>();
    }


    @Override
    public Thread newThread(Runnable r) {

        Thread thread = new Thread(r);
        thread.setDaemon(isDaemon);

        String prefix = this.name;
        if (StringUtils.isBlank(prefix)){
            prefix = getInvoker(2);
        }
        thread.setName(prefix + "-" + getSequence(prefix));

        if (this.uncaughtExceptionHandler != null){
            thread.setUncaughtExceptionHandler(this.uncaughtExceptionHandler);
        } else {
            thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    LOGGER.error("unhandled exception in thread: " + t.getId() + ":" + t.getName(), e);
                }
            });
        }

        return thread;
    }


    private String getInvoker(int depth) {
        Exception e = new Exception();
        StackTraceElement[] stes = e.getStackTrace();
        if (stes.length > depth) {
            return ClassUtils.getShortClassName(stes[depth].getClassName());
        }
        return getClass().getSimpleName();
    }


    private long getSequence(String invoker) {
        AtomicLong r = this.sequences.get(invoker);
        if (r == null) {
            r = new AtomicLong(0);
            AtomicLong previous = this.sequences.putIfAbsent(invoker, r);
            if (previous != null) {
                r = previous;
            }
        }

        return r.incrementAndGet();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDaemon() {
        return isDaemon;
    }

    public void setDaemon(boolean daemon) {
        isDaemon = daemon;
    }

    public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return uncaughtExceptionHandler;
    }

    public void setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
    }

    public ConcurrentHashMap<String, AtomicLong> getSequences() {
        return sequences;
    }
}
