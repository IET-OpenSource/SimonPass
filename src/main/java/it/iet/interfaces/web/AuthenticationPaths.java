package it.iet.interfaces.web;

public class AuthenticationPaths {


    private AuthenticationPaths(){}

    public static final String REGISTRATION = "/public/signup";
    public static final String REGISTRATION_ONLY_EMAIL = "/public/signup-email";
    public static final String LOGIN = "/public/login";
    public static final String LOGOUT = "/public/logout";
    public static final String REFRESH_TOKEN = "/public/refresh-token";
    public static final String PASSWORD_RESET ="/public/reset-password";
    public static final String PASSWORD_UPDATE = "/public/update-password";
    public static final String CONFIRM_EMAIL = "/public/confirm-email";
    public static final String GET_ALL_USERS = "/user/admin/get-all";
    public static final String PAGE_ALL_USERS = "/user/admin/page-all";
    public static final String CHANGE_ROLE = "/user/admin/change-role";
    public static final String GET_ONE_USER = "/user/{id}";
    public static final String UPDATE_USER = "/update-user";
}
