////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2020 the original author or authors.
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

import org.sonar.api.ExtensionPoint;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.checkstyle.metadata.CheckstyleMetadata;

@ExtensionPoint
@ScannerSide
public final class CheckstyleRulesDefinition implements RulesDefinition {

    @Override
    public void define(Context context) {
        final NewRepository repository = context.createRepository(
                CheckstyleConstants.REPOSITORY_KEY, "java").setName(
                CheckstyleConstants.REPOSITORY_NAME);

        final CheckstyleMetadata checkstyleMetadata = new CheckstyleMetadata(repository);
        checkstyleMetadata.createRulesWithMetadata();

        repository.done();
    }
}
