/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.impl;

import com.powsybl.contingency.ContingencyElementType;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.openrao.commons.OpenRaoException;
import com.powsybl.openrao.commons.Unit;
import com.powsybl.openrao.data.crac.api.Crac;
import com.powsybl.openrao.data.crac.api.InstantKind;
import com.powsybl.openrao.data.crac.api.networkaction.ActionType;
import com.powsybl.openrao.data.crac.api.networkaction.NetworkAction;
import com.powsybl.openrao.data.crac.api.networkaction.NetworkActionAdder;
import com.powsybl.openrao.data.crac.api.usagerule.UsageRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
class AndUsageRuleAdderImplTest {
    private static final String PREVENTIVE_INSTANT_ID = "preventive";
    private static final String OUTAGE_INSTANT_ID = "outage";
    private static final String CURATIVE_INSTANT_ID = "curative";

    private Crac crac;

    @BeforeEach
    public void setUp() {
        crac = new CracImplFactory().create("cracId")
            .newInstant(PREVENTIVE_INSTANT_ID, InstantKind.PREVENTIVE)
            .newInstant(OUTAGE_INSTANT_ID, InstantKind.OUTAGE)
            .newInstant(CURATIVE_INSTANT_ID, InstantKind.CURATIVE);
        crac.newContingency()
            .withId("contingencyId")
            .withContingencyElement("coNetworkElementId", ContingencyElementType.LINE)
            .add();
        crac.newFlowCnec()
            .withId("preventiveCnec")
            .withNetworkElement("cnecNetworkElementId")
            .withInstant(PREVENTIVE_INSTANT_ID)
            .newThreshold().withUnit(Unit.MEGAWATT).withSide(TwoSides.ONE).withMax(100.).add()
            .add();
    }

    private NetworkActionAdder newNetworkActionAdder() {
        return crac.newNetworkAction()
            .withId("networkActionId")
            .withOperator("operator")
            .newSwitchAction().withNetworkElement("switchId").withActionType(ActionType.OPEN).add();
    }

    @Test
    void testAndMixingInstantsIsRejected() {
        var andAdder = newNetworkActionAdder()
            .newAndUsageRule()
                .newOnConstraintUsageRule().withInstant(PREVENTIVE_INSTANT_ID).withCnec("preventiveCnec").add()
                .newOnSwitchStateUsageRule().withInstant(CURATIVE_INSTANT_ID).withNetworkElement("switchId").withExpectedOpen(true).add();
        OpenRaoException exception = assertThrows(OpenRaoException.class, andAdder::add);
        assertEquals("Cannot add AndUsageRule: its usage rules are defined at different instants, so it can never be activated. Use an OrUsageRule instead.", exception.getMessage());
    }

    @Test
    void testAndOfSameInstantIsAccepted() {
        NetworkAction networkAction = newNetworkActionAdder()
            .newAndUsageRule()
                .newOnContingencyStateUsageRule().withInstant(CURATIVE_INSTANT_ID).withContingency("contingencyId").add()
                .newOnSwitchStateUsageRule().withInstant(CURATIVE_INSTANT_ID).withNetworkElement("switchId").withExpectedOpen(true).add()
                .add()
            .add();
        assertEquals(Set.of(crac.getInstant(CURATIVE_INSTANT_ID)), networkAction.getUsageRules().iterator().next().getInstants());
    }

    @Test
    void testAndWithNestedOrIsDefinedAtIntersection() {
        NetworkAction networkAction = newNetworkActionAdder()
            .newAndUsageRule()
                .newOnContingencyStateUsageRule().withInstant(CURATIVE_INSTANT_ID).withContingency("contingencyId").add()
                .newOrUsageRule()
                    .newOnInstantUsageRule().withInstant(PREVENTIVE_INSTANT_ID).add()
                    .newOnInstantUsageRule().withInstant(CURATIVE_INSTANT_ID).add()
                    .add()
                .add()
            .add();
        UsageRule andUsageRule = networkAction.getUsageRules().iterator().next();
        assertEquals(Set.of(crac.getInstant(CURATIVE_INSTANT_ID)), andUsageRule.getInstants());
    }
}
