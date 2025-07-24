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

package org.apache.seatunnel.transform.iisparse;

import org.apache.seatunnel.api.configuration.ReadonlyConfig;
import org.apache.seatunnel.api.table.catalog.CatalogTable;
import org.apache.seatunnel.api.table.catalog.TableIdentifier;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.api.table.type.SeaTunnelRowType;
import org.apache.seatunnel.transform.common.FilterRowTransform;
import org.apache.seatunnel.transform.exception.TransformCommonError;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@Slf4j
public class IISParseTransform extends FilterRowTransform {

    private final List<String> requiredFormat;
    private final int fieldIndex;
    private String[] logFormat;

    private String mergeContent = "";

    public IISParseTransform(@NonNull ReadonlyConfig config, @NonNull CatalogTable catalogTable) {
        super(catalogTable);
        SeaTunnelRowType seaTunnelRowType = catalogTable.getTableSchema().toPhysicalRowDataType();
        requiredFormat = config.get(IISParseTransformConfig.REQUIRED_FORMAT);
        String field = config.get(IISParseTransformConfig.PARSER_FIELD);
        fieldIndex = seaTunnelRowType.indexOf(field, false);

        if (fieldIndex == -1) {
            throw TransformCommonError.cannotFindInputFieldsError(
                    getPluginName(), Collections.singletonList(field));
        }
    }

    @Override
    public String getPluginName() {
        return IISParseTransformConfig.IDENTIFIER;
    }

    @Override
    protected SeaTunnelRow transformRow(SeaTunnelRow inputRow) {
        Object[] values = inputRow.getFields();
        String value = String.valueOf(values[fieldIndex]);
        if (value.startsWith("#") && !value.startsWith("#Fields")) {
            return null;
        }
        if (value.startsWith("#Fields")) {
            value = value.substring(8); // 去掉串“#Fields：”，得到格式串
            value = value.trim(); // 去掉串首尾的空白字符
            logFormat = value.split("\\s");
            return null;
        }

        values[fieldIndex] = formatLogs(value, logFormat, requiredFormat.toArray(new String[0]));
        SeaTunnelRow outputRow = new SeaTunnelRow(values);
        outputRow.setRowKind(inputRow.getRowKind());
        outputRow.setTableId(inputRow.getTableId());
        return outputRow;
    }

    private String formatLogs(String line, String[] logFormat, String[] requiredFormat) {
        String[] logSection = line.split("\\s");
        StringBuilder sBuf = new StringBuilder();
        //        sBuf.append("iis_");
        // 按照要求的格式重新排列字段
        if (requiredFormat != null) {
            for (String s : requiredFormat) {
                int j = 0;
                if (logFormat != null) {
                    for (; j < logFormat.length; j++) {
                        if (s.equalsIgnoreCase(logFormat[j])) {
                            sBuf.append(logSection[j]).append("\t");
                            break;
                        }
                    }
                    if (j == logFormat.length) {
                        sBuf.append("-\t");
                    }
                }
            }
        }
        return sBuf.toString();
    }

    @Override
    protected TableIdentifier transformTableIdentifier() {
        return inputCatalogTable.getTableId().copy();
    }
}
