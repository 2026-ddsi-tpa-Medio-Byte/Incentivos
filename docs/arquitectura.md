graph TD
    subgraph "Aplicación Java (JVM)"
        F[Fachada] --> R1[RepoDonadores]
        F --> R2[RepoInsignias]
        F --> R3[RepoMisiones]

        R1 --> D[Donador]
        R2 --> I[Insignia]
        R3 --> M[Mision]
        
        D --> I
        M --> I
    end
        
    subgraph "Almacenamiento"
        R1 --> Mem[Memoria]
        R2 --> Mem
        R3 --> Mem
    end
    