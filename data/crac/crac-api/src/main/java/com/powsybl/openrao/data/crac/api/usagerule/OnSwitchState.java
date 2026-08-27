/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.api.usagerule;

import com.powsybl.openrao.data.crac.api.Instant;
import com.powsybl.openrao.data.crac.api.NetworkElement;
import com.powsybl.openrao.data.crac.api.State;

/**
 * The OnSwitchState UsageRule is defined at a given Instant, and is triggered depending on the open/closed
 * state of a switch of the network. It is meant to be used as a leaf condition inside a gate logic
 * combination ({@link AndUsageRule}, {@link OrUsageRule}), together with other UsageRules, but it can also
 * be used on its own.
 *
 * Note that {@link #isDefinedForState(State)} only checks the Instant of the usage rule: the actual state
 * of the switch in the network can only be evaluated at RAO runtime, when a Network is available.
 *
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public interface OnSwitchState extends UsageRule {
    /**
     * Get the Instant at which the usage rule is triggered
     */
    Instant getInstant();

    /**
     * Get the NetworkElement of the switch whose state is monitored
     */
    NetworkElement getSwitchElement();

    /**
     * Get the switch state expected to trigger the usage rule: true if the switch is expected to be open,
     * false if it is expected to be closed
     */
    boolean isExpectedOpen();

    default boolean isDefinedForState(State state) {
        return state.getInstant().equals(getInstant());
    }
}
