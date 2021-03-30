package com.yahh.uid.autoconfigure;

import com.yahh.uid.autoconfigure.property.CachedUidProperties;
import com.yahh.uid.autoconfigure.property.UidProperties;
import com.yahh.uid.buffer.RejectedPutBufferHandler;
import com.yahh.uid.buffer.RejectedTakeBufferHandler;
import com.yahh.uid.impl.CachedUidGenerator;
import com.yahh.uid.impl.DefaultUidGenerator;
import com.yahh.uid.worker.DisposableWorkerIdAssigner;
import com.yahh.uid.worker.WorkerIdAssigner;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * @author 邹磊
 * @version 1.0
 * @description:
 * @date 2021/3/15 12:34
 */
@Configuration
@ConditionalOnClass({DefaultUidGenerator.class, CachedUidGenerator.class})
@EnableConfigurationProperties(UidProperties.class)
@MapperScan("com.yahh.uid.worker.dao")
public class UidAutoConfiguration {

    @Autowired
    UidProperties uidProperties;

    @Autowired(required = false)
    RejectedPutBufferHandler rejectedPutBufferHandler;

    @Autowired(required = false)
    RejectedTakeBufferHandler rejectedTakeBufferHandler;

    @Bean
    @ConditionalOnProperty(prefix = "uid", name = "type", havingValue = "standard", matchIfMissing = true)
    @Lazy
    DefaultUidGenerator defaultUidGenerator(WorkerIdAssigner workerIdAssigner){

        DefaultUidGenerator uidGenerator = new DefaultUidGenerator();
        uidGenerator.setTimeBits(uidProperties.getTimeBits());
        uidGenerator.setWorkerIdBits(uidProperties.getWorkerBits());
        uidGenerator.setSeqBits(uidProperties.getSeqBits());
        uidGenerator.setEpochStr(uidProperties.getEpochStr());
        uidGenerator.setWorkerIdAssigner(workerIdAssigner);
        return uidGenerator;
    }

    @Bean
    @ConditionalOnProperty(prefix = "uid", name = "type", havingValue = "cached")
    @Lazy
    CachedUidGenerator cachedUidGenerator(WorkerIdAssigner workerIdAssigner){
        CachedUidGenerator uidGenerator = new CachedUidGenerator();
        uidGenerator.setTimeBits(uidProperties.getTimeBits());
        uidGenerator.setWorkerIdBits(uidProperties.getWorkerBits());
        uidGenerator.setSeqBits(uidProperties.getSeqBits());
        uidGenerator.setEpochStr(uidProperties.getEpochStr());
        uidGenerator.setWorkerIdAssigner(workerIdAssigner);

        if(uidProperties.getCached() == null){
            return uidGenerator;
        }

        CachedUidProperties cachedProperties = uidProperties.getCached();
        uidGenerator.setBoostPower(cachedProperties.getBoostPower());
        uidGenerator.setPaddingFactor(cachedProperties.getPaddingFactor());
        if(cachedProperties.getScheduleInterval() != null){
            uidGenerator.setScheduleInterval(cachedProperties.getScheduleInterval());
        }
        if(rejectedPutBufferHandler != null){
            uidGenerator.setRejectedPutBufferHandler(rejectedPutBufferHandler);
        }
        if(rejectedTakeBufferHandler != null){
            uidGenerator.setRejectedTakeBufferHandler(rejectedTakeBufferHandler);
        }

        return uidGenerator;
    }


    @Bean
    @ConditionalOnMissingBean
    WorkerIdAssigner workerIdAssigner(){
        return new DisposableWorkerIdAssigner();
    }

}
