package it.iet.interfaces.facade.dto.user;

import it.iet.interfaces.facade.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SignUpMailAndPswDTO extends BaseDTO {

    private String email;
    private String password;
}
