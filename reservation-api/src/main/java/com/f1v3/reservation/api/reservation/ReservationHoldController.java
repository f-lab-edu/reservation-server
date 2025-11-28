package com.f1v3.reservation.api.reservation;

import com.f1v3.reservation.api.reservation.dto.CreateReservationHoldRequest;
import com.f1v3.reservation.api.reservation.dto.ReservationHoldResponse;
import com.f1v3.reservation.auth.web.user.Login;
import com.f1v3.reservation.auth.web.user.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/{holdKey}/confirm")
    public void confirmHold(
            @PathVariable String holdKey,
            @Login LoginUser user
    ) {
        reservationHoldFacade.confirmReservationHold(holdKey, user.id());
    }
}
