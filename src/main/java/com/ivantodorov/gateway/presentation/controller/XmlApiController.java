package com.ivantodorov.gateway.presentation.controller;

import com.ivantodorov.gateway.domain.model.xml.XmlRequest;
import com.ivantodorov.gateway.domain.model.xml.XmlResponse;
import com.ivantodorov.gateway.application.service.XmlApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/xml")
@RequiredArgsConstructor
public class XmlApiController {

    private final XmlApiService xmlApiService;

    @PostMapping(
            value = "/command",
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    public XmlResponse handleXml(@RequestBody XmlRequest request) {
        return xmlApiService.process(request);
    }
}
