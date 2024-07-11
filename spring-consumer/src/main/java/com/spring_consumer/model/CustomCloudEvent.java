package com.spring_consumer.model;

import io.dapr.client.domain.CloudEvent;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CustomCloudEvent<T> extends CloudEvent<T> {

    private String topic;

    private String pubsubname;

    private String traceid;

    private String tracestate;

    private String traceparent;

    private String subject;

}
