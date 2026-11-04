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

package org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.kingbase;

import org.apache.seatunnel.api.table.catalog.Column;
import org.apache.seatunnel.api.table.catalog.PhysicalColumn;
import org.apache.seatunnel.api.table.converter.BasicTypeDefine;
import org.apache.seatunnel.api.table.converter.TypeConverter;
import org.apache.seatunnel.api.table.type.BasicType;
import org.apache.seatunnel.api.table.type.DecimalType;
import org.apache.seatunnel.api.table.type.LocalTimeType;
import org.apache.seatunnel.api.table.type.PrimitiveByteArrayType;
import org.apache.seatunnel.common.exception.SeaTunnelRuntimeException;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.DatabaseIdentifier;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.psql.PostgresTypeConverter;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;

// reference https://help.kingbase.com.cn/v8/development/sql-plsql/sql/datatype.html#id2
@Slf4j
@AutoService(TypeConverter.class)
public class KingbaseTypeConverter extends PostgresTypeConverter {
    public static final String KB_TINYINT = "TINYINT";
    public static final String KB_MONEY = "MONEY";
    public static final String KB_BLOB = "BLOB";
    public static final String KB_CLOB = "CLOB";
    public static final String KB_BIT = "BIT";
    public static final String KB_SMALLINT_ALIAS = "SMALLINT";
    public static final String KB_INT_ALIAS = "INT";
    public static final String KB_INTEGER_ALIAS = "INTEGER";
    public static final String KB_BIGINT_ALIAS = "BIGINT";
    public static final String KB_REAL_ALIAS = "REAL";
    public static final String KB_NUMERIC_ALIAS = "DECIMAL";
    public static final String KB_BOOLEAN_ALIAS = "BOOLEAN";
    public static final String KB_DATETIME = "DATETIME";

    public static final KingbaseTypeConverter INSTANCE = new KingbaseTypeConverter();

    @Override
    public String identifier() {
        return DatabaseIdentifier.KINGBASE;
    }

    @Override
    public Column convert(BasicTypeDefine typeDefine) {
        try {
            return super.convert(typeDefine);
        } catch (SeaTunnelRuntimeException e) {
            PhysicalColumn.PhysicalColumnBuilder builder =
                    PhysicalColumn.builder()
                            .name(typeDefine.getName())
                            .sourceType(typeDefine.getColumnType())
                            .nullable(typeDefine.isNullable())
                            .defaultValue(typeDefine.getDefaultValue())
                            .comment(typeDefine.getComment());

            String kingbaseDataType = normalizeTypeName(typeDefine.getDataType());
            switch (kingbaseDataType) {
                case KB_TINYINT:
                    builder.dataType(BasicType.BYTE_TYPE);
                    break;
                case KB_SMALLINT_ALIAS:
                    builder.dataType(BasicType.SHORT_TYPE);
                    break;
                case KB_INT_ALIAS:
                case KB_INTEGER_ALIAS:
                    builder.dataType(BasicType.INT_TYPE);
                    break;
                case KB_BIGINT_ALIAS:
                    builder.dataType(BasicType.LONG_TYPE);
                    break;
                case KB_REAL_ALIAS:
                    builder.dataType(BasicType.FLOAT_TYPE);
                    break;
                case KB_NUMERIC_ALIAS:
                    builder.dataType(toDecimalType(typeDefine));
                    break;
                case KB_BOOLEAN_ALIAS:
                    builder.dataType(BasicType.BOOLEAN_TYPE);
                    break;
                case KB_DATETIME:
                    builder.dataType(LocalTimeType.LOCAL_DATE_TIME_TYPE);
                    if (typeDefine.getScale() != null
                            && typeDefine.getScale() > MAX_TIMESTAMP_SCALE) {
                        builder.scale(MAX_TIMESTAMP_SCALE);
                        log.warn(
                                "The scale of datetime type is larger than {}, it will be truncated to {}",
                                MAX_TIMESTAMP_SCALE,
                                MAX_TIMESTAMP_SCALE);
                    } else {
                        builder.scale(typeDefine.getScale());
                    }
                    break;
                case KB_MONEY:
                    builder.dataType(new DecimalType(38, 18));
                    builder.columnLength(38L);
                    builder.scale(18);
                    break;
                case KB_BLOB:
                    builder.dataType(PrimitiveByteArrayType.INSTANCE);
                    builder.columnLength((long) (1024 * 1024 * 1024));
                    break;
                case KB_CLOB:
                    builder.dataType(BasicType.STRING_TYPE);
                    builder.columnLength(typeDefine.getLength());
                    builder.columnLength((long) (1024 * 1024 * 1024));
                    break;
                case KB_BIT:
                    builder.dataType(PrimitiveByteArrayType.INSTANCE);
                    // BIT(M) -> BYTE(M/8)
                    long byteLength = typeDefine.getLength() / 8;
                    byteLength += typeDefine.getLength() % 8 > 0 ? 1 : 0;
                    builder.columnLength(byteLength);
                    break;
                default:
                    log.warn(
                            "Kingbase unsupported type '{}' of column '{}', fallback to STRING",
                            kingbaseDataType,
                            typeDefine.getName());
                    builder.dataType(BasicType.STRING_TYPE);
                    break;
            }
            return builder.build();
        }
    }

    private static DecimalType toDecimalType(BasicTypeDefine typeDefine) {
        Long precision = typeDefine.getPrecision();
        if (precision != null && precision > 0) {
            return new DecimalType(
                    precision.intValue(),
                    typeDefine.getScale() == null ? 0 : typeDefine.getScale());
        }
        return new DecimalType(DEFAULT_PRECISION, DEFAULT_SCALE);
    }

    /**
     * Kingbase reports standard SQL type aliases (e.g. DECIMAL, DATETIME) which are absent from the
     * PostgreSQL type names, and may qualify them with the schema (e.g. {@code "sys"."datetime"}).
     */
    private static String normalizeTypeName(String dataType) {
        String name = dataType;
        int schemaSeparator = name.lastIndexOf('.');
        if (schemaSeparator >= 0) {
            name = name.substring(schemaSeparator + 1);
        }
        return name.replace("\"", "").toUpperCase();
    }

    @Override
    public BasicTypeDefine reconvert(Column column) {
        try {
            return super.reconvert(column);
        } catch (SeaTunnelRuntimeException e) {
            log.warn(
                    "Kingbase unsupported data type '{}' of column '{}', fallback to STRING",
                    column.getDataType().getSqlType(),
                    column.getName());
            return BasicTypeDefine.builder()
                    .name(column.getName())
                    .nullable(column.isNullable())
                    .comment(column.getComment())
                    .defaultValue(column.getDefaultValue())
                    .columnType(PG_TEXT)
                    .dataType(PG_TEXT)
                    .build();
        }
    }
}
