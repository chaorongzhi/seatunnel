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

import lombok.Getter;
import lombok.Setter;
import org.apache.seatunnel.api.configuration.Option;
import org.apache.seatunnel.api.configuration.Options;
import org.apache.seatunnel.api.configuration.ReadonlyConfig;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class IISParseTransformConfig implements Serializable {
    public static final String IDENTIFIER = "IisParse";

    public static final Option<List<String>> REQUIRED_FORMAT =
            Options.key("required_format")
                    .listType(String.class)
                    .noDefaultValue()
                    .withDescription("The separator to split the field");

    public static final Option<String> PARSER_FIELD =
            Options.key("parse_field")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("The field to be parse");

    private String parse_field;
    private List<String> requiredFormat;

    public static IISParseTransformConfig of(ReadonlyConfig config) {
        IISParseTransformConfig splitTransformConfig = new IISParseTransformConfig();
        splitTransformConfig.setRequiredFormat(config.get(REQUIRED_FORMAT));
        splitTransformConfig.setParse_field(config.get(PARSER_FIELD));
        return splitTransformConfig;
    }
}
