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

package org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.informix;

import org.apache.seatunnel.api.table.type.*;
import org.apache.seatunnel.connectors.seatunnel.jdbc.internal.dialect.JdbcDialectTypeMapper;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class InformixTypeMapper implements JdbcDialectTypeMapper {

    // ============================data types=====================

    // -------------------------number----------------------------
    private static final String INFORMIX_SMALLINT = "SMALLINT";
    private static final String INFORMIX_INT = "INT";
    private static final String INFORMIX_INTEGER = "INTEGER";
    private static final String INFORMIX_SERIAL = "SERIAL";
    private static final String INFORMIX_BIGINT = "BIGINT";
    private static final String INFORMIX_INT8 = "INT8";
    private static final String INFORMIX_SERIAL8 = "SERIAL8";
    private static final String INFORMIX_FLOAT = "FLOAT";
    private static final String INFORMIX_SMALLFLOAT = "SMALLFLOAT";
    private static final String INFORMIX_DECIMAL = "DECIMAL";
    private static final String INFORMIX_NUMERIC = "NUMERIC";

    // -------------------------boolean----------------------------
    private static final String INFORMIX_BOOLEAN = "BOOLEAN";

    // -------------------------string----------------------------
    private static final String INFORMIX_CHAR = "CHAR";
    private static final String INFORMIX_NCHAR = "NCHAR";
    private static final String INFORMIX_CHARACTER = "CHARACTER";
    private static final String INFORMIX_VARCHAR = "VARCHAR";
    private static final String INFORMIX_NVARCHAR = "NVARCHAR";
    private static final String INFORMIX_LVARCHAR = "LVARCHAR";
    private static final String INFORMIX_TEXT = "TEXT";
    private static final String INFORMIX_CLOB = "CLOB";

    // ------------------------------time-------------------------
    private static final String INFORMIX_DATE = "DATE";
    private static final String INFORMIX_DATETIME = "DATETIME";

    // ------------------------------blob-------------------------
    private static final String INFORMIX_BLOB = "BLOB";
    private static final String INFORMIX_BYTE = "BYTE";

    @Override
    public SeaTunnelDataType<?> mapping(ResultSetMetaData metadata, int colIndex)
            throws SQLException {
        String type = metadata.getColumnTypeName(colIndex).toUpperCase();
        switch (type) {
            case INFORMIX_BYTE:
                return BasicType.BYTE_TYPE;
            case INFORMIX_SMALLINT:
                return BasicType.SHORT_TYPE;
            case INFORMIX_INT:
            case INFORMIX_INTEGER:
            case INFORMIX_SERIAL:
                return BasicType.INT_TYPE;
            case INFORMIX_BIGINT:
            case INFORMIX_INT8:
            case INFORMIX_SERIAL8:
                return BasicType.LONG_TYPE;
            case INFORMIX_FLOAT:
            case INFORMIX_SMALLFLOAT:
                return BasicType.FLOAT_TYPE;
            case INFORMIX_DECIMAL:
            case INFORMIX_NUMERIC:
                return new DecimalType(
                        metadata.getPrecision(colIndex), metadata.getScale(colIndex));
            case INFORMIX_DATE:
                return LocalTimeType.LOCAL_DATE_TYPE;
            case INFORMIX_DATETIME:
                return LocalTimeType.LOCAL_DATE_TIME_TYPE;
            case INFORMIX_BLOB:
                return PrimitiveByteArrayType.INSTANCE;
            case INFORMIX_BOOLEAN:
                return BasicType.BOOLEAN_TYPE;
            case INFORMIX_CHAR:
            case INFORMIX_NCHAR:
            case INFORMIX_VARCHAR:
            case INFORMIX_NVARCHAR:
            case INFORMIX_TEXT:
            case INFORMIX_CLOB:
            case INFORMIX_CHARACTER:
            case INFORMIX_LVARCHAR:
            default:
                return BasicType.STRING_TYPE;
                //                final String jdbcColumnName = metadata.getColumnName(colIndex);
                //                throw CommonError.convertToSeaTunnelTypeError(
                //                        DatabaseIdentifier.INFORMIX, type, jdbcColumnName);
        }
    }
}
