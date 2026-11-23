/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.api.usagerule;

/**
 * Anything an OrUsageRule can be added to: a {@link com.powsybl.openrao.data.crac.api.RemedialActionAdder}
 * or an {@link AndUsageRuleAdder}.
 *
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public interface OrUsageRuleAdderOwner<T> {
    OrUsageRuleAdder<T> newOrUsageRule();
}
