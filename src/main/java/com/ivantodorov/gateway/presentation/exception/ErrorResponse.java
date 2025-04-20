package com.ivantodorov.gateway.presentation.exception;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "error")
public class ErrorResponse {
    private String code;
    private String message;
}