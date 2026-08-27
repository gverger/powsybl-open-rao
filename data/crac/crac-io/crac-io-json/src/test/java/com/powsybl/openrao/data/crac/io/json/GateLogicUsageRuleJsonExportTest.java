/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.io.json;

import com.powsybl.contingency.ContingencyElementType;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.openrao.commons.Unit;
import com.powsybl.openrao.data.crac.api.Crac;
import com.powsybl.openrao.data.crac.api.CracFactory;
import com.powsybl.openrao.data.crac.api.InstantKind;
import com.powsybl.openrao.data.crac.api.networkaction.ActionType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Not a "real" test of a stable contract: it exists to show what the Json export of a NetworkAction
 * using gate logic (AndUsageRule/OrUsageRule/OnSwitchState) currently looks like.
 *
 * Builds a NetworkAction "spsId" available if:
 *   (switchA is open AND cnecCurrent is constrained) OR contingencyA has happened
 * i.e. OrUsageRule(AndUsageRule(OnSwitchState, OnConstraint), OnContingencyState)
 *
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
class GateLogicUsageRuleJsonExportTest {

    @Test
    void printGateLogicUsageRuleJson() {
        Crac crac = CracFactory.findDefault().create("cracId")
            .newInstant("preventive", InstantKind.PREVENTIVE)
            .newInstant("outage", InstantKind.OUTAGE)
            .newInstant("auto", InstantKind.AUTO)
            .newInstant("curative", InstantKind.CURATIVE);

        crac.newContingency()
            .withId("contingencyA")
            .withContingencyElement("ne1Id", ContingencyElementType.LINE)
            .add();

        crac.newFlowCnec()
            .withId("cnecCurrent")
            .withNetworkElement("ne2Id")
            .withInstant("curative")
            .withContingency("contingencyA")
            .withOperator("operator1")
            .withNominalVoltage(400., TwoSides.ONE)
            .withNominalVoltage(400., TwoSides.TWO)
            .newThreshold().withUnit(Unit.AMPERE).withSide(TwoSides.ONE).withMax(1000.).add()
            .add();

        crac.newNetworkAction()
            .withId("spsId")
            .withName("SPS")
            .withOperator("RTE")
            .newSwitchAction().withActionType(ActionType.OPEN).withNetworkElement("switchA").add()
            .newOrUsageRule()
                .newAndUsageRule()
                    .newOnSwitchStateUsageRule().withInstant("curative").withNetworkElement("switchA").withExpectedOpen(true).add()
                    .newOnConstraintUsageRule().withInstant("curative").withCnec("cnecCurrent").add()
                    .add()
                .newOnContingencyStateUsageRule().withContingency("contingencyA").withInstant("curative").add()
                .add()
            .add();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new JsonExport().exportData(crac, outputStream);
        String json = outputStream.toString();

        System.out.println(json);

        // sanity checks that the gate logic actually made it into the export
        assertTrue(json.contains("\"orUsageRules\""));
        assertTrue(json.contains("\"andUsageRules\""));
        assertTrue(json.contains("\"onSwitchStateUsageRules\""));
        assertTrue(json.contains("\"onContingencyStateUsageRules\""));
        assertTrue(json.contains("\"onConstraintUsageRules\""));
        assertTrue(json.contains("\"expectedOpen\" : true"));
    }
}
