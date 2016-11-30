/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.common.spring.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Christian Bremer
 *
 */
@ConfigurationProperties("bremersee.scheduling")
public class SchedulingProperties {
    
    private int scheduledThreadPool = 100;
    
    private int taskExecutorCorePoolSize = 100;

    private int taskExecutorMaxPoolSize = Integer.MAX_VALUE;

    private int taskExecutorQueueCapacity = Integer.MAX_VALUE;

    private int taskExecutorKeepAliveSeconds = 60;

    private String taskExecutorThreadNamePrefix = "b2cTaskExecutor-";

    @Override
    public String toString() {
        // @formatter:off
        return "SchedulingProperties ["
                + "\n- scheduledThreadPool=" + scheduledThreadPool 
                + ", \n- taskExecutorCorePoolSize=" + taskExecutorCorePoolSize 
                + ", \n- taskExecutorMaxPoolSize=" + taskExecutorMaxPoolSize 
                + ", \n- taskExecutorQueueCapacity=" + taskExecutorQueueCapacity 
                + ", \n- taskExecutorKeepAliveSeconds=" + taskExecutorKeepAliveSeconds
                + ", \n- taskExecutorThreadNamePrefix=" + taskExecutorThreadNamePrefix 
                + "]";
        // @formatter:on
    }

    public int getScheduledThreadPool() {
        return scheduledThreadPool;
    }

    public void setScheduledThreadPool(int scheduledThreadPool) {
        this.scheduledThreadPool = scheduledThreadPool;
    }

    public int getTaskExecutorCorePoolSize() {
        return taskExecutorCorePoolSize;
    }

    public void setTaskExecutorCorePoolSize(int taskExecutorCorePoolSize) {
        this.taskExecutorCorePoolSize = taskExecutorCorePoolSize;
    }

    public int getTaskExecutorMaxPoolSize() {
        return taskExecutorMaxPoolSize;
    }

    public void setTaskExecutorMaxPoolSize(int taskExecutorMaxPoolSize) {
        this.taskExecutorMaxPoolSize = taskExecutorMaxPoolSize;
    }

    public int getTaskExecutorQueueCapacity() {
        return taskExecutorQueueCapacity;
    }

    public void setTaskExecutorQueueCapacity(int taskExecutorQueueCapacity) {
        this.taskExecutorQueueCapacity = taskExecutorQueueCapacity;
    }

    public int getTaskExecutorKeepAliveSeconds() {
        return taskExecutorKeepAliveSeconds;
    }

    public void setTaskExecutorKeepAliveSeconds(int taskExecutorKeepAliveSeconds) {
        this.taskExecutorKeepAliveSeconds = taskExecutorKeepAliveSeconds;
    }

    public String getTaskExecutorThreadNamePrefix() {
        return taskExecutorThreadNamePrefix;
    }

    public void setTaskExecutorThreadNamePrefix(
            String taskExecutorThreadNamePrefix) {
        this.taskExecutorThreadNamePrefix = taskExecutorThreadNamePrefix;
    }

}
