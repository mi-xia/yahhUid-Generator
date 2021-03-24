package com.yahh.uid.buffer;

import java.util.List;

/**
 * @author 邹磊
 * @version 1.0
 * @description:
 * @date 2021/3/21 20:41
 */
@FunctionalInterface
public interface BufferedUidProvider {

    List<Long> proide(long momentInSecond);
}
