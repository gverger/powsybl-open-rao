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
 * The AndUsageRule is a gate logic UsageRule, defined as the conjunction of a set of other UsageRules
 * (which can themselves be leaf UsageRules such as {@link OnInstant}, {@link OnContingencyState},
 * {@link OnConstraint} or {@link OnSwitchState}, or nested {@link OrUsageRule}). It is defined for a
 * given State if, and only if, all of its UsageRules are defined for this State.
 *
 * The UsageRules composing an AndUsageRule are not required to share the same Instant: e.g.
 * OnConstraint(preventive, cnec1) AND OnSwitchState(curative, switchA) is a valid AndUsageRule. This is
 * why AndUsageRule is not a {@link SingleInstantUsageRule}: it has no single Instant of its own.
 *
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public interface AndUsageRule extends UsageRule {
    /**
     * Get the UsageRules combined by this AndUsageRule
     */
    Set<UsageRule> getUsageRules();

    default boolean isDefinedForState(State state) {
        return getUsageRules().stream().allMatch(usageRule -> usageRule.isDefinedForState(state));
    }
}
