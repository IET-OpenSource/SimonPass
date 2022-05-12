package it.iet.infrastructure.mongo.entity.user;

import it.iet.infrastructure.mongo.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "User")
@CompoundIndex(def = "{'email': 1, 'tenant': 1}", unique = true)
public class User extends BaseEntity {

    private String firstName;
    private String lastName;
    private String email; // use always email or username could be possible?
    private String password;
    private boolean blocked;
    private boolean mailVerified;
    private Set<Authority> authorities;
    private boolean rememberMe; // check how it works, probably not useful anymore after three way login

    private String tenant = "";

    public void addRole(Authority a) {
        this.authorities.add(a);
    }
}
