////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2026 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////

package org.sonar.plugins.checkstyle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.scanner.sensor.ProjectSensor;

public class CheckstyleSensor implements ProjectSensor {
    private static final String CHECKSTYLE_ENABLED = "sonar.checkstyle.enabled";

    private final CheckstyleExecutor executor;
    private static final Logger LOG = LoggerFactory.getLogger(CheckstyleSensor.class);

    public CheckstyleSensor(CheckstyleExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.onlyOnLanguage("java").name("CheckstyleSensor");
    }

    @Override
    public void execute(SensorContext context) {
        if (context.config().getBoolean(CHECKSTYLE_ENABLED).orElse(true)) {
            executor.execute(context);
        } else {
            LOG.info("Checkstyle plugin is disabled.");
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
