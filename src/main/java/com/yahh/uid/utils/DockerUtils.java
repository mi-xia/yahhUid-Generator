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

import com.yahh.uid.worker.DisposableWorkerIdAssigner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 邹磊
 * @version 1.0
 * @description:
 * @date 2021/3/14 17:42
 */
public abstract class DockerUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerUtils.class);

    /** 环境变量参数 */
    private static final String ENV_KEY_HOST = "JPAAS_HOST";
    private static final String ENV_KEY_PORT = "JPAAS_HTTP_PORT";
    private static final String ENV_KEY_PORT_ORIGINAL = "JPAAS_HOST_PORT_8080";

    /** Docker host & port */
    private static String DOCKER_HOST = "";
    private static String DOCKER_PORT = "";

    /** 判断是否是在docker环境下 */
    private static boolean IS_DOCKER;

    static {
        retrieveFromEnv();
    }

    /**
     * 返回 docker host
     */
    public static String getDockerHost() {
        return DOCKER_HOST;
    }

    /**
     * 返回 docker port
     */
    public static String getDockerPort() {
        return DOCKER_PORT;
    }

    /**
     * 否是在docker环境下
     */
    public static boolean isDocker() {
        return IS_DOCKER;
    }

    /**
     * Retrieve host & port from environment
     */
    private static void retrieveFromEnv() {
        // 从环境变量中获取 host & port
        DOCKER_HOST = System.getenv(ENV_KEY_HOST);
        DOCKER_PORT = System.getenv(ENV_KEY_PORT);

        if (StringUtils.isBlank(DOCKER_PORT)) {
            DOCKER_PORT = System.getenv(ENV_KEY_PORT_ORIGINAL);
        }

        boolean hasEnvHost = StringUtils.isNotBlank(DOCKER_HOST);
        boolean hasEnvPort = StringUtils.isNotBlank(DOCKER_PORT);

        if (hasEnvHost && hasEnvPort) {
            IS_DOCKER = true;

        } else if (!hasEnvHost && !hasEnvPort) {
            IS_DOCKER = false;

        } else {
            LOGGER.error("Missing host or port from env for Docker. host:{}, port:{}", DOCKER_HOST, DOCKER_PORT);
            throw new RuntimeException(
                    "Missing host or port from env for Docker. host:" + DOCKER_HOST + ", port:" + DOCKER_PORT);
        }
    }

}
