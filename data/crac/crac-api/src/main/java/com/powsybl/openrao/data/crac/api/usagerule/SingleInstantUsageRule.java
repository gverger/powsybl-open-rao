/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.api.usagerule;

import com.powsybl.openrao.data.crac.api.Instant;

import java.util.Set;

/**
 * A leaf UsageRule, defined at a single Instant (as opposed to {@link AndUsageRule} and {@link OrUsageRule}).
 *
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public interface SingleInstantUsageRule extends UsageRule {
    /**
     * Get the Instant of the usage rule
     */
    Instant getInstant();

    @Override
    default Set<Instant> getInstants() {
        return Set.of(getInstant());
    }
}
