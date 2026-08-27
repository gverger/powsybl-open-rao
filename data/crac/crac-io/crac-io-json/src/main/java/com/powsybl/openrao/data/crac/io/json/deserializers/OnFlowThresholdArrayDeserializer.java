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
import com.powsybl.openrao.data.crac.api.usagerule.OnFlowThresholdAdder;
import com.powsybl.openrao.data.crac.api.usagerule.OnFlowThresholdAdderOwner;
import com.powsybl.openrao.data.crac.io.json.JsonSerializationConstants;

import java.io.IOException;

/**
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public final class OnFlowThresholdArrayDeserializer {
    private OnFlowThresholdArrayDeserializer() {
    }

    /**
     * ownerAdder can only be an AndUsageRuleAdder or an OrUsageRuleAdder: a RemedialActionAdder does not
     * implement OnFlowThresholdAdderOwner, so a standalone OnFlowThreshold UsageRule directly on a
     * remedial action cannot be built or read back.
     */
    public static void deserialize(JsonParser jsonParser, OnFlowThresholdAdderOwner<?> ownerAdder, String version) throws IOException {
        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
            OnFlowThresholdAdder<?> adder = ownerAdder.newOnFlowThresholdUsageRule();
            while (!jsonParser.nextToken().isStructEnd()) {
                switch (jsonParser.currentName()) {
                    case JsonSerializationConstants.INSTANT:
                        adder.withInstant(jsonParser.nextTextValue());
                        break;
                    case JsonSerializationConstants.CNEC_ID:
                        adder.withCnec(jsonParser.nextTextValue());
                        break;
                    case JsonSerializationConstants.SIDE:
                        jsonParser.nextToken();
                        adder.withSide(JsonSerializationConstants.deserializeSide(jsonParser.getIntValue()));
                        break;
                    case JsonSerializationConstants.UNIT:
                        adder.withUnit(JsonSerializationConstants.deserializeUnit(jsonParser.nextTextValue()));
                        break;
                    case JsonSerializationConstants.AT_LEAST:
                        jsonParser.nextToken();
                        adder.withAtLeast(jsonParser.getDoubleValue());
                        break;
                    case JsonSerializationConstants.AT_MOST:
                        jsonParser.nextToken();
                        adder.withAtMost(jsonParser.getDoubleValue());
                        break;
                    default:
                        throw new OpenRaoException("Unexpected field in OnFlowThreshold: " + jsonParser.currentName());
                }
            }
            adder.add();
        }
    }
}
