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
 * Conjunction of UsageRules: defined for a State if all of its UsageRules are, hence at the intersection
 * of their Instants (see {@link #getInstants()}).
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
