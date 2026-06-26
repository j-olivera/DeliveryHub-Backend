package com.deliveryhub.application.port.in.user;

import com.deliveryhub.domain.model.User;
import java.util.List;

/**
 * Puerto de entrada que define el caso de uso para obtener todos los repartidores activos.
 */
public interface GetActiveDeliveriesUseCase {
    /**
     * Obtiene la lista de repartidores activos.
     *
     * @return lista de repartidores activos
     */
    List<User> execute();
}
