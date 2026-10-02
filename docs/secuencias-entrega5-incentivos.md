# Entrega 5 — Secuencias integradas (vista preliminar)

Los diagramas muestran los contratos a nivel de servicio descritos en la consigna y la documentación disponible. Las partes internas de Donaciones, Donadores y Entidades y Logística deben validarse con sus repositorios/equipos antes de considerar estos flujos demostrados.

## 1. Realizar una donación

```mermaid
sequenceDiagram
    actor Usuario
    participant D as Donaciones
    participant DE as Donadores y Entidades
    participant L as Logística
    Usuario->>D: POST /donaciones
    D->>DE: validar donador / permiso para donar
    DE-->>D: resultado de validación
    D->>L: gestionar ingreso de donación
    L-->>D: encolada / resultado
    D-->>Usuario: DonacionDTO
```

## 2. Reportar la entrega de un paquete

```mermaid
sequenceDiagram
    actor Operador
    participant L as Logística
    participant D as Donaciones
    participant DE as Donadores y Entidades
    Operador->>L: reportar entrega (paquete)
    L->>D: cambiar estado de donación a ACEPTADA
    D-->>L: resultado
    L->>DE: satisfacer necesidad (si corresponde)
    DE-->>L: resultado
    L-->>Operador: confirmación de entrega
```

## 3. Registrar queja sobre una donación entregada

```mermaid
sequenceDiagram
    actor Usuario
    participant DE as Donadores y Entidades
    participant D as Donaciones
    participant I as Incentivos
    Usuario->>DE: POST queja del donador
    DE->>D: asociar queja a donación / ajustar estado
    D-->>DE: resultado
    DE-->>Usuario: QuejaDTO
    Note over I: El próximo procesamiento reevalúa la misión y puede retirar progreso
```

## 4. Procesar un donador (Incentivos)

```mermaid
sequenceDiagram
    actor Operador
    participant I as Incentivos
    participant D as Donaciones
    participant DE as Donadores y Entidades
    Operador->>I: POST /donadores/{id}/procesar
    I->>DE: leer datos/quejas del donador
    I->>D: consultar historial de donaciones
    D-->>I: donaciones del donador
    I->>I: evaluar misión y pérdida de progreso
    I->>DE: actualizar categoría si corresponde
    I-->>Operador: resultado
```

## 5. Registrar una nueva necesidad

```mermaid
sequenceDiagram
    actor Entidad
    participant DE as Donadores y Entidades
    participant D as Donaciones
    Entidad->>DE: POST /necesidades
    DE->>D: validar producto solicitado
    D-->>DE: producto válido / error
    DE-->>Entidad: NecesidadMaterialDTO o error de validación
```

## 6. Obtener estadísticas de un donador

```mermaid
sequenceDiagram
    actor Usuario
    participant DE as Donadores y Entidades
    participant D as Donaciones
    Usuario->>DE: GET estadísticas del donador
    DE->>D: consultar historial/conteos de donaciones
    D-->>DE: historial o agregados
    DE->>DE: calcular estadísticas
    DE-->>Usuario: DonadorStatsDTO
```

> Estos diagramas son un punto de partida contractual derivado de la consigna y docs de Entrega 4. Ajustar los mensajes/rutas internos luego de confirmar OpenAPI y código de los cuatro servicios.
