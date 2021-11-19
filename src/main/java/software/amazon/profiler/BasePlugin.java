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
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import software.amazon.codeguruprofilerjavaagent.Profiler;

/**
 * A base class interacts with AWS CodeGuru to start and stop profiling.
 */
@Slf4j
public class BasePlugin {

    static final ExecutorService SERVICE = Executors.newSingleThreadExecutor(runnable -> {
        final Thread thread = new Thread(runnable, "Amazon-Profiler-Plugin");
        // Use daemon threads to not block application shutdown
        thread.setDaemon(true);
        return thread;
    });

    // One profiler per JVM process
    transient volatile Profiler _profiler;

    public synchronized void startProfiler(String profilingGroupName, boolean heapSummaryEnabled) {
        Profiler profiler = _profiler;
        if (profiler == null) {
            profiler = createProfiler(profilingGroupName, heapSummaryEnabled);
            log.info("Profiling is being started");
            profiler.start();
            _profiler = profiler;
        }
    }

    public synchronized void stopProfiler() {
        Profiler profiler = _profiler;
        if (profiler != null && profiler.isRunning()) {
            profiler.stop();
            log.info("Profiling is stopped");
        }
        _profiler = null;
    }

    public Profiler createProfiler(String profilingGroupName, boolean heapSummaryEnabled) {
        return Profiler.builder()
            .profilingGroupName(profilingGroupName)
            .withHeapSummary(heapSummaryEnabled)
            .build();
    }

    public ProfilingContext getContext() throws IOException {
        if ("true".equals(System.getenv("ENABLE_AMAZON_PROFILER"))) {
            log.info("Profiling is enabled");
            String json = System.getenv("PROFILING_CONTEXT");
            if (json != null) {
                return new ObjectMapper().readValue(json, ProfilingContext.class);
            }
        }
        return null;
    }

}
