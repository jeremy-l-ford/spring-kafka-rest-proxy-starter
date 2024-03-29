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
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jeremylford.spring.kafkarestproxy.properties;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
class PropertySupport {

    static Map putInteger(Map properties, String key, Integer value) {
        if (value != null) {
            properties.put(key, String.valueOf(value));
        }

        return properties;
    }

    static Map putShort(Map properties, String key, Short value) {
        if (value != null) {
            properties.put(key, String.valueOf(value));
        }

        return properties;
    }

    static Map putLong(Map properties, String key, Long value) {
        if (value != null) {
            properties.put(key, String.valueOf(value));
        }

        return properties;
    }

    static Map putDouble(Map properties, String key, Double value) {
        if (value != null) {
            properties.put(key, String.valueOf(value));
        }

        return properties;
    }

    static Map putString(Map properties, String key, String value) {
        if (value != null) {
            properties.put(key, value);
        }
        return properties;
    }

    static Map putBoolean(Map properties, String key, boolean value) {
        properties.put(key, String.valueOf(value));
        return properties;
    }

    static Map putList(Map properties, String key, List<String> values) {
        if (values != null && !values.isEmpty()) {
            properties.put(key, String.join(",", values));
        }

        return properties;
    }

    static Map putArray(Map properties, String key, String[] values) {
        if (values != null && values.length > 0) {
            properties.put(key, String.join(",", values));
        }

        return properties;
    }
}
