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

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.lang.CharUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public final class CheckstyleTestUtils {

    private CheckstyleTestUtils() {
        // no code
    }

    public static String getResourceContent(String path) {
        try {
            return Resources.toString(Resources.getResource(CheckstyleTestUtils.class, path),
                    Charsets.UTF_8);
        }
        catch (IOException ex) {
            throw new IllegalArgumentException("Could not load resource " + path, ex);
        }
    }

    public static void assertSimilarXml(String expectedXml, String xml) {
        XMLUnit.setIgnoreWhitespace(true);
        final Diff diff;
        try {
            diff = XMLUnit.compareXML(xml, expectedXml);
        }
        catch (SAXException | IOException ex) {
            throw new IllegalArgumentException("Could not run XML comparison", ex);
        }
        final String message = "Diff: " + diff + CharUtils.LF + "XML: " + xml;
        assertTrue(message, diff.similar());
    }

    public static void assertSimilarXmlWithResource(String expectedXmlResourcePath, String xml) {
        assertSimilarXml(getResourceContent(expectedXmlResourcePath), xml);
    }

}
