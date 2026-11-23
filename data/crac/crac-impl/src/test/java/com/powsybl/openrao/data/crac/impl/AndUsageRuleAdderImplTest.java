/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.impl;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.powsybl.contingency.ContingencyElementType;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.openrao.commons.OpenRaoException;
import com.powsybl.openrao.commons.Unit;
import com.powsybl.openrao.commons.logs.RaoBusinessWarns;
import com.powsybl.openrao.data.crac.api.Crac;
import com.powsybl.openrao.data.crac.api.InstantKind;
import com.powsybl.openrao.data.crac.api.networkaction.ActionType;
import com.powsybl.openrao.data.crac.api.networkaction.NetworkAction;
import com.powsybl.openrao.data.crac.api.networkaction.NetworkActionAdder;
import com.powsybl.openrao.data.crac.api.usagerule.UsageRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.List;
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

    private ListAppender<ILoggingEvent> getBusinessWarns() {
        Logger logger = (Logger) LoggerFactory.getLogger(RaoBusinessWarns.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        return listAppender;
    }

    private List<String> getUnreachableUsageRuleWarnings(ListAppender<ILoggingEvent> listAppender) {
        return listAppender.list.stream()
            .map(ILoggingEvent::getFormattedMessage)
            .filter(message -> message.contains("can never be activated"))
            .toList();
    }

    @Test
    void testWarnOnOrBranchUnreachableInAnd() {
        ListAppender<ILoggingEvent> listAppender = getBusinessWarns();
        newNetworkActionAdder()
            .newAndUsageRule()
                .newOnContingencyStateUsageRule().withInstant(CURATIVE_INSTANT_ID).withContingency("contingencyId").add()
                .newOrUsageRule()
                    .newOnInstantUsageRule().withInstant(PREVENTIVE_INSTANT_ID).add()
                    .newOnInstantUsageRule().withInstant(CURATIVE_INSTANT_ID).add()
                    .add()
                .add()
            .add();
        assertEquals(
            List.of("Remedial action networkActionId: OnInstant usage rule at instant 'preventive' can never be activated, "
                + "as it is combined in an AND usage rule only defined at instant(s) [curative]"),
            getUnreachableUsageRuleWarnings(listAppender));
    }

    @Test
    void testWarnOnUnreachableBranchEvenIfRootOrIsDefinedAtItsInstant() {
        ListAppender<ILoggingEvent> listAppender = getBusinessWarns();
        // the root OR is defined at the preventive instant thanks to its OnInstant branch, but the preventive
        // OnConstraint can still never be activated, as it is inside an AND only defined at the curative instant
        newNetworkActionAdder()
            .newOrUsageRule()
                .newAndUsageRule()
                    .newOnContingencyStateUsageRule().withInstant(CURATIVE_INSTANT_ID).withContingency("contingencyId").add()
                    .newOrUsageRule()
                        .newOnConstraintUsageRule().withInstant(PREVENTIVE_INSTANT_ID).withCnec("preventiveCnec").add()
                        .newOnInstantUsageRule().withInstant(CURATIVE_INSTANT_ID).add()
                        .add()
                    .add()
                .newOnInstantUsageRule().withInstant(PREVENTIVE_INSTANT_ID).add()
                .add()
            .add();
        assertEquals(
            List.of("Remedial action networkActionId: OnConstraint usage rule at instant 'preventive' can never be activated, "
                + "as it is combined in an AND usage rule only defined at instant(s) [curative]"),
            getUnreachableUsageRuleWarnings(listAppender));
    }

    @Test
    void testWarnOnceOnUsageRuleUnreachableInNestedAnds() {
        ListAppender<ILoggingEvent> listAppender = getBusinessWarns();
        newNetworkActionAdder()
            .newAndUsageRule()
                .newOnContingencyStateUsageRule().withInstant(CURATIVE_INSTANT_ID).withContingency("contingencyId").add()
                .newOrUsageRule()
                    .newAndUsageRule()
                        .newOnInstantUsageRule().withInstant(CURATIVE_INSTANT_ID).add()
                        .newOrUsageRule()
                            .newOnInstantUsageRule().withInstant(PREVENTIVE_INSTANT_ID).add()
                            .newOnInstantUsageRule().withInstant(CURATIVE_INSTANT_ID).add()
                            .add()
                        .add()
                    .newOnSwitchStateUsageRule().withInstant(CURATIVE_INSTANT_ID).withNetworkElement("switchId").withExpectedOpen(true).add()
                    .add()
                .add()
            .add();
        assertEquals(1, getUnreachableUsageRuleWarnings(listAppender).size());
    }

    @Test
    void testNoWarningForOrMixingInstants() {
        ListAppender<ILoggingEvent> listAppender = getBusinessWarns();
        newNetworkActionAdder()
            .newOrUsageRule()
                .newOnInstantUsageRule().withInstant(PREVENTIVE_INSTANT_ID).add()
                .newOnInstantUsageRule().withInstant(CURATIVE_INSTANT_ID).add()
                .add()
            .add();
        assertEquals(List.of(), getUnreachableUsageRuleWarnings(listAppender));
    }
}
