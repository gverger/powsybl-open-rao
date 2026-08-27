/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.api.usagerule;

/**
 * Adds an OnSwitchState usage rule to its owner (a RemedialActionAdder, or a gate logic
 * combination adder such as {@link AndUsageRuleAdder} or {@link OrUsageRuleAdder}).
 *
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public interface OnSwitchStateAdder<T> {
    OnSwitchStateAdder<T> withInstant(String instantId);

    OnSwitchStateAdder<T> withNetworkElement(String networkElementId);

    OnSwitchStateAdder<T> withExpectedOpen(boolean expectedOpen);

    T add();
}
