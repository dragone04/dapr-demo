package com.spring.service.mongo.repository;

import com.spring.service.mongo.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<Message, String> {


}