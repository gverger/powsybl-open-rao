/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.impl;

import com.powsybl.iidm.network.TwoSides;
import com.powsybl.openrao.commons.Unit;
import com.powsybl.openrao.data.crac.api.Instant;
import com.powsybl.openrao.data.crac.api.cnec.FlowCnec;
import com.powsybl.openrao.data.crac.api.usagerule.OnFlowThreshold;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public final class OnFlowThresholdImpl implements OnFlowThreshold {

    private final Instant instant;
    private final FlowCnec cnec;
    private final TwoSides side;
    private final Unit unit;
    private final Double atLeast;
    private final Double atMost;

    OnFlowThresholdImpl(Instant instant, FlowCnec cnec, TwoSides side, Unit unit, Double atLeast, Double atMost) {
        this.instant = instant;
        this.cnec = cnec;
        this.side = side;
        this.unit = unit;
        this.atLeast = atLeast;
        this.atMost = atMost;
    }

    @Override
    public Instant getInstant() {
        return instant;
    }

    @Override
    public FlowCnec getCnec() {
        return cnec;
    }

    @Override
    public TwoSides getSide() {
        return side;
    }

    @Override
    public Unit getUnit() {
        return unit;
    }

    @Override
    public Optional<Double> atLeast() {
        return Optional.ofNullable(atLeast);
    }

    @Override
    public Optional<Double> atMost() {
        return Optional.ofNullable(atMost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OnFlowThresholdImpl rule = (OnFlowThresholdImpl) o;
        return rule.getInstant().equals(instant)
            && rule.getCnec().equals(cnec)
            && rule.getSide().equals(side)
            && rule.getUnit().equals(unit)
            && Objects.equals(rule.atLeast, atLeast)
            && Objects.equals(rule.atMost, atMost);
    }

    @Override
    public int hashCode() {
        return instant.hashCode() * 47 + cnec.hashCode() * 19 + side.hashCode() * 3
            + unit.hashCode() * 7 + Objects.hashCode(atLeast) * 11 + Objects.hashCode(atMost) * 13;
    }
}
