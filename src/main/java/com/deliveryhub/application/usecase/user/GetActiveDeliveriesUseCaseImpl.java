package com.deliveryhub.application.usecase.user;

import com.deliveryhub.application.port.in.user.GetActiveDeliveriesUseCase;
import com.deliveryhub.application.port.out.user.UserRepositoryPort;
import com.deliveryhub.domain.model.User;

import java.util.List;

/**
 * Implementación del caso de uso para obtener todos los repartidores activos.
 */
public class GetActiveDeliveriesUseCaseImpl implements GetActiveDeliveriesUseCase {

    private final UserRepositoryPort userRepositoryPort;

    /**
     * Constructor del caso de uso.
     *
     * @param userRepositoryPort puerto de persistencia de usuarios
     */
    public GetActiveDeliveriesUseCaseImpl(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    /**
     * Obtiene la lista de repartidores activos.
     *
     * @return lista de repartidores activos
     */
    @Override
    public List<User> execute() {
        return userRepositoryPort.findActiveDeliveries();
    }
}
