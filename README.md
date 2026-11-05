# POOBkemon

Juego de batallas Pokémon por turnos para escritorio, escrito en Java con interfaz Swing. Dos entrenadores arman su equipo de seis Pokémon, eligen sus movimientos e ítems y se enfrentan hasta que uno de los equipos queda sin Pokémon en pie.

## Características

- **Cuatro modos de juego**
  - *Jugador vs Jugador*: dos personas comparten el equipo.
  - *Jugador vs Máquina*: el rival lo controla una de las estrategias de IA.
  - *Máquina vs Máquina*: dos IA se enfrentan para observar cómo se comportan.
  - *Supervivencia*: equipos aleatorios de nivel 100, sin ítems.
- **Catálogo completo**: 386 Pokémon, 357 movimientos y una tabla de efectividad entre tipos, cargados desde archivos CSV.
- **Estados alterados** (parálisis, quemadura, veneno, sueño, confusión, mejoras y reducciones de estadísticas, entre otros) que se aplican al terminar cada turno completo.
- **Estadísticas base o aleatorias** (IV/EV y nivel aleatorios), con probabilidad de Pokémon variocolor.
- **Ítems**: pociones de distinta potencia y revivir.
- **Temporizador de 20 segundos por turno**; si se agota, el Pokémon activo gasta un PP de cada uno de sus movimientos.
- **Pokédex y galería de ítems** consultables desde el menú.
- **Guardar y abrir partidas** (archivos `.poob`).
- Fondos y marcos intercambiables desde el menú *Opciones*.

### Estrategias de la máquina

| Estrategia | Comportamiento |
|---|---|
| `Offensive` | Ataca siempre con el primer movimiento que conserve PP. |
| `Defensive` | Prioriza conservar al equipo: cura cuando la vida es baja, cambia de Pokémon ante desventaja de tipo y usa *Protect*. |
| `Switcher` | Cambia de Pokémon con una probabilidad que depende de la efectividad del emparejamiento. |
| `Expert` | Evalúa cada movimiento (daño esperado, efectividad, estados) y elige la poción adecuada según la vida faltante. |

## Requisitos

- JDK 21 o superior.
- Ejecutar siempre desde la carpeta `Pokemon/`: los datos (`resources/csv`) y los recursos gráficos y de audio se leen con rutas relativas.

## Compilar y ejecutar

```sh
cd Pokemon
javac -d out $(find src -name '*.java')
java -cp out presentacion.POOBkemonGUI
```

El proyecto también se abre directamente en IntelliJ IDEA (`Pokemon/Pokemon.iml`).

## Pruebas

Las pruebas usan JUnit 5 y están en `Pokemon/test`. Desde IntelliJ basta con ejecutar la carpeta `test`. Por línea de comandos, con el JAR `junit-platform-console-standalone`:

```sh
cd Pokemon
javac -d out $(find src -name '*.java')
javac -cp out:junit-platform-console-standalone.jar -d out-test $(find test -name '*.java')
java -jar junit-platform-console-standalone.jar -cp out:out-test --scan-classpath out-test
```

## Arquitectura

El código se organiza en tres capas que solo dependen hacia abajo: `presentacion` → `domain` → `persistence`.

```
Pokemon/
├── resources/          Datos CSV, imágenes, fuentes y música
├── src/
│   ├── persistence/    Repositorios de solo lectura sobre los CSV
│   ├── domain/         Reglas del juego
│   └── presentacion/   Interfaz Swing
└── test/               Pruebas unitarias
```

### `persistence`

Cada repositorio (`PokemonRepository`, `MovesRepository`, `ItemRepository`, `StatusRepository`, `StatsRepository`) lee su archivo una sola vez y comparte los datos entre instancias; entrega copias para que los llamadores no puedan alterar el catálogo. `CsvReader` concentra el análisis de CSV con campos entre comillas.

### `domain`

`POOBkemon` es la fachada del juego: la interfaz solo conversa con ella, y le entrega y recibe datos simples (cadenas, listas y arreglos). `Survive` la extiende para el modo supervivencia.

Patrones y decisiones de diseño:

- **Singleton** para la partida en curso (`POOBkemon.getInstance()`), reiniciable con `resetInstance()`.
- **Template Method**: el nivel inicial de los Pokémon es el gancho `pokemonLevel()`, que `Survive` sobrescribe.
- **Strategy**: `Machine` define el contrato `machineMovement(...)` y las estrategias `Offensive`, `Defensive`, `Switcher` y `Expert` lo implementan. Las consultas y constructores de decisiones comunes viven en `Machine`.
- **Factory Method**: el tipo de entrenador se resuelve con una tabla inmutable de constructores a partir del nombre.
- **Command (mensajes)**: toda acción de un turno es una decisión (`Attack`, `UseItem`, `ChangePokemon`, `Run`, `timeOver`) cuyo formato y validación están en `Decision`.
- **Template Method** también en `Item`: `effect()` descuenta la unidad y delega en `itemEffect()` de `Potion` o `Revive`.
- Los errores esperados se señalan con `POOBkemonException`; los inesperados se registran en `POOBkemon.log` mediante `Log`.

### `presentacion`

- `POOBkemonGUI`: ventana principal y navegación entre pantallas (introducción, menú, modos, selección de equipo, movimientos e ítems).
- `PokemonBattlePanel`: pantalla de batalla (turnos, temporizador, animaciones y registro de acciones).
- `GameSetup`: datos que el jugador arma antes de pelear; no depende de Swing y es quien crea la partida en el dominio.
- `Assets`: carga y caché de imágenes, íconos y fuente; `Theme` reúne rutas y colores.
- `UiFactory` y `RoundedButton`: botones comunes a todas las pantallas, con estados de hover, presionado, foco y deshabilitado.
- `SoundPlayer` y `Dialogs`: música de fondo y diálogos estándar.

## Cómo se juega

1. En la pantalla de inicio presiona **Enter** y elige **Jugar**.
2. Selecciona el modo de juego e ingresa los nombres (entre 3 y 10 caracteres).
3. Cada jugador elige seis Pokémon, cuatro movimientos por Pokémon y reparte sus ítems (hasta dos pociones de cada tipo y un revivir).
4. En batalla, en tu turno elige **Attack**, **Item**, **Pokémon** (cambiar) o **Run** (rendirse).
5. Gana quien deje sin Pokémon en pie al rival.

Desde el menú *Archivo* puedes iniciar una partida nueva, guardar la batalla en curso o abrir una guardada.
