package com.atlas.core.dw;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;

@Data
@EqualsAndHashCode(callSuper = true)
public class JmsConfig extends AppConfig {

    @Valid
    private MessageConfig jms = new MessageConfig();
}
