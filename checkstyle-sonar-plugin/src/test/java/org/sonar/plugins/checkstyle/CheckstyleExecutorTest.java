/*
 * SonarQube Checkstyle Plugin
 * Copyright (C) 2012 SonarSource
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.checkstyle;

import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.sonar.java.DefaultJavaResourceLocator;
import org.sonar.java.JavaClasspath;
import org.sonar.plugins.java.api.JavaResourceLocator;

import com.google.common.collect.ImmutableList;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

public class CheckstyleExecutorTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void execute() throws CheckstyleException {
    CheckstyleConfiguration conf = mockConf();
    CheckstyleAuditListener listener = mockListener();
    CheckstyleExecutor executor =
            new CheckstyleExecutor(conf, listener, createJavaResourceLocator());
    executor.execute();

    verify(listener, times(1)).auditStarted(any(AuditEvent.class));
    verify(listener, times(1)).auditFinished(any(AuditEvent.class));

    InOrder inOrder = Mockito.inOrder(listener);
    ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
    inOrder.verify(listener).fileStarted(captor.capture());
    assertThat(captor.getValue().getFileName()).matches(".*Hello.java");
    inOrder.verify(listener).fileFinished(captor.capture());
    assertThat(captor.getValue().getFileName()).matches(".*Hello.java");
    inOrder.verify(listener).fileStarted(captor.capture());
    assertThat(captor.getValue().getFileName()).matches(".*World.java");
    inOrder.verify(listener).fileFinished(captor.capture());
    assertThat(captor.getValue().getFileName()).matches(".*World.java");
    verify(listener, atLeast(1)).addError(captor.capture());
    AuditEvent event = captor.getValue();
    assertThat(event.getFileName()).matches(".*Hello.java");
    assertThat(event.getSourceName())
            .matches("com.puppycrawl.tools.checkstyle.checks.coding.EmptyStatementCheck");
  }

  @Test
  public void executeException() throws CheckstyleException {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Can not execute Checkstyle");
    CheckstyleConfiguration conf = mockConf();
    CheckstyleExecutor executor =
            new CheckstyleExecutor(conf, null, createJavaResourceLocator());
    executor.execute();
  }

  @Test
  public void getUrlException() throws URISyntaxException {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Fail to create the project classloader. "
            + "Classpath element is invalid: htp://aa");
    CheckstyleExecutor executor =
            new CheckstyleExecutor(null, null, createJavaResourceLocator());
    executor.getUrl(new URI("htp://aa"));
  }

  private static JavaResourceLocator createJavaResourceLocator() {
    JavaClasspath javaClasspath = mock(JavaClasspath.class);
    when(javaClasspath.getElements()).thenReturn(ImmutableList.of(new File(".")));
    return new DefaultJavaResourceLocator(null, javaClasspath, null);
  }

  @Test
  public void canGenerateXmlReportInEnglish() throws CheckstyleException, IOException {
    Locale initialLocale = Locale.getDefault();
    Locale.setDefault(Locale.FRENCH);

    try {
      CheckstyleConfiguration conf = mockConf();
      File report = new File("target/test-tmp/checkstyle-report.xml");
      when(conf.getTargetXmlReport()).thenReturn(report);
      CheckstyleAuditListener listener = mockListener();
      CheckstyleExecutor executor =
              new CheckstyleExecutor(conf, listener, createJavaResourceLocator());
      executor.execute();

      assertThat(report.exists(), is(true));

      String reportContents = FileUtils.readFileToString(report);
      assertThat(reportContents).contains("<error");
      assertThat(reportContents).contains("Empty statement.");
    } finally {
      assertThat(Locale.getDefault()).isEqualTo(Locale.FRENCH);
      Locale.setDefault(initialLocale);
    }
  }

  private static CheckstyleAuditListener mockListener() {
    return mock(CheckstyleAuditListener.class);
  }

  private static CheckstyleConfiguration mockConf() throws CheckstyleException {
    CheckstyleConfiguration conf = mock(CheckstyleConfiguration.class);
    when(conf.getCharset()).thenReturn(Charset.defaultCharset());
    when(conf.getCheckstyleConfiguration()).thenReturn(
            CheckstyleConfiguration.toCheckstyleConfiguration(
                    new File("test-resources/checkstyle-conf.xml"))
    );
    when(conf.getSourceFiles()).thenReturn(
            Arrays.asList(new File("test-resources/Hello.java"),
                    new File("test-resources/World.java"))
    );
    return conf;
  }

}
