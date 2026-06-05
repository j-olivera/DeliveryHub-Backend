# WebSockets — DeliveryHub

## Stack
STOMP over SockJS. Spring WebSocket en el backend, `@stomp/stompjs` + `sockjs-client` en Angular.

## Flujo de conexión

```
Cliente Angular
    ↓
SockJS handshake (HTTP upgrade)
    ↓
JwtHandshakeInterceptor (valida token JWT)
    ↓
Conexión STOMP establecida
    ↓
Cliente se suscribe a su topic
```

## Topics

| Topic                          | Quién se suscribe | Qué recibe                              |
|-------------------------------|-------------------|-----------------------------------------|
| `/topic/admin/orders`         | ADMIN             | Cualquier cambio de estado en cualquier pedido |
| `/user/{userId}/orders`       | DELIVERY          | Solo cambios en sus pedidos asignados   |

El prefijo `/user/` es manejado automáticamente por Spring para ruteo por usuario autenticado.

## Eventos que disparan mensajes WebSocket

Todos los eventos se publican desde los casos de uso en Application, usando un port de salida:

```java
// application/port/out/
public interface OrderEventPublisherPort {
    void publishOrderUpdated(Order order);
}
```

Eventos que disparan publicación:
- Pedido creado → notifica a ADMIN
- Pedido asignado → notifica a ADMIN + al DELIVERY asignado
- Pedido actualizado (IN_PROGRESS, DELIVERED) → notifica a ADMIN
- Pedido cancelado → notifica a ADMIN + al DELIVERY si tenía uno asignado

## Payload de los mensajes

```json
{
  "eventType": "ORDER_STATUS_UPDATED",
  "orderId": "uuid",
  "newStatus": "IN_PROGRESS",
  "deliveryUserId": "uuid",
  "timestamp": "2024-01-15T11:00:00Z"
}
```

## Configuración backend (referencia)

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Broker en memoria. Para producción: RabbitMQ o ActiveMQ
        config.enableSimpleBroker("/topic", "/user");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
            .addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }
}
```

## Configuración frontend Angular (referencia)

```typescript
// websocket.service.ts — en infrastructure/websocket/
@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private client: Client;
  private orderUpdates$ = new Subject<OrderEvent>();

  connect(token: string): void {
    this.client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      connectHeaders: { Authorization: `Bearer ${token}` },
      onConnect: () => this.subscribeToTopics(),
    });
    this.client.activate();
  }

  private subscribeToTopics(): void {
    // El backend resuelve automáticamente /user/{userId}/orders
    // según el usuario autenticado en el token
    this.client.subscribe('/user/orders', (message) => {
      this.orderUpdates$.next(JSON.parse(message.body));
    });
  }

  getOrderUpdates(): Observable<OrderEvent> {
    return this.orderUpdates$.asObservable();
  }
}
```

## Reglas de uso

- La conexión WebSocket se establece después del login exitoso y se cierra en logout.
- El JWT se valida en el handshake. Si expira durante la sesión, el cliente debe reconectar con el token renovado.
- Los mensajes WebSocket son solo notificaciones. El cliente siempre hace una llamada REST para obtener el estado completo actualizado.
- No enviar datos sensibles en el payload del WebSocket más allá del ID del pedido y el nuevo estado.