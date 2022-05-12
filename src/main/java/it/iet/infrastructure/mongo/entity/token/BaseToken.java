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
@Document(collection = "BaseToken")
public class BaseToken extends BaseEntity implements Serializable {

    private String content;
    private String tokenAssociated;
    private TokenType type;

    // In the content string i have to use jwtUtil to decode it and find all the token fields that i decide to save in
}
