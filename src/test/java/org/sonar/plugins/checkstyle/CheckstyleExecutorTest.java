////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2023 the original author or authors.
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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.internal.DefaultIndexedFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.SensorContext;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

public class CheckstyleExecutorTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private final SensorContext context = mock(SensorContext.class);

    @Test
    public void execute() throws CheckstyleException {
        final CheckstyleConfiguration conf = mockConf();
        final CheckstyleAuditListener listener = mockListener();
        final CheckstyleExecutor executor = new CheckstyleExecutor(conf, listener);
        executor.execute(context);

        verify(listener, times(1)).auditStarted(any(AuditEvent.class));
        verify(listener, times(1)).auditFinished(any(AuditEvent.class));

        final InOrder inOrder = Mockito.inOrder(listener);
        final ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        inOrder.verify(listener).fileStarted(captor.capture());
        assertThat(captor.getValue().getFileName()).matches(".*Hello.java");
        inOrder.verify(listener).fileFinished(captor.capture());
        assertThat(captor.getValue().getFileName()).matches(".*Hello.java");
        inOrder.verify(listener).fileStarted(captor.capture());
        assertThat(captor.getValue().getFileName()).matches(".*World.java");
        inOrder.verify(listener).fileFinished(captor.capture());
        assertThat(captor.getValue().getFileName()).matches(".*World.java");
        verify(listener, atLeast(1)).addError(captor.capture());
        final AuditEvent event = captor.getValue();
        assertThat(event.getFileName()).matches(".*Hello.java");
        assertThat(event.getSourceName()).matches(
                "com.puppycrawl.tools.checkstyle.checks.coding.EmptyStatementCheck");
    }

    @Test
    public void executeException() throws CheckstyleException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Can not execute Checkstyle");
        final CheckstyleConfiguration conf = mockConf();
        final CheckstyleExecutor executor = new CheckstyleExecutor(conf, null);
        executor.execute(context);
    }

    @Test
    public void getUrlException() throws URISyntaxException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Fail to create the project classloader. "
                + "Classpath element is invalid: htp://aa");
        final CheckstyleExecutor executor = new CheckstyleExecutor(null, mockListener());
        executor.getUrl(new URI("htp://aa"));
    }

    /**
     * We do suppression as we need to cache value initialLocale
     *
     * @noinspection TooBroadScope
     * @noinspectionreason Cache the value of the default locale.
     */
    @Test
    public void generateXmlReportInEnglish() throws Exception {
        final Locale initialLocale = Locale.getDefault();
        Locale.setDefault(Locale.FRENCH);

        try {
            final CheckstyleConfiguration conf = mockConf();
            final File report = new File("target/test-tmp/checkstyle-report.xml");
            // delete if exists from a previous run
            report.delete();
            when(conf.getTargetXmlReport()).thenReturn(report);
            final CheckstyleAuditListener listener = mockListener();
            final CheckstyleExecutor executor = new CheckstyleExecutor(conf, listener);
            executor.execute(context);

            Assert.assertTrue("Report should exists", report.exists());

            final String reportContents = FileUtils.readFileToString(report);
            assertThat(reportContents).contains("<error");
            assertThat(reportContents).contains("Empty statement.");
        }
        finally {
            assertThat(Locale.getDefault()).isEqualTo(Locale.FRENCH);
            Locale.setDefault(initialLocale);
        }
    }

    @Test
    public void generateXmlReportNull() throws CheckstyleException {
        final CheckstyleConfiguration conf = mockConf();
        final File report = new File("target/test-tmp/checkstyle-report.xml");
        // delete if exists from a previous run
        report.delete();
        when(conf.getTargetXmlReport()).thenReturn(null);
        final CheckstyleAuditListener listener = mockListener();
        final CheckstyleExecutor executor = new CheckstyleExecutor(conf, listener);
        executor.execute(context);

        Assert.assertFalse("Report should NOT exists", report.exists());
    }

    @Test
    public void closeNoException() throws IOException {
        final Closeable closeable = mock(Closeable.class);

        CheckstyleExecutor.close(closeable);

        verify(closeable, times(1)).close();
    }

    @Test
    public void closeWithException() throws IOException {
        final Closeable closeable = mock(Closeable.class);
        // using a static import pushes us above the PMD import limit
        Mockito.doThrow(IOException.class).when(closeable).close();

        thrown.expect(IllegalStateException.class);
        CheckstyleExecutor.close(closeable);
    }

    private static CheckstyleAuditListener mockListener() {
        return mock(CheckstyleAuditListener.class);
    }

    private static CheckstyleConfiguration mockConf() throws CheckstyleException {
        final CheckstyleConfiguration conf = mock(CheckstyleConfiguration.class);
        when(conf.getCharset()).thenReturn(Charset.defaultCharset());
        when(conf.getCheckstyleConfiguration()).thenReturn(
                CheckstyleConfiguration.toCheckstyleConfiguration(
                        new File("test-resources/checkstyle-conf.xml")));

        final File file = new File("test-resources/Hello.java");
        final File file2 = new File("test-resources/World.java");
        when(conf.getSourceFiles()).thenReturn(Arrays.asList(
                new DefaultInputFile(
                        new DefaultIndexedFile("",
                                file.getParentFile().toPath(),
                                file.getName(),
                                "java"),
                        DefaultInputFile::checkMetadata),
                new DefaultInputFile(
                        new DefaultIndexedFile("",
                                file2.getParentFile().toPath(),
                                file2.getName(),
                                "java"),
                        DefaultInputFile::checkMetadata)
        ));

        return conf;
    }

}
