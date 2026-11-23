/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.impl;

import com.powsybl.openrao.data.crac.api.usagerule.UsageRule;

/**
 * Anything a usage rule adder can be attached to: {@link AbstractRemedialActionAdder},
 * {@link AndUsageRuleAdderImpl} or {@link OrUsageRuleAdderImpl}.
 *
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
interface UsageRuleGateAdderOwner {
    CracImpl getCrac();

    void addUsageRule(UsageRule usageRule);
}
