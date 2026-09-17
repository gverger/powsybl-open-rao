/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.api.usagerule;

import com.powsybl.openrao.data.crac.api.Instant;

/**
 * A UsageRule that is defined at a single, well-identified Instant. Every leaf UsageRule (OnInstant,
 * OnContingencyState, OnConstraint, OnFlowConstraintInCountry, OnSwitchState, OnFlowThreshold) is a
 * SingleInstantUsageRule. {@link AndUsageRule} and {@link OrUsageRule} are not: they combine UsageRules
 * that may be defined at different Instants, so they have no single Instant of their own.
 *
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public interface SingleInstantUsageRule extends UsageRule {
    /**
     * Get the Instant of the usage rule
     */
    Instant getInstant();
}
