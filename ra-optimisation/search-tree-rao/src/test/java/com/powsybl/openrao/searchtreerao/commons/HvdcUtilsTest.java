/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.searchtreerao.commons;

import com.powsybl.action.Action;
import com.powsybl.action.HvdcAction;
import com.powsybl.action.HvdcActionBuilder;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.contingency.ContingencyElementType;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.openloadflow.OpenLoadFlowParameters;
import com.powsybl.openrao.commons.Unit;
import com.powsybl.openrao.data.crac.api.Crac;
import com.powsybl.openrao.data.crac.api.CracFactory;
import com.powsybl.openrao.data.crac.api.InstantKind;
import com.powsybl.openrao.data.crac.api.NetworkElement;
import com.powsybl.openrao.data.crac.api.State;
import com.powsybl.openrao.data.crac.api.networkaction.NetworkAction;
import com.powsybl.openrao.data.crac.api.rangeaction.HvdcRangeAction;
import com.powsybl.openrao.data.crac.api.rangeaction.RangeAction;
import com.powsybl.openrao.data.crac.impl.HvdcRangeActionImpl;
import com.powsybl.openrao.raoapi.parameters.RaoParameters;
import com.powsybl.openrao.raoapi.parameters.extensions.OpenRaoSearchTreeParameters;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.powsybl.openrao.searchtreerao.commons.HvdcUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * @author Roxane Chen {@literal <roxane.chen at rte-france.com>}
 */
public class HvdcUtilsTest {

    @Test
    void checkAddNetworkActionAssociatedWithHvdcRangeAction() throws IOException {
        // Two hvdc range actions both using HVDC line "BBE2AA11 FFR3AA11 1" that is initially in AC emulation mode, so one
        // AC emulation deactivation network action is created but with usage rule of both range actions.
        Network network = Network.read("TestCase16NodesWithHvdc_AC_emulation.xiidm", getClass().getResourceAsStream("/network/TestCase16NodesWithHvdc_AC_emulation.xiidm"));
        Crac crac = Crac.read("crac_hvdc_allinstants_allusagerules.json", getClass().getResourceAsStream("/crac/crac_hvdc_allinstants_allusagerules.json"), network);
        addNetworkActionAssociatedWithHvdcRangeAction(crac, network);
        assertEquals(1, crac.getNetworkActions().size());
        assertEquals(8, crac.getNetworkAction("acEmulationDeactivation_BBE2AA11 FFR3AA11 1").getUsageRules().size());
        assertTrue(crac.getNetworkAction("acEmulationDeactivation_BBE2AA11 FFR3AA11 1").getUsageRules().containsAll(crac.getHvdcRangeAction("HVDC_RA1").getUsageRules()));
        assertTrue(crac.getNetworkAction("acEmulationDeactivation_BBE2AA11 FFR3AA11 1").getUsageRules().containsAll(crac.getHvdcRangeAction("HVDC_RA2").getUsageRules()));
    }

