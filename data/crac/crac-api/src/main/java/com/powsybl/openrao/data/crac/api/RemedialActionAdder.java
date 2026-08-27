/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.api;

import com.powsybl.openrao.data.crac.api.usagerule.AndUsageRuleAdderOwner;
import com.powsybl.openrao.data.crac.api.usagerule.OrUsageRuleAdderOwner;
import com.powsybl.openrao.data.crac.api.usagerule.UsageRuleAdder;

/**
 * @author Peter Mitri {@literal <peter.mitri at rte-france.com>}
 * @author Baptiste Seguinot {@literal <baptiste.seguinot at rte-france.com>}
 */
public interface RemedialActionAdder<T extends RemedialActionAdder<T>> extends IdentifiableAdder<T>, UsageRuleAdder<T>, AndUsageRuleAdderOwner<T>, OrUsageRuleAdderOwner<T> {

    T withOperator(String operator);

    T withSpeed(Integer speed);

    T withActivationCost(Double activationCost);

    RemedialAction<?> add();
}
