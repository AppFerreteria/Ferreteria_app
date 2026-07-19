# Resultados: Prueba del flujo completo de notificacion

**Fecha:** 18 de julio 2026
**Entorno:** JVM unit test, Gradle 9.3.1, JUnit 4
**Archivo:** `app/src/test/java/com/example/ferreteria_app/NotificacionFlujoTest.kt`

---

## Resumen

| Total | Pasaron | Fallaron |
|-------|---------|----------|
| 4     | 4       | 0        |

---

## Cómo se realizó la prueba

1. Se creó la clase `NotificacionFlujoTest` simulando el ciclo de vida completo de un pedido desde que se crea hasta que se entrega
2. Se usó `cargarPedidosDePrueba` para inyectar listas de pedidos mock con estados cambiantes, simulando las notificaciones de Firestore que llegarían en tiempo real
3. Se verificó que el ViewModel refleja correctamente cada cambio de estado en la pestaña correspondiente
4. Se probó que el `id` del pedido se preserva a través de los cambios de estado, permitiendo la navegación a `MapaSeguimientoActivity`

El ViewModel bajo prueba es el mismo `PedidoViewModel` que usa la pantalla `PedidoActivity`. El flujo real con Firestore funciona igual: el `PedidoRepository` escucha cambios con `addSnapshotListener` y entrega los pedidos actualizados al mismo método `aplicarFiltro`.

---

## Flujo completo probado

```
PENDIENTE  -->  PREPARACION  -->  EN_CAMINO  -->  ENTREGADO
  (Activos)      (Activos)       (En camino)     (Entregados)

[1]             [2]              [3]              [4]
```

---

## Casos de prueba

### 1. flujoCompleto_desdePendienteHastaEntregado_reflejaCambiosEnTiempoReal

Simula 4 notificaciones consecutivas de cambio de estado sobre el mismo pedido y verifica que en cada paso el ViewModel refleja el estado correcto en la pestaña adecuada.

| Paso | Estado | Pestaña activa | Pedidos visibles | Estado del pedido |
|------|--------|---------------|-----------------|-------------------|
| 0 | PENDIENTE | ACT (default) | 1 | PENDIENTE |
| 1 | PREPARACION | ACT (default) | 1 | PREPARACION |
| 2 | EN_CAMINO | EN_CAMINO | 1 | EN_CAMINO |
| 3 | ENTREGADO | ENTREGADOS | 1 | ENTREGADO |

### 2. notificacionEnCamino_pedidoApareceEnPestanaCorrecta_conIdParaNavegacion

Verifica que cuando un pedido entra en EN_CAMINO, la pestaña "En camino" lo muestra con su `id` y `numeroPedido` preservados, listo para la navegación a `MapaSeguimientoActivity`.

| Atributo | Valor esperado |
|----------|---------------|
| id | "pedido-123" |
| numeroPedido | "#NN-001" |
| estado | "en_camino" |
| visible en ACTIVOS | No (estado != ENTREGADO no aplica para EN_CAMINO en esta prueba) |

### 3. notificacionEntregado_pedidoDesapareceDeActivos_yApareceEnEntregados

Simula que dos pedidos pasan a ENTREGADO. Verifica que desaparecen de la pestaña "Activos" y aparecen en "Entregados".

| Momento | Pestaña | Visibles |
|---------|---------|----------|
| Antes (1 EN_CAMINO, 1 ENTREGADO) | Activos | 1 (el de EN_CAMINO) |
| Despues (2 ENTREGADO) | Activos | 0 |
| Despues (2 ENTREGADO) | Entregados | 2 |

### 4. multiplesActualizaciones_soloPedidoAfectadoCambiaDePestana

4 pedidos con estados diferentes. Se actualiza el pedido "b" de PREPARACION a EN_CAMINO. Se verifica que el pedido "b" desaparece de la pestaña "Preparando" y aparece en "En camino", mientras que los demás pedidos permanecen en sus pestañas.

| Momento | Pestaña | Visibles |
|---------|---------|----------|
| Inicial | Activos | 3 (a PENDIENTE, b PREPARACION, c EN_CAMINO) |
| Cambio b -> EN_CAMINO | Preparando | 0 |
| Cambio b -> EN_CAMINO | En camino | 2 (b y c) |

---

## Mecanismo de notificación en producción

El flujo real con Firestore funciona así:

```
RepartidorActivity          Firestore              PedidoRepository
      │                         │                         │
      │── marcarEnCamino() ──→  │                         │
      │                         │── addSnapshotListener ──→│
      │                         │   (pedidos actualizados) │
      │                         │←─────────────────────────│
      │                         │                         │
      │                         │    PedidoViewModel       │
      │                         │    .aplicarFiltro()      │
      │                         │         │                │
      │                         │    PedidoActivity        │
      │                         │    .pedidosData.collect  │
      │                         │    → actualiza Recycler  │
```

El ViewModel recibe la lista actualizada a través del `Flow<List<Pedido>>` del repositorio. El método `aplicarFiltro` procesa la lista igual que en las pruebas unitarias, clasificando los pedidos en activos y entregados según el filtro seleccionado.

---

## Comando de ejecución

```
./gradlew test
```
