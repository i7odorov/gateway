package com.ivantodorov.gateway.domain.model;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ErrorInfo {

    private int code;
    private String type;
    private String info;
}
