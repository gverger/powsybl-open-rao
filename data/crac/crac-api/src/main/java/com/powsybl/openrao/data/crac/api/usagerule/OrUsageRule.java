/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.api.usagerule;

import com.powsybl.openrao.data.crac.api.Instant;
import com.powsybl.openrao.data.crac.api.State;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Disjunction of UsageRules: defined for a State if at least one of its UsageRules is, hence at the union
 * of their Instants (see {@link #getInstants()}).
 *
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public interface OrUsageRule extends UsageRule {
    /**
     * Get the UsageRules combined by this OrUsageRule
     */
    Set<UsageRule> getUsageRules();

    @Override
    default Set<Instant> getInstants() {
        return getUsageRules().stream()
            .flatMap(usageRule -> usageRule.getInstants().stream())
            .collect(Collectors.toSet());
    }

    default boolean isDefinedForState(State state) {
        return getUsageRules().stream().anyMatch(usageRule -> usageRule.isDefinedForState(state));
    }
}
