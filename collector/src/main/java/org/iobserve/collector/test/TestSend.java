/***************************************************************************
 * Copyright (C) 2016 iObserve Project (https://www.iobserve-devops.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/
package org.iobserve.collector.test;

import kieker.common.configuration.Configuration;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.flow.trace.TraceMetadata;
import kieker.common.record.flow.trace.operation.BeforeOperationEvent;
import kieker.monitoring.core.configuration.ConfigurationFactory;
import kieker.monitoring.core.configuration.ConfigurationKeys;
import kieker.monitoring.core.controller.IMonitoringController;
import kieker.monitoring.core.controller.MonitoringController;
import kieker.monitoring.writer.tcp.SingleSocketTcpWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an integration test class used to test the Collector service.
 *
 * @author Reiner Jung
 *
 */
public final class TestSend {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestSend.class);

    private static final String WRITER_NAME = "kieker.monitoring.writer.tcp.SingleSocketTcpWriter";

    /**
     * This is only a integration test.
     */
    private TestSend() {
    }

    /**
     * Execute send test.
     *
     * @param args
     *            arguments are ignored.
     */
    public static void main(final String[] args) {
        TestSend.LOGGER.debug("Sender");
        final Configuration configuration = ConfigurationFactory.createDefaultConfiguration();
        configuration.setProperty(ConfigurationKeys.CONTROLLER_NAME, "Kieker-Test");
        configuration.setProperty(ConfigurationKeys.WRITER_CLASSNAME, TestSend.WRITER_NAME);
        configuration.setProperty(SingleSocketTcpWriter.CONFIG_HOSTNAME, "localhost");
        configuration.setProperty(SingleSocketTcpWriter.CONFIG_PORT, "9876");
        configuration.setProperty(SingleSocketTcpWriter.CONFIG_BUFFERSIZE, "1024");
        configuration.setProperty(SingleSocketTcpWriter.CONFIG_FLUSH, "true");

        // add ignored values
        configuration.setProperty(ConfigurationKeys.PREFIX + "test", "true");
        configuration.setProperty(TestSend.WRITER_NAME + ".test", "true");

        TestSend.LOGGER.debug("Configuration complete");

        final IMonitoringController ctrl = MonitoringController.createInstance(configuration);

        TestSend.LOGGER.debug("Controller active");
        TestSend.LOGGER.debug("Send first record");

        IMonitoringRecord record = new TraceMetadata(1, 2, "demo", "hostname", 0, 0);
        ctrl.newMonitoringRecord(record);

        TestSend.LOGGER.debug("Send second record");

        record = new BeforeOperationEvent(0, 1, 0, "Send", "main");
        ctrl.newMonitoringRecord(record);

        TestSend.LOGGER.debug("Done");
    }
}
