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
package com.github.jeremylford.spring.kafkarestproxy.properties;

import io.confluent.kafkarest.KafkaRestConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

import static com.github.jeremylford.spring.kafkarestproxy.properties.PropertySupport.putArray;
import static com.github.jeremylford.spring.kafkarestproxy.properties.PropertySupport.putInteger;
import static com.github.jeremylford.spring.kafkarestproxy.properties.PropertySupport.putLong;
import static com.github.jeremylford.spring.kafkarestproxy.properties.PropertySupport.putString;

@ConfigurationProperties("kafka.restproxy")
public class KafkaRestProperties {

    /**
     * Unique ID for this REST server instance. This is used in generating unique IDs for consumers that do not specify their ID. The ID is empty by default, which makes a single server setup easier to get up and running, but is not safe for multi-server deployments where automatic consumer IDs are used.
     */
    private String id;

    /**
     * The maximum number of records returned in a single call to poll().
     */
    private int maxPollRecords = 30;

    /**
     * The host name used to generate absolute URLs in responses. If empty, the default canonical hostname is used
     */
    private String hostName;

    /**
     * The maximum number of threads to run consumer requests on. The value of -1 denotes unbounded thread creation
     */
    private int consumerMaxThreads = 50;

    /**
     * NOTE: Only required when using v1 Consumer API's. Specifies the ZooKeeper connection string in the form hostname:port where host and port are the host and port of a ZooKeeper server. To allow connecting through other ZooKeeper nodes when that ZooKeeper machine is down you can also specify multiple hosts in the form hostname1:port1,hostname2:port2,hostname3:port3.
     * <p>
     * The server may also have a ZooKeeper chroot path as part of it's ZooKeeper connection string which puts its data under some path in the global ZooKeeper namespace. If so the consumer should use the same chroot path in its connection string. For example to give a chroot path of /chroot/path you would give the connection string as hostname1:port1,hostname2:port2,hostname3:port3/chroot/path.
     */
    private String[] zookeeperConnect = new String[0];

    /**
     * A list of host/port pairs to use for establishing the initial connection to the Kafka cluster. The client will make use of all servers irrespective of which servers are specified here for bootstrapping—this list only impacts the initial hosts used to discover the full set of servers. This list should be in the form host1:port1,host2:port2,.... Since these servers are just used for the initial connection to discover the full cluster membership (which may change dynamically), this list need not contain the full set of servers (you may want more than one, though, in case a server is down).
     */
    private String[] bootstrapServers = new String[0];

    /**
     * The base URL for the schema registry that should be used by the Avro serializer.
     */
    private String schemaRegistryUrl;

    /**
     * Minimum bytes of records for the proxy to accumulate beforereturning a response to a consumer request. The special sentinel value of -1 disables this functionality.
     */
    private int fetchMinBytes = -1;

    /**
     * Number of threads to run produce requests on.
     */
    private int producerThreads = Integer.parseInt(KafkaRestConfig.PRODUCER_THREADS_DEFAULT);

    /**
     * Timeout for blocking consumer iterator operations. This should be set to a small enough value that it is possible to effectively peek() on the iterator.
     */
    private long consumerIteratorTimeoutMillis = Long.parseLong(KafkaRestConfig.CONSUMER_ITERATOR_TIMEOUT_MS_DEFAULT);

    /**
     * Amount of time to backoff when an iterator runs out of data. If a consumer has a dedicated worker thread, this is effectively the maximum error for the entire request timeout. It should be small enough to closely target the timeout, but large enough to avoid busy waiting.
     */
    private long consumerIteratorBackoffMillis = Long.parseLong(KafkaRestConfig.CONSUMER_ITERATOR_BACKOFF_MS_DEFAULT);

    /**
     * The maximum total time to wait for messages for a request if the maximum number of messages has not yet been reached.
     */
    private long consumerRequestTimeoutMills = Long.parseLong(KafkaRestConfig.CONSUMER_REQUEST_TIMEOUT_MS_DEFAULT);

    /**
     * Maximum number of bytes in unencoded message keys and values returned by a single request. This can be used by administrators to limit the memory used by a single consumer and to control the memory usage required to decode responses on clients that cannot perform a streaming decode. Note that the actual payload will be larger due to overhead from base64 encoding the response data and from JSON encoding the entire response.
     */
    private long consumeRequestMaxBytes = KafkaRestConfig.CONSUMER_REQUEST_MAX_BYTES_DEFAULT;

    /**
     * Amount of idle time before a consumer instance is automatically destroyed.
     */
    private long consumerInstanceTimeoutMillis = Long.parseLong(KafkaRestConfig.CONSUMER_INSTANCE_TIMEOUT_MS_DEFAULT);

    /**
     * Maximum number of SimpleConsumers that can be instantiated per broker. If 0, then the pool size is not limited.
     */
    private int simpleConsumerMaxPoolSize = Integer.parseInt(KafkaRestConfig.SIMPLE_CONSUMER_MAX_POOL_SIZE_DEFAULT);

    /**
     * Amount of time to wait for an available SimpleConsumer from the pool before failing. Use 0 for no timeout
     */
    private long simpleConsumerPoolTimeoutMillis = Long.parseLong(KafkaRestConfig.SIMPLE_CONSUMER_POOL_TIMEOUT_MS_DEFAULT);

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMaxPollRecords() {
        return maxPollRecords;
    }

