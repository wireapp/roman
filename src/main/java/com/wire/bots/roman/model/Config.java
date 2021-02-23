//
// Wire
// Copyright (C) 2016 Wire Swiss GmbH
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see http://www.gnu.org/licenses/.
//

package com.wire.bots.roman.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wire.bots.roman.Tools;
import com.wire.lithium.Configuration;
import io.dropwizard.validation.ValidationMethod;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class Config extends Configuration {
    @NotNull
    @JsonProperty
    public String key;

    @NotNull
    @JsonProperty
    public String domain;

    @NotNull
    @NotEmpty
    @JsonProperty
    public String romanPubKeyBase64;

    @ValidationMethod(message = "`romanPubKeyBase64` is not a valid base65")
    @JsonIgnore
    public boolean pubKeyFormatIsNotValid() {
        boolean isValid = romanPubKeyBase64 != null && !romanPubKeyBase64.isEmpty();
        if (isValid) {
            try {
                Tools.getPubKey(this);
            } catch (Exception ignored) {
                isValid = false;
            }
        }
        return isValid;
    }

}
