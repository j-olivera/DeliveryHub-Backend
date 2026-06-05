# Reglas de dominio — DeliveryHub

## Reglas de negocio para la 
1. Solo puede asignarse un pedido a un delivery
2. No puede haber mas de un usuario con el mismo nombre
3. Para asignar nuevos pedidos el delivery debe haber despachado todos los pedidos asignados o haber marcado como cancelados los pedidos que no pudo completar. (Reasignación) (revisar logica)
4. Los pedidos en estado de asigando, in_progress o deliver no pueden ser modificados.
 
## Reglas de order
1. Se muestran los pedidos separados por estado (PENDING, ASSIGNED, IN_PROGRESS, DELIVERED, CANCELLED) correspondiente al rol, para el caso de ADMIN se muestran todos los pedidos, para el caso de DELIVERY se muestran solo los pedidos asignados.
2. Se ordenan segun la fecha de encargo, de menor antiguedad a mayor.
3. Si no hay pedidos segun su estado por mostrar, se omite la proyección.
## Roles

| Rol      | Descripción                                      |
|----------|--------------------------------------------------|
| ADMIN    | Gestión completa del sistema                     |
| DELIVERY | Vista y acciones acotadas a sus pedidos asignados|

## Máquina de estados del pedido

```
PENDING → ASSIGNED → IN_PROGRESS → DELIVERED
                  ↘                ↗
                   CANCELLED (solo ADMIN, desde cualquier estado no terminal)
```

### Transiciones válidas

| Desde       | Hacia       | Quién puede hacerlo     | Condición adicional                  |
|-------------|-------------|-------------------------|--------------------------------------|
| PENDING     | ASSIGNED    | ADMIN                   | El repartidor debe existir y estar activo |
| ASSIGNED    | IN_PROGRESS | DELIVERY                | Solo el delivery asignado al pedido  |
| IN_PROGRESS | DELIVERED   | DELIVERY                | Solo el delivery asignado al pedido  |
| Cualquiera  | CANCELLED   | ADMIN                   | No aplica si ya está en DELIVERED    |

### Transiciones inválidas (lanzar `InvalidStatusTransitionException`)
- PENDING → IN_PROGRESS (salto de estado)
- PENDING → DELIVERED (salto de estado)
- DELIVERED → cualquier estado (estado terminal)
- CANCELLED → cualquier estado (estado terminal)
- ASSIGNED → CANCELLED por DELIVERY (solo ADMIN puede cancelar)

## Reglas de visibilidad

- **ADMIN** ve todos los pedidos sin filtro.
- **DELIVERY** solo ve los pedidos donde `deliveryUserId = usuarioAutenticado.id`.
- El filtro se aplica en el caso de uso, no en el controller ni en la query directamente.

## Reglas de asignación

- Solo se puede asignar un pedido en estado PENDING.
- Un pedido solo puede tener un repartidor asignado a la vez.
- No existe reasignación directa: hay que cancelar y crear un nuevo pedido si aplica.

## Reglas de ownership

Cuando un DELIVERY intenta transicionar un pedido, el caso de uso verifica:
```
pedido.deliveryUserId == usuarioAutenticado.id
```
Si no coincide → `UnauthorizedTransitionException` → HTTP 403.

## Invariantes que nunca pueden violarse

1. Un pedido DELIVERED no puede modificarse bajo ninguna circunstancia.
2. Un pedido CANCELLED no puede modificarse bajo ninguna circunstancia.
3. No existe endpoint que exponga pedidos de otro repartidor a un DELIVERY.
4. El ADMIN no puede marcar un pedido como IN_PROGRESS ni DELIVERED.