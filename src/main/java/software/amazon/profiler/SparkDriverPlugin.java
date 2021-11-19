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
import java.util.Collections;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.SparkContext;
import org.apache.spark.api.plugin.DriverPlugin;
import org.apache.spark.api.plugin.PluginContext;

/**
 * This plugin provides a way to profile a Spark driver process.
 */
@Slf4j
public class SparkDriverPlugin extends BasePlugin implements DriverPlugin {

    /**
     * Each driver process will, during its initialization, invoke this method on each plugin.
     */
    @Override
    public Map<String, String> init(SparkContext sc, PluginContext pluginContext) {
        SERVICE.submit(() -> {
            try {
                ProfilingContext context = getContext();
                if (context != null && context.isDriverEnabled()) {
                    log.info("Profiling context: " + context);
                    startProfiler(context.getProfilingGroupName(), context.isHeapSummaryEnabled());
                }
            } catch (IOException | RuntimeException e) {
                log.warn("Failed to start profiling in driver", e);
            }
        });
        return Collections.emptyMap();
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
            log.warn("Failed to stop profiling in driver", e);
        }
    }

}
