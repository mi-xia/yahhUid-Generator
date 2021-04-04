package com.yahh.uid.worker.entity;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.Date;

/**
 * @author 邹磊
 * @version 1.0
 * @description:
 * @date 2021/3/14 17:42
 */
public class WorkerNodeEntity {

    private Long id;

    /**
     * CONTAINER: HostName, ACTUAL : IP.
     */
    private String hostName;

    /**
     * CONTAINER: Port, ACTUAL : Timestamp + Random(0-10000)
     */
    private String port;

    /**
     * {@link com.yahh.uid.worker.WorkerNodeType}
     */
    private int type;

    /**
     * Worker launch date, default now
     */
    private Date launchDate = new Date();

    /**
     * Created time
     */
    private Date created;

    /**
     * Last modified
     */
    private Date modified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Date getLaunchDate() {
        return launchDate;
    }

    public void setLaunchDateDate(Date launchDate) {
        this.launchDate = launchDate;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
