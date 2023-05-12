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
import io.confluent.kafkarest.KafkaRestConfig;
import io.confluent.kafkarest.controllers.BrokerManager;
import io.confluent.kafkarest.controllers.ClusterManager;
import io.confluent.kafkarest.controllers.ConsumerGroupManager;
import io.confluent.kafkarest.controllers.ConsumerManager;
import io.confluent.kafkarest.controllers.PartitionManager;
import io.confluent.kafkarest.controllers.TopicConfigManager;
import io.confluent.kafkarest.controllers.TopicManager;
import io.confluent.kafkarest.response.CrnFactory;
import io.confluent.kafkarest.response.CrnFactoryImpl;
import io.confluent.kafkarest.response.FakeUrlFactory;
import io.confluent.kafkarest.response.UrlFactory;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@EnableConfigurationProperties({KafkaRestProperties.class})
@Configuration
@Import(JerseyConfiguration.class)
public class KafkaRestProxyAutoConfiguration {

    @SuppressWarnings("unchecked")
    @Bean
    public ClusterManager clusterManager(Admin adminClient) {
        return createImpl(
                "io.confluent.kafkarest.controllers.ClusterManagerImpl",
                new Class[]{Admin.class},
                new Object[]{adminClient}
        );
    }

    @SuppressWarnings("unchecked")
    @Bean
    public TopicManager topicManager(Admin adminClient, ClusterManager clusterManager) {
        return createImpl(
                "io.confluent.kafkarest.controllers.TopicManagerImpl",
                new Class[]{Admin.class, ClusterManager.class},
                new Object[]{adminClient, clusterManager}
        );
    }

    @SuppressWarnings("unchecked")
    @Bean
    public TopicConfigManager topicConfigManager(Admin adminClient, ClusterManager clusterManager) {
        return createImpl(
                "io.confluent.kafkarest.controllers.TopicConfigManagerImpl",
                new Class[]{Admin.class, ClusterManager.class},
                new Object[]{adminClient, clusterManager}
        );
    }

    @Bean
    public BrokerManager brokerManager(ClusterManager clusterManager) {
        return createImpl("io.confluent.kafkarest.controllers.BrokerManagerImpl", new Class[]{ClusterManager.class}, new Object[]{clusterManager});
    }

    @Bean
    public PartitionManager partitionManager(Admin admin, TopicManager topicManager) {
        return createImpl("io.confluent.kafkarest.controllers.PartitionManagerImpl", new Class[]{Admin.class, TopicManager.class}, new Object[]{admin, topicManager});
    }

    @Bean
    public ConsumerGroupManager consumerGroupManager(Admin admin, ClusterManager clusterManager) {
        return createImpl("io.confluent.kafkarest.controllers.ConsumerGroupManagerImpl", new Class[]{Admin.class, ClusterManager.class}, new Object[]{admin, clusterManager});
    }

    @Bean
    public ConsumerManager consumerManager(ConsumerGroupManager consumerGroupManager) {
        return createImpl("io.confluent.kafkarest.controllers.ConsumerManagerImpl", new Class[]{ConsumerGroupManager.class}, new Object[]{consumerGroupManager});
    }

