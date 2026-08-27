/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.impl;

import com.powsybl.openrao.data.crac.api.Instant;
import com.powsybl.openrao.data.crac.api.NetworkElement;
import com.powsybl.openrao.data.crac.api.usagerule.OnSwitchState;

/**
 * The OnSwitchStateImpl UsageRule is effective at a given Instant, depending on the open/closed
 * state of a switch of the network.
 *
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public final class OnSwitchStateImpl implements OnSwitchState {

    private final Instant instant;
    private final NetworkElement switchElement;
    private final boolean expectedOpen;

    OnSwitchStateImpl(Instant instant, NetworkElement switchElement, boolean expectedOpen) {
        this.instant = instant;
        this.switchElement = switchElement;
        this.expectedOpen = expectedOpen;
    }

    @Override
    public Instant getInstant() {
        return instant;
    }

    @Override
    public NetworkElement getSwitchElement() {
        return switchElement;
    }

    @Override
    public boolean isExpectedOpen() {
        return expectedOpen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OnSwitchStateImpl rule = (OnSwitchStateImpl) o;
        return rule.getInstant().equals(instant)
            && rule.getSwitchElement().equals(switchElement)
            && rule.isExpectedOpen() == expectedOpen;
    }

    @Override
    public int hashCode() {
        return instant.hashCode() * 47 + switchElement.hashCode() * 19 + Boolean.hashCode(expectedOpen);
    }
}
