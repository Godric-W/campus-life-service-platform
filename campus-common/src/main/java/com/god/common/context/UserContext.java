package com.god.common.context;

import com.god.common.dto.LoginUserDTO;

public final class UserContext {

    private static final ThreadLocal<LoginUserDTO> USER_HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setUser(LoginUserDTO loginUser) {
        USER_HOLDER.set(loginUser);
    }

    public static LoginUserDTO getUser() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        LoginUserDTO loginUser = getUser();
        return loginUser == null ? null : loginUser.getUserId();
    }

    public static String getUsername() {
        LoginUserDTO loginUser = getUser();
        return loginUser == null ? null : loginUser.getUsername();
    }

    public static String getRole() {
        LoginUserDTO loginUser = getUser();
        return loginUser == null ? null : loginUser.getRole();
    }

    public static void clear() {
        USER_HOLDER.remove();
    }
}
