package com.pgms.mongo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document("notices") @Getter @Setter @NoArgsConstructor
public class Notice {
    @Id private String id;
    private String title, message, postedBy;
    private Instant postedAt;
}
