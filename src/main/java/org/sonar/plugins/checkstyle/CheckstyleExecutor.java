////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2025 the original author or authors.
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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.ExtensionPoint;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.scanner.ScannerSide;

import com.google.common.annotations.VisibleForTesting;
import com.puppycrawl.tools.checkstyle.AbstractAutomaticBean;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.PackageNamesLoader;
import com.puppycrawl.tools.checkstyle.XMLLogger;

@ExtensionPoint
@ScannerSide
public class CheckstyleExecutor {
    public static final String PROPERTIES_PATH =
            "/org/sonar/plugins/checkstyle/checkstyle-plugin.properties";

    private static final Logger LOG = LoggerFactory.getLogger(CheckstyleExecutor.class);

    private final CheckstyleConfiguration configuration;
    private final CheckstyleAuditListener listener;

    public CheckstyleExecutor(CheckstyleConfiguration configuration,
            CheckstyleAuditListener listener) {
        this.configuration = configuration;
        this.listener = listener;
    }

    /**
     * Execute Checkstyle and return the generated XML report.
     *
     * @param context The context of the execution.
     * @noinspection TooBroadScope
     * @noinspectionreason Cache the value of the default locale.
     */
    public void execute(SensorContext context) {
        if (Objects.nonNull(listener)) {
            listener.setContext(context);
        }

        final Locale initialLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        final ClassLoader initialClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(PackageNamesLoader.class.getClassLoader());
        try {
            executeWithClassLoader();
        }
        finally {
            Thread.currentThread().setContextClassLoader(initialClassLoader);
            Locale.setDefault(initialLocale);
        }
    }

    private void executeWithClassLoader() {
        final Checker checker = new Checker();
        OutputStream xmlOutput = null;
        try {
            checker.setModuleClassLoader(Thread.currentThread().getContextClassLoader());
            checker.addListener(listener);

            final File xmlReport = configuration.getTargetXmlReport();
            if (xmlReport != null) {
                LOG.info("Checkstyle output report: {}", xmlReport.getAbsolutePath());
                xmlOutput = FileUtils.openOutputStream(xmlReport);
                checker.addListener(
                        new XMLLogger(xmlOutput, AbstractAutomaticBean.OutputStreamOptions.CLOSE));
            }

            checker.setCharset(configuration.getCharset().name());
            checker.configure(configuration.getCheckstyleConfiguration());
            checker.process(configuration
                    .getSourceFiles()
                .stream()
                .map(inputFile -> new File(inputFile.uri()))
                .collect(Collectors.toList()));
        }
        catch (Exception exception) {
            throw new IllegalStateException("Can not execute Checkstyle", exception);
        }
        finally {
            checker.destroy();
            if (Objects.nonNull(xmlOutput)) {
                close(xmlOutput);
            }
        }
    }

    @VisibleForTesting
    static void close(Closeable closeable) {
        try {
            closeable.close();
        }
        catch (IOException exception) {
            throw new IllegalStateException("failed to close object", exception);
        }
    }

    /**
     * Retrieves the URL from the URI.
     *
     * @param uri The URI to generate the URL from.
     * @return The generated URL.
     * @throws IllegalStateException if the conversion of the URI failed.
     */
    @VisibleForTesting
    URL getUrl(URI uri) {
        try {
            return uri.toURL();
        }
        catch (MalformedURLException exception) {
            throw new IllegalStateException("Fail to create the project classloader. "
                    + "Classpath element is invalid: " + uri, exception);
        }
    }
}
