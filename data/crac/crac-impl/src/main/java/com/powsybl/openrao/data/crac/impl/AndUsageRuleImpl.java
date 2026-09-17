/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.impl;

import com.powsybl.openrao.data.crac.api.usagerule.AndUsageRule;
import com.powsybl.openrao.data.crac.api.usagerule.UsageRule;

import java.util.HashSet;
import java.util.Set;

/**
 * The AndUsageRuleImpl UsageRule is effective when all of its combined UsageRules are effective.
 *
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public final class AndUsageRuleImpl implements AndUsageRule {

    private final Set<UsageRule> usageRules;

    AndUsageRuleImpl(Set<UsageRule> usageRules) {
        this.usageRules = usageRules;
    }

    @Override
    public Set<UsageRule> getUsageRules() {
        return usageRules;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AndUsageRuleImpl rule = (AndUsageRuleImpl) o;
        return new HashSet<>(rule.getUsageRules()).equals(new HashSet<>(usageRules));
    }

    @Override
    public int hashCode() {
        return usageRules.hashCode() * 19;
    }
}
