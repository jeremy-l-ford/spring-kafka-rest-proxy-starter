/*
 * Copyright 2019 Jeremy Ford
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS ISBASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jeremylford.spring.kafkarestproxy;

import com.github.jeremylford.spring.kafkarestproxy.properties.KafkaRestProperties;
import io.confluent.kafkarest.AdminClientWrapper;
import io.confluent.kafkarest.KafkaRestConfig;
import io.confluent.kafkarest.KafkaRestContext;
import io.confluent.kafkarest.ProducerPool;
import io.confluent.kafkarest.ScalaConsumersContext;
import io.confluent.kafkarest.extension.ContextInvocationHandler;
import io.confluent.kafkarest.extension.KafkaRestContextProvider;
import io.confluent.kafkarest.v2.KafkaConsumerManager;
import io.confluent.rest.RestConfigException;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.reflect.Proxy;

@EnableConfigurationProperties({KafkaRestProperties.class})
@Configuration
@Import(JerseyConfiguration.class)
public class KafkaRestProxyAutoConfiguration {


    @Bean
    public KafkaRestConfig kafkaRestConfig(KafkaRestProperties kafkaRestProperties) throws RestConfigException {
        return new KafkaRestConfig(kafkaRestProperties.asProperties());
    }

    @Bean
    public ScalaConsumersContext scalaConsumersContext(KafkaRestConfig kafkaRestConfig) {
        return new ScalaConsumersContext(kafkaRestConfig);
    }

    @Bean
    public AdminClientWrapper adminClientWrapper(KafkaRestConfig kafkaRestConfig, AdminClient adminClient) {
        return new AdminClientWrapper(kafkaRestConfig, adminClient);
    }

    @Bean
    public KafkaConsumerManager kafkaConsumerManager(KafkaRestConfig kafkaRestConfig) {
        return new KafkaConsumerManager(kafkaRestConfig);
    }

    @Bean
    public ProducerPool producerPool(KafkaRestConfig kafkaRestConfig) {
        return new ProducerPool(kafkaRestConfig);
    }

    @Bean
    public KafkaRestContext kafkaRestContext(KafkaRestConfig kafkaRestConfig,
                                             ProducerPool producerPool,
                                             KafkaConsumerManager kafkaConsumerManager,
                                             AdminClientWrapper adminClientWrapper,
                                             ScalaConsumersContext scalaConsumersContext) {
        KafkaRestContextProvider.initialize(kafkaRestConfig, producerPool, kafkaConsumerManager, adminClientWrapper, scalaConsumersContext);

        ContextInvocationHandler contextInvocationHandler = new ContextInvocationHandler();
        return (KafkaRestContext) Proxy.newProxyInstance(
                KafkaRestContext.class.getClassLoader(),
                new Class[]{KafkaRestContext.class},
                contextInvocationHandler
        );
    }

    @Bean
    public AdminClient adminClient(KafkaRestConfig kafkaRestConfig) {
        return AdminClient.create(AdminClientWrapper.adminProperties(kafkaRestConfig));
    }

}
