/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.api.usagerule;

import com.powsybl.iidm.network.TwoSides;
import com.powsybl.openrao.commons.Unit;
import com.powsybl.openrao.data.crac.api.State;
import com.powsybl.openrao.data.crac.api.cnec.FlowCnec;

import java.util.Optional;

/**
 * The OnFlowThreshold UsageRule is defined at a given Instant, and is triggered depending on the flow of
 * an existing FlowCnec, compared against threshold(s) defined by the usage rule itself, rather than
 * against the FlowCnec's own defined thresholds:
 * <ul>
 *     <li>if only atLeast is defined, the usage rule is triggered when the flow is greater than or equal
 *     to atLeast (a "flow &gt;= atLeast" trigger);</li>
 *     <li>if only atMost is defined, the usage rule is triggered when the flow is lower than or equal to
 *     atMost (a "flow &lt;= atMost" trigger);</li>
 *     <li>if both are defined, the usage rule is triggered only when the flow satisfies both conditions
 *     at once, i.e. when it lies within [atLeast, atMost].</li>
 * </ul>
 * At least one of atLeast/atMost must be defined. To express an "either bound" (OR) trigger instead of
 * this "both bounds" (AND) one, use two separate OnFlowThreshold UsageRules combined through an
 * {@link OrUsageRule}.
 *
 * It is meant to be used as a leaf condition inside a gate logic combination ({@link AndUsageRule},
 * {@link OrUsageRule}), together with other UsageRules.
 *
 * Note that {@link #isDefinedForState(State)} only checks the Instant (and Contingency, if any) of the
 * usage rule: the actual flow on the FlowCnec can only be evaluated at RAO runtime, when a FlowResult
 * is available.
 *
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public interface OnFlowThreshold extends SingleInstantUsageRule {
    /**
     * Get the FlowCnec whose flow should be compared to this usage rule's threshold(s)
     */
    FlowCnec getCnec();

    /**
     * Get the side of the FlowCnec on which the flow should be evaluated
     */
    TwoSides getSide();

    /**
     * Get the Unit in which atLeast and atMost are defined
     */
    Unit getUnit();

    /**
     * Returns the value the flow must be greater than or equal to for this usage rule to be triggered.
     * Can be empty if this usage rule has no such lower trigger value.
     */
    Optional<Double> atLeast();

    /**
     * Returns the value the flow must be lower than or equal to for this usage rule to be triggered.
     * Can be empty if this usage rule has no such upper trigger value.
     */
    Optional<Double> atMost();

    default boolean isDefinedForState(State state) {
        if (!state.getInstant().equals(getInstant())) {
            return false;
        }
        return getInstant().isPreventive() || getCnec().getState().getContingency().equals(state.getContingency());
    }
}
