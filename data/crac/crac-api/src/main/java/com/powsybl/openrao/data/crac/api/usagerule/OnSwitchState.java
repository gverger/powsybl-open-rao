/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.api.usagerule;

import com.powsybl.openrao.data.crac.api.NetworkElement;
import com.powsybl.openrao.data.crac.api.State;

/**
 * UsageRule triggered by the open/closed state of a switch, only usable inside an {@link AndUsageRule} or
 * an {@link OrUsageRule}. {@link #isDefinedForState(State)} only checks the Instant: the switch state is
 * evaluated at RAO runtime, against the Network.
 *
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public interface OnSwitchState extends SingleInstantUsageRule {
    /**
     * Get the NetworkElement of the switch whose state is monitored
     */
    NetworkElement getSwitchElement();

    /**
     * True if the switch is expected to be open, false if it is expected to be closed
     */
    boolean isExpectedOpen();

    default boolean isDefinedForState(State state) {
        return state.getInstant().equals(getInstant());
    }
}
