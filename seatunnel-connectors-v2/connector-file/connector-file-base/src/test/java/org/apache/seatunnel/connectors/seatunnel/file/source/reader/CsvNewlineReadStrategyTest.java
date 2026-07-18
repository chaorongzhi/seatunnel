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

package org.apache.seatunnel.connectors.seatunnel.file.source.reader;

import org.apache.seatunnel.shade.com.typesafe.config.Config;
import org.apache.seatunnel.shade.com.typesafe.config.ConfigFactory;

import org.apache.seatunnel.api.source.Collector;
import org.apache.seatunnel.api.table.type.BasicType;
import org.apache.seatunnel.api.table.type.SeaTunnelDataType;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.api.table.type.SeaTunnelRowType;
import org.apache.seatunnel.connectors.seatunnel.file.config.BaseSourceConfigOptions;
import org.apache.seatunnel.connectors.seatunnel.file.config.HadoopConf;
import org.apache.seatunnel.connectors.seatunnel.file.exception.FileConnectorException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.hadoop.fs.CommonConfigurationKeysPublic.FS_DEFAULT_NAME_DEFAULT;

public class CsvNewlineReadStrategyTest {

    private static final SeaTunnelRowType ROW_TYPE =
            new SeaTunnelRowType(
                    new String[] {"id", "remark", "name"},
                    new SeaTunnelDataType<?>[] {
                        BasicType.STRING_TYPE, BasicType.STRING_TYPE, BasicType.STRING_TYPE
                    });

    @TempDir Path tempDir;

    @Test
    public void testQuotedNewlineIsReadAsSingleRecord() throws Exception {
        List<SeaTunnelRow> rows = readCsv("1,\"line1\nline2\",alice\n", null, 0);

        Assertions.assertEquals(1, rows.size());
        Assertions.assertEquals("1", rows.get(0).getField(0));
        Assertions.assertEquals("line1\nline2", rows.get(0).getField(1));
        Assertions.assertEquals("alice", rows.get(0).getField(2));
    }

    @Test
    public void testQuotedNewlineAndEscapedQuote() throws Exception {
        List<SeaTunnelRow> rows = readCsv("1,\"a\"\"b\nc\",alice\n", null, 0);

        Assertions.assertEquals(1, rows.size());
        Assertions.assertEquals("a\"b\nc", rows.get(0).getField(1));
    }

    @Test
    public void testCustomFieldDelimiterWithQuotedNewline() throws Exception {
        List<SeaTunnelRow> rows = readCsv("1;\"line1\nline2\";alice\n", ";", 0);

        Assertions.assertEquals(1, rows.size());
        Assertions.assertEquals("line1\nline2", rows.get(0).getField(1));
        Assertions.assertEquals("alice", rows.get(0).getField(2));
    }

    @Test
    public void testSkipHeaderCountsCsvRecords() throws Exception {
        String csv = "id,\"multi\nline header\",name\n1,value,alice\n";

        List<SeaTunnelRow> rows = readCsv(csv, null, 1);

        Assertions.assertEquals(1, rows.size());
        Assertions.assertEquals("1", rows.get(0).getField(0));
        Assertions.assertEquals("value", rows.get(0).getField(1));
        Assertions.assertEquals("alice", rows.get(0).getField(2));
    }

    @Test
    public void testPlainCsvRowsRemainIndependent() throws Exception {
        List<SeaTunnelRow> rows = readCsv("1,a,alice\n2,b,bob\n", null, 0);

        Assertions.assertEquals(2, rows.size());
        Assertions.assertEquals("1", rows.get(0).getField(0));
        Assertions.assertEquals("2", rows.get(1).getField(0));
    }

    @Test
    public void testUnclosedQuoteFails() {
        Assertions.assertThrows(
                FileConnectorException.class, () -> readCsv("1,\"line1\nline2,alice\n", null, 0));
    }

    private List<SeaTunnelRow> readCsv(String csv, String delimiter, long skipHeaderNumber)
            throws Exception {
        Path csvFile = tempDir.resolve("data.csv");
        Files.write(csvFile, csv.getBytes(StandardCharsets.UTF_8));

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(BaseSourceConfigOptions.FILE_FORMAT_TYPE.key(), "csv");
        configMap.put(BaseSourceConfigOptions.PARSE_PARTITION_FROM_PATH.key(), false);
        configMap.put(BaseSourceConfigOptions.SKIP_HEADER_ROW_NUMBER.key(), skipHeaderNumber);
        if (delimiter != null) {
            configMap.put(BaseSourceConfigOptions.FIELD_DELIMITER.key(), delimiter);
        }
        Config pluginConfig = ConfigFactory.parseMap(configMap);

        TestCollector collector = new TestCollector();
        try (TextReadStrategy readStrategy = new TextReadStrategy()) {
            readStrategy.setPluginConfig(pluginConfig);
            readStrategy.init(new LocalConf(FS_DEFAULT_NAME_DEFAULT));
            readStrategy.getFileNamesByPath(csvFile.toString());
            readStrategy.setSeaTunnelRowTypeInfo(ROW_TYPE);
            readStrategy.read(csvFile.toString(), "", collector);
        }
        return collector.rows;
    }

    private static class TestCollector implements Collector<SeaTunnelRow> {
        private final List<SeaTunnelRow> rows = new ArrayList<>();

        @Override
        public void collect(SeaTunnelRow record) {
            rows.add(record);
        }

        @Override
        public Object getCheckpointLock() {
            return null;
        }
    }

    private static class LocalConf extends HadoopConf {
        private static final String HDFS_IMPL = "org.apache.hadoop.fs.LocalFileSystem";
        private static final String SCHEMA = "file";

        private LocalConf(String hdfsNameKey) {
            super(hdfsNameKey);
        }

        @Override
        public String getFsHdfsImpl() {
            return HDFS_IMPL;
        }

        @Override
        public String getSchema() {
            return SCHEMA;
        }
    }
}
