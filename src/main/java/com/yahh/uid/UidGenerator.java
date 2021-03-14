package com.yahh.uid;

import com.yahh.uid.exception.YahhUIDException;

/**
 * @author 邹磊
 * @version 1.0
 * @description:
 * @date 2021/3/14 16:01
 */
public interface UidGenerator {


    long getUID() throws YahhUIDException;


    String pareUID(long uid);

}
