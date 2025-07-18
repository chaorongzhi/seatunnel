/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.sybase;

import org.apache.seatunnel.api.table.type.BasicType;
import org.apache.seatunnel.api.table.type.DecimalType;
import org.apache.seatunnel.api.table.type.LocalTimeType;
import org.apache.seatunnel.api.table.type.SeaTunnelDataType;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.JdbcDialectTypeMapper;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class SybaseTypeMapper implements JdbcDialectTypeMapper {

    // ============================data types=====================

    // -------------------------number----------------------------
    private static final String SYBASE_TINYINT = "TINYINT";
    private static final String SYBASE_SMALLINT = "SMALLINT";
    private static final String SYBASE_INT = "INT";
    private static final String SYBASE_BIGINT = "BIGINT";
    private static final String SYBASE_FLOAT = "FLOAT";
    private static final String SYBASE_DECIMAL = "DECIMAL";

    // -------------------------string----------------------------
    private static final String SYBASE_CHAR = "CHAR";
    private static final String SYBASE_NCHAR = "NCHAR";
    private static final String SYBASE_VARCHAR = "VARCHAR";
    private static final String SYBASE_NVARCHAR = "NVARCHAR";
    private static final String SYBASE_TEXT = "TEXT";

    // ------------------------------time-------------------------
    private static final String SYBASE_DATE = "DATE";
    private static final String SYBASE_DATETIME = "DATETIME";

    @Override
    public SeaTunnelDataType<?> mapping(ResultSetMetaData metadata, int colIndex)
            throws SQLException {
        String type = metadata.getColumnTypeName(colIndex).toUpperCase();
        switch (type) {
            case SYBASE_TINYINT:
                return BasicType.BYTE_TYPE;
            case SYBASE_SMALLINT:
                return BasicType.SHORT_TYPE;
            case SYBASE_INT:
                return BasicType.INT_TYPE;
            case SYBASE_BIGINT:
                return BasicType.LONG_TYPE;
            case SYBASE_FLOAT:
                return BasicType.FLOAT_TYPE;
            case SYBASE_DECIMAL:
                return new DecimalType(
                        metadata.getPrecision(colIndex), metadata.getScale(colIndex));
            case SYBASE_DATE:
                return LocalTimeType.LOCAL_DATE_TYPE;
            case SYBASE_DATETIME:
                return LocalTimeType.LOCAL_DATE_TIME_TYPE;
            case SYBASE_CHAR:
            case SYBASE_NCHAR:
            case SYBASE_VARCHAR:
            case SYBASE_NVARCHAR:
            case SYBASE_TEXT:
            default:
                return BasicType.STRING_TYPE;
                //                final String jdbcColumnName = metadata.getColumnName(colIndex);
                //                throw CommonError.convertToSeaTunnelTypeError(
                //                        DatabaseIdentifier.SYBASE, type, jdbcColumnName);
        }
    }
}
