package it.iet.infrastructure.mongo.entity.token;

import it.iet.infrastructure.mongo.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "UtilityToken")
public class UtilityToken extends BaseEntity implements Serializable {

    private String userId;
    private String token;
    private TokenType type;
}
