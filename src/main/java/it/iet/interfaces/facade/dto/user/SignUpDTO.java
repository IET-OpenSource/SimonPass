package it.iet.interfaces.facade.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SignUpDTO extends SignUpMailAndPswDTO {

    private String firstName;
    private String lastName;
    private String tenant = "";

    public SignUpDTO(String firstName, String lastName, String email, String psw, String tenant) {
        super(email, psw);
        this.firstName = firstName;
        this.lastName = lastName;
        this.tenant = tenant;
    }
}
