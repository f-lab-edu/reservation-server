package com.f1v3.reservation.api.term;

import com.f1v3.reservation.api.term.dto.TermResponse;
import com.f1v3.reservation.common.api.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 사용자 약관 API 컨트롤러
 *
 * @author Seungjo, Jeong
 */
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class TermController {

    private final TermService termService;

    /**
     * 회원가입을 위한 활성 약관 조회
     */
    @GetMapping("/terms/active")
    public ApiResponse<List<TermResponse>> getActiveTerms() {
        return ApiResponse.success(termService.getActiveTerms());
    }
}
