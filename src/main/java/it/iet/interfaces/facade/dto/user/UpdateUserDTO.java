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
public class UpdateUserDTO extends BaseDTO {

    private String firstName;
    private String lastName;
    private String email;
}
