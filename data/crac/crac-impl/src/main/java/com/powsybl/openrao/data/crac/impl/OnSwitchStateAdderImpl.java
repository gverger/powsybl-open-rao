/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.impl;

import com.powsybl.openrao.commons.OpenRaoException;
import com.powsybl.openrao.data.crac.api.Instant;
import com.powsybl.openrao.data.crac.api.NetworkElement;
import com.powsybl.openrao.data.crac.api.usagerule.OnSwitchStateAdder;

import static com.powsybl.openrao.data.crac.impl.AdderUtils.assertAttributeNotNull;

/**
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public class OnSwitchStateAdderImpl<T extends UsageRuleGateAdderOwner> implements OnSwitchStateAdder<T> {
    private static final String CLASS_NAME = "OnSwitchState";
    private final T owner;
    private String instantId;
    private String networkElementId;
    private Boolean expectedOpen;

    OnSwitchStateAdderImpl(UsageRuleGateAdderOwner owner) {
        this.owner = (T) owner;
    }

    @Override
    public OnSwitchStateAdder<T> withInstant(String instantId) {
        this.instantId = instantId;
        return this;
    }

    @Override
    public OnSwitchStateAdder<T> withNetworkElement(String networkElementId) {
        this.networkElementId = networkElementId;
        return this;
    }

    @Override
    public OnSwitchStateAdder<T> withExpectedOpen(boolean expectedOpen) {
        this.expectedOpen = expectedOpen;
        return this;
    }

    @Override
    public T add() {
        assertAttributeNotNull(instantId, CLASS_NAME, "instant", "withInstant()");
        assertAttributeNotNull(networkElementId, CLASS_NAME, "network element", "withNetworkElement()");
        assertAttributeNotNull(expectedOpen, CLASS_NAME, "expected open state", "withExpectedOpen()");

        Instant instant = owner.getCrac().getInstant(instantId);
        if (instant.isOutage()) {
            throw new OpenRaoException("OnSwitchState usage rules are not allowed for OUTAGE instant.");
        }
        if (instant.isPreventive()) {
            owner.getCrac().addPreventiveState();
        }

        NetworkElement switchElement = owner.getCrac().addNetworkElement(networkElementId, null);

        owner.addUsageRule(new OnSwitchStateImpl(instant, switchElement, expectedOpen));
        return owner;
    }
}
