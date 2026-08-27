/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.io.json.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.powsybl.openrao.data.crac.api.usagerule.OnFlowThreshold;
import com.powsybl.openrao.data.crac.io.json.JsonSerializationConstants;

import java.io.IOException;
import java.util.Optional;

/**
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public class OnFlowThresholdSerializer extends AbstractJsonSerializer<OnFlowThreshold> {
    @Override
    public void serialize(OnFlowThreshold value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField(JsonSerializationConstants.INSTANT, value.getInstant().getId());
        gen.writeStringField(JsonSerializationConstants.CNEC_ID, value.getCnec().getId());
        gen.writeNumberField(JsonSerializationConstants.SIDE, JsonSerializationConstants.serializeSide(value.getSide()));
        gen.writeStringField(JsonSerializationConstants.UNIT, JsonSerializationConstants.serializeUnit(value.getUnit()));
        Optional<Double> atLeast = value.atLeast();
        if (atLeast.isPresent()) {
            gen.writeNumberField(JsonSerializationConstants.AT_LEAST, atLeast.get());
        }
        Optional<Double> atMost = value.atMost();
        if (atMost.isPresent()) {
            gen.writeNumberField(JsonSerializationConstants.AT_MOST, atMost.get());
        }
        gen.writeEndObject();
    }
}
