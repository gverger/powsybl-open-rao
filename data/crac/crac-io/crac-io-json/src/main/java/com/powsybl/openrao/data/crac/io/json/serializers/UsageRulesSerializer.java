/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.io.json.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.openrao.data.crac.api.RemedialAction;
import com.powsybl.openrao.data.crac.api.usagerule.AndUsageRule;
import com.powsybl.openrao.data.crac.api.usagerule.OnConstraint;
import com.powsybl.openrao.data.crac.api.usagerule.OnContingencyState;
import com.powsybl.openrao.data.crac.api.usagerule.OnFlowConstraintInCountry;
import com.powsybl.openrao.data.crac.api.usagerule.OnInstant;
import com.powsybl.openrao.data.crac.api.usagerule.OnSwitchState;
import com.powsybl.openrao.data.crac.api.usagerule.OrUsageRule;
import com.powsybl.openrao.data.crac.api.usagerule.UsageRule;
import com.powsybl.openrao.data.crac.io.json.JsonSerializationConstants;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author Peter Mitri {@literal <peter.mitri at rte-france.com>}
 */
public final class UsageRulesSerializer {
    private UsageRulesSerializer() {
    }

    public static void serializeUsageRules(RemedialAction<?> remedialAction, JsonGenerator gen) throws IOException {
        serializeUsageRules(remedialAction.getUsageRules(), gen);
    }

    /**
     * Serializes a set of UsageRules as sibling fields of the currently open Json object: one array per
     * UsageRule type. Used both for a RemedialAction's own top-level UsageRules, and, recursively, for the
     * UsageRules combined by an AndUsageRule/OrUsageRule gate logic combination.
     */
    public static void serializeUsageRules(Set<UsageRule> usageRules, JsonGenerator gen) throws IOException {
        serializeUsageRulesOfType(usageRules, OnInstant.class, JsonSerializationConstants.ON_INSTANT_USAGE_RULES, gen);
        serializeUsageRulesOfType(usageRules, OnContingencyState.class, JsonSerializationConstants.ON_CONTINGENCY_STATE_USAGE_RULES, gen);
        serializeUsageRulesOfType(usageRules, OnConstraint.class, JsonSerializationConstants.ON_CONSTRAINT_USAGE_RULES, gen);
        serializeUsageRulesOfType(usageRules, OnFlowConstraintInCountry.class, JsonSerializationConstants.ON_FLOW_CONSTRAINT_IN_COUNTRY_USAGE_RULES, gen);
        serializeUsageRulesOfType(usageRules, OnSwitchState.class, JsonSerializationConstants.ON_SWITCH_STATE_USAGE_RULES, gen);
        serializeUsageRulesOfType(usageRules, AndUsageRule.class, JsonSerializationConstants.AND_USAGE_RULES, gen);
        serializeUsageRulesOfType(usageRules, OrUsageRule.class, JsonSerializationConstants.OR_USAGE_RULES, gen);
    }

    private static void serializeUsageRulesOfType(Set<UsageRule> usageRules, Class<? extends UsageRule> usageRuleType, String arrayName, JsonGenerator gen) throws IOException {
        List<UsageRule> filteredUsageRules = usageRules.stream()
                .filter(usageRule -> usageRuleType.isAssignableFrom(usageRule.getClass()))
                .sorted(new JsonSerializationConstants.UsageRuleComparator())
                .toList();
        if (!filteredUsageRules.isEmpty()) {
            gen.writeArrayFieldStart(arrayName);
            for (UsageRule ea : filteredUsageRules) {
                gen.writeObject(ea);
            }
            gen.writeEndArray();
        }
    }
}
