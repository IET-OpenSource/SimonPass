package it.iet.interfaces.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.iet.config.security.JwtAuthenticationRequest;
import it.iet.infrastructure.mongo.entity.token.TokenType;
import it.iet.interfaces.facade.UserService;
import it.iet.interfaces.facade.dto.user.*;
import it.iet.util.Constants;
import it.iet.util.CookieManager;
import it.iet.util.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthenticationController {

    private final UserService userService;

    @Autowired
    public AuthenticationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/public/ciao")
    public ResponseEntity<ResponseWrapper> ciao() {
        return ResponseWrapper.format("Ciao", () -> "ciao");
    }


    // registration -> save new user in DB
    @PostMapping(value = AuthenticationPaths.REGISTRATION)
    public ResponseEntity<ResponseWrapper> signUp(@RequestHeader(value = "Sign-Token") String token, @RequestBody SignUpDTO signUpDTO) {
        return ResponseWrapper.format(
                "Signed up",
                () -> userService.saveUser(signUpDTO, token)
        );
    }

    //registration with email, password is generated from BE and return tokens as it is already logged in
    @PostMapping(value = AuthenticationPaths.REGISTRATION_ONLY_EMAIL)
    public ResponseEntity<ResponseWrapper> signUpOnlyEmail(@RequestHeader(value = "Sign-Token") String token, @RequestBody SignUpMailAndPswDTO signUpMailAndPswDTO, HttpServletResponse response) {
        return ResponseWrapper.format(
                "Signed up and logged in",
                () -> {
                    final LoginServiceDTO tokens =  userService.saveUserAndLogin(signUpMailAndPswDTO, token);
                    CookieManager.generateAndAssignCookie(response, Constants.ACCESS_TOKEN_COOKIE_NAME, tokens.getAccessToken(), TokenType.ACCESS);
                    CookieManager.generateAndAssignCookie(response, Constants.REFRESH_TOKEN_COOKIE_NAME, tokens.getRefreshToken(), TokenType.REFRESH);
                    if (tokens.getCryptedTriplet() != null) {
                        CookieManager.generateAndAssignCookie(response, Constants.LOGIN_COOKIE_NAME, tokens.getCryptedTriplet(), TokenType.LOGIN);
                    }
                    return true;
                }
        );
    }

    // login -> login in the application
    @PostMapping(value = AuthenticationPaths.LOGIN)
    public ResponseEntity<ResponseWrapper> createAuthenticationToken(@RequestBody JwtAuthenticationRequest authenticationRequest, HttpServletResponse response) {

        return ResponseWrapper.format(
                "logged in",
                () -> {
                    final LoginServiceDTO tokens = userService.loginUser(authenticationRequest);
                    CookieManager.generateAndAssignCookie(response, Constants.ACCESS_TOKEN_COOKIE_NAME, tokens.getAccessToken(), TokenType.ACCESS);
                    CookieManager.generateAndAssignCookie(response, Constants.REFRESH_TOKEN_COOKIE_NAME, tokens.getRefreshToken(), TokenType.REFRESH);
                    if (tokens.getCryptedTriplet() != null) {
                        CookieManager.generateAndAssignCookie(response, Constants.LOGIN_COOKIE_NAME, tokens.getCryptedTriplet(), TokenType.LOGIN);
                    }
                    return true;
                }
        );
    }

    // refresh token -> refresh the access token each time with new one after controls
    @GetMapping(value = AuthenticationPaths.REFRESH_TOKEN)
    public ResponseEntity<ResponseWrapper> refreshAndGetAuthenticationToken(HttpServletRequest request, HttpServletResponse response) {
        return ResponseWrapper.format(
                "token refreshed",
                () -> {
                    final LoginServiceDTO tokens = userService.refreshToken(request);
                    CookieManager.generateAndAssignCookie(response, Constants.ACCESS_TOKEN_COOKIE_NAME, tokens.getAccessToken(), TokenType.ACCESS);
                    CookieManager.generateAndAssignCookie(response, Constants.REFRESH_TOKEN_COOKIE_NAME, tokens.getRefreshToken(), TokenType.REFRESH);
                    return true;
                }
        );
    }


    @GetMapping(value = AuthenticationPaths.GET_ONE_USER)
    @Operation(summary = "Retrieve a single user", security = @SecurityRequirement(name = Constants.andRolesKey, scopes = {Constants.adminRole}))
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<ResponseWrapper> getOneUser(@PathVariable String id, HttpServletRequest request) {
        return ResponseWrapper.format(
                "Retrieve all users",
                () -> userService.getUser(id, request.getHeader(Constants.ACCESS_TOKEN_COOKIE_NAME))
        );
    }


    // forgotPassword -> to send email to request new password
    @PostMapping(value = AuthenticationPaths.PASSWORD_RESET)
    public ResponseEntity<ResponseWrapper> resetPassword(@RequestBody String email) {
        return ResponseWrapper.format(
                "The mail for reset password has been sent",
                () -> userService.resetPassword(email.replaceAll("\"", "")));
    }

    // setNewPassword -> to change effectively the password sent from FE
    @PostMapping(value = AuthenticationPaths.PASSWORD_UPDATE)
    public ResponseEntity<ResponseWrapper> updatePassword(@RequestBody UpdateUserPasswordDTO updateUserPasswordDTO) {
        return ResponseWrapper.format("The password has been modified",
                () -> userService.updatePassword(updateUserPasswordDTO.getToken(), updateUserPasswordDTO.getPassword()));
    }

    // confirm email -> to confirm email after link clicked
    @PostMapping(value = AuthenticationPaths.CONFIRM_EMAIL)
    public ResponseEntity<ResponseWrapper> confirmEmail(@RequestParam("confirmToken") String confirmationToken) {
        return ResponseWrapper.format("The email has been confirmed",
                () -> userService.updateMailVerification(confirmationToken));
    }


    @GetMapping(value = AuthenticationPaths.GET_ALL_USERS)
    @Operation(summary = "Retrieve a user List", security = @SecurityRequirement(name = Constants.andRolesKey, scopes = {Constants.adminRole}))
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ResponseWrapper> getAllUsers() {
        return ResponseWrapper.format(
                "Retrieve all users",
                userService::getAllUsers
        );
    }

    @GetMapping(value = AuthenticationPaths.PAGE_ALL_USERS)
    @Operation(summary = "Retrieve a user List in paged format", security = @SecurityRequirement(name = Constants.andRolesKey, scopes = {Constants.adminRole}))
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ResponseWrapper> getAllUsersPaged(@RequestParam(name = "pageNumber", defaultValue = "0") int pageNumber,
                                                            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
                                                            @RequestParam(name = "sortBy", defaultValue = "firstName") String sortBy,
                                                            @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection) {
        return ResponseWrapper.format(
                "Retrieve all users in paged Format",
                () -> userService.pageAllUsers(pageNumber, pageSize, sortBy, sortDirection)
        );
    }


    @PostMapping(value = AuthenticationPaths.CHANGE_ROLE)
    @Operation(summary = "Change role of the specific user", security = @SecurityRequirement(name = Constants.andRolesKey, scopes = {Constants.adminRole}))
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ResponseWrapper> changeRole(@RequestBody ChangeRoleDTO changeRoleDTO) {
        return ResponseWrapper.format(
                "Change role of the specific user",
                () -> userService.changeUserRole(changeRoleDTO)
        );
    }

    @PostMapping(value = AuthenticationPaths.UPDATE_USER)
    public ResponseEntity<ResponseWrapper> updateUser(@RequestBody UpdateUserDTO userDTO, @RequestHeader(Constants.ACCESS_TOKEN_COOKIE_NAME) String act) {
        return ResponseWrapper.format(
                "Update user",
                () -> userService.updateUser(userDTO, act)
        );
    }

    @PostMapping(value = AuthenticationPaths.LOGOUT)
    public ResponseEntity<ResponseWrapper> logout(@RequestBody DeleteTokensDTO deleteTokensDTO, @RequestHeader(Constants.ACCESS_TOKEN_COOKIE_NAME) String act) {
        return ResponseWrapper.format(
                "Logout user",
                () -> userService.logout(deleteTokensDTO.getTokens(), act)
        );
    }


}
