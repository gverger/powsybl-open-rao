/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.api.usagerule;

/**
 * Adds an OrUsageRule to its owner (a RemedialActionAdder, or another gate logic combination adder).
 * The UsageRules to be combined are added one by one, using the same newXxxUsageRule() factory methods
 * as a RemedialActionAdder; OrUsageRuleAdder and {@link AndUsageRuleAdder} can be nested inside one
 * another to build arbitrary boolean expressions.
 *
 * All the UsageRules added to this adder must share the same Instant.
 *
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public interface OrUsageRuleAdder<T> extends UsageRuleAdder<OrUsageRuleAdder<T>>, AndUsageRuleAdderOwner<OrUsageRuleAdder<T>>, OnSwitchStateAdderOwner<OrUsageRuleAdder<T>> {
    T add();
}
