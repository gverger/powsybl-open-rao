/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.impl;

import com.powsybl.openrao.data.crac.api.usagerule.UsageRule;

/**
 * Minimal contract shared by everything a leaf usage-rule adder (OnInstantAdderImpl,
 * OnContingencyStateAdderImpl, OnConstraintAdderImpl, OnFlowConstraintInCountryAdderImpl,
 * OnSwitchStateAdderImpl) can be attached to: either a RemedialActionAdder ({@link
 * AbstractRemedialActionAdder}), or a gate logic combination adder ({@link AndUsageRuleAdderImpl},
 * {@link OrUsageRuleAdderImpl}). It lets the same leaf adder implementations be reused whether they
 * are attached directly to a remedial action or nested inside a gate logic combination.
 *
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
interface UsageRuleGateAdderOwner {
    CracImpl getCrac();

    void addUsageRule(UsageRule usageRule);
}
