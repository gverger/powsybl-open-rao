/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.api.usagerule;

/**
 * Common contract for anything an OnFlowThreshold UsageRule can be added to: an {@link AndUsageRuleAdder}
 * or an {@link OrUsageRuleAdder}. On purpose, this is not extended by {@link com.powsybl.openrao.data.crac.api.RemedialActionAdder}:
 * a RemedialAction cannot have a standalone OnFlowThreshold UsageRule, it can only combine one inside a
 * gate logic combination.
 *
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public interface OnFlowThresholdAdderOwner<T> {
    OnFlowThresholdAdder<T> newOnFlowThresholdUsageRule();
}
