package com.spring.service.scheduler.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExternalServiceObject {

    public String userId;
    public String id;
    public String title;
    public Boolean completed;

}