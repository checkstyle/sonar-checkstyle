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

package org.sonar.plugins.checkstyle.rule;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.RuleParam;
import org.sonar.plugins.checkstyle.CheckstyleSeverityUtils;

/**
 * Wrapper as per {@link ActiveRuleWrapper} for the server side {@link ActiveRule}.
 */
public class ActiveRuleWrapperServerImpl implements ActiveRuleWrapper {
    private final ActiveRule activeRule;

    public ActiveRuleWrapperServerImpl(ActiveRule activeRule) {
        this.activeRule = activeRule;
    }

    @Override
    public String getInternalKey() {
        return activeRule.getConfigKey();
    }

    @Override
    public String getRuleKey() {
        return activeRule.getRuleKey();
    }

    @Override
    public String getTemplateRuleKey() {
        String result = null;
        if (Objects.nonNull(activeRule.getRule().getTemplate())) {
            result = activeRule.getRule().getTemplate().getKey();
        }
        return result;
    }

    @Override
    public String getSeverity() {
        return CheckstyleSeverityUtils.toSeverity(activeRule.getSeverity());
    }

    @Override
    public Map<String, String> getParams() {
        final Map<String, String> result = new HashMap<>();
        for (RuleParam param : activeRule.getRule().getParams()) {
            final String value = activeRule.getParameter(param.getKey());
            if (StringUtils.isNotBlank(value)) {
                result.put(param.getKey(), value);
            }
        }
        return result;
    }
}
