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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.base.JsonParseExceptionMapper;
import com.github.jeremylford.spring.kafkarestproxy.metrics.KafkaRestProxyMetricsReporter;
import io.confluent.common.metrics.JmxReporter;
import io.confluent.common.metrics.MetricConfig;
import io.confluent.common.metrics.Metrics;
import io.confluent.common.metrics.MetricsReporter;
import io.confluent.kafkarest.KafkaRestConfig;
import io.confluent.kafkarest.KafkaRestContext;
import io.confluent.kafkarest.exceptions.ZkExceptionMapper;
import io.confluent.kafkarest.extension.KafkaRestCleanupFilter;
import io.confluent.kafkarest.extension.RestResourceExtension;
import io.confluent.rest.Application;
import io.confluent.rest.RestConfig;
import io.confluent.rest.exceptions.ConstraintViolationExceptionMapper;
import io.confluent.rest.exceptions.KafkaExceptionMapper;
import io.confluent.rest.exceptions.WebApplicationExceptionMapper;
import io.confluent.rest.metrics.MetricsResourceMethodApplicationListener;
import io.confluent.rest.validation.JacksonMessageBodyProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.validation.ValidationFeature;
import org.glassfish.jersey.servlet.init.FilterUrlMappingsProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jersey.JerseyAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.core.Configurable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
@AutoConfigureBefore(JerseyAutoConfiguration.class)
public class JerseyConfiguration extends ResourceConfig {

    @Autowired
    public JerseyConfiguration(KafkaRestConfig kafkaRestConfig, KafkaRestContext kafkaRestContext) {
        register(new io.confluent.kafkarest.resources.ConsumersResource(kafkaRestContext));
        register(new io.confluent.kafkarest.resources.v2.ConsumersResource(kafkaRestContext));
        register(new io.confluent.kafkarest.resources.PartitionsResource(kafkaRestContext));
        register(new io.confluent.kafkarest.resources.v2.PartitionsResource(kafkaRestContext));
        register(new io.confluent.kafkarest.resources.BrokersResource(kafkaRestContext));
        register(new io.confluent.kafkarest.resources.RootResource());
        register(new io.confluent.kafkarest.resources.TopicsResource(kafkaRestContext));

        register(new ZkExceptionMapper(kafkaRestConfig));
        register(new ConstraintViolationExceptionMapper());
        register(new WebApplicationExceptionMapper(kafkaRestConfig));
        register(new KafkaExceptionMapper(kafkaRestConfig));
        register(new KafkaRestCleanupFilter());
        register(new FilterUrlMappingsProviderImpl());

        registerFeatures(this, kafkaRestConfig);
        registerJsonProvider(this, kafkaRestConfig, true);

        property("jersey.config.beanValidation.enableOutputValidationErrorEntity.server", true);
        property("jersey.config.server.wadl.disableWadl", true);

        configureMetrics(kafkaRestConfig);

        List<RestResourceExtension> restResourceExtensions = kafkaRestConfig.getConfiguredInstances(
                KafkaRestConfig.KAFKA_REST_RESOURCE_EXTENSION_CONFIG,
                RestResourceExtension.class
        );
        for (RestResourceExtension restResourceExtension : restResourceExtensions) {
            restResourceExtension.register(this, kafkaRestConfig);
        }
    }

    protected void registerJsonProvider(Configurable<?> config, KafkaRestConfig restConfig, boolean registerExceptionMapper) {
        ObjectMapper jsonMapper = new ObjectMapper();
        JacksonMessageBodyProvider jsonProvider = new JacksonMessageBodyProvider(jsonMapper);
        config.register(jsonProvider);
        if (registerExceptionMapper) {
            config.register(JsonParseExceptionMapper.class);
        }
    }

    protected void registerFeatures(Configurable<?> config, KafkaRestConfig restConfig) {
        config.register(ValidationFeature.class);
    }

    private void configureMetrics(KafkaRestConfig kafkaRestConfig) {
        MetricConfig metricConfig = new MetricConfig()
                .samples(kafkaRestConfig.getInt(RestConfig.METRICS_NUM_SAMPLES_CONFIG))
                .timeWindow(kafkaRestConfig.getLong(RestConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG),
                        TimeUnit.MILLISECONDS);

        //TODO: should this be supported, or rely on standard micrometer/spring configuration
        List<MetricsReporter> reporters =
                kafkaRestConfig.getConfiguredInstances(RestConfig.METRICS_REPORTER_CLASSES_CONFIG,
                        MetricsReporter.class);
        reporters.add(new KafkaRestProxyMetricsReporter());

        Metrics metrics = new Metrics(metricConfig, reporters, kafkaRestConfig.getTime());

        Map<String, String> configuredTags = Application.parseListToMap(
                kafkaRestConfig.getList(RestConfig.METRICS_TAGS_CONFIG)
        );

        register(new MetricsResourceMethodApplicationListener(metrics, "jersey",
                configuredTags, kafkaRestConfig.getTime()));
    }
}
