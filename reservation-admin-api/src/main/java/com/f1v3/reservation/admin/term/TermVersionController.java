package com.f1v3.reservation.admin.term;

import com.f1v3.reservation.admin.term.dto.CreateTermVersionRequest;
import com.f1v3.reservation.admin.term.dto.CreateTermVersionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 약관 버전 API 컨트롤러
 *
 * @author Seungjo, Jeong
 */
@RestController
@RequestMapping("/v1/admin/terms/{termId}/versions")
@RequiredArgsConstructor
public class TermVersionController {

    private final TermVersionService termVersionService;

    @PostMapping
    public ResponseEntity<CreateTermVersionResponse> createTermVersion(@PathVariable Long termId,
                                                                       @Valid @RequestBody CreateTermVersionRequest request) {

        CreateTermVersionResponse response = termVersionService.create(termId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
