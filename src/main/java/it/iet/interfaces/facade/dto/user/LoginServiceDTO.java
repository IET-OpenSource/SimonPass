package it.iet.interfaces.facade.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginServiceDTO {

    String accessToken;
    String refreshToken;
    String cryptedTriplet;
}
