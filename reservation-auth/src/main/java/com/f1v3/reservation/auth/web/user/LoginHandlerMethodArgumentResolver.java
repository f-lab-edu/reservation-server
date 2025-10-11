package com.f1v3.reservation.auth.web.user;

import com.f1v3.reservation.common.api.error.ErrorCode;
import com.f1v3.reservation.common.api.error.ReservationException;
import com.f1v3.reservation.common.domain.user.enums.UserRole;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Login 어노테이션이 붙은 파라미터에 사용자 정보를 주입하는 Argument Resolver.
 *
 * @author Seungjo, Jeong
 */
@Component
public class LoginHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Login.class) && parameter.getParameterType().equals(LoginUser.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        UserRole userRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                .map(UserRole::valueOf)
                .findFirst()
                .orElseThrow(() -> new ReservationException(ErrorCode.UNAUTHORIZED));

        return new LoginUser(userId, userRole);
    }
}
