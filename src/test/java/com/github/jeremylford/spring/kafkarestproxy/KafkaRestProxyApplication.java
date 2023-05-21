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
import org.junit.Ignore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.testcontainers.redpanda.RedpandaContainer;

@SpringBootApplication
@Ignore
public class KafkaRestProxyApplication {

    private static final RedpandaContainer container = new RedpandaContainer("docker.redpanda.com/redpandadata/redpanda:v23.1.2");


    public static void main(String[] args) {
        container.start();
        System.out.println("Bootstrap servers: " + container.getBootstrapServers());
        System.setProperty("kafka.restproxy.bootstrap-servers", container.getBootstrapServers());
        SpringApplication.run(KafkaRestProxyApplication.class, args);
    }


}
