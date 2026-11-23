/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.api.usagerule;

/**
 * Anything leaf UsageRules can be added to: a {@link com.powsybl.openrao.data.crac.api.RemedialActionAdder},
 * an {@link AndUsageRuleAdder} or an {@link OrUsageRuleAdder}. OnSwitchState is not part of it: see
 * {@link OnSwitchStateAdderOwner}.
 *
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public interface UsageRuleAdder<T> {
    OnInstantAdder<T> newOnInstantUsageRule();

    OnContingencyStateAdder<T> newOnContingencyStateUsageRule();

    OnConstraintAdder<T, ?> newOnConstraintUsageRule();

    OnFlowConstraintInCountryAdder<T> newOnFlowConstraintInCountryUsageRule();
}
