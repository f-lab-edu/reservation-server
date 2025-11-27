package com.f1v3.reservation.api.reservation;

import com.f1v3.reservation.api.reservation.dto.CreateReservationHoldRequest;
import com.f1v3.reservation.api.reservation.dto.ReservationHoldResponse;
import com.f1v3.reservation.auth.web.user.Login;
import com.f1v3.reservation.auth.web.user.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 임시 예약 API
 *
 * @author Seungjo, Jeong
 */
@RestController
@RequestMapping("/v1/reservation-holds")
@RequiredArgsConstructor
public class ReservationHoldController {

    private final ReservationHoldFacade reservationHoldFacade;

    @PostMapping
    public ReservationHoldResponse createHold(
            @Login LoginUser user,
            @Valid @RequestBody CreateReservationHoldRequest request
    ) {
        return reservationHoldFacade.createReservationHold(user.id(), request);
    }
}
