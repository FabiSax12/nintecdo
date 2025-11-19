# Nintecdo Game Development Guide

## Requisitos Rápidos para Crear tu Juego

### 1. **Dependencias**
Copia las siguientes interfaces que necesitas implementar en tu juego:

/com/nintecdo/core/GameStats.java
/com/nintecdo/core/IGame.java
/com/nintecdo/core/IGameListener.java

Agrega estas dependencias en tu proyecto Maven:
```xml
<dependencies>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>21</version>
    </dependency>
</dependencies>
```

JavaFX es necesario para crear UI.

### 2. **Implementa la Interfaz IGame**
Crea tu clase principal que implemente `IGame`:

```java
public class MiJuego implements IGame { }
```

### 3. **Archivo de Configuración**
Crea `manifest.properties` en `src/main/resources/`:

```properties
game.class=com.tujuego.MiJuego
game.title=Mi Juego
game.version=1.0.0
game.author=Tu Nombre
game.category=arcade
game.width=800
game.height=600
game.resizable=false
```

### 4. **Estructura del Proyecto**
```
mi-juego/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── com/tujuego/
                ├── core/ (Interfaces Obligatorias)
│       │       └── MiJuego.java     (implementación IGame)
│       └── resources/
│           ├── manifest.properties
│           └── assets/              (imágenes, sonidos)
└── target/
    └── MiJuego.jar                 (generado)
```

### 5. **Compilar y Empaquetar**
```bash
# Compilar
mvn clean compile

# Crear JAR ejecutable
mvn clean package

# El JAR queda en: target/MiJuego.jar
```

Abre Nintecdo, busca el .jar del juego compilado y tu juego aparecerá en la biblioteca.

## Recursos Adicionales

- **Assets**: Coloca imágenes/sonidos en `src/main/resources/assets/`
- **Acceso a recursos**: Usa `getClass().getResource("/assets/imagen.png")`