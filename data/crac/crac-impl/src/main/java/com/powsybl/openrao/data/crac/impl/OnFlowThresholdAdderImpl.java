/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.impl;

import com.powsybl.iidm.network.TwoSides;
import com.powsybl.openrao.commons.OpenRaoException;
import com.powsybl.openrao.commons.Unit;
import com.powsybl.openrao.data.crac.api.Instant;
import com.powsybl.openrao.data.crac.api.cnec.FlowCnec;
import com.powsybl.openrao.data.crac.api.usagerule.OnFlowThresholdAdder;

import java.util.Objects;

import static com.powsybl.openrao.data.crac.impl.AdderUtils.assertAttributeNotNull;

/**
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public class OnFlowThresholdAdderImpl<T extends UsageRuleGateAdderOwner> implements OnFlowThresholdAdder<T> {
    private static final String CLASS_NAME = "OnFlowThreshold";
    private final T owner;
    private String instantId;
    private String cnecId;
    private TwoSides side;
    private Unit unit;
    private Double atLeast;
    private Double atMost;

    OnFlowThresholdAdderImpl(UsageRuleGateAdderOwner owner) {
        this.owner = (T) owner;
    }

    @Override
    public OnFlowThresholdAdder<T> withInstant(String instantId) {
        this.instantId = instantId;
        return this;
    }

    @Override
    public OnFlowThresholdAdder<T> withCnec(String cnecId) {
        this.cnecId = cnecId;
        return this;
    }

    @Override
    public OnFlowThresholdAdder<T> withSide(TwoSides side) {
        this.side = side;
        return this;
    }

    @Override
    public OnFlowThresholdAdder<T> withUnit(Unit unit) {
        this.unit = unit;
        return this;
    }

    @Override
    public OnFlowThresholdAdder<T> withAtLeast(Double atLeast) {
        this.atLeast = atLeast;
        return this;
    }

    @Override
    public OnFlowThresholdAdder<T> withAtMost(Double atMost) {
        this.atMost = atMost;
        return this;
    }

    @Override
    public T add() {
        assertAttributeNotNull(instantId, CLASS_NAME, "instant", "withInstant()");
        assertAttributeNotNull(cnecId, CLASS_NAME, "cnec", "withCnec()");
        assertAttributeNotNull(side, CLASS_NAME, "side", "withSide()");
        assertAttributeNotNull(unit, CLASS_NAME, "unit", "withUnit()");
        if (atLeast == null && atMost == null) {
            throw new OpenRaoException("Cannot add " + CLASS_NAME + " without atLeast nor atMost values. Please use withAtLeast() or withAtMost().");
        }

        Instant instant = owner.getCrac().getInstant(instantId);
        if (instant.isOutage()) {
            throw new OpenRaoException("OnFlowThreshold usage rules are not allowed for OUTAGE instant.");
        }
        if (instant.isPreventive()) {
            owner.getCrac().addPreventiveState();
        }

        FlowCnec cnec = owner.getCrac().getFlowCnec(cnecId);
        if (Objects.isNull(cnec)) {
            throw new OpenRaoException(String.format("FlowCnec %s does not exist in crac. Consider adding it first.", cnecId));
        }

        AbstractRemedialActionAdder.checkOnConstraintUsageRules(instant, cnec);

        owner.addUsageRule(new OnFlowThresholdImpl(instant, cnec, side, unit, atLeast, atMost));
        return owner;
    }
}
