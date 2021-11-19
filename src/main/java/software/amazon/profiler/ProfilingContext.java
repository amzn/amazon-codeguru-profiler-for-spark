/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package software.amazon.profiler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * This model class represents the profiling context of a given profiling environment.
 */
@Value
@Builder
@AllArgsConstructor
@JsonDeserialize(builder = ProfilingContext.ProfilingContextBuilder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ProfilingContext {

    String profilingGroupName;

    @Builder.Default
    boolean driverEnabled = false;

    @Builder.Default
    boolean executorEnabled = true;

    @Builder.Default
    boolean heapSummaryEnabled = true;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class ProfilingContextBuilder {
        // This method declaration is needed only for the JSON annotation
        // lombok will fill in the implementation details
    }

}
