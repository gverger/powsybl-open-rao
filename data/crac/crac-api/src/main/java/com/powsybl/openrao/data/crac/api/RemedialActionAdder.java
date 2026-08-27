/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.api;

import com.powsybl.openrao.data.crac.api.usagerule.AndUsageRuleAdder;
import com.powsybl.openrao.data.crac.api.usagerule.OnConstraintAdder;
import com.powsybl.openrao.data.crac.api.usagerule.OnContingencyStateAdder;
import com.powsybl.openrao.data.crac.api.usagerule.OnFlowConstraintInCountryAdder;
import com.powsybl.openrao.data.crac.api.usagerule.OnInstantAdder;
import com.powsybl.openrao.data.crac.api.usagerule.OnSwitchStateAdder;
import com.powsybl.openrao.data.crac.api.usagerule.OrUsageRuleAdder;

/**
 * @author Peter Mitri {@literal <peter.mitri at rte-france.com>}
 * @author Baptiste Seguinot {@literal <baptiste.seguinot at rte-france.com>}
 */
public interface RemedialActionAdder<T extends RemedialActionAdder<T>> extends IdentifiableAdder<T> {

    T withOperator(String operator);

    T withSpeed(Integer speed);

    T withActivationCost(Double activationCost);

    RemedialAction<?> add();

    OnInstantAdder<T> newOnInstantUsageRule();

    OnContingencyStateAdder<T> newOnContingencyStateUsageRule();

    OnConstraintAdder<T, ?> newOnConstraintUsageRule();

    OnFlowConstraintInCountryAdder<T> newOnFlowConstraintInCountryUsageRule();

    OnSwitchStateAdder<T> newOnSwitchStateUsageRule();

    /**
     * Starts building an AndUsageRule: a gate logic UsageRule defined as the conjunction of the
     * UsageRules that will be added to it, which is then attached to this remedial action.
     */
    AndUsageRuleAdder<T> newAndUsageRule();

    /**
     * Starts building an OrUsageRule: a gate logic UsageRule defined as the disjunction of the
     * UsageRules that will be added to it, which is then attached to this remedial action.
     */
    OrUsageRuleAdder<T> newOrUsageRule();
}
