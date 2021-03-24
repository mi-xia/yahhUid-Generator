/*
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserve.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yahh.uid.utils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 解决java伪共享，进行缓存行填充
 * 详见：https://blog.csdn.net/qq_27680317/article/details/78486220
 * The CPU cache line commonly be 64 bytes, here is a sample of cache line after padding:<br>
 * 64 bytes = 8 bytes (object reference) + 6 * 8 bytes (padded long) + 8 bytes (a long value)
 *
 */
public class PaddedAtomicLong extends AtomicLong {

    private static final long serialVersionUID = -7831361943426529198L;

    /** 48 bytes */
    public volatile long p1, p2, p3, p4, p5, p6 = 7L;

    public PaddedAtomicLong() {
        super();
    }

    public PaddedAtomicLong(long initialValue) {
        super(initialValue);
    }

    /**
     * To prevent GC optimizations for cleaning unused padded references填充
     * 防止GC清除未被是用的
     */
    public long sumPaddingToPreventOptimization() {
        return p1 + p2 + p3 + p4 + p5 + p6;
    }

}