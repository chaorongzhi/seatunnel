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

import org.apache.seatunnel.api.configuration.Option;
import org.apache.seatunnel.api.configuration.Options;
import org.apache.seatunnel.api.configuration.ReadonlyConfig;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class MulLineMergeTransformConfig implements Serializable {
    public static final String IDENTIFIER = "MulLineMerge";

    public static final Option<String> MERGE_PREFIX =
            Options.key("merge_prefix")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("The separator to split the field");

    public static final Option<String> MERGE_FIELD =
            Options.key("merge_field")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("The field to be merge");

    public static final Option<String> MERGE_SUFFIX =
            Options.key("merge_suffix")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("The result fields after split");

    private String mergePrefix;
    private String mergeField;
    private String mergeSuffix;

    public static MulLineMergeTransformConfig of(ReadonlyConfig config) {
        MulLineMergeTransformConfig splitTransformConfig = new MulLineMergeTransformConfig();
        splitTransformConfig.setMergeSuffix(config.get(MERGE_SUFFIX));
        splitTransformConfig.setMergeField(config.get(MERGE_FIELD));
        splitTransformConfig.setMergePrefix(config.get(MERGE_PREFIX));
        return splitTransformConfig;
    }
}
