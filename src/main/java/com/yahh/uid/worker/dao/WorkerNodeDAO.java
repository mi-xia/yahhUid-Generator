package com.yahh.uid.worker.dao;

import com.yahh.uid.worker.entity.WorkerNodeEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @author 邹磊
 * @version 1.0
 * @description:
 * @date 2021/3/14 18:12
 */
@Repository
public interface WorkerNodeDAO {

    @Select("SELECT " +
                   " ID," +
                   " HOST_NAME," +
                   " PORT," +
                   " TYPE," +
                   " LAUNCH_DATE," +
                   " MODIFIED," +
                   " CREATED" +
                   " FROM" +
                   " WORKER_NODE" +
                   " WHERE" +
                   " HOST_NAME = #{host,jdbcType=VARCHAR} AND PORT = #{port,jdbcType=VARCHAR}")
    WorkerNodeEntity getWorkerNodeByHostPort(@Param("host") String host, @Param("port") String port);


    @Insert("INSERT INTO WORKER_NODE" +
            "(HOST_NAME," +
            "PORT," +
            "TYPE," +
            "LAUNCH_DATE," +
            "MODIFIED," +
            "CREATED)" +
            "VALUES (" +
            "#{hostName}," +
            "#{port}," +
            "#{type}," +
            "#{launchDate}," +
            "NOW()," +
            "NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void addWorkerNode(WorkerNodeEntity workerNodeEntity);
}
