/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.api.usagerule;

import com.powsybl.openrao.data.crac.api.Instant;
import com.powsybl.openrao.data.crac.api.State;

import java.util.HashSet;
import java.util.Set;

/**
 * The AndUsageRule is a gate logic UsageRule, defined as the conjunction of a set of other UsageRules
 * (which can themselves be leaf UsageRules such as {@link OnInstant}, {@link OnContingencyState},
 * {@link OnConstraint} or {@link OnSwitchState}, or nested {@link OrUsageRule}). It is defined for a
 * given State if, and only if, all of its UsageRules are defined for this State.
 *
 * As each leaf UsageRule is only defined at its own Instant, an AndUsageRule is defined at the
 * intersection of the Instants of its UsageRules (see {@link #getInstants()}), which must not be empty:
 * e.g. OnConstraint(preventive, cnec1) AND OnSwitchState(curative, switchA) is defined for no State and is
 * rejected. The UsageRules may still be defined at several Instants when they are nested OrUsageRules:
 * e.g. OnContingencyState(curative, co1) AND (OnConstraint(preventive, cnec1) OR OnConstraint(curative, cnec2))
 * is defined at the curative Instant only. This is why AndUsageRule is not a {@link SingleInstantUsageRule}.
 *
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public interface AndUsageRule extends UsageRule {
    /**
     * Get the UsageRules combined by this AndUsageRule
     */
    Set<UsageRule> getUsageRules();

    @Override
    default Set<Instant> getInstants() {
        Set<Instant> instants = null;
        for (UsageRule usageRule : getUsageRules()) {
            if (instants == null) {
                instants = new HashSet<>(usageRule.getInstants());
            } else {
                instants.retainAll(usageRule.getInstants());
            }
        }
        return instants == null ? Set.of() : instants;
    }

    default boolean isDefinedForState(State state) {
        return getUsageRules().stream().allMatch(usageRule -> usageRule.isDefinedForState(state));
    }
}
