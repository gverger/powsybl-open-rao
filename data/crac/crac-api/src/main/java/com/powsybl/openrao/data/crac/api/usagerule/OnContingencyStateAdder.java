/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.api.usagerule;

/**
 * Adds an OnContingencyState usage rule to its owner: a RemedialActionAdder, or a gate logic
 * combination adder such as {@link AndUsageRuleAdder} or {@link OrUsageRuleAdder}.
 *
 * @author Baptiste Seguinot {@literal <baptiste.seguinot at rte-france.com>}
 */
public interface OnContingencyStateAdder<T> {

    OnContingencyStateAdder<T> withContingency(String contingencyId);

    OnContingencyStateAdder<T> withInstant(String instantId);

    T add();
}
