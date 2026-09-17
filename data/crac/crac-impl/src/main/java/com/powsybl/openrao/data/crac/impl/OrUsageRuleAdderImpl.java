/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.impl;

import com.powsybl.openrao.data.crac.api.usagerule.AndUsageRuleAdder;
import com.powsybl.openrao.data.crac.api.usagerule.OnConstraintAdder;
import com.powsybl.openrao.data.crac.api.usagerule.OnContingencyStateAdder;
import com.powsybl.openrao.data.crac.api.usagerule.OnFlowConstraintInCountryAdder;
import com.powsybl.openrao.data.crac.api.usagerule.OnFlowThresholdAdder;
import com.powsybl.openrao.data.crac.api.usagerule.OnInstantAdder;
import com.powsybl.openrao.data.crac.api.usagerule.OnSwitchStateAdder;
import com.powsybl.openrao.data.crac.api.usagerule.OrUsageRuleAdder;
import com.powsybl.openrao.data.crac.api.usagerule.UsageRule;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public class OrUsageRuleAdderImpl<T extends UsageRuleGateAdderOwner> implements OrUsageRuleAdder<T>, UsageRuleGateAdderOwner {
    private static final String CLASS_NAME = "OrUsageRule";
    private final T owner;
    private final Set<UsageRule> usageRules = new HashSet<>();

    OrUsageRuleAdderImpl(UsageRuleGateAdderOwner owner) {
        this.owner = (T) owner;
    }

    @Override
    public CracImpl getCrac() {
        return owner.getCrac();
    }

    @Override
    public void addUsageRule(UsageRule usageRule) {
        this.usageRules.add(usageRule);
    }

    @Override
    public OnInstantAdder<OrUsageRuleAdder<T>> newOnInstantUsageRule() {
        return new OnInstantAdderImpl(this);
    }

    @Override
    public OnContingencyStateAdder<OrUsageRuleAdder<T>> newOnContingencyStateUsageRule() {
        return new OnContingencyStateAdderImpl(this);
    }

    @Override
    public OnConstraintAdder<OrUsageRuleAdder<T>, ?> newOnConstraintUsageRule() {
        return new OnConstraintAdderImpl(this);
    }

    @Override
    public OnFlowConstraintInCountryAdder<OrUsageRuleAdder<T>> newOnFlowConstraintInCountryUsageRule() {
        return new OnFlowConstraintInCountryAdderImpl(this);
    }

    @Override
    public OnSwitchStateAdder<OrUsageRuleAdder<T>> newOnSwitchStateUsageRule() {
        return new OnSwitchStateAdderImpl(this);
    }

    @Override
    public AndUsageRuleAdder<OrUsageRuleAdder<T>> newAndUsageRule() {
        return new AndUsageRuleAdderImpl(this);
    }

    @Override
    public OnFlowThresholdAdder<OrUsageRuleAdder<T>> newOnFlowThresholdUsageRule() {
        return new OnFlowThresholdAdderImpl(this);
    }

    @Override
    public T add() {
        AdderUtils.assertAttributeNotEmpty(usageRules, CLASS_NAME, "usage rule",
            "newOnInstantUsageRule(), newOnContingencyStateUsageRule(), newOnConstraintUsageRule(), "
                + "newOnFlowConstraintInCountryUsageRule(), newOnSwitchStateUsageRule(), newOnFlowThresholdUsageRule() or newAndUsageRule()");
        owner.addUsageRule(new OrUsageRuleImpl(usageRules));
        return owner;
    }
}
