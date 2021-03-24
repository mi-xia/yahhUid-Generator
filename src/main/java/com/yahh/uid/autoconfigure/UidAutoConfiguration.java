package com.yahh.uid.autoconfigure;

import com.yahh.uid.autoconfigure.property.UidProperties;
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
@ConditionalOnClass({DefaultUidGenerator.class})
@EnableConfigurationProperties(UidProperties.class)
@MapperScan("com.yahh.uid.worker.dao")
public class UidAutoConfiguration {

    @Autowired
    UidProperties uidProperties;

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
    @ConditionalOnMissingBean
    WorkerIdAssigner workerIdAssigner(){
        return new DisposableWorkerIdAssigner();
    }

}
