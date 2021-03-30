package com.yahh.uid.worker;

import com.yahh.uid.utils.DockerUtils;
import com.yahh.uid.utils.NetUtils;
import com.yahh.uid.worker.dao.WorkerNodeDAO;
import com.yahh.uid.worker.entity.WorkerNodeEntity;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author 邹磊
 * @version 1.0
 * @description:
 * @date 2021/3/14 18:19
 */
public class DisposableWorkerIdAssigner implements WorkerIdAssigner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DisposableWorkerIdAssigner.class);

    @Autowired
    private WorkerNodeDAO workerNodeDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long assignWorkerId() {

        WorkerNodeEntity workerNodeEntity = this.buildWorkerNode();

        workerNodeDAO.addWorkerNode(workerNodeEntity);

        LOGGER.info("Add worker node: " + workerNodeEntity);

        return workerNodeEntity.getId();
    }


    private WorkerNodeEntity buildWorkerNode(){
        WorkerNodeEntity workerNodeEntity = new WorkerNodeEntity();

        if (DockerUtils.isDocker()){
            workerNodeEntity.setType(WorkerNodeType.CONTAINER.value());
            workerNodeEntity.setHostName(DockerUtils.getDockerHost());
            workerNodeEntity.setPort(DockerUtils.getDockerPort());
        } else {
            workerNodeEntity.setType(WorkerNodeType.ACTUAL.value());
            workerNodeEntity.setHostName(NetUtils.getLocalAddress());
            workerNodeEntity.setPort(System.currentTimeMillis() + "-" + RandomUtils.nextInt(100000));
        }

        return workerNodeEntity;
    }


}
