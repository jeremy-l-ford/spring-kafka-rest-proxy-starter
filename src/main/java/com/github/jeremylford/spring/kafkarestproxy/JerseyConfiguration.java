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

import io.confluent.kafkarest.KafkaRestConfig;
import io.confluent.kafkarest.controllers.BrokerManager;
import io.confluent.kafkarest.controllers.ConsumerManager;
import io.confluent.kafkarest.controllers.PartitionManager;
import io.confluent.kafkarest.controllers.TopicConfigManager;
import io.confluent.kafkarest.controllers.TopicManager;
import io.confluent.kafkarest.exceptions.RestConstraintViolationExceptionMapper;
import io.confluent.kafkarest.extension.RestResourceExtension;
import io.confluent.kafkarest.resources.ResourcesFeature;
import io.confluent.kafkarest.response.CrnFactory;
import io.confluent.kafkarest.response.UrlFactory;
import io.confluent.rest.exceptions.ConstraintViolationExceptionMapper;
import io.confluent.rest.exceptions.KafkaExceptionMapper;
import io.confluent.rest.exceptions.WebApplicationExceptionMapper;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.init.FilterUrlMappingsProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jersey.JerseyAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import javax.inject.Provider;
import javax.ws.rs.core.FeatureContext;
import java.util.List;

@Configuration
@AutoConfigureBefore(JerseyAutoConfiguration.class)
public class JerseyConfiguration extends ResourceConfig {

    @Autowired
    public JerseyConfiguration(
            KafkaRestConfig kafkaRestConfig,
            TopicManager topicManager,
            TopicConfigManager topicConfigManager,
            CrnFactory crnFactory,
            UrlFactory urlFactory,
            BrokerManager brokerManager,
            PartitionManager partitionManager,
            ConsumerManager consumerManager
    ) {
        Provider<TopicManager> topicManagerProvider = () -> topicManager;
        Provider<TopicConfigManager> topicConfigManagerProvider = () -> topicConfigManager;
        Provider<BrokerManager> brokerManagerProvider = () -> brokerManager;
        Provider<PartitionManager> partitionManagerProvider = () -> partitionManager;
        Provider<ConsumerManager> consumerManagerProvider = () -> consumerManager;

//        register(new io.confluent.kafkarest.resources.v2.PartitionsResource(partitionManagerProvider));
//        register(new io.confluent.kafkarest.resources.v2.RootResource());

        register(new io.confluent.kafkarest.resources.v3.ConsumersResource(consumerManagerProvider, crnFactory, urlFactory));
        register(new io.confluent.kafkarest.resources.v3.PartitionsResource(partitionManagerProvider, crnFactory, urlFactory));
        register(new io.confluent.kafkarest.resources.v3.BrokersResource(brokerManagerProvider, crnFactory, urlFactory));
        register(new io.confluent.kafkarest.resources.v2.TopicsResource(topicManagerProvider, topicConfigManagerProvider));
        register(new io.confluent.kafkarest.resources.v3.TopicsResource(topicManagerProvider, crnFactory, urlFactory));

//        register(new ZkExceptionMapper(kafkaRestConfig));
        register(new RestConstraintViolationExceptionMapper());
        register(new ConstraintViolationExceptionMapper());
        register(new WebApplicationExceptionMapper(kafkaRestConfig));
        register(new KafkaExceptionMapper(kafkaRestConfig));
//        register(new KafkaRestCleanupFilter());
        register(new FilterUrlMappingsProviderImpl());


//        registerFeatures(this, kafkaRestConfig);
//        registerJsonProvider(this, kafkaRestConfig, true);

        property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        property(ServerProperties.WADL_FEATURE_DISABLE, true);

//        configureMetrics(kafkaRestConfig);

        List<RestResourceExtension> restResourceExtensions = kafkaRestConfig.getConfiguredInstances(
                KafkaRestConfig.KAFKA_REST_RESOURCE_EXTENSION_CONFIG,
                RestResourceExtension.class
        );
        for (RestResourceExtension restResourceExtension : restResourceExtensions) {
            restResourceExtension.register(this, kafkaRestConfig);
        }
    }

}
