package com.f1v3.reservation.auth.web.user;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 로그인한 사용자 정보를 컨트롤러 메서드 파라미터로 주입할 때 사용하는 어노테이션.
 *
 * @author Seungjo, Jeong
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Login {
}
