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

package org.sonar.plugins.checkstyle.metadata;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.ModuleDetails;
import org.example.ModulePropertyDetails;
import org.example.ModuleType;
import org.example.XMLMetaReader;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.checkstyle.CheckstyleConstants;
import org.sonar.plugins.checkstyle.CheckstyleRulesDefinition;

public class CheckstyleMetadataTest {
    private static RulesDefinition.Repository repository;
    private static List<String> checkSet;
    private static Map<String, ModuleDetails> metadataRepo;

    @BeforeClass
    public static void getPreparedRepository() {
        final CheckstyleRulesDefinition definition = new CheckstyleRulesDefinition("rules.xml",
                CheckstyleConstants.REPOSITORY_KEY);
        final RulesDefinition.Context context = new RulesDefinition.Context();
        definition.define(context);
        repository = context.repository(CheckstyleConstants.REPOSITORY_KEY);
        checkSet = Arrays.asList("com.puppycrawl.tools.checkstyle.checks.blocks.EmptyBlockCheck",
                "com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocPackageCheck",
                "com.puppycrawl.tools.checkstyle.checks.javadoc.NonEmptyAtclauseDescriptionCheck",
                "com.puppycrawl.tools.checkstyle.checks.coding.NestedForDepthCheck",
                "com.puppycrawl.tools.checkstyle.checks.javadoc.NonEmptyAtclauseDescriptionCheck",
                "com.puppycrawl.tools.checkstyle.checks.indentation.IndentationCheck");
        metadataRepo = new HashMap<>();
        new XMLMetaReader().readAllModulesIncludingThirdPartyIfAny()
                .forEach(moduleDetails -> {
                    metadataRepo.put(moduleDetails.getFullQualifiedName(),
                            moduleDetails);
                });
    }

    @Test
    public void testUpdate() {
        checkSet.forEach(fullyQualifiedCheckName -> {
            final RulesDefinition.Rule sampleCheckRule = repository.rule(fullyQualifiedCheckName);

            final ModuleDetails moduleDetails = metadataRepo.get(fullyQualifiedCheckName);
            assertEquals("HTML Descriptions don't match", moduleDetails.getDescription(),
                    sampleCheckRule.htmlDescription());
            assertEquals("Name doesn't match", convertName(moduleDetails.getName() + "Check"),
                    sampleCheckRule.name());
            assertEquals("InternalKey doesn't match", convertInternalKey(moduleDetails),
                    sampleCheckRule.internalKey());
            sampleCheckRule.params().forEach(param -> {
                if (!"tabWidth".equals(param.key())) {
                    final String key = param.key();
                    final ModulePropertyDetails modulePropertyDetails =
                            moduleDetails.getModulePropertyByKey(key);
                    assertEquals("Description doesn't match for param: " + key,
                            modulePropertyDetails.getDescription(),
                            param.description());
                    assertEquals("Default value doesn't match for param: " + key,
                            modulePropertyDetails.getDefaultValue(),
                            param.defaultValue());
                }
            });
        });
    }

    @Test
    public void testCreate() {
        final CheckstyleRulesDefinition definition = new CheckstyleRulesDefinition(
                "rule-deletedChecks.xml", "test");
        final RulesDefinition.Context context = new RulesDefinition.Context();
        definition.define(context);
        final RulesDefinition.Repository modRepo = context.repository("test");
        checkSet.forEach(fullyQualifiedCheckName -> {
            final RulesDefinition.Rule origRule = repository.rule(fullyQualifiedCheckName);
            final RulesDefinition.Rule modRule = modRepo.rule(fullyQualifiedCheckName);
            assertEquals("Rule name doesn't match", origRule.name(), modRule.name());
            assertEquals("Rule description doesn't match", origRule.htmlDescription(),
                    modRule.htmlDescription());
            assertEquals("Rule internalKey doesn't match", origRule.internalKey(),
                    modRule.internalKey());
            assertEquals("Rule status doesn't match", origRule.status(), modRule.status());
            origRule.params().forEach(origParam -> {
                if (!"tabWidth".equals(origParam.key())) {
                    final String paramKey = origParam.key();
                    final RulesDefinition.Param modParam = modRule.param(paramKey);
                    assertEquals("Description doesn't match for param: " + paramKey,
                            origParam.description(), modParam.description());
                    assertEquals("Default value doesn't match for param: " + paramKey,
                            origParam.defaultValue(), modParam.defaultValue());
                    assertEquals("Type doesn't match for param: " + paramKey, origParam.type(),
                            modParam.type());
                }
            });
        });
    }

    private static String convertName(String checkName) {
        final int capacity = 1024;
        final StringBuilder result = new StringBuilder(capacity);
        for (int i = 0; i < checkName.length(); i++) {
            result.append(checkName.charAt(i));
            if (i + 1 < checkName.length() && Character.isUpperCase(checkName.charAt(i + 1))) {
                result.append(' ');
            }
        }
        return result.toString();
    }

    private static String convertInternalKey(ModuleDetails moduleDetails) {
        String result = "Checker/";
        if (moduleDetails.getParent().contains("Checker")) {
            result += moduleDetails.getName();
        }
        else {
            result += "TreeWalker/" + moduleDetails.getName();
        }
        if (moduleDetails.getModuleType() == ModuleType.CHECK) {
            result += "Check";
        }
        return result;
    }
}