    public void setMaxPollRecords(int maxPollRecords) {
        this.maxPollRecords = maxPollRecords;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getConsumerMaxThreads() {
        return consumerMaxThreads;
    }

    public void setConsumerMaxThreads(int consumerMaxThreads) {
        this.consumerMaxThreads = consumerMaxThreads;
    }

    public String[] getZookeeperConnect() {
        return zookeeperConnect;
    }

    public void setZookeeperConnect(String[] zookeeperConnect) {
        this.zookeeperConnect = zookeeperConnect;
    }

    public String[] getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String[] bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getSchemaRegistryUrl() {
        return schemaRegistryUrl;
    }

    public void setSchemaRegistryUrl(String schemaRegistryUrl) {
        this.schemaRegistryUrl = schemaRegistryUrl;
    }

    public int getFetchMinBytes() {
        return fetchMinBytes;
    }

    public void setFetchMinBytes(int fetchMinBytes) {
        this.fetchMinBytes = fetchMinBytes;
    }

    public int getProducerThreads() {
        return producerThreads;
    }

    public void setProducerThreads(int producerThreads) {
        this.producerThreads = producerThreads;
    }

    public long getConsumerIteratorTimeoutMillis() {
        return consumerIteratorTimeoutMillis;
    }

    public void setConsumerIteratorTimeoutMillis(long consumerIteratorTimeoutMillis) {
        this.consumerIteratorTimeoutMillis = consumerIteratorTimeoutMillis;
    }

    public long getConsumerIteratorBackoffMillis() {
        return consumerIteratorBackoffMillis;
    }

    public void setConsumerIteratorBackoffMillis(long consumerIteratorBackoffMillis) {
        this.consumerIteratorBackoffMillis = consumerIteratorBackoffMillis;
    }

    public long getConsumerRequestTimeoutMills() {
        return consumerRequestTimeoutMills;
    }

    public void setConsumerRequestTimeoutMills(long consumerRequestTimeoutMills) {
        this.consumerRequestTimeoutMills = consumerRequestTimeoutMills;
    }

    public long getConsumeRequestMaxBytes() {
        return consumeRequestMaxBytes;
    }

    public void setConsumeRequestMaxBytes(long consumeRequestMaxBytes) {
        this.consumeRequestMaxBytes = consumeRequestMaxBytes;
    }

    public long getConsumerInstanceTimeoutMillis() {
        return consumerInstanceTimeoutMillis;
    }

    public void setConsumerInstanceTimeoutMillis(long consumerInstanceTimeoutMillis) {
        this.consumerInstanceTimeoutMillis = consumerInstanceTimeoutMillis;
    }

    public int getSimpleConsumerMaxPoolSize() {
        return simpleConsumerMaxPoolSize;
    }

    public void setSimpleConsumerMaxPoolSize(int simpleConsumerMaxPoolSize) {
        this.simpleConsumerMaxPoolSize = simpleConsumerMaxPoolSize;
    }

    public long getSimpleConsumerPoolTimeoutMillis() {
        return simpleConsumerPoolTimeoutMillis;
    }

    public void setSimpleConsumerPoolTimeoutMillis(long simpleConsumerPoolTimeoutMillis) {
        this.simpleConsumerPoolTimeoutMillis = simpleConsumerPoolTimeoutMillis;
    }

    public Properties asProperties() {
        Properties properties = new Properties();

        putString(properties, KafkaRestConfig.ID_CONFIG, this.id);
        putInteger(properties, KafkaRestConfig.MAX_POLL_RECORDS_CONFIG, this.maxPollRecords);
        putString(properties, KafkaRestConfig.HOST_NAME_CONFIG, this.hostName);
        putInteger(properties, KafkaRestConfig.CONSUMER_MAX_THREADS_CONFIG, this.consumerMaxThreads);
        putArray(properties, KafkaRestConfig.ZOOKEEPER_CONNECT_CONFIG, this.zookeeperConnect);
        putArray(properties, KafkaRestConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        putString(properties, KafkaRestConfig.SCHEMA_REGISTRY_URL_CONFIG, this.schemaRegistryUrl);
        putInteger(properties, KafkaRestConfig.PROXY_FETCH_MIN_BYTES_CONFIG, this.fetchMinBytes);
        putInteger(properties, KafkaRestConfig.PRODUCER_THREADS_CONFIG, this.producerThreads);
        putLong(properties, KafkaRestConfig.CONSUMER_ITERATOR_TIMEOUT_MS_CONFIG, this.consumerIteratorTimeoutMillis);
        putLong(properties, KafkaRestConfig.CONSUMER_ITERATOR_BACKOFF_MS_CONFIG, this.consumerIteratorBackoffMillis);
        putLong(properties, KafkaRestConfig.CONSUMER_REQUEST_TIMEOUT_MS_CONFIG, this.consumerRequestTimeoutMills);
        putLong(properties, KafkaRestConfig.CONSUMER_REQUEST_MAX_BYTES_CONFIG, this.consumeRequestMaxBytes);
        putLong(properties, KafkaRestConfig.CONSUMER_INSTANCE_TIMEOUT_MS_CONFIG, this.consumerInstanceTimeoutMillis);
        putInteger(properties, KafkaRestConfig.SIMPLE_CONSUMER_MAX_POOL_SIZE_CONFIG, this.simpleConsumerMaxPoolSize);
        putLong(properties, KafkaRestConfig.SIMPLE_CONSUMER_POOL_TIMEOUT_MS_CONFIG, this.simpleConsumerPoolTimeoutMillis);

        return properties;
    }


}
