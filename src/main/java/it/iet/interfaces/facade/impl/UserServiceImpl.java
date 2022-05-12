package it.iet.interfaces.facade.impl;

import it.iet.config.exceptions.impl.*;
import it.iet.config.security.JwtAuthenticationRequest;
import it.iet.config.security.JwtTokenUtil;
import it.iet.config.security.PublicRequestTokenValidator;
import it.iet.infrastructure.mongo.entity.token.TokenType;
import it.iet.infrastructure.mongo.entity.token.UtilityToken;
import it.iet.infrastructure.mongo.entity.user.Authority;
import it.iet.infrastructure.mongo.entity.user.User;
import it.iet.interfaces.facade.EmailService;
import it.iet.interfaces.facade.UserService;
import it.iet.interfaces.facade.dto.user.*;
import it.iet.interfaces.facade.mapper.SignupBidirectionalMapper;
import it.iet.interfaces.facade.mapper.UserBidirectionalMapper;
import it.iet.model.repository.BaseTokenRepository;
import it.iet.model.repository.SecurityTripletRepository;
import it.iet.model.repository.UserRepository;
import it.iet.model.repository.UtilityTokenRepository;
import it.iet.util.Constants;
import it.iet.util.CookieManager;
import it.iet.util.PasswordValidation;
import it.iet.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BaseTokenRepository tokenRepository, UtilityTokenRepository utilityTokenRepository, SecurityTripletRepository securityTripletRepository, UserBidirectionalMapper userBidirectionalMapper, SignupBidirectionalMapper signupBidirectionalMapper, PasswordEncoder passwordEncoder, JwtTokenUtil jwtTokenUtil, UserDetailsService userDetailsService, PublicRequestTokenValidator publicRequestTokenValidator, PasswordValidation passwordValidation, EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.utilityTokenRepository = utilityTokenRepository;
        this.securityTripletRepository = securityTripletRepository;
        this.userBidirectionalMapper = userBidirectionalMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.publicRequestTokenValidator = publicRequestTokenValidator;
        this.passwordValidation = passwordValidation;
        this.emailService = emailService;
        this.signupBidirectionalMapper = signupBidirectionalMapper;
    }

    private final UserRepository userRepository;
    private final BaseTokenRepository tokenRepository;
    private final UtilityTokenRepository utilityTokenRepository;
    private final SecurityTripletRepository securityTripletRepository;

    private final UserBidirectionalMapper userBidirectionalMapper;
    private final SignupBidirectionalMapper signupBidirectionalMapper;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;
    private final PublicRequestTokenValidator publicRequestTokenValidator;

    private final PasswordEncoder passwordEncoder;
    private final PasswordValidation passwordValidation;

    private final EmailService emailService;

    @Override
    public boolean saveUser(SignUpDTO signUpDTO, String token) throws AlreadyExistingException, SignUpException {
        // due to the fact that this API is public it is possible to call it everytime and create a huge amount of users,
        // so we implement this mechanism with valid static tokens
        if (publicRequestTokenValidator.isValidToken(token)) {
            if (passwordValidation.validPassword(signUpDTO.getPassword())) {
                return attemptToSave(createNewUser(signUpDTO));
            } else {
                throw new PasswordNotValidException(Constants.PASSWORD_NOT_VALID, HttpStatus.BAD_REQUEST);
            }
        } else {
            throw new SignUpException(Constants.ACCESS_DENIED, HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public LoginServiceDTO saveUserAndLogin(SignUpMailAndPswDTO signUpDTO, String token) throws AlreadyExistingException, SignUpException {
        if (publicRequestTokenValidator.isValidToken(token)) {
            var psw = UUID.randomUUID().toString();
            signUpDTO.setPassword(psw);
            var u = createNewUser(signUpDTO);
            final boolean condition = attemptToSave(u);
            if (condition) {
                var jwtAuthenticationRequest = new JwtAuthenticationRequest(u.getEmail(), psw);
                return this.loginUser(jwtAuthenticationRequest);
            } else {
                throw new SignUpException("Cannot save user, internal error", HttpStatus.UNAUTHORIZED);
            }
        } else {
            throw new SignUpException("Invalid public token", HttpStatus.UNAUTHORIZED);
        }

    }

    @Override
    public LoginServiceDTO loginUser(JwtAuthenticationRequest authenticationRequest) {
        // If I am making a simple login with email and password I don't need particular controls. Check them and generate access token
        final var authentication = jwtTokenUtil.getAuthentication(authenticationRequest);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        final var userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getEmail());

        // Create access and refresh token
        String accessToken = jwtTokenUtil.generateToken(userDetails, TokenType.ACCESS);
        String refreshToken = jwtTokenUtil.generateToken(userDetails, TokenType.REFRESH);

        tokenRepository.saveToken(accessToken, refreshToken, TokenType.ACCESS);
        tokenRepository.saveToken(refreshToken, accessToken, TokenType.REFRESH);

        var loginServiceDTO = new LoginServiceDTO();
        loginServiceDTO.setAccessToken(accessToken);
        loginServiceDTO.setRefreshToken(refreshToken);

        // If remember me create also the triplet
        if (authenticationRequest.isRememberMe()) {
            var secret = Utils.generateCryptString(UUID.randomUUID().toString());
            String loginToken = jwtTokenUtil.generateToken(userDetails, TokenType.LOGIN);
            var userFromDb = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            if (userFromDb != null) {
                var tripletSaved = securityTripletRepository.createTriplet(userFromDb.get_id(), loginToken, secret);
                String tripletToken = jwtTokenUtil.generateLoginToken(userDetails, tripletSaved);
                loginServiceDTO.setCryptedTriplet(tripletToken);
            } else {
                throw new UserNotFoundException(Constants.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }
        }

        return loginServiceDTO;
    }

    @Override
    public LoginServiceDTO refreshToken(HttpServletRequest request) {
        var cookieTokens = new LoginServiceDTO();

        var accessToken = CookieManager.retrieveCookie(request, Constants.ACCESS_TOKEN_COOKIE_NAME);
        var refreshToken = CookieManager.retrieveCookie(request, Constants.REFRESH_TOKEN_COOKIE_NAME);
        var loginToken = CookieManager.retrieveCookie(request, Constants.LOGIN_COOKIE_NAME);

        if (accessToken == null && refreshToken == null) {
            throw new TokenIntegrityException("Token not found", HttpStatus.BAD_REQUEST);
        } else {
            cookieTokens.setAccessToken(Objects.requireNonNull(accessToken).getValue());
            cookieTokens.setRefreshToken(Objects.requireNonNull(refreshToken).getValue());
        }
        if (loginToken != null)
            cookieTokens.setCryptedTriplet(loginToken.getValue());

        LoginServiceDTO answerDTO = jwtTokenUtil.refreshToken(cookieTokens.getAccessToken(), cookieTokens.getRefreshToken()); // this method check the refresh token and, if it is valid it generates a pair with new access token and updated refresh token
        tokenRepository.saveToken(answerDTO.getAccessToken(), answerDTO.getRefreshToken(), TokenType.ACCESS);
        //in this place I create the new access token because the refresh token was just updated in the refreshToken method
        return answerDTO;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAllUsers().stream().map(userBidirectionalMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Page<UserDTO> pageAllUsers(int pageNumber, int pageSize, String sortBy, String sortDirection) {
        var paging = Utils.makePageableObject(pageNumber, pageSize, sortBy, sortDirection);
        return userRepository.findAllUsersPaged(paging).map(userBidirectionalMapper::toDto);
    }

    @Override
    public boolean resetPassword(String email) {
        Optional<User> userFromDb = userRepository.findByEmail(email);
        if (userFromDb.isPresent()) {
            var user = userFromDb.get();
            var utilityToken = new UtilityToken();
            utilityToken.setUserId(user.get_id());
            utilityToken.setToken(UUID.randomUUID().toString());
            utilityToken.setType(TokenType.RESET_PASSWORD);
            utilityTokenRepository.save(utilityToken);
            emailService.sendEmail(user.getEmail(), Constants.RESET_PASSWORD_SUBJECT, Constants.RESET_PASSWORD_BODY + utilityToken.getToken());
            return true;
        } else {
            throw new UserNotFoundException(Constants.USER_NOT_FOUND, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @Override
    public boolean updatePassword(String token, String password) {
        Optional<UtilityToken> opt = utilityTokenRepository.findByTokenAndType(token, TokenType.RESET_PASSWORD);
        if (opt.isEmpty()) {
            throw new TokenNotFoundException("Failed to change password", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        var utilityToken = opt.get();
        if (calculateTimeDifference(utilityToken.getCreationDate().getTime()) > Constants.RESET_PASSWORD_TOKEN_EXPIRATION || !utilityToken.getDeleted().equals(new Date(0))) {
            throw new TokenExpiredException("Time out to change password", HttpStatus.REQUEST_TIMEOUT);
        }
        Optional<User> optionalUser = userRepository.findbyId(utilityToken.getUserId());
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException(Constants.USER_NOT_FOUND, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        var user = optionalUser.get();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        utilityToken.setDeleted(new Date());
        utilityTokenRepository.save(utilityToken);
        return true;
    }

    @Override
    public boolean updateMailVerification(String token) {
        Optional<UtilityToken> opt = utilityTokenRepository.findByTokenAndType(token, TokenType.MAIL_VERIFICATION);
        if (opt.isEmpty()) {
            throw new TokenNotFoundException("Failed to verify email", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        var utilityToken = opt.get();
        if (calculateTimeDifference(utilityToken.getCreationDate().getTime()) > Constants.MAIL_VERIFICATION_TOKEN_EXPIRATION || !utilityToken.getDeleted().equals(new Date(0))) {
            throw new TokenExpiredException("Time out to verify email", HttpStatus.REQUEST_TIMEOUT);
        }
        Optional<User> optionalUser = userRepository.findbyId(utilityToken.getUserId());
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException(Constants.USER_NOT_FOUND, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        var user = optionalUser.get();
        user.setMailVerified(true);
        userRepository.save(user);
        utilityTokenRepository.deleteToken(utilityToken.getToken(), TokenType.MAIL_VERIFICATION);
        return true;
    }

    @Override
    public boolean changeUserRole(ChangeRoleDTO changeRoleDTO) {
        Optional<User> userFromDb = userRepository.findByEmail(changeRoleDTO.getUserMail());
        if (userFromDb.isPresent()) {
            var user = userFromDb.get();
            var authority = Utils.containsInAuthorityEnum(changeRoleDTO.getRole());
            if (authority != null) {
                user.addRole(authority);
                userRepository.save(user);
                return true;
            } else {
                throw new RoleNotFoundException(Constants.ROLE_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
        } else {
            throw new UserNotFoundException(Constants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public UserDTO getUser(String id, String header) {
        if (header == null || !jwtTokenUtil.getUserIdFromToken(header).equals(id))
            throw new BadCredentialsException("Bad request", HttpStatus.BAD_REQUEST);
        Optional<User> optionalUser = userRepository.findbyId(id);
        if (optionalUser.isPresent())
            return userBidirectionalMapper.toDto(optionalUser.get());
        throw new UserNotFoundException("User not found", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public UserDTO updateUser(UpdateUserDTO userDTO, String header) {
        if (header == null) {
            throw new TokenNotFoundException("Token not valid or not present", HttpStatus.BAD_REQUEST);
        } else {
            var claims = jwtTokenUtil.getClaimsFromToken(header);
            String idFromToken = claims.getSubject();
            if (idFromToken == null) {
                throw new TokenNotFoundException("Id not found", HttpStatus.BAD_REQUEST);
            } else {
                Optional<User> optionalUser = userRepository.findbyId(idFromToken);
                if (optionalUser.isEmpty()) {
                    throw new UserNotFoundException("User with specified id not found", HttpStatus.BAD_REQUEST);
                } else {
                    var userFromDb = userBidirectionalMapper.fromUpdate(userDTO, optionalUser.get());
                    userRepository.save(userFromDb);
                    return userBidirectionalMapper.toDto(userFromDb);
                }
            }
        }
    }

    @Override
    public boolean logout(String[] tokens, String act) {
        if (tokens.length > 0 && tokens[0].equals(act)) {
            for (String s : tokens) {
                tokenRepository.deleteTokenByContent(s);
            }
            return true;
        }
        return false;
    }

    private long calculateTimeDifference(long tokenTime) {
        var actualTime = new Timestamp(System.currentTimeMillis());
        return TimeUnit.MILLISECONDS.toSeconds(actualTime.getTime() - tokenTime);
    }

    private User createNewUser(SignUpDTO signUpDTO) {
        var user = signupBidirectionalMapper.toEntity(signUpDTO);
        user.setPassword(passwordEncoder.encode(signUpDTO.getPassword()));
        user.setBlocked(false);
        Set<Authority> authoritySet = new HashSet<>(Collections.singletonList(Authority.ROLE_USER));
        user.setAuthorities(authoritySet);
        return user;
    }

    private User createNewUser(SignUpMailAndPswDTO signUpMailAndPswDTO) {
        SignUpDTO signUpDTO = new SignUpDTO();
        signUpDTO.setEmail(signUpMailAndPswDTO.getEmail());
        signUpDTO.setPassword(signUpMailAndPswDTO.getPassword());
        return createNewUser(signUpDTO);
    }

    private boolean attemptToSave(User user) {
        user.set_id(Utils.generateCryptId());
        try {
            userRepository.save(user);
            this.sendVerificationMail(user.getEmail(), user.get_id());
        } catch (DuplicateKeyException duplicateKeyException) {
            String tenant = user.getTenant();
            if (tenant != null && tenant.length() > 0) {
                tenant = " in tenant: " + tenant + ".";
            } else {
                tenant = ".";
            }
            throw new AlreadyExistingException(Constants.USER_ALREADY_EXISTS + tenant, HttpStatus.BAD_REQUEST);
        }
        return true;
    }


    private void sendVerificationMail(String userMail, String userId) {
        var utilityToken = new UtilityToken();
        utilityToken.setUserId(userId);
        utilityToken.setToken(UUID.randomUUID().toString());
        utilityToken.setType(TokenType.MAIL_VERIFICATION);
        utilityTokenRepository.save(utilityToken);
        emailService.sendEmail(userMail, Constants.MAIL_VERIFICATION_SUBJECT, Constants.MAIL_VERIFICATION_BODY + utilityToken.getToken());
    }
}

