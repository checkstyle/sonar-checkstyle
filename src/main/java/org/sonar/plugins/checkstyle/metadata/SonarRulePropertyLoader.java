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

package org.sonar.plugins.checkstyle.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SonarRulePropertyLoader {
    private List<AdditionalRuleProperties> rules;

    /**
     * Retrieves the list of rules.
     *
     * @return The list of rules.
     */
    public List<AdditionalRuleProperties> getRules() {
        return Collections.unmodifiableList(rules);
    }

    /**
     * Populates the list of rules.
     *
     * @param rules The list of rules.
     */
    public void setRules(List<AdditionalRuleProperties> rules) {
        this.rules = new ArrayList<>(rules);
    }

    public static class AdditionalRuleProperties {
        private String rule;
        private String tag;

        /**
         * Retrieves the rule.
         *
         * @return The rule.
         */
        public String getRule() {
            return rule;
        }

        /**
         * Populates the rule.
         *
         * @param rule The rule.
         */
        public void setRule(String rule) {
            this.rule = rule;
        }

        /**
         * Retrieves the tag.
         *
         * @return The tag.
         */
        public String getTag() {
            return tag;
        }

        /**
         * Populates the tag.
         *
         * @param tag The tag.
         */
        public void setTag(String tag) {
            this.tag = tag;
        }
    }
}
