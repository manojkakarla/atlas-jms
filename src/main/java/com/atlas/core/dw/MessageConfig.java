package com.atlas.core.dw;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageConfig {
    private String brokerUrl;
    private String username;
    private String password;
    private String inputQueue;
    private String outputQueue;
    private String routeName;
    private int concurrentConsumers = 10;

}
