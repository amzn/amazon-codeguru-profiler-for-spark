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

import org.apache.spark.SparkConf;
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

        plugin.startProfiler("Sample-Spark-App-Beta", true, 1.0);
        verify(profiler, times(1)).start();
        Assertions.assertEquals(profiler, plugin._profiler);
    }

    @Test
    public void testStartProfilerZeroProbability() {
        Profiler profiler = mock(Profiler.class);
        when(profiler.isRunning()).thenReturn(true);

        BasePlugin plugin = new BasePlugin() {
            @Override
            public Profiler createProfiler(String profilingGroupName, boolean heapSummaryEnabled) {
                return profiler;
            }
        };

        plugin.startProfiler("Sample-Spark-App-Beta", true, 0.00);
        verify(profiler, times(0)).start();
        Assertions.assertNull(plugin._profiler);
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
    public void testGetContextWithoutEnvOrSparkConfDefined() throws Exception {
        Assertions.assertNull(new BasePlugin().getContext(new SparkConf(), false));
    }

    @Test
    public void testGetContextWithFirstEnvDefined() throws Exception {
        ProfilingContext context = SystemLambda.withEnvironmentVariable("ENABLE_AMAZON_PROFILER", "true")
          .execute(() ->  new BasePlugin().getContext(new SparkConf(), false));
        Assertions.assertNull(context);
    }

    @Test
    public void testGetContextWithEnvDefined() throws Exception {
        ProfilingContext context = SystemLambda.withEnvironmentVariable("ENABLE_AMAZON_PROFILER", "true")
          .and("PROFILING_CONTEXT", "{\"profilingGroupName\":\"Sample-Spark-App-Beta\"}")
          .execute(() ->  new BasePlugin().getContext(new SparkConf(), false));

        Assertions.assertEquals("Sample-Spark-App-Beta", context.getProfilingGroupName());
        Assertions.assertFalse(context.isDriverEnabled());
        Assertions.assertTrue(context.isExecutorEnabled());
        Assertions.assertTrue(context.isHeapSummaryEnabled());
    }

    @Test
    public void testGetContextWithAllFlagsEnabledInEnv() throws Exception {
        ProfilingContext context = SystemLambda.withEnvironmentVariable("ENABLE_AMAZON_PROFILER", "true")
          .and("PROFILING_CONTEXT", "{\"profilingGroupName\":\"Sample-Spark-App-Beta\",\"driverEnabled\":\"true\"}")
          .execute(() ->  new BasePlugin().getContext(new SparkConf(), false));

        Assertions.assertEquals("Sample-Spark-App-Beta", context.getProfilingGroupName());
        Assertions.assertTrue(context.isDriverEnabled());
        Assertions.assertTrue(context.isExecutorEnabled());
        Assertions.assertTrue(context.isHeapSummaryEnabled());
    }

    @Test
    public void testGetContextWithAllFlagsDisabledInEnv() throws Exception {
        ProfilingContext context = SystemLambda.withEnvironmentVariable("ENABLE_AMAZON_PROFILER", "true")
          .and("PROFILING_CONTEXT", "{\"profilingGroupName\":\"Sample-Spark-App-Gamma\",\"executorEnabled\":\"false\",\"heapSummaryEnabled\":\"false\"}")
          .execute(() ->  new BasePlugin().getContext(new SparkConf(), false));

        Assertions.assertEquals("Sample-Spark-App-Gamma", context.getProfilingGroupName());
        Assertions.assertFalse(context.isDriverEnabled());
        Assertions.assertFalse(context.isExecutorEnabled());
        Assertions.assertFalse(context.isHeapSummaryEnabled());
    }

    @Test
    public void testGetContextWithFirstSparkConfDefinedInExecutor() throws Exception {
        SparkConf sparkConf = new SparkConf();
        sparkConf.set("spark.executorEnv.ENABLE_AMAZON_PROFILER", "true");
        ProfilingContext context = new BasePlugin().getContext(sparkConf, false);
        Assertions.assertNull(context);
    }

    @Test
    public void testGetContextWithBothSparkConfDefinedInExecutor() throws Exception {
        SparkConf sparkConf = new SparkConf();
        sparkConf.set("spark.executorEnv.ENABLE_AMAZON_PROFILER", "true");
        sparkConf.set("spark.executorEnv.PROFILING_CONTEXT", "{\"profilingGroupName\":\"Sample-Spark-App-Beta\"}");

        ProfilingContext context = new BasePlugin().getContext(sparkConf, false);

        Assertions.assertEquals("Sample-Spark-App-Beta", context.getProfilingGroupName());
        Assertions.assertFalse(context.isDriverEnabled());
        Assertions.assertTrue(context.isExecutorEnabled());
        Assertions.assertTrue(context.isHeapSummaryEnabled());
    }

    @Test
    public void testGetContextWithAllFlagsEnabledInSparkConfForExecutor() throws Exception {
        SparkConf sparkConf = new SparkConf();
        sparkConf.set("spark.executorEnv.ENABLE_AMAZON_PROFILER", "true");
        sparkConf.set("spark.executorEnv.PROFILING_CONTEXT", "{\"profilingGroupName\":\"Sample-Spark-App-Beta\",\"driverEnabled\":\"true\"}");

        ProfilingContext context = new BasePlugin().getContext(sparkConf, false);

        Assertions.assertEquals("Sample-Spark-App-Beta", context.getProfilingGroupName());
        Assertions.assertTrue(context.isDriverEnabled());
        Assertions.assertTrue(context.isExecutorEnabled());
        Assertions.assertTrue(context.isHeapSummaryEnabled());
    }

    @Test
    public void testGetContextWithAllFlagsDisabledInSparkConfForExecutor() throws Exception {
        SparkConf sparkConf = new SparkConf();
        sparkConf.set("spark.executorEnv.ENABLE_AMAZON_PROFILER", "true");
        sparkConf.set("spark.executorEnv.PROFILING_CONTEXT", "{\"profilingGroupName\":\"Sample-Spark-App-Gamma\",\"executorEnabled\":\"false\",\"heapSummaryEnabled\":\"false\"}");

        ProfilingContext context = new BasePlugin().getContext(sparkConf, false);

        Assertions.assertEquals("Sample-Spark-App-Gamma", context.getProfilingGroupName());
        Assertions.assertFalse(context.isDriverEnabled());
        Assertions.assertFalse(context.isExecutorEnabled());
        Assertions.assertFalse(context.isHeapSummaryEnabled());
    }

    @Test
    public void testGetContextWithFirstSparkConfDefinedInDriver() throws Exception {
        SparkConf sparkConf = new SparkConf();
        sparkConf.set("spark.yarn.appMasterEnv.ENABLE_AMAZON_PROFILER", "true");
        ProfilingContext context = new BasePlugin().getContext(sparkConf, true);
        Assertions.assertNull(context);
    }

    @Test
    public void testGetContextWithBothSparkConfDefinedInDriver() throws Exception {
        SparkConf sparkConf = new SparkConf();
        sparkConf.set("spark.yarn.appMasterEnv.ENABLE_AMAZON_PROFILER", "true");
        sparkConf.set("spark.yarn.appMasterEnv.PROFILING_CONTEXT", "{\"profilingGroupName\":\"Sample-Spark-App-Beta\"}");

        ProfilingContext context = new BasePlugin().getContext(sparkConf, true);

        Assertions.assertEquals("Sample-Spark-App-Beta", context.getProfilingGroupName());
        Assertions.assertFalse(context.isDriverEnabled());
        Assertions.assertTrue(context.isExecutorEnabled());
        Assertions.assertTrue(context.isHeapSummaryEnabled());
    }

    @Test
    public void testGetContextWithAllFlagsEnabledInSparkConfForDriver() throws Exception {
        SparkConf sparkConf = new SparkConf();
        sparkConf.set("spark.yarn.appMasterEnv.ENABLE_AMAZON_PROFILER", "true");
        sparkConf.set("spark.yarn.appMasterEnv.PROFILING_CONTEXT", "{\"profilingGroupName\":\"Sample-Spark-App-Beta\",\"driverEnabled\":\"true\"}");

        ProfilingContext context = new BasePlugin().getContext(sparkConf, true);

        Assertions.assertEquals("Sample-Spark-App-Beta", context.getProfilingGroupName());
        Assertions.assertTrue(context.isDriverEnabled());
        Assertions.assertTrue(context.isExecutorEnabled());
        Assertions.assertTrue(context.isHeapSummaryEnabled());
    }

    @Test
    public void testGetContextWithAllFlagsDisabledInSparkConfForDriver() throws Exception {
        SparkConf sparkConf = new SparkConf();
        sparkConf.set("spark.yarn.appMasterEnv.ENABLE_AMAZON_PROFILER", "true");
        sparkConf.set("spark.yarn.appMasterEnv.PROFILING_CONTEXT", "{\"profilingGroupName\":\"Sample-Spark-App-Gamma\",\"executorEnabled\":\"false\",\"heapSummaryEnabled\":\"false\"}");

        ProfilingContext context = new BasePlugin().getContext(sparkConf, true);

        Assertions.assertEquals("Sample-Spark-App-Gamma", context.getProfilingGroupName());
        Assertions.assertFalse(context.isDriverEnabled());
        Assertions.assertFalse(context.isExecutorEnabled());
        Assertions.assertFalse(context.isHeapSummaryEnabled());
    }
}
