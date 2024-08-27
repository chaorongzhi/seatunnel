/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.seatunnel.connectors.seatunnel.elasticsearch.hw.source;

import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.api.source.SeaTunnelSource;
import org.apache.seatunnel.api.table.catalog.schema.TableSchemaOptions;
import org.apache.seatunnel.api.table.factory.Factory;
import org.apache.seatunnel.api.table.factory.TableSourceFactory;
import org.apache.seatunnel.connectors.seatunnel.elasticsearch.hw.config.EsClusterConnectionConfig;
import org.apache.seatunnel.connectors.seatunnel.elasticsearch.hw.config.SourceConfig;

import com.google.auto.service.AutoService;

@AutoService(Factory.class)
public class ElasticsearchSourceFactory implements TableSourceFactory {
    @Override
    public String factoryIdentifier() {
        return "HwElasticsearch";
    }

    @Override
    public OptionRule optionRule() {
        return OptionRule.builder()
                .required(EsClusterConnectionConfig.HOSTS, SourceConfig.INDEX)
                .optional(
                        EsClusterConnectionConfig.USERNAME,
                        EsClusterConnectionConfig.PASSWORD,
                        SourceConfig.SCROLL_TIME,
                        SourceConfig.SCROLL_SIZE,
                        SourceConfig.QUERY,
                        EsClusterConnectionConfig.TLS_VERIFY_CERTIFICATE,
                        EsClusterConnectionConfig.TLS_VERIFY_HOSTNAME,
                        EsClusterConnectionConfig.TLS_KEY_STORE_PATH,
                        EsClusterConnectionConfig.TLS_KEY_STORE_PASSWORD,
                        EsClusterConnectionConfig.TLS_TRUST_STORE_PATH,
                        EsClusterConnectionConfig.TLS_TRUST_STORE_PASSWORD,
                        EsClusterConnectionConfig.HW_ES_AUTH_CONFIG)
                .exclusive(SourceConfig.SOURCE, TableSchemaOptions.SCHEMA)
                .build();
    }

    @Override
    public Class<? extends SeaTunnelSource> getSourceClass() {
        return ElasticsearchSource.class;
    }
}
