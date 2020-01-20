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
package com.github.jeremylford.spring.kafkarestproxy.metrics;

import io.confluent.common.metrics.KafkaMetric;
import io.confluent.common.metrics.MetricsReporter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KafkaRestProxyMetricsReporter implements MetricsReporter {

    @Override
    public void init(List<KafkaMetric> list) {
        for (KafkaMetric kafkaMetric : list) {
            registerWithMicrometer(kafkaMetric);
        }
    }

    private void registerWithMicrometer(KafkaMetric kafkaMetric) {
        List<Tag> tags = kafkaMetric.metricName().tags().entrySet().stream()
                .map(x -> Tag.of(x.getKey(), x.getValue())).collect(Collectors.toList());

        Metrics.gauge(kafkaMetric.metricName().name(),
                tags, kafkaMetric, KafkaMetric::value);
    }

    @Override
    public void metricChange(KafkaMetric kafkaMetric) {
        registerWithMicrometer(kafkaMetric);
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> map) {
    }
}
