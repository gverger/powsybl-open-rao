/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.api.usagerule;

/**
 * Common contract for anything that leaf UsageRules (OnInstant, OnContingencyState, OnConstraint,
 * OnFlowConstraintInCountry, OnSwitchState) can be added to: a {@link com.powsybl.openrao.data.crac.api.RemedialActionAdder},
 * or a gate logic combination adder ({@link AndUsageRuleAdder}, {@link OrUsageRuleAdder}).
 *
 * Extracting this contract lets code that adds leaf UsageRules (e.g. a Json deserializer) be written
 * once and reused regardless of whether the UsageRule is being attached directly to a remedial action
 * or nested inside a gate logic combination.
 *
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public interface UsageRuleAdder<T> {
    OnInstantAdder<T> newOnInstantUsageRule();

    OnContingencyStateAdder<T> newOnContingencyStateUsageRule();

    OnConstraintAdder<T, ?> newOnConstraintUsageRule();

    OnFlowConstraintInCountryAdder<T> newOnFlowConstraintInCountryUsageRule();

    OnSwitchStateAdder<T> newOnSwitchStateUsageRule();
}
