package it.iet.interfaces.facade.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.iet.infrastructure.mongo.entity.user.Authority;
import it.iet.interfaces.facade.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO extends BaseDTO {

    private String firstName;
    private String lastName;
    private String email;
    private String mailVerified;
    @JsonIgnore
    private String password;

    private boolean blocked;
    private Set<Authority> authorities;
    private boolean rememberMe;
}
