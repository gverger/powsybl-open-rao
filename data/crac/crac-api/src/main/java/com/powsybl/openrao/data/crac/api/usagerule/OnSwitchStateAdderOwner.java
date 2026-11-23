/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.api.usagerule;

/**
 * Anything an OnSwitchState UsageRule can be added to: an {@link AndUsageRuleAdder} or an
 * {@link OrUsageRuleAdder}. Not extended by {@link com.powsybl.openrao.data.crac.api.RemedialActionAdder}:
 * an OnSwitchState cannot be a top-level usage rule.
 *
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public interface OnSwitchStateAdderOwner<T> {
    OnSwitchStateAdder<T> newOnSwitchStateUsageRule();
}
