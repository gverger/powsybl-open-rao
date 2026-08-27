/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.api.usagerule;

import com.powsybl.openrao.data.crac.api.State;

import java.util.Set;

/**
 * The OrUsageRule is a gate logic UsageRule, defined as the disjunction of a set of other UsageRules
 * (which can themselves be leaf UsageRules such as {@link OnInstant}, {@link OnContingencyState},
 * {@link OnConstraint} or {@link OnSwitchState}, or nested {@link AndUsageRule}). It is defined for a
 * given State if, and only if, at least one of its UsageRules is defined for this State.
 *
 * All the UsageRules composing an OrUsageRule must share the same Instant, which is then the Instant
 * of the OrUsageRule itself.
 *
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public interface OrUsageRule extends UsageRule {
    /**
     * Get the UsageRules combined by this OrUsageRule
     */
    Set<UsageRule> getUsageRules();

    default boolean isDefinedForState(State state) {
        return getUsageRules().stream().anyMatch(usageRule -> usageRule.isDefinedForState(state));
    }
}
