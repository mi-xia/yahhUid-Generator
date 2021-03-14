package com.yahh.uid.worker;

public enum WorkerNodeType {

    CONTAINER(1),

    ACTUAL(2);


    private final Integer type;


    private WorkerNodeType(Integer type) {
        this.type = type;
    }

    public Integer value() {
        return type;
    }

}
