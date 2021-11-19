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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class ProfilingContextTest {

    @Test
    public void testGetProfilingGroupName() {
        ProfilingContext context = ProfilingContext.builder()
                                                   .profilingGroupName("Sample-Spark-App-Beta")
                                                   .heapSummaryEnabled(false)
                                                   .build();

        Assertions.assertEquals("Sample-Spark-App-Beta", context.getProfilingGroupName());
        Assertions.assertFalse(context.isHeapSummaryEnabled());
    }

    @Test
    public void testIsHeapSummaryEnabled() {
        ProfilingContext context = ProfilingContext.builder()
                                                   .profilingGroupName("Sample-Spark-App-Gamma")
                                                   .build();

        Assertions.assertEquals("Sample-Spark-App-Gamma", context.getProfilingGroupName());
        Assertions.assertTrue(context.isHeapSummaryEnabled());
    }

    @Test
    public void testNullProfilingGroupName() {
        ProfilingContext context = ProfilingContext.builder()
                                                   .build();

        Assertions.assertNull(context.getProfilingGroupName());
        Assertions.assertTrue(context.isHeapSummaryEnabled());
    }

}
