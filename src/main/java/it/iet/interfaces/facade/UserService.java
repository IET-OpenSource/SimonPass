package it.iet.interfaces.facade;

import it.iet.config.exceptions.impl.AlreadyExistingException;
import it.iet.config.exceptions.impl.SignUpException;
import it.iet.config.security.JwtAuthenticationRequest;
import it.iet.interfaces.facade.dto.user.*;
import org.json.simple.parser.ParseException;
import org.springframework.data.domain.Page;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface UserService {

    boolean saveUser(SignUpDTO signUpDTO, String token) throws AlreadyExistingException, SignUpException;
    LoginServiceDTO saveUserAndLogin(SignUpMailAndPswDTO signUpDTO, String token) throws AlreadyExistingException, SignUpException;
    LoginServiceDTO loginUser(JwtAuthenticationRequest authenticationRequest);
    LoginServiceDTO refreshToken(HttpServletRequest request) throws ParseException;
    List<UserDTO> getAllUsers();
    Page<UserDTO> pageAllUsers(int pageNumber, int pageSize, String sortBy, String sortDirection);
    boolean resetPassword(String email);
    boolean updatePassword(String token, String password);
    boolean updateMailVerification(String token);
    boolean changeUserRole(ChangeRoleDTO changeRoleDTO);
    UserDTO getUser(String id, String header);
    UserDTO updateUser(UpdateUserDTO userDTO, String header);

    boolean logout(String[] tokens, String act);
}
