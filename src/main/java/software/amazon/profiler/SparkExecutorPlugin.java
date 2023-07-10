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

import java.io.IOException;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.plugin.ExecutorPlugin;
import org.apache.spark.api.plugin.PluginContext;

/**
 * This plugin provides a way to profile Spark executor processes.
 */
@Slf4j
public class SparkExecutorPlugin extends BasePlugin implements ExecutorPlugin {

    /**
     * Each executor process will, during its initialization, invoke this method on each plugin.
     */
    @Override
    public void init(PluginContext ctx, Map<String, String> extraConf) {
        SERVICE.submit(() -> {
            try {
                ProfilingContext context = getContext();
                if (context != null && context.isExecutorEnabled()) {
                    log.info("Profiling context: " + context);
                    startProfiler(context.getProfilingGroupName(), context.isHeapSummaryEnabled(), context.getProbability());
                }
            } catch (IOException | RuntimeException e) {
                log.warn("Failed to start profiling in executor", e);
            }
        });
    }

    /**
     * Clean up and terminate this plugin.
     */
    @Override
    public void shutdown() {
        try {
            stopProfiler();
            SERVICE.shutdown();
        } catch (RuntimeException e) {
            log.warn("Failed to stop profiling in executor", e);
        }
    }

}
