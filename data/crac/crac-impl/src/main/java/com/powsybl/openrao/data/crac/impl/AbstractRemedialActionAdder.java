/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.impl;

import com.powsybl.openrao.commons.OpenRaoException;
import com.powsybl.openrao.data.crac.api.Instant;
import com.powsybl.openrao.data.crac.api.RemedialActionAdder;
import com.powsybl.openrao.data.crac.api.cnec.Cnec;
import com.powsybl.openrao.data.crac.api.usagerule.AndUsageRule;
import com.powsybl.openrao.data.crac.api.usagerule.AndUsageRuleAdder;
import com.powsybl.openrao.data.crac.api.usagerule.OnConstraintAdder;
import com.powsybl.openrao.data.crac.api.usagerule.OnContingencyStateAdder;
import com.powsybl.openrao.data.crac.api.usagerule.OnFlowConstraintInCountryAdder;
import com.powsybl.openrao.data.crac.api.usagerule.OnInstantAdder;
import com.powsybl.openrao.data.crac.api.usagerule.OrUsageRule;
import com.powsybl.openrao.data.crac.api.usagerule.OrUsageRuleAdder;
import com.powsybl.openrao.data.crac.api.usagerule.SingleInstantUsageRule;
import com.powsybl.openrao.data.crac.api.usagerule.UsageRule;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.powsybl.openrao.commons.logs.OpenRaoLoggerProvider.BUSINESS_WARNS;

/**
 * @author Peter Mitri {@literal <peter.mitri at rte-france.com>}
 * @author Baptiste Seguinot {@literal <baptiste.seguinot at rte-france.com>}
 */
public abstract class AbstractRemedialActionAdder<T extends RemedialActionAdder<T>> extends AbstractIdentifiableAdder<T> implements RemedialActionAdder<T>, UsageRuleGateAdderOwner {

    protected String operator;
    protected Integer speed;
    protected Double activationCost;
    protected Set<UsageRule> usageRules = new HashSet<>();
    private final CracImpl crac;

    AbstractRemedialActionAdder(CracImpl crac) {
        Objects.requireNonNull(crac);
        this.crac = crac;
    }

    @Override
    public T withOperator(String operator) {
        this.operator = operator;
        return (T) this;
    }

    @Override
    public T withSpeed(Integer speed) {
        this.speed = speed;
        return (T) this;
    }

    @Override
    public T withActivationCost(Double activationCost) {
        this.activationCost = activationCost;
        return (T) this;
    }

    @Override
    public OnInstantAdder<T> newOnInstantUsageRule() {
        return new OnInstantAdderImpl(this);
    }

    @Override
    public OnContingencyStateAdder<T> newOnContingencyStateUsageRule() {
        return new OnContingencyStateAdderImpl(this);
    }

    @Override
    public OnConstraintAdder<T, ?> newOnConstraintUsageRule() {
        return new OnConstraintAdderImpl(this);
    }

    @Override
    public OnFlowConstraintInCountryAdder<T> newOnFlowConstraintInCountryUsageRule() {
        return new OnFlowConstraintInCountryAdderImpl(this);
    }

    @Override
    public AndUsageRuleAdder<T> newAndUsageRule() {
        return new AndUsageRuleAdderImpl(this);
    }

    @Override
    public OrUsageRuleAdder<T> newOrUsageRule() {
        return new OrUsageRuleAdderImpl(this);
    }

    @Override
    public void addUsageRule(UsageRule usageRule) {
        this.usageRules.add(usageRule);
    }

    @Override
    public CracImpl getCrac() {
        return this.crac;
    }

    /**
     * Warns about the leaf usage rules whose instant is not among the instants of an AndUsageRule they are
     * combined in, e.g. the preventive leaf of AND(OnContingencyState(curative), OR(OnInstant(preventive),
     * OnInstant(curative))): they can never be activated.
     */
    protected void warnUnreachableUsageRules() {
        Map<SingleInstantUsageRule, Set<Instant>> unreachableUsageRules = new LinkedHashMap<>();
        usageRules.forEach(usageRule -> collectUnreachableUsageRules(usageRule, unreachableUsageRules));
        unreachableUsageRules.forEach((usageRule, andInstants) -> BUSINESS_WARNS.warn(
            "Remedial action {}: {} usage rule at instant '{}' can never be activated, as it is combined in an AND usage rule only defined at instant(s) {}",
            id,
            usageRule.getClass().getSimpleName().replace("Impl", ""),
            usageRule.getInstant().getId(),
            andInstants.stream().sorted(Comparator.comparingInt(Instant::getOrder)).map(Instant::getId).toList()));
    }

    private static void collectUnreachableUsageRules(UsageRule usageRule, Map<SingleInstantUsageRule, Set<Instant>> unreachableUsageRules) {
        if (usageRule instanceof AndUsageRule andUsageRule) {
            Set<Instant> andInstants = andUsageRule.getInstants();
            UsageRule.flatten(andUsageRule)
                .filter(leaf -> !andInstants.contains(leaf.getInstant()))
                .forEach(leaf -> unreachableUsageRules.putIfAbsent(leaf, andInstants));
            andUsageRule.getUsageRules().forEach(child -> collectUnreachableUsageRules(child, unreachableUsageRules));
        } else if (usageRule instanceof OrUsageRule orUsageRule) {
            orUsageRule.getUsageRules().forEach(child -> collectUnreachableUsageRules(child, unreachableUsageRules));
        }
    }

    static void checkOnConstraintUsageRules(Instant instant, Cnec<?> cnec) {
        // Only allow PRAs with usage rule OnConstraint, for CNECs of instants PREVENTIVE & OUTAGE & CURATIVE
        // Only allow ARAs with usage rule OnConstraint, for CNECs of instant AUTO
        // Only allow CRAs with usage rule OnConstraint, for CNECs of instant CURATIVE
        // TODO: inconsistency between comment and code
        if (cnec.getState().getInstant().comesBefore(instant)) {
            throw new OpenRaoException(String.format("Remedial actions available at instant '%s' on a CNEC constraint at instant '%s' are not allowed.", instant, cnec.getState().getInstant()));
        }
    }
}