    @SuppressWarnings("unchecked")
    private <T> T createImpl(String impl, Class<?>[] parameterTypes, Object[] parameters) {


        try {
            Class<T> clazz = (Class<T>) Class.forName(impl);
            Constructor<T> declaredConstructor = clazz.getDeclaredConstructor(parameterTypes);
            declaredConstructor.setAccessible(true);
            MethodHandle constructor = MethodHandles.lookup().unreflectConstructor(declaredConstructor);
            declaredConstructor.setAccessible(false);

//            MethodHandle constructor = MethodHandles.lookup().findConstructor(clazz, MethodType.methodType(void.class, parameterTypes));
            return (T) constructor.invokeWithArguments(parameters);

//            Constructor<T> declaredConstructor = clazz.getDeclaredConstructor(parameterTypes);
//            declaredConstructor.setAccessible(true);
//            return declaredConstructor.newInstance(parameters);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public CrnFactory crnFactory(KafkaRestConfig config) {
        String crnConfig = config.getString(KafkaRestConfig.CRN_AUTHORITY_CONFIG);
        return new CrnFactoryImpl(crnConfig);
    }

    @Bean
    @RequestScope
    public UrlFactory urlFactory(KafkaRestConfig config, HttpServletRequest httpServletRequest) {
        //@HostNameConfig String hostNameConfig,
        //      @PortConfig Integer portConfig,
        //      @AdvertisedListenersConfig List<URI> advertisedListenersConfig,
        //      @ListenersConfig List<URI> listenersConfig,
        //      @Context UriInfo requestUriInfo
        String hostName = config.getString(KafkaRestConfig.HOST_NAME_CONFIG);
        int port = config.getInt(KafkaRestConfig.PORT_CONFIG);
        List<URI> advertisedListeners = config.getList(KafkaRestConfig.ADVERTISED_LISTENERS_CONFIG).stream().map(URI::create).collect(Collectors.toList());
        List<URI> listeners = config.getList(KafkaRestConfig.LISTENERS_CONFIG).stream().map(URI::create).collect(Collectors.toList());
        UriInfo uriInfo = new UriInfo() {
            @Override
            public String getPath() {
                return httpServletRequest.getRequestURI();
            }

            @Override
            public String getPath(boolean decode) {
                return getRequestUriBuilder().build().getPath();
            }

            @Override
            public List<PathSegment> getPathSegments() {
                return null;
            }

            @Override
            public List<PathSegment> getPathSegments(boolean decode) {
                return null;
            }

            @Override
            public URI getRequestUri() {
                return URI.create(httpServletRequest.getRequestURI());
            }

            @Override
            public UriBuilder getRequestUriBuilder() {
                return UriBuilder.fromUri(httpServletRequest.getRequestURI());
            }

            @Override
            public URI getAbsolutePath() {
                return null;
            }

            @Override
            public UriBuilder getAbsolutePathBuilder() {
                return null;
            }

            @Override
            public URI getBaseUri() {
                return null;
            }

            @Override
            public UriBuilder getBaseUriBuilder() {
                return null;
            }

            @Override
            public MultivaluedMap<String, String> getPathParameters() {
                return null;
            }

            @Override
            public MultivaluedMap<String, String> getPathParameters(boolean decode) {
                return null;
            }

            @Override
            public MultivaluedMap<String, String> getQueryParameters() {
                return null;
            }

            @Override
            public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
                return null;
            }

            @Override
            public List<String> getMatchedURIs() {
                return null;
            }

            @Override
            public List<String> getMatchedURIs(boolean decode) {
                return null;
            }

            @Override
            public List<Object> getMatchedResources() {
                return null;
            }

            @Override
            public URI resolve(URI uri) {
                return null;
            }

            @Override
            public URI relativize(URI uri) {
                return null;
            }
        };

        return new FakeUrlFactory();
//        return new UrlFactoryImpl(hostName, port, advertisedListeners, listeners, uriInfo);
    }


    @Bean
    public KafkaRestConfig kafkaRestConfig(KafkaRestProperties kafkaRestProperties) {
        return new KafkaRestConfig(kafkaRestProperties.asProperties());
    }

    //
//    @Bean
//    public ScalaConsumersContext scalaConsumersContext(KafkaRestConfig kafkaRestConfig) {
//        return new ScalaConsumersContext(kafkaRestConfig);
//    }
//
//    @Bean
//    public AdminClientWrapper adminClientWrapper(KafkaRestConfig kafkaRestConfig, AdminClient adminClient) {
//        return new AdminClientWrapper(kafkaRestConfig, adminClient);
//    }
//
//    @Bean
//    public KafkaConsumerManager kafkaConsumerManager(KafkaRestConfig kafkaRestConfig) {
//        return new KafkaConsumerManager(kafkaRestConfig);
//    }
//
//    @Bean
//    public ProducerPool producerPool(KafkaRestConfig kafkaRestConfig) {
//        return new ProducerPool(kafkaRestConfig);
//    }
//
//    @Bean
//    public KafkaRestContext kafkaRestContext(KafkaRestConfig kafkaRestConfig,
//                                             ProducerPool producerPool,
//                                             KafkaConsumerManager kafkaConsumerManager,
//                                             AdminClientWrapper adminClientWrapper,
//                                             ScalaConsumersContext scalaConsumersContext) {
//        KafkaRestContextProvider.initialize(kafkaRestConfig, producerPool, kafkaConsumerManager, adminClientWrapper, scalaConsumersContext);
//
//        ContextInvocationHandler contextInvocationHandler = new ContextInvocationHandler();
//        return (KafkaRestContext) Proxy.newProxyInstance(
//                KafkaRestContext.class.getClassLoader(),
//                new Class[]{KafkaRestContext.class},
//                contextInvocationHandler
//        );
//    }
//
    @Bean
    public Admin adminClient(KafkaRestConfig kafkaRestConfig) {
        return AdminClient.create(kafkaRestConfig.getAdminProperties());
    }

}
