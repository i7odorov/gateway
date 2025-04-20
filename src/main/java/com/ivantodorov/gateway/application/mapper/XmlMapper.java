package com.ivantodorov.gateway.application.mapper;

import com.ivantodorov.gateway.domain.model.xml.XmlResponse;
import com.ivantodorov.gateway.infrastructure.entity.CurrencyRate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface XmlMapper {

    @Mapping(target = "baseCurrency", expression = "java(getBaseCurrency(rates))")
    XmlResponse toXmlResponse(String currency, List<CurrencyRate> rates);

    default String getBaseCurrency(List<CurrencyRate> rates) {
        return rates != null && !rates.isEmpty() ? rates.get(0).getBaseCurrency() : null;
    }
}
