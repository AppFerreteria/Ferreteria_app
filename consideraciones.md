# Consideraciones pendientes

## 1. API Key de Google Maps expuesta

**Archivo:** `app/src/main/AndroidManifest.xml` linea 51-53

**Problema:**
La API Key de Google Maps esta hardcodeada con un placeholder `TU_API_KEY`.

**Accion futura recomendada:**
- Leer la clave desde `local.properties` o una variable de entorno
- Usar `BuildConfig` o `res/values/strings.xml` con un placeholder que se sustituye en build
- Mantener el archivo `local.properties` fuera del control de versiones

---

## 2. Pruebas unitarias y de integracion

**Archivos:**
- `app/src/test/java/com/example/ferreteria_app/ExampleUnitTest.kt`
- `app/src/androidTest/java/com/example/ferreteria_app/ExampleInstrumentedTest.kt`

**Problema:**
Solo existen archivos de prueba generados por el template de Android Studio sin contenido util.

**Accion futura recomendada:**
- Implementar pruebas unitarias para cada ViewModel usando JUnit 4 y Turbine para testing de StateFlow
- Implementar pruebas de integracion para los repositorios usando Firebase Emulator Suite
- Agregar dependencias de testing en `app/build.gradle.kts`:
  - `appcompat-testing`
  - `kotlinx-coroutines-test`
  - `turbine` para StateFlow testing

---

## 3. Paginacion en catalogo y busqueda

**Archivos:**
- `app/src/main/java/com/example/ferreteria_app/CatalogoActivity.kt` linea 105
- `app/src/main/java/com/example/ferreteria_app/BuscarActivity.kt` linea 246

**Problema:**
El catalogo y la busqueda cargan todos los productos de Firestore sin limite ni paginacion. Con mas de 100 productos esto degradara el rendimiento y consumira ancho de banda innecesario.

**Accion futura recomendada:**
- Usar `FirebaseFirestore.Query.limit()` combinado con `startAfter()` para paginacion infinita
- Implementar `Paging 3` con `PagingData` y `Flow` en el ViewModel
- Agregar un `ScrollListener` en el RecyclerView para detectar cuando el usuario llega al final y cargar mas datos