    @Test
    void checkAddNetworkActionAssociatedWithHvdcRangeActionWithAndOrUsageRules() {
        // The usage rules of the HVDC range action are AND/OR usage rules: they must be copied recursively
        // to the AC emulation deactivation network action.
        Network network = Network.read("TestCase16NodesWithHvdc_AC_emulation.xiidm", getClass().getResourceAsStream("/network/TestCase16NodesWithHvdc_AC_emulation.xiidm"));
        Crac crac = CracFactory.findDefault().create("crac")
            .newInstant("preventive", InstantKind.PREVENTIVE)
            .newInstant("outage", InstantKind.OUTAGE)
            .newInstant("auto", InstantKind.AUTO)
            .newInstant("curative", InstantKind.CURATIVE);
        crac.newContingency().withId("co").withContingencyElement("BBE1AA11 FFR5AA11 1", ContingencyElementType.LINE).add();
        crac.newFlowCnec()
            .withId("cnec-curative")
            .withNetworkElement("BBE4AA11 FFR5AA11 1")
            .withContingency("co")
            .withInstant("curative")
            .withNominalVoltage(380.)
            .newThreshold().withSide(TwoSides.ONE).withMax(1000.).withUnit(Unit.AMPERE).add()
            .add();
        HvdcRangeAction hvdcRangeAction = crac.newHvdcRangeAction()
            .withId("hvdc-ra")
            .withNetworkElement("BBE2AA11 FFR3AA11 1")
            .withSpeed(1)
            .newRange().withMin(-1000).withMax(1000).add()
            .newAndUsageRule()
                .newOnContingencyStateUsageRule().withContingency("co").withInstant("auto").add()
                .newOnSwitchStateUsageRule().withInstant("auto").withNetworkElement("switch").withExpectedOpen(true).add()
                .add()
            .newOrUsageRule()
                .newOnConstraintUsageRule().withInstant("curative").withCnec("cnec-curative").add()
                .newAndUsageRule()
                    .newOnInstantUsageRule().withInstant("curative").add()
                    .newOnSwitchStateUsageRule().withInstant("curative").withNetworkElement("switch").withExpectedOpen(false).add()
                    .add()
                .add()
            .add();

        addNetworkActionAssociatedWithHvdcRangeAction(crac, network);

        NetworkAction acEmulationDeactivationAction = crac.getNetworkAction("acEmulationDeactivation_BBE2AA11 FFR3AA11 1");
        assertNotNull(acEmulationDeactivationAction);
        assertEquals(2, acEmulationDeactivationAction.getUsageRules().size());
        assertEquals(hvdcRangeAction.getUsageRules(), acEmulationDeactivationAction.getUsageRules());
    }

    @Test
    void checkNoAcEmulationNetworkActions() throws IOException {
        // Two HVDC range actions both using HVDC line "BBE2AA11 FFR3AA11 1" that is initially in fixed point mode
        // So no AC emulation deactivation network action is created
        Network network = Network.read(
            "TestCase16NodesWithHvdc_fixed_setpoint.xiidm",
            getClass().getResourceAsStream("/network/TestCase16NodesWithHvdc_fixed_setpoint.xiidm")
        );
        Crac crac = Crac.read(
            "crac_hvdc_allinstants_allusagerules.json",
            getClass().getResourceAsStream("/crac/crac_hvdc_allinstants_allusagerules.json"),
            network
        );
        addNetworkActionAssociatedWithHvdcRangeAction(crac, network);
        assertEquals(0, crac.getNetworkActions().size());
    }

    @Test
    void testUpdateHvdcRangeActionInitialSetpoint() throws IOException {
        // Two HVDC range actions both using HVDC line "BBE2AA11 FFR3AA11 1" that is initially in AC emulation mode
        // the initial set point is set to the initial flow passing on the line.
        Network network = Network.read(
            "TestCase16NodesWithHvdc_AC_emulation.xiidm",
            getClass().getResourceAsStream("/network/TestCase16NodesWithHvdc_AC_emulation.xiidm")
        );
        Crac crac = Crac.read(
            "crac_hvdc_allinstants_allusagerules.json",
            getClass().getResourceAsStream("/crac/crac_hvdc_allinstants_allusagerules.json"),
            network
        );
        // Before
        assertEquals(0.0, crac.getHvdcRangeAction("HVDC_RA1").getInitialSetpoint());
        assertEquals(0.0, crac.getHvdcRangeAction("HVDC_RA2").getInitialSetpoint());

        RaoParameters raoParameters = new RaoParameters(ReportNode.NO_OP);
        raoParameters.addExtension(OpenRaoSearchTreeParameters.class, new OpenRaoSearchTreeParameters(ReportNode.NO_OP));
        updateHvdcRangeActionInitialSetpoint(crac, network, raoParameters, ReportNode.NO_OP);

        assertEquals(823.0, crac.getHvdcRangeAction("HVDC_RA1").getInitialSetpoint(), 1);
        assertEquals(823.0, crac.getHvdcRangeAction("HVDC_RA2").getInitialSetpoint(), 1);
    }

