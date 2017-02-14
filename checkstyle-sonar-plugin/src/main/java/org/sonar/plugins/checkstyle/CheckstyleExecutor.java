////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2017 the original author or authors.
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
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.utils.TimeProfiler;
import org.sonar.plugins.java.api.JavaResourceLocator;

import com.google.common.annotations.VisibleForTesting;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.PackageNamesLoader;
import com.puppycrawl.tools.checkstyle.XMLLogger;

public class CheckstyleExecutor implements BatchExtension {
    private static final Logger LOG = LoggerFactory.getLogger(CheckstyleExecutor.class);

    private final CheckstyleConfiguration configuration;
    private final CheckstyleAuditListener listener;
    private final JavaResourceLocator javaResourceLocator;

    public CheckstyleExecutor(CheckstyleConfiguration configuration,
            CheckstyleAuditListener listener, JavaResourceLocator javaResourceLocator) {
        this.configuration = configuration;
        this.listener = listener;
        this.javaResourceLocator = javaResourceLocator;
    }

    /**
     * Execute Checkstyle and return the generated XML report.
     */
    public void execute() {

        Locale initialLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        ClassLoader initialClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(PackageNamesLoader.class.getClassLoader());
        URLClassLoader projectClassloader = createClassloader();
        try {
            executeWithClassLoader(projectClassloader);
        }
        finally {
            Thread.currentThread().setContextClassLoader(initialClassLoader);
            Locale.setDefault(initialLocale);
            close(projectClassloader);
        }
    }

    private void executeWithClassLoader(URLClassLoader projectClassloader) {
        TimeProfiler profiler = new TimeProfiler().start("Execute Checkstyle "
                + CheckstyleVersion.getVersion());
        Checker checker = new Checker();
        OutputStream xmlOutput = null;
        try {
            checker.setClassLoader(projectClassloader);
            checker.setModuleClassLoader(Thread.currentThread().getContextClassLoader());
            checker.addListener(listener);

            File xmlReport = configuration.getTargetXmlReport();
            if (xmlReport != null) {
                LOG.info("Checkstyle output report: {}", xmlReport.getAbsolutePath());
                xmlOutput = FileUtils.openOutputStream(xmlReport);
                checker.addListener(new XMLLogger(xmlOutput, true));
            }

            checker.setCharset(configuration.getCharset().name());
            checker.configure(configuration.getCheckstyleConfiguration());
            checker.process(configuration.getSourceFiles());

            profiler.stop();

        }
        catch (Exception e) {
            throw new IllegalStateException("Can not execute Checkstyle", e);
        }
        finally {
            checker.destroy();
            if (xmlOutput != null) {
                close(xmlOutput);
            }
        }
    }

    @VisibleForTesting
    static void close(Closeable closeable) {
        try {
            closeable.close();
        }
        catch (IOException e) {
            throw new IllegalStateException("failed to close object", e);
        }
    }

    @VisibleForTesting
    URL getUrl(URI uri) {
        try {
            return uri.toURL();
        }
        catch (MalformedURLException e) {
            throw new IllegalStateException("Fail to create the project classloader. "
                    + "Classpath element is invalid: " + uri, e);
        }
    }

    private URLClassLoader createClassloader() {
        Collection<File> classpathElements = javaResourceLocator.classpath();
        List<URL> urls = new ArrayList<>(classpathElements.size());
        for (File file : classpathElements) {
            urls.add(getUrl(file.toURI()));
        }
        return new URLClassLoader(urls.toArray(new URL[urls.size()]), null);
    }

}
