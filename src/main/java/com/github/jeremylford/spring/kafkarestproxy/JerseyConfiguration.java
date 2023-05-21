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
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.base.JsonMappingExceptionMapper;
import com.fasterxml.jackson.jaxrs.base.JsonParseExceptionMapper;
import io.confluent.kafkarest.KafkaRestConfig;
import io.confluent.kafkarest.backends.BackendsModule;
import io.confluent.kafkarest.config.ConfigModule;
import io.confluent.kafkarest.controllers.ControllersModule;
import io.confluent.kafkarest.exceptions.ExceptionsModule;
import io.confluent.kafkarest.exceptions.KafkaRestExceptionMapper;
import io.confluent.kafkarest.extension.EnumConverterProvider;
import io.confluent.kafkarest.extension.InstantConverterProvider;
import io.confluent.kafkarest.extension.ResourceAccesslistFeature;
import io.confluent.kafkarest.extension.RestResourceExtension;
import io.confluent.kafkarest.ratelimit.RateLimitFeature;
import io.confluent.kafkarest.resources.ResourcesFeature;
import io.confluent.kafkarest.response.JsonStreamMessageBodyReader;
import io.confluent.kafkarest.response.ResponseModule;
import io.confluent.rest.exceptions.ConstraintViolationExceptionMapper;
import io.confluent.rest.exceptions.WebApplicationExceptionMapper;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jersey.JerseyAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.core.Configurable;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

@Configuration
@AutoConfigureBefore(JerseyAutoConfiguration.class)
public class JerseyConfiguration extends ResourceConfig {

    @Autowired
    public JerseyConfiguration(
            KafkaRestConfig kafkaRestConfig
    ) {

        this.property(ServerProperties.OUTBOUND_CONTENT_LENGTH_BUFFER, 0);
        this.register(new JsonStreamMessageBodyReader(getJsonMapper()));
        this.register(new BackendsModule());
        this.register(new ConfigModule(kafkaRestConfig));
        this.register(new ControllersModule());
        this.register(new ExceptionsModule());
        this.register(RateLimitFeature.class);
        this.register(new ResourcesFeature(kafkaRestConfig));
        this.register(new ResponseModule());

        this.register(ResourceAccesslistFeature.class);

        this.register(EnumConverterProvider.class);
        this.register(InstantConverterProvider.class);

        registerExceptionMappers(this, kafkaRestConfig);

        property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        property(ServerProperties.WADL_FEATURE_DISABLE, true);

        List<RestResourceExtension> restResourceExtensions = kafkaRestConfig.getConfiguredInstances(
                KafkaRestConfig.KAFKA_REST_RESOURCE_EXTENSION_CONFIG,
                RestResourceExtension.class
        );
        for (RestResourceExtension restResourceExtension : restResourceExtensions) {
            restResourceExtension.register(this, kafkaRestConfig);
        }
    }

    protected static ObjectMapper getJsonMapper() {
        return new ObjectMapper()
                .registerModule(new GuavaModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"))
                .setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    protected static void registerExceptionMappers(Configurable<?> config, KafkaRestConfig restConfig) {
        config.register(JsonParseExceptionMapper.class);
        config.register(JsonMappingExceptionMapper.class);
        config.register(ConstraintViolationExceptionMapper.class);
        config.register(new WebApplicationExceptionMapper(restConfig));
        config.register(new KafkaRestExceptionMapper(restConfig));
    }


}
