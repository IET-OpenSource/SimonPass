package it.iet.infrastructure.mongo.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "SecurityTriplet")
public class SecurityTriplet extends BaseEntity {

    private String userId;
    private String token;
    private String identifier;

}
