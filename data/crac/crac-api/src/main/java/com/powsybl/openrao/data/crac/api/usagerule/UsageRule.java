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
 * It is either a leaf ({@link SingleInstantUsageRule}) or a combination of UsageRules
 * ({@link AndUsageRule}, {@link OrUsageRule}).
 *
 * @author Viktor Terrier {@literal <viktor.terrier at rte-france.com>}
 */
public interface UsageRule {
    boolean isDefinedForState(State state);

    /**
     * Get the Instants at which the usage rule can be defined. Unlike {@link #flatten(UsageRule)}, the
     * Instant of a leaf that can never be activated (e.g. a preventive leaf ANDed with a curative one) is
     * not returned.
     */
    Set<Instant> getInstants();

    /**
     * Get all the leaf UsageRules, recursively.
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
