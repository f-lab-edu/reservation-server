package com.f1v3.reservation.admin.term;

import com.f1v3.reservation.admin.term.dto.TermResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 관리자 약관 API 컨트롤러
 *
 * @author Seungjo, Jeong
 */
@RestController
@RequestMapping("/v1/admin/terms")
@RequiredArgsConstructor
public class TermController {

    private final TermService termService;

    /**
     * 약관 리스트 조회 (페이지네이션 적용)
     *
     * @param pageable default page = 0, size = 10
     */
    @GetMapping
    public ResponseEntity<List<TermResponse>> getPagedTerms(@PageableDefault Pageable pageable) {
        return ResponseEntity.ok(termService.getPagedTerms(pageable));
    }
}
