package com.ivantodorov.gateway.application.mapper;

import com.ivantodorov.gateway.domain.model.rabbitmq.RequestEventMessage;
import com.ivantodorov.gateway.infrastructure.entity.RequestLog;
import com.ivantodorov.gateway.infrastructure.entity.RequestTracker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface TrackingMapper {

    @Mappings({
            @Mapping(source = "serviceId", target = "sourceService"),
            @Mapping(source = "requestId", target = "requestId"),
            @Mapping(target = "receivedAt", expression = "java(java.time.LocalDateTime.now())")
    })
    RequestTracker toRequestTracker(String serviceId, String requestId);

    @Mappings({
            @Mapping(source = "serviceId", target = "service"),
            @Mapping(source = "requestId", target = "requestId"),
            @Mapping(source = "client", target = "client"),
            @Mapping(source = "timestamp", target = "timestamp")
    })
    RequestEventMessage toRequestEventMessage(String serviceId, String requestId, String client, LocalDateTime timestamp);

    @Mappings({
            @Mapping(source = "serviceId", target = "serviceId"),
            @Mapping(source = "requestId", target = "requestId"),
            @Mapping(source = "client", target = "client"),
            @Mapping(source = "timestamp", target = "timestamp")
    })
    RequestLog toRequestLog(String serviceId, String requestId, String client, LocalDateTime timestamp);
}
