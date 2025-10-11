package com.f1v3.reservation.api.user;

import com.f1v3.reservation.api.user.dto.SignupUserRequest;
import com.f1v3.reservation.api.user.dto.SignupUserResponse;
import com.f1v3.reservation.common.api.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SignupUserResponse> signup(@Valid @RequestBody SignupUserRequest request) {
        return ApiResponse.success(userService.signup(request));
    }
}
