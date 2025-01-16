package com.spring_performance_c.service;

import com.spring_performance_c.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<Message, String> {


}