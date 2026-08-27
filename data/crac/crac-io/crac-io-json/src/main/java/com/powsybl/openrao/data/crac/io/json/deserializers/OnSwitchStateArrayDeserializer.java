/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.io.json.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.powsybl.openrao.commons.OpenRaoException;
import com.powsybl.openrao.data.crac.api.usagerule.OnSwitchStateAdder;
import com.powsybl.openrao.data.crac.io.json.JsonSerializationConstants;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public final class OnSwitchStateArrayDeserializer {
    private OnSwitchStateArrayDeserializer() {
    }

    /**
     * adderSupplier is a supplier (rather than a single owner) so that this deserializer can be reused both
     * for a RemedialActionAdder's own top-level UsageRules, and, recursively, for the UsageRules combined by
     * an AndUsageRule/OrUsageRule gate logic combination adder (e.g. ownerAdder::newOnSwitchStateUsageRule).
     */
    public static void deserialize(JsonParser jsonParser, Supplier<OnSwitchStateAdder<?>> adderSupplier, String version) throws IOException {
        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
            OnSwitchStateAdder<?> adder = adderSupplier.get();
            while (!jsonParser.nextToken().isStructEnd()) {
                switch (jsonParser.currentName()) {
                    case JsonSerializationConstants.INSTANT:
                        adder.withInstant(jsonParser.nextTextValue());
                        break;
                    case JsonSerializationConstants.NETWORK_ELEMENT_ID:
                        adder.withNetworkElement(jsonParser.nextTextValue());
                        break;
                    case JsonSerializationConstants.EXPECTED_OPEN:
                        adder.withExpectedOpen(jsonParser.nextBooleanValue());
                        break;
                    default:
                        throw new OpenRaoException("Unexpected field in OnSwitchState: " + jsonParser.currentName());
                }
            }
            adder.add();
        }
    }
}
