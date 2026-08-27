/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.api.usagerule;

/**
 * Common contract for anything an OrUsageRule can be added to: a {@link com.powsybl.openrao.data.crac.api.RemedialActionAdder},
 * or an {@link AndUsageRuleAdder} (to nest an OrUsageRule inside an AndUsageRule).
 *
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public interface OrUsageRuleAdderOwner<T> {
    /**
     * Starts building an OrUsageRule: a gate logic UsageRule defined as the disjunction of the
     * UsageRules that will be added to it, which is then attached to this owner.
     */
    OrUsageRuleAdder<T> newOrUsageRule();
}
