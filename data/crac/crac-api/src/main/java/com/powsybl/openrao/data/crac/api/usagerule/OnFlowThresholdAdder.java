/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.api.usagerule;

import com.powsybl.iidm.network.TwoSides;
import com.powsybl.openrao.commons.Unit;

/**
 * Adds an OnFlowThreshold usage rule to its owner: an {@link AndUsageRuleAdder} or an {@link OrUsageRuleAdder}.
 * At least one of withAtLeast()/withAtMost() must be used.
 *
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public interface OnFlowThresholdAdder<T> {
    OnFlowThresholdAdder<T> withInstant(String instantId);

    OnFlowThresholdAdder<T> withCnec(String cnecId);

    OnFlowThresholdAdder<T> withSide(TwoSides side);

    OnFlowThresholdAdder<T> withUnit(Unit unit);

    OnFlowThresholdAdder<T> withAtLeast(Double atLeast);

    OnFlowThresholdAdder<T> withAtMost(Double atMost);

    T add();
}