    @Test
    void testUpdateHvdcRangeActionInitialSetpointWithSide2RRectifier() throws IOException {
        // Two HVDC range actions both using HVDC line "BBE2AA11 FFR3AA11 1" that is initially in AC emulation mode
        // The difference with test testUpdateHvdcRangeActionInitialSetpoint is that the flow goes from converter station 2 to converter station 1 on the line
        // We should :
        // - read the power at station 1
        // - see that it is negative
        // - keep converter mode to SIDE_1_INVERTER_SIDE_2_RECTIFIER + active power setpoint to abs(setpoint)
        Network network = Network.read(
            "TestCase16NodesWithHvdc_AC_emulation_side1_inverter.xiidm",
            getClass().getResourceAsStream("/network/TestCase16NodesWithHvdc_AC_emulation_side1_inverter.xiidm")
        );
        Crac crac = Crac.read(
            "crac_hvdc_allinstants_allusagerules.json",
            getClass().getResourceAsStream("/crac/crac_hvdc_allinstants_allusagerules.json"),
            network
        );
        // Before
        assertEquals(-0.0, crac.getHvdcRangeAction("HVDC_RA1").getInitialSetpoint());
        assertEquals(-0.0, crac.getHvdcRangeAction("HVDC_RA2").getInitialSetpoint());
        assertEquals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, network.getHvdcLine("BBE2AA11 FFR3AA11 1").getConvertersMode());

        RaoParameters raoParameters = new RaoParameters(ReportNode.NO_OP);
        raoParameters.addExtension(OpenRaoSearchTreeParameters.class, new OpenRaoSearchTreeParameters(ReportNode.NO_OP));
        updateHvdcRangeActionInitialSetpoint(crac, network, raoParameters, ReportNode.NO_OP);

