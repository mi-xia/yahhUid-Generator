package com.yahh.uid.exception;

/**
 * @author 邹磊
 * @version 1.0
 * @description:
 * @date 2021/3/14 16:02
 */
public class YahhUIDException extends RuntimeException {
    private static final long serialVersionUID = -146142488211059758L;


    public YahhUIDException(){
        super();
    }

    public YahhUIDException(String message){
        super(message);
    }


    public YahhUIDException(String msgFormat, Object... args) {
        super(String.format(msgFormat, args));
    }

    public YahhUIDException(Throwable cause) {
        super(cause);
    }
}
