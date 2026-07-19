# Resultados: Prueba de consulta manual del estado del pedido

**Fecha:** 18 de julio 2026
**Entorno:** JVM unit test, Gradle 9.3.1, JUnit 4
**Archivo:** `app/src/test/java/com/example/ferreteria_app/PedidoViewModelTest.kt`

---

## Resumen

| Total | Pasaron | Fallaron |
|-------|---------|----------|
| 6     | 6       | 0        |

---

## Cómo se realizó la prueba

1. Se agregó el método `cargarPedidosDePrueba` en `PedidoViewModel` para inyectar datos mock sin depender de Firebase ni del repositorio
2. Se cambió `PedidoRepository` a inicialización lazy en el ViewModel para evitar la inicialización de Firebase en tests unitarios
3. Se crearon 6 casos de prueba que cubren los filtros por estado, el límite de entregados y el toggle de historial
4. Se ejecutaron con `./gradlew test`

---

## Casos de prueba

### 1. filtroActivos_muestraSoloNoEntregados
**Qué prueba:** 4 pedidos con estados PENDIENTE, PREPARACION, EN_CAMINO, ENTREGADO. El filtro por defecto "ACTIVOS" debe mostrar solo los 3 no entregados.

| Dato de entrada | Resultado esperado | Resultado obtenido |
|----------------|-------------------|-------------------|
| 4 pedidos mezclados | 3 visibles, ninguno ENTREGADO | 3 visibles, ninguno ENTREGADO |

### 2. filtroPreparando_muestraSoloPreparacion
**Qué prueba:** Con 4 pedidos mezclados, al seleccionar la pestaña "Preparando" solo se muestra el que tiene estado PREPARACION.

| Dato de entrada | Resultado esperado | Resultado obtenido |
|----------------|-------------------|-------------------|
| Filtro PREPARACION sobre 4 pedidos | 1 visible, estado = PREPARACION | 1 visible, estado = PREPARACION |

### 3. filtroEnCamino_muestraSoloEnCamino
**Qué prueba:** 4 pedidos con 2 en estado EN_CAMINO. La pestaña "En camino" debe mostrar solo esos 2.

| Dato de entrada | Resultado esperado | Resultado obtenido |
|----------------|-------------------|-------------------|
| Filtro EN_CAMINO, 2 coincidencias | 2 visibles, todos EN_CAMINO | 2 visibles, todos EN_CAMINO |

### 4. filtroEntregados_limitaATresMasRecientes
**Qué prueba:** 4 pedidos ENTREGADO + 1 PENDIENTE. La pestaña "Entregados" muestra 3, con `hayMasEntregados = true` y `totalEntregados = 4`.

| Dato de entrada | Resultado esperado | Resultado obtenido |
|----------------|-------------------|-------------------|
| 4 entregados | 3 visibles, hayMasEntregados=true | 3 visibles, hayMasEntregados=true |

### 5. toggleHistorial_muestraTodosLosEntregados
**Qué prueba:** 5 pedidos ENTREGADO. Inicialmente se ven 3. Al hacer toggle, se muestran los 5.

| Dato de entrada | Resultado esperado | Resultado obtenido |
|----------------|-------------------|-------------------|
| 5 entregados, toggle | 3 antes, 5 después | 3 antes, 5 después |

### 6. cambioDeFiltro_reseteaHistorial
**Qué prueba:** Con historial expandido en la pestaña "Entregados", al cambiar a otra pestaña y volver, el historial vuelve al límite de 3.

| Dato de entrada | Resultado esperado | Resultado obtenido |
|----------------|-------------------|-------------------|
| Cambiar pestaña con historial expandido | Se resetea a 3 al volver | Se resetea a 3 al volver |

---

## Método de inyección para pruebas

El mecanismo usado para simular datos sin Firebase:

```kotlin
// PedidoViewModel.kt - linea 58
fun cargarPedidosDePrueba(pedidos: List<Pedido>) {
    todosLosPedidos = pedidos
    aplicarFiltro()
}
```

Este método recibe una lista de `Pedido` mock, la asigna como datos fuente y ejecuta el motor de filtros sin necesidad de conexión a Firestore ni repositorio.

La inicialización lazy del repositorio evita que Firebase se inicialice en el constructor:

```kotlin
// PedidoViewModel.kt - linea 20
private val repository by lazy { PedidoRepository() }
```

---

## Comando de ejecución

```
./gradlew test
```
