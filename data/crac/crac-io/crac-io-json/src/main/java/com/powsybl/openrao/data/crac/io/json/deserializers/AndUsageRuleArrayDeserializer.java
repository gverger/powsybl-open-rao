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
import com.powsybl.openrao.data.crac.api.usagerule.AndUsageRuleAdder;
import com.powsybl.openrao.data.crac.api.usagerule.AndUsageRuleAdderOwner;
import com.powsybl.openrao.data.crac.io.json.JsonSerializationConstants;

import java.io.IOException;

/**
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public final class AndUsageRuleArrayDeserializer {
    private AndUsageRuleArrayDeserializer() {
    }

    /**
     * ownerAdder is typed as AndUsageRuleAdderOwner rather than RemedialActionAdder so that this
     * deserializer can be reused both for a RemedialActionAdder's own top-level UsageRules, and,
     * recursively, for the UsageRules combined by an OrUsageRule gate logic combination adder.
     */
    public static void deserialize(JsonParser jsonParser, AndUsageRuleAdderOwner<?> ownerAdder, String version) throws IOException {
        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
            AndUsageRuleAdder<?> adder = ownerAdder.newAndUsageRule();
            while (!jsonParser.nextToken().isStructEnd()) {
                switch (jsonParser.currentName()) {
                    case JsonSerializationConstants.INSTANT:
                        // the instant of an AndUsageRule is derived from its combined UsageRules; this field
                        // is informative only and is simply consumed here.
                        jsonParser.nextTextValue();
                        break;
                    case JsonSerializationConstants.ON_INSTANT_USAGE_RULES:
                        jsonParser.nextToken();
                        OnInstantArrayDeserializer.deserialize(jsonParser, adder, version);
                        break;
                    case JsonSerializationConstants.ON_CONTINGENCY_STATE_USAGE_RULES:
                        jsonParser.nextToken();
                        OnStateArrayDeserializer.deserialize(jsonParser, adder, version);
                        break;
                    case JsonSerializationConstants.ON_CONSTRAINT_USAGE_RULES:
                        jsonParser.nextToken();
                        OnConstraintArrayDeserializer.deserialize(jsonParser, adder, version);
                        break;
                    case JsonSerializationConstants.ON_FLOW_CONSTRAINT_IN_COUNTRY_USAGE_RULES:
                        jsonParser.nextToken();
                        OnFlowConstraintInCountryArrayDeserializer.deserialize(jsonParser, adder, version);
                        break;
                    case JsonSerializationConstants.ON_SWITCH_STATE_USAGE_RULES:
                        jsonParser.nextToken();
                        OnSwitchStateArrayDeserializer.deserialize(jsonParser, adder, version);
                        break;
                    case JsonSerializationConstants.OR_USAGE_RULES:
                        jsonParser.nextToken();
                        OrUsageRuleArrayDeserializer.deserialize(jsonParser, adder, version);
                        break;
                    default:
                        throw new OpenRaoException("Unexpected field in AndUsageRule: " + jsonParser.currentName());
                }
            }
            adder.add();
        }
    }
}
