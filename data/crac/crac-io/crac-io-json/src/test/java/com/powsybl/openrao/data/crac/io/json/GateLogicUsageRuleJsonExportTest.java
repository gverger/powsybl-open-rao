/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.io.json;

import com.powsybl.contingency.ContingencyElementType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.openrao.commons.Unit;
import com.powsybl.openrao.data.crac.api.Crac;
import com.powsybl.openrao.data.crac.api.CracFactory;
import com.powsybl.openrao.data.crac.api.Instant;
import com.powsybl.openrao.data.crac.api.InstantKind;
import com.powsybl.openrao.data.crac.api.State;
import com.powsybl.openrao.data.crac.api.networkaction.ActionType;
import com.powsybl.openrao.data.crac.api.networkaction.NetworkAction;
import com.powsybl.openrao.data.crac.api.parameters.CracCreationParameters;
import com.powsybl.openrao.data.crac.api.usagerule.AndUsageRule;
import com.powsybl.openrao.data.crac.api.usagerule.OnConstraint;
import com.powsybl.openrao.data.crac.api.usagerule.OnContingencyState;
import com.powsybl.openrao.data.crac.api.usagerule.OnFlowThreshold;
import com.powsybl.openrao.data.crac.api.usagerule.OnSwitchState;
import com.powsybl.openrao.data.crac.api.usagerule.OrUsageRule;
import com.powsybl.openrao.data.crac.api.usagerule.UsageRule;
import com.powsybl.openrao.data.crac.impl.utils.NetworkImportsUtil;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Builds a NetworkAction "spsId" available if:
 *   (switchA is open AND cnecCurrent is constrained) OR contingencyA has happened
 * i.e. OrUsageRule(AndUsageRule(OnSwitchState, OnConstraint), OnContingencyState)
 * and checks what its Json export looks like, and that it can be read back.
 *
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
class GateLogicUsageRuleJsonExportTest {

    private static Crac createCrac() {
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
                    .newOnFlowThresholdUsageRule().withInstant("curative").withCnec("cnecCurrent").withSide(TwoSides.ONE).withUnit(Unit.MEGAWATT).withAtLeast(-50.).withAtMost(50.).add()
                    .add()
                .newOnContingencyStateUsageRule().withContingency("contingencyA").withInstant("curative").add()
                .add()
            .add();

        return crac;
    }

    @Test
    void printGateLogicUsageRuleJson() {
        String json = exportToJson(createCrac());
        System.out.println(json);

        // sanity checks that the gate logic actually made it into the export
        assertTrue(json.contains("\"orUsageRules\""));
        assertTrue(json.contains("\"andUsageRules\""));
        assertTrue(json.contains("\"onSwitchStateUsageRules\""));
        assertTrue(json.contains("\"onContingencyStateUsageRules\""));
        assertTrue(json.contains("\"onConstraintUsageRules\""));
        assertTrue(json.contains("\"onFlowThresholdUsageRules\""));
        assertTrue(json.contains("\"expectedOpen\" : true"));
    }

    @Test
    void roundTripGateLogicUsageRule() {
        Network network = NetworkImportsUtil.createNetworkForJsonRetrocompatibilityTest(0.0);
        String json = exportToJson(createCrac());

        // Bypasses JsonSchemaProvider/Crac.read() format auto-detection on purpose: the per-type Json
        // schemas (network-action-vX.json, etc.) have not been updated yet to allow the new
        // onSwitchStateUsageRules/andUsageRules/orUsageRules fields, so Crac.read() would currently
        // reject this file. Calling JsonImport().importData() directly exercises the deserializers
        // without going through schema validation.
        Crac importedCrac;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))) {
            importedCrac = new JsonImport().importData(inputStream, CracCreationParameters.load(), network).getCrac();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        NetworkAction sps = importedCrac.getNetworkAction("spsId");
        Instant curativeInstant = importedCrac.getInstant("curative");

        Set<UsageRule> usageRules = sps.getUsageRules();
        assertEquals(1, usageRules.size());
        OrUsageRule orUsageRule = assertInstanceOf(OrUsageRule.class, usageRules.iterator().next());
        assertEquals(curativeInstant, orUsageRule.getInstant());
        assertEquals(2, orUsageRule.getUsageRules().size());

        OnContingencyState onContingencyState = orUsageRule.getUsageRules().stream()
            .filter(OnContingencyState.class::isInstance).map(OnContingencyState.class::cast)
            .findAny().orElseThrow();
        assertEquals("contingencyA", onContingencyState.getContingency().getId());
        assertEquals(curativeInstant, onContingencyState.getInstant());

        AndUsageRule andUsageRule = orUsageRule.getUsageRules().stream()
            .filter(AndUsageRule.class::isInstance).map(AndUsageRule.class::cast)
            .findAny().orElseThrow();
        assertEquals(curativeInstant, andUsageRule.getInstant());
        assertEquals(3, andUsageRule.getUsageRules().size());

        OnSwitchState onSwitchState = andUsageRule.getUsageRules().stream()
            .filter(OnSwitchState.class::isInstance).map(OnSwitchState.class::cast)
            .findAny().orElseThrow();
        assertEquals("switchA", onSwitchState.getSwitchElement().getId());
        assertTrue(onSwitchState.isExpectedOpen());
        assertEquals(curativeInstant, onSwitchState.getInstant());

        OnConstraint<?> onConstraint = andUsageRule.getUsageRules().stream()
            .filter(OnConstraint.class::isInstance).map(oc -> (OnConstraint<?>) oc)
            .findAny().orElseThrow();
        assertEquals("cnecCurrent", onConstraint.getCnec().getId());
        assertEquals(curativeInstant, onConstraint.getInstant());

        OnFlowThreshold onFlowThreshold = andUsageRule.getUsageRules().stream()
            .filter(OnFlowThreshold.class::isInstance).map(OnFlowThreshold.class::cast)
            .findAny().orElseThrow();
        assertEquals("cnecCurrent", onFlowThreshold.getCnec().getId());
        assertEquals(TwoSides.ONE, onFlowThreshold.getSide());
        assertEquals(Unit.MEGAWATT, onFlowThreshold.getUnit());
        assertEquals(-50., onFlowThreshold.atLeast().orElseThrow(), 1e-6);
        assertEquals(50., onFlowThreshold.atMost().orElseThrow(), 1e-6);
        assertEquals(curativeInstant, onFlowThreshold.getInstant());

        // sanity check: the round-tripped gate logic still evaluates structurally as expected
        State curativeContingencyAState = importedCrac.getState("contingencyA", curativeInstant);
        assertTrue(sps.isAvailableForState(curativeContingencyAState));
    }

    private static String exportToJson(Crac crac) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new JsonExport().exportData(crac, outputStream);
        return outputStream.toString();
    }
}
