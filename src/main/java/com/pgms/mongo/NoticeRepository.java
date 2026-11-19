package com.pgms.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface NoticeRepository extends MongoRepository<Notice, String> {
    List<Notice> findTop20ByOrderByPostedAtDesc();
}
