package com.deliveryhub.application.usecase.user;

import com.deliveryhub.application.port.out.user.UserRepositoryPort;
import com.deliveryhub.domain.enums.UserRole;
import com.deliveryhub.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class GetActiveDeliveriesUseCaseImplTest {

    private UserRepositoryPort userRepositoryPort;
    private GetActiveDeliveriesUseCaseImpl getActiveDeliveriesUseCase;

    @BeforeEach
    void setUp() {
        userRepositoryPort = mock(UserRepositoryPort.class);
        getActiveDeliveriesUseCase = new GetActiveDeliveriesUseCaseImpl(userRepositoryPort);
    }

    @Test
    void execute_whenCalled_shouldReturnActiveDeliveries() {
        // Arrange
        UUID deliveryId = UUID.randomUUID();
        User activeDelivery = User.reconstruct(deliveryId, "Active Rider", "active@delivery.com", "pwd123", UserRole.DELIVERY);
        when(userRepositoryPort.findActiveDeliveries()).thenReturn(List.of(activeDelivery));

        // Act
        List<User> result = getActiveDeliveriesUseCase.execute();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(activeDelivery, result.get(0));
        verify(userRepositoryPort, times(1)).findActiveDeliveries();
    }
}
