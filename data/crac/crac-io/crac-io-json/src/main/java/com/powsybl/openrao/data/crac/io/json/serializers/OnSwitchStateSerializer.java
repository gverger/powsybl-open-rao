/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.openrao.data.crac.io.json.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.powsybl.openrao.data.crac.api.usagerule.OnSwitchState;
import com.powsybl.openrao.data.crac.io.json.JsonSerializationConstants;

import java.io.IOException;

/**
 * @author Guillaume Verger {@literal <guillaume.verger at artelys.com>}
 */
public class OnSwitchStateSerializer extends AbstractJsonSerializer<OnSwitchState> {
    @Override
    public void serialize(OnSwitchState value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField(JsonSerializationConstants.INSTANT, value.getInstant().getId());
        gen.writeStringField(JsonSerializationConstants.NETWORK_ELEMENT_ID, value.getSwitchElement().getId());
        gen.writeBooleanField(JsonSerializationConstants.EXPECTED_OPEN, value.isExpectedOpen());
        gen.writeEndObject();
    }
}
