package com.f1v3.reservation.batch.reservation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 만료된 임시예약을 1분 간격으로 복원/정리한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationHoldExpireScheduler {

    private final ReservationHoldExpireService reservationHoldExpireService;

    @Scheduled(cron = "0 * * * * *")
    public void restoreExpiredHolds() {
        reservationHoldExpireService.restoreExpiredHolds();
    }
}
