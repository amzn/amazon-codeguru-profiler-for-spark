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

import software.amazon.codeguruprofilerjavaagent.Profiler;

import com.github.stefanbirkner.systemlambda.SystemLambda;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BasePluginTest {

    @Test
    public void testStartProfiler() {
        Profiler profiler = mock(Profiler.class);
        when(profiler.isRunning()).thenReturn(true);

        BasePlugin plugin = new BasePlugin() {
            @Override
            public Profiler createProfiler(String profilingGroupName, boolean heapSummaryEnabled) {
                return profiler;
            }
        };

        plugin.startProfiler("Sample-Spark-App-Beta", true);
        verify(profiler, times(1)).start();
        Assertions.assertEquals(profiler, plugin._profiler);
    }

    @Test
    public void testStopProfiler() {
        Profiler profiler = mock(Profiler.class);
        when(profiler.isRunning()).thenReturn(true);

        BasePlugin plugin = new BasePlugin();
        plugin._profiler = profiler;

        plugin.stopProfiler();
        verify(profiler, times(1)).stop();
    }

    @Test
    public void testCreateProfiler() {
        BasePlugin plugin = new BasePlugin();
        Profiler profiler = plugin.createProfiler("Sample-Spark-App-Gamma", true);
        Assertions.assertFalse(profiler.isRunning());
        Assertions.assertFalse(profiler.isProfiling());
    }

    @Test
    public void testGetContextWithoutEnvDefined() throws Exception {
        Assertions.assertNull(new BasePlugin().getContext());
    }

    @Test
    public void testGetContextWithFirstEnvDefined() throws Exception {
        ProfilingContext context = SystemLambda.withEnvironmentVariable("ENABLE_AMAZON_PROFILER", "true")
          .execute(() ->  new BasePlugin().getContext());
        Assertions.assertNull(context);
    }

    @Test
    public void testGetContext() throws Exception {
        ProfilingContext context = SystemLambda.withEnvironmentVariable("ENABLE_AMAZON_PROFILER", "true")
          .and("PROFILING_CONTEXT", "{\"profilingGroupName\":\"Sample-Spark-App-Beta\"}")
          .execute(() ->  new BasePlugin().getContext());

        Assertions.assertEquals("Sample-Spark-App-Beta", context.getProfilingGroupName());
        Assertions.assertFalse(context.isDriverEnabled());
        Assertions.assertTrue(context.isExecutorEnabled());
        Assertions.assertTrue(context.isHeapSummaryEnabled());
    }

    @Test
    public void testGetContextWithAllFlagsEnabled() throws Exception {
        ProfilingContext context = SystemLambda.withEnvironmentVariable("ENABLE_AMAZON_PROFILER", "true")
          .and("PROFILING_CONTEXT", "{\"profilingGroupName\":\"Sample-Spark-App-Beta\",\"driverEnabled\":\"true\"}")
          .execute(() ->  new BasePlugin().getContext());

        Assertions.assertEquals("Sample-Spark-App-Beta", context.getProfilingGroupName());
        Assertions.assertTrue(context.isDriverEnabled());
        Assertions.assertTrue(context.isExecutorEnabled());
        Assertions.assertTrue(context.isHeapSummaryEnabled());
    }

    @Test
    public void testGetContextWithAllFlagsDisabled() throws Exception {
        ProfilingContext context = SystemLambda.withEnvironmentVariable("ENABLE_AMAZON_PROFILER", "true")
          .and("PROFILING_CONTEXT", "{\"profilingGroupName\":\"Sample-Spark-App-Gamma\",\"executorEnabled\":\"false\",\"heapSummaryEnabled\":\"false\"}")
          .execute(() ->  new BasePlugin().getContext());

        Assertions.assertEquals("Sample-Spark-App-Gamma", context.getProfilingGroupName());
        Assertions.assertFalse(context.isDriverEnabled());
        Assertions.assertFalse(context.isExecutorEnabled());
        Assertions.assertFalse(context.isHeapSummaryEnabled());
    }

}
