package com.f1v3.reservation.common.domain.room.repository;

import com.f1v3.reservation.common.domain.room.RoomTypeStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomTypeStockRepository extends JpaRepository<RoomTypeStock, RoomTypeStock.RoomTypeStockPk> {

    @Query("SELECT s " +
            "FROM RoomTypeStock s " +
            "WHERE s.roomTypeStockPk.roomTypeId = :roomTypeId " +
            "AND s.roomTypeStockPk.targetDate IN :dates"
    )
    List<RoomTypeStock> findAllByRoomTypeIdAndTargetDates(@Param("roomTypeId") Long roomTypeId,
                                                          @Param("dates") List<LocalDate> dates);
}
