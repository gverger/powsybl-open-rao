/*
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.api.usagerule;

import com.powsybl.openrao.data.crac.api.Instant;
import com.powsybl.openrao.data.crac.api.State;

import java.util.Set;
import java.util.stream.Stream;

/**
 * The UsageRule defines conditions under which a RemedialAction can be used.
 *
 * Most UsageRules ({@link OnInstant}, {@link OnContingencyState}, {@link OnConstraint},
 * {@link OnFlowConstraintInCountry}, {@link OnSwitchState}) are defined at a
 * single Instant: see {@link SingleInstantUsageRule}. {@link AndUsageRule} and {@link OrUsageRule} are
 * gate logic combinations of other UsageRules, which may themselves be defined at different Instants
 * (e.g. an OnConstraint at "preventive" OR an OnContingencyState at "curative"), so they have no single
 * Instant of their own: see {@link #getInstants()}.
 *
 * @author Viktor Terrier {@literal <viktor.terrier at rte-france.com>}
 */
public interface UsageRule {
    boolean isDefinedForState(State state);

    /**
     * Get the Instants at which the usage rule can be defined, following the gate logic:
     * - a {@link SingleInstantUsageRule} is defined at its own Instant only;
     * - an {@link OrUsageRule} at the union of the Instants of its UsageRules;
     * - an {@link AndUsageRule} at the intersection of the Instants of its UsageRules.
     * Contrary to {@link #flatten(UsageRule)}, which returns all the leaves, the Instants of a leaf that can
     * never take part in the gate logic (e.g. a preventive leaf ANDed with a curative one) are not returned:
     * use this method to classify a remedial action by Instant.
     */
    Set<Instant> getInstants();

    /**
     * Recursively unfolds AndUsageRule/OrUsageRule gate logic combinations into the leaf,
     * single-Instant UsageRules they combine.
     */
    static Stream<SingleInstantUsageRule> flatten(UsageRule usageRule) {
        if (usageRule instanceof AndUsageRule andUsageRule) {
            return andUsageRule.getUsageRules().stream().flatMap(UsageRule::flatten);
        } else if (usageRule instanceof OrUsageRule orUsageRule) {
            return orUsageRule.getUsageRules().stream().flatMap(UsageRule::flatten);
        } else if (usageRule instanceof SingleInstantUsageRule singleInstantUsageRule) {
            return Stream.of(singleInstantUsageRule);
        } else {
            throw new IllegalStateException("Unknown UsageRule implementation: " + usageRule.getClass().getName());
        }
    }
}
