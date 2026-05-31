classDiagram

    class IncentivosController {
        +crearInsignia(InsigniaDTO) InsigniaDTO
        +listarInsignias() List~InsigniaDTO~
        +obtenerInsignia(String) InsigniaDTO
        +crearMision(MisionDTO) MisionDTO
        +listarMisiones() List~MisionDTO~
        +obtenerMision(String) MisionDTO
    }

    class Fachada {
        -RepoMisiones repoMisiones
        -RepoInsignias repoInsignias
        -RepoDonadores repoDonadores
        -FachadaDonadoresYEntidades fachadaDonadoresYEntidades
        -FachadaDonaciones fachadaDonaciones
        +agregarInsignia(InsigniaDTO) InsigniaDTO
        +agregarMision(MisionDTO) MisionDTO
        +getInsigniasDeDonador(String) List~InsigniaDTO~
        +getMisionEnCursoDeDonador(String) MisionDTO
        +asignarMisionADonador(String, MisionDTO)
        +asignarInsigniaADonador(String, InsigniaDTO)
        +procesarDonador(String)
        +getAllInsignias() List~InsigniaDTO~
        +getInsigniaById(String) InsigniaDTO
        +getAllMisiones() List~MisionDTO~
        +getMisionById(String) MisionDTO
        +eliminarInsignia(Insignia) Insignia
        +modificarInsignia(Insignia) Insignia
        +eliminarMision(Mision) Mision
        +modificarMision(Mision) Mision
    }

    class RepoDonadores {
        -List~Donador~ donadores
        -Map~String,String~ misionesPorDonador
        -Map~String,List~String~~ insigniasPorDonador
        -Map~String,List~CategoriaDonadorEnum~~ categoriasPorDonador
        +agregarDonador(Donador) Donador
        +buscarDonadorPorID(String) Donador
        +getDonadores() List
        +asignarMisionADonador(String, String)
        +asignarInsigniaADonador(String, String)
    }

    class RepoInsignias {
        -List~Insignia~ insignias
        +getInsignias() List
        +agregarInsignia(Insignia) Insignia
        +eliminarInsignia(Insignia) Insignia
        +modificarInsignia(Insignia) Insignia
    }

    class RepoMisiones {
        -List~Mision~ misiones
        +agregarMision(Mision) Mision
        +eliminarMision(Mision) Mision
        +modificarMision(Mision) Mision
        +getMisionByID(String) Mision
        +getMisiones() List
    }

    class Donador {
        -String id
        -String nombre
        -String apellido
        -Integer edad
        -String email
        -String nroDocumento
        -String domicilio
        -EstadoDonadorEnum estado
        -String categoria
        -String misionActualID
        -List~Insignia~ insignias
        +getInsignias() List
        +getDonadorDTO() Donador
        +getMisionActual() String
        +setMisionActual(String)
        +agregarInsignia(String)
    }

    class Insignia {
        -String id
        -String nombre
        -String descripcion
        +getId() String
        +setId(String)
        +getNombre() String
        +setNombre(String)
        +getDescripcion() String
        +setDescripcion(String)
    }

    class Mision {
        -String id
        -String nombre
        -String insigniaID
        -CategoriaDonadorEnum categoriaInicio
        -CategoriaDonadorEnum categoriaFin
        -TipoMisionEnum tipo
        +getId() String
        +setId(String)
        +getNombre() String
        +setNombre(String)
        +getInsigniaID() String
        +setInsigniaID(String)
        +getCategoriaInicio() CategoriaDonadorEnum
        +setCategoriaInicio(CategoriaDonadorEnum)
        +getCategoriaFin() CategoriaDonadorEnum
        +setCategoriaFin(CategoriaDonadorEnum)
        +getTipo() TipoMisionEnum
        +setTipo(TipoMisionEnum)
    }

    IncentivosController --> Fachada
    Fachada --> RepoDonadores
    Fachada --> RepoInsignias
    Fachada --> RepoMisiones

    RepoDonadores --> Donador
    RepoInsignias --> Insignia
    RepoMisiones --> Mision

    Donador --> Insignia
    Mision --> Insignia : insigniaID