        // the flow goes from converter station 2 to converter station 1 on the line => setpoint should be negative.
        assertEquals(-806, crac.getHvdcRangeAction("HVDC_RA1").getInitialSetpoint(), 1);
        assertEquals(-806, crac.getHvdcRangeAction("HVDC_RA2").getInitialSetpoint(), 1);
        assertEquals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, network.getHvdcLine("BBE2AA11 FFR3AA11 1").getConvertersMode());
        assertEquals(806, network.getHvdcLine("BBE2AA11 FFR3AA11 1").getActivePowerSetpoint(), 1);

    }

    @Test
    void testRunLoadFlowAndUpdateHvdcActiveSetpointAfterContingency() throws IOException {
        // Test that check if the runLoadFlowAndUpdateHvdcActiveSetpoint, is able to apply contingency and update the setpoint after applying the contingency.
        // Plus that the returned values correspond to what was applied in network.

        Network network = Network.read("TestCase16NodesWithHvdc_AC_emulation.xiidm", getClass().getResourceAsStream("/network/TestCase16NodesWithHvdc_AC_emulation.xiidm"));
        Crac crac = Crac.read("crac_hvdc_allinstants_allusagerules.json", getClass().getResourceAsStream("/crac/crac_hvdc_allinstants_allusagerules.json"), network);
        LoadFlowParameters loadFlowParameters = new LoadFlowParameters();
        OpenLoadFlowParameters openLoadFlowParameters = new OpenLoadFlowParameters();
        loadFlowParameters.addExtension(OpenLoadFlowParameters.class, openLoadFlowParameters);

        // Before running load flow
        // The setpoint is read directly from the network
        assertEquals(0.0, crac.getHvdcRangeAction("HVDC_RA1").getCurrentSetpoint(network));

        // Preventive state, no contingency is applied
        RaoParameters raoParameters = new RaoParameters(ReportNode.NO_OP);
        raoParameters.addExtension(OpenRaoSearchTreeParameters.class, new OpenRaoSearchTreeParameters(ReportNode.NO_OP));
        Map<HvdcRangeAction, Double> hvdcRangeActionActivePowerSetpoint = runLoadFlowAndUpdateHvdcActivePowerSetpoint(
            network,
            crac.getPreventiveState(),
            "OpenLoadFlow",
            loadFlowParameters,
            crac.getHvdcRangeActions().stream().map(HvdcRangeActionImpl.class::cast).collect(Collectors.toSet()),
            ReportNode.NO_OP
        );

        assertEquals(hvdcRangeActionActivePowerSetpoint.get(crac.getHvdcRangeAction("HVDC_RA1")), crac.getHvdcRangeAction("HVDC_RA1").getCurrentSetpoint(network));
        assertEquals(823, hvdcRangeActionActivePowerSetpoint.get(crac.getHvdcRangeAction("HVDC_RA1")), 1);

        // Auto state, after applying contingency
        State autoState = crac.getState("co1_be1_fr5", crac.getInstant(InstantKind.AUTO));
        hvdcRangeActionActivePowerSetpoint = runLoadFlowAndUpdateHvdcActivePowerSetpoint(
            network,
            autoState,
            "OpenLoadFlow",
            loadFlowParameters,
            crac.getHvdcRangeActions().stream().map(HvdcRangeActionImpl.class::cast).collect(Collectors.toSet()),
            ReportNode.NO_OP
        );
        assertEquals(hvdcRangeActionActivePowerSetpoint.get(crac.getHvdcRangeAction("HVDC_RA1")), crac.getHvdcRangeAction("HVDC_RA1").getCurrentSetpoint(network));
        assertEquals(864, hvdcRangeActionActivePowerSetpoint.get(crac.getHvdcRangeAction("HVDC_RA1")), 1);
    }

    @Test
    void testRunLoadFlowAndUpdateHvdcActiveSetpointWithDisconnectedHvdc() throws IOException {
        // same test as testRunLoadFlowAndUpdateHvdcActiveSetpointAfterContingency but with a disconnected HVDC line
        // The setpoint is not valid, the HVDC active power set point is not updated.
        Network network = Network.read("TestCase16NodesWithHvdc_disconnected.xiidm", getClass().getResourceAsStream("/network/TestCase16NodesWithHvdc_disconnected.xiidm"));
        Crac crac = Crac.read("crac_hvdc_allinstants_allusagerules.json", getClass().getResourceAsStream("/crac/crac_hvdc_allinstants_allusagerules.json"), network);
        LoadFlowParameters loadFlowParameters = new LoadFlowParameters();
        OpenLoadFlowParameters openLoadFlowParameters = new OpenLoadFlowParameters();
        loadFlowParameters.addExtension(OpenLoadFlowParameters.class, openLoadFlowParameters);

        Map<HvdcRangeAction, Double> hvdcRangeActionActivePowerSetpoint = runLoadFlowAndUpdateHvdcActivePowerSetpoint(
            network,
            crac.getPreventiveState(),
            "OpenLoadFlow",
            loadFlowParameters,
            crac.getHvdcRangeActions().stream().map(HvdcRangeActionImpl.class::cast).collect(Collectors.toSet()),
            ReportNode.NO_OP
        );
        assertTrue(hvdcRangeActionActivePowerSetpoint.isEmpty());
        assertEquals(0, crac.getHvdcRangeAction("HVDC_RA1").getCurrentSetpoint(network));
    }

    @Test
    void testGetHvdcRangeActionsPerStateAssociatedWithHvdcLine() {
        State state1 = Mockito.mock(State.class);
        State state2 = Mockito.mock(State.class);

        NetworkElement hvdc1 = Mockito.mock(NetworkElement.class);
        when(hvdc1.getId()).thenReturn("hvdc-1");

        NetworkElement hvdc2 = Mockito.mock(NetworkElement.class);
        when(hvdc2.getId()).thenReturn("hvdc-2");

        HvdcRangeAction hvdcRangeActionOnTargetLine = Mockito.mock(HvdcRangeAction.class);
        when(hvdcRangeActionOnTargetLine.getNetworkElement()).thenReturn(hvdc1);

        HvdcRangeAction hvdcRangeActionOnOtherLine = Mockito.mock(HvdcRangeAction.class);
        when(hvdcRangeActionOnOtherLine.getNetworkElement()).thenReturn(hvdc2);

        RangeAction<?> nonHvdcRangeAction = Mockito.mock(RangeAction.class);

        Map<State, Set<RangeAction<?>>> rangeActionsPerState = Map.of(
            state1, Set.of(hvdcRangeActionOnTargetLine, hvdcRangeActionOnOtherLine, nonHvdcRangeAction),
            state2, Set.of(hvdcRangeActionOnTargetLine, hvdcRangeActionOnOtherLine, nonHvdcRangeAction)
        );

        Map<State, Set<HvdcRangeAction>> result = getHvdcRangeActionsPerStateAssociatedWithHvdcLine(rangeActionsPerState, "hvdc-1");

        assertEquals(Set.of(state1, state2), result.keySet());
        assertEquals(Set.of(hvdcRangeActionOnTargetLine), result.get(state1));
        assertEquals(Set.of(hvdcRangeActionOnTargetLine), result.get(state2));
    }

    @Test
    void testIsAcEmulationDeactivationAction() {
        // Test 1: NetworkAction with only HvdcAction with AC emulation disabled - should return true
        HvdcAction hvdcAction1 = new HvdcActionBuilder()
            .withId("hvdc-deactivate-1")
            .withHvdcId("HVDC1")
            .withAcEmulationEnabled(false)
            .build();

        NetworkAction acEmulationDeactivationAction = Mockito.mock(NetworkAction.class);
        when(acEmulationDeactivationAction.getElementaryActions()).thenReturn(Set.of(hvdcAction1));

        assertTrue(HvdcUtils.isAcEmulationDeactivationAction(acEmulationDeactivationAction));

        // Test 2: NetworkAction with multiple HvdcActions all with AC emulation disabled - should return true
        HvdcAction hvdcAction2 = new HvdcActionBuilder()
            .withId("hvdc-deactivate-2")
            .withHvdcId("HVDC2")
            .withAcEmulationEnabled(false)
            .build();

        NetworkAction multipleHvdcDeactivationAction = Mockito.mock(NetworkAction.class);
        when(multipleHvdcDeactivationAction.getElementaryActions()).thenReturn(Set.of(hvdcAction1, hvdcAction2));

        assertTrue(HvdcUtils.isAcEmulationDeactivationAction(multipleHvdcDeactivationAction));

        // Test 3: NetworkAction with HvdcAction with AC emulation enabled - should return false
        HvdcAction hvdcActionEnabled = new HvdcActionBuilder()
            .withId("hvdc-activate-1")
            .withHvdcId("HVDC3")
            .withAcEmulationEnabled(true)
            .build();

        NetworkAction acEmulationActivationAction = Mockito.mock(NetworkAction.class);
        when(acEmulationActivationAction.getElementaryActions()).thenReturn(Set.of(hvdcActionEnabled));

        assertFalse(HvdcUtils.isAcEmulationDeactivationAction(acEmulationActivationAction));

        // Test 5: NetworkAction with HvdcAction without AC emulation specification - should return false
        HvdcAction hvdcActionNoAcEmulation = new HvdcActionBuilder()
            .withId("hvdc-no-ac-1")
            .withHvdcId("HVDC4")
            .withActivePowerSetpoint(100.0)
            .build();

        NetworkAction noAcEmulationSpecAction = Mockito.mock(NetworkAction.class);
        when(noAcEmulationSpecAction.getElementaryActions()).thenReturn(Set.of(hvdcActionNoAcEmulation));

        assertFalse(HvdcUtils.isAcEmulationDeactivationAction(noAcEmulationSpecAction));

        // Test 6: NetworkAction with non-HvdcAction - should return false
        Action otherAction = Mockito.mock(Action.class);
        NetworkAction nonHvdcAction = Mockito.mock(NetworkAction.class);
        when(nonHvdcAction.getElementaryActions()).thenReturn(Set.of(otherAction));

        assertFalse(HvdcUtils.isAcEmulationDeactivationAction(nonHvdcAction));

        // Test 7: NetworkAction with mix of HvdcAction and other actions - should return false
        NetworkAction mixedActionTypes = Mockito.mock(NetworkAction.class);
        when(mixedActionTypes.getElementaryActions()).thenReturn(Set.of(hvdcAction1, otherAction));

        assertFalse(HvdcUtils.isAcEmulationDeactivationAction(mixedActionTypes));

        // Test 8: NetworkAction with empty elementary actions - should return true (vacuous truth)
        NetworkAction emptyAction = Mockito.mock(NetworkAction.class);
        when(emptyAction.getElementaryActions()).thenReturn(Set.of());

        assertTrue(HvdcUtils.isAcEmulationDeactivationAction(emptyAction));
    }
}
