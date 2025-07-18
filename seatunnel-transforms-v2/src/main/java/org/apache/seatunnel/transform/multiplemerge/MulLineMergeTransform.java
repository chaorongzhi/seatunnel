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

package org.apache.seatunnel.transform.multiplemerge;

import org.apache.seatunnel.api.configuration.ReadonlyConfig;
import org.apache.seatunnel.api.table.catalog.CatalogTable;
import org.apache.seatunnel.api.table.catalog.TableIdentifier;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.api.table.type.SeaTunnelRowType;
import org.apache.seatunnel.transform.common.FilterRowTransform;
import org.apache.seatunnel.transform.exception.TransformCommonError;

import org.apache.commons.lang3.StringUtils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

@Slf4j
public class MulLineMergeTransform extends FilterRowTransform {

    private final String prefix;
    private final String suffix;
    private final int fieldIndex;

    private String mergeContent = "";

    public MulLineMergeTransform(
            @NonNull ReadonlyConfig config, @NonNull CatalogTable catalogTable) {
        super(catalogTable);
        SeaTunnelRowType seaTunnelRowType = catalogTable.getTableSchema().toPhysicalRowDataType();
        prefix = config.get(MulLineMergeTransformConfig.MERGE_PREFIX);
        suffix = config.get(MulLineMergeTransformConfig.MERGE_SUFFIX);
        String field = config.get(MulLineMergeTransformConfig.MERGE_FIELD);
        fieldIndex = seaTunnelRowType.indexOf(field, false);

        if (fieldIndex == -1) {
            throw TransformCommonError.cannotFindInputFieldsError(
                    getPluginName(), Collections.singletonList(field));
        }
    }

    @Override
    public String getPluginName() {
        return MulLineMergeTransformConfig.IDENTIFIER;
    }

    @Override
    protected SeaTunnelRow transformRow(SeaTunnelRow inputRow) {
        Object[] values = inputRow.getFields();
        String value = String.valueOf(values[fieldIndex]);
        if (StringUtils.isNoneEmpty(value)) {
            if (StringUtils.isNoneEmpty(this.mergeContent)) {
                if (value.startsWith(this.prefix)) {
                    values[fieldIndex] = this.mergeContent;
                    this.mergeContent = value;
                } else if (value.endsWith(this.suffix)) {
                    values[fieldIndex] = this.mergeContent + value;
                    this.mergeContent = "";
                    SeaTunnelRow outputRow = new SeaTunnelRow(values);
                    outputRow.setRowKind(inputRow.getRowKind());
                    outputRow.setTableId(inputRow.getTableId());
                    return outputRow;
                }
            }
            this.mergeContent += value;
        }

        return null;
    }

    @Override
    protected TableIdentifier transformTableIdentifier() {
        return inputCatalogTable.getTableId().copy();
    }

    @Override
    public void close() {
        super.close();
    }
}
