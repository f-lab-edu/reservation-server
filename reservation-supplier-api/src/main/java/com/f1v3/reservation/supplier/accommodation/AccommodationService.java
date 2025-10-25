package com.f1v3.reservation.supplier.accommodation;

import com.f1v3.reservation.common.api.error.ErrorCode;
import com.f1v3.reservation.common.api.error.ReservationException;
import com.f1v3.reservation.common.domain.accommodation.Accommodation;
import com.f1v3.reservation.common.domain.accommodation.AccommodationStatusHistory;
import com.f1v3.reservation.common.domain.accommodation.enums.AccommodationStatus;
import com.f1v3.reservation.common.domain.accommodation.repository.AccommodationRepository;
import com.f1v3.reservation.common.domain.accommodation.repository.AccommodationStatusHistoryRepository;
import com.f1v3.reservation.common.domain.user.User;
import com.f1v3.reservation.common.domain.user.repository.UserRepository;
import com.f1v3.reservation.supplier.accommodation.dto.AccommodationResponse;
import com.f1v3.reservation.supplier.accommodation.dto.CreateAccommodationRequest;
import com.f1v3.reservation.supplier.accommodation.dto.CreateAccommodationResponse;
import com.f1v3.reservation.supplier.accommodation.dto.UpdateAccommodationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 공급자 숙소 관리 서비스 클래스
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccommodationService {

    private final UserRepository userRepository;
    private final AccommodationRepository accommodationRepository;
    private final AccommodationStatusHistoryRepository historyRepository;

    @Transactional
    public CreateAccommodationResponse create(CreateAccommodationRequest request, Long supplierId) {


        User supplier = userRepository.findById(supplierId)
                .orElseThrow(() -> new ReservationException(ErrorCode.USER_NOT_FOUND, log::info));

        Accommodation accommodation = Accommodation.builder()
                .supplier(supplier)
                .name(request.name())
                .description(request.description())
                .address(request.address())
                .contactNumber(request.contactNumber())
                .thumbnail(request.thumbnail())
                .build();

        Accommodation savedAccommodation = accommodationRepository.save(accommodation);

        AccommodationStatusHistory history = AccommodationStatusHistory.builder()
                .accommodation(savedAccommodation)
                .previousStatus(AccommodationStatus.NONE)
                .newStatus(AccommodationStatus.PENDING)
                .reason("숙소 등록 신청")
                .changedBy(supplier)
                .build();

        historyRepository.save(history);

        return new CreateAccommodationResponse(savedAccommodation.getId());
    }

    public List<AccommodationResponse> findAccommodation(Long supplierId) {
        // fixme: 예외를 던지는게 맞을까 아니면 빈 리스트를 반환하는게 맞을까?
        List<Accommodation> accommodations = accommodationRepository.findBySupplierId(supplierId);

        return accommodations.stream()
                .map(AccommodationResponse::from)
                .toList();
    }

    public AccommodationResponse getDetailAccommodation(Long accommodationId, Long supplierId) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new ReservationException(ErrorCode.ACCOMMODATION_NOT_FOUND, log::info));

        validateOwner(accommodation, supplierId);
        return AccommodationResponse.from(accommodation);
    }

    @Transactional
    public void updateAccommodation(Long accommodationId, UpdateAccommodationRequest request, Long supplierId) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new ReservationException(ErrorCode.ACCOMMODATION_NOT_FOUND, log::info));

        validateOwner(accommodation, supplierId);

        accommodation.update(
                request.name(),
                request.description(),
                request.address(),
                request.contactNumber(),
                request.thumbnail()
        );
    }

    @Transactional
    public void deleteAccommodation(Long accommodationId, Long userId) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new ReservationException(ErrorCode.ACCOMMODATION_NOT_FOUND, log::info));

        validateOwner(accommodation, userId);
        accommodationRepository.delete(accommodation);
    }

    private void validateOwner(Accommodation accommodation, Long userId) {
        if (!accommodation.getSupplier().getId().equals(userId)) {
            throw new ReservationException(ErrorCode.ACCOMMODATION_ACCESS_DENIED, log::info);
        }
    }
}
