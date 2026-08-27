/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.api.usagerule;

/**
 * Common contract for anything an AndUsageRule can be added to: a {@link com.powsybl.openrao.data.crac.api.RemedialActionAdder},
 * or an {@link OrUsageRuleAdder} (to nest an AndUsageRule inside an OrUsageRule).
 *
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public interface AndUsageRuleAdderOwner<T> {
    /**
     * Starts building an AndUsageRule: a gate logic UsageRule defined as the conjunction of the
     * UsageRules that will be added to it, which is then attached to this owner.
     */
    AndUsageRuleAdder<T> newAndUsageRule();
}
