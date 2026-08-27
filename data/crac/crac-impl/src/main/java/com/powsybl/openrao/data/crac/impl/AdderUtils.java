/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.impl;

import com.powsybl.openrao.commons.OpenRaoException;
import com.powsybl.openrao.data.crac.api.Instant;
import com.powsybl.openrao.data.crac.api.usagerule.UsageRule;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Baptiste Seguinot {@literal <baptiste.seguinot at rte-france.com>}
 */
public final class AdderUtils {

    private AdderUtils() {
    }

    static void assertAttributeNotNull(Object attribute, String className, String attributeDescription, String methodName) {
        if (Objects.isNull(attribute)) {
            throw new OpenRaoException(String.format("Cannot add %s without a %s. Please use %s with a non null value", className, attributeDescription, methodName));
            // example: "Cannot add a PstRangeAction without a maximum value. Please use setMaxValue()."
        }
    }

    static void assertAttributeNotEmpty(Collection<?> attribute, String className, String attributeDescription, String methodName) {
        if (attribute.isEmpty()) {
            throw new OpenRaoException(String.format("Cannot add %s without a %s. Please use %s", className, attributeDescription, methodName));
            // example: "Cannot add a InjectionShiftRangeAction without an injectionShiftKey. Please use withNetworkElementAndKey()."
        }
    }

    /**
     * Checks that a gate logic combination (AndUsageRule/OrUsageRule) has at least one child UsageRule,
     * and that all its children share the same Instant, then returns that shared Instant.
     */
    static Instant checkAndGetSharedInstant(Set<UsageRule> usageRules, String className) {
        assertAttributeNotEmpty(usageRules, className, "usage rule", "newOnInstantUsageRule(), newOnContingencyStateUsageRule(), newOnConstraintUsageRule(), newOnFlowConstraintInCountryUsageRule(), newOnSwitchStateUsageRule(), newAndUsageRule() or newOrUsageRule()");
        Set<Instant> instants = usageRules.stream().map(UsageRule::getInstant).collect(Collectors.toSet());
        if (instants.size() > 1) {
            throw new OpenRaoException(String.format(
                "Cannot add %s: all the combined usage rules must share the same instant, but found: %s.",
                className,
                instants.stream().map(Instant::getId).sorted().collect(Collectors.joining(", "))));
        }
        return instants.iterator().next();
    }
}
