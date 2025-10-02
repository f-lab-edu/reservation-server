package com.f1v3.reservation.api.user;

import com.f1v3.reservation.api.user.dto.SignupUserRequest;
import com.f1v3.reservation.api.user.dto.SignupUserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원 API 컨트롤러
 *
 * @author Seungjo, Jeong
 */
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<SignupUserResponse> signup(@Valid @RequestBody SignupUserRequest request) {
        SignupUserResponse response = userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
