# Juego de Adivinanzas ("¿Quién es Quién?") — Documentación

**Materia:** Programación III — Trabajo Práctico Obligatorio
**Lenguaje:** Java 17+ (Swing para la interfaz gráfica)

---

## 1. Resumen

El proyecto implementa un juego de adivinanzas del estilo "¿Quién es Quién?" con 23 personajes. Hay tres modos de juego:

| Modo | Descripción |
|---|---|
| Humano vs Máquina 1 | El jugador compite contra la máquina menos asertiva. |
| Humano vs Máquina 2 | El jugador compite contra la máquina más asertiva, que parte con la ventaja de conocer las preguntas hechas por la Máquina 1. |
| Máquina 1 vs Máquina 2 | Simulación en la que se muestra, turno por turno, el razonamiento de cada máquina para acortar la búsqueda. |

En cada turno un jugador puede **hacer una pregunta** (aplicar un filtro sobre una característica) o **arriesgar** un personaje. Las preguntas son ilimitadas y la partida la gana quien primero adivina el personaje del rival. Las victorias del humano se guardan en un **marcador récord** persistente.

### Cómo ejecutar

```bash
javac -d bin $(find src -name "*.java")
java -cp bin App            # interfaz gráfica (Swing)
java -cp bin App --console  # versión por consola
```

---

## 2. Cómo se cumple cada requisito de la consigna

| Requisito | Implementación |
|---|---|
| 23 personajes con características distinguibles | `Personaje.crearPersonajes()`. Con 4 características (2 géneros × calvicie × lentes × 3 colores) hay 24 combinaciones: cada personaje usa una distinta, por lo que **siempre** se puede identificar a uno solo con filtros. |
| Empiezan ordenados solo por género; la máquina los dispone en una lista con ID autoincremental | `Tablero`: ordena con **MergeSort** por género y los agrega uno a uno asignando `proximoId++`. El ID ya no se escribe a mano en el constructor. |
| El jugador elige 1 y la máquina elige otro, sin acceder al del jugador | El secreto vive en `Jugador` (campo `private final`). Las máquinas reciben al rival **solo** como la interfaz `Oponente` (`responder`, `verificarSuposicion`): no tienen forma de leer el personaje. |
| Turno a turno: suposición directa o pistas/filtros | `MotorJuego.preguntarHumano`, `arriesgarHumano` y `Maquina.jugarTurno` (que pregunta o arriesga). |
| Dos máquinas con formas diferentes de jugar | Máquina 1: preguntas al azar y arriesga solo con certeza. Máquina 2: Greedy, arriesga con 2 candidatos y usa información del rival (sección 5). |
| La Máquina 2 conoce las preguntas hechas por la Máquina 1 | Humano vs M2: arranca aplicando las preguntas y respuestas que la M1 le hizo al humano en la sesión. MvM: escucha las preguntas de la M1 para saber si está por perder (sección 5.2). |
| El usuario no puede cambiar de personaje; cada máquina elige uno diferente | El personaje del humano se elige una vez por sesión y es `final`. Las máquinas eligen al azar excluyendo al humano y entre sí (`Tablero.elegirAlAzar`). |
| Marcador récord persistente con nombre y victorias | `Marcador` guarda `nombre=victorias` en `marcador_record.txt`. El ranking se muestra ordenado de mayor a menor. |
| Preguntas infinitas; se pierde cuando gana el otro | No hay límite de turnos (se eliminó el tope de 25 del modo MvM). La partida termina sólo cuando alguien adivina. |
| Filtros: género, calvicie, lentes, 3 colores de pelo | `Preguntas.TODAS`: 9 preguntas `CARACTERISTICA=VALOR`, con validación de entrada. |

---

## 3. Estructura del proyecto

```
src/
├── App.java                   Entrada: GUI (o --console)
├── Interfaz/
│   ├── JuegoAdivinanzas.java  Contrato de la consola
│   ├── Oponente.java          Preguntar / arriesgar al rival
│   └── EstrategiaPregunta.java  Strategy: elegir preguntas
└── Implementacion/
    ├── Personaje.java         Datos de cada personaje
    ├── Tablero.java           Lista ordenada, IDs, b. binaria
    ├── Ordenamiento.java      MergeSort generico
    ├── Preguntas.java         Filtros y su evaluacion
    ├── Jugador.java           Personaje secreto (Oponente)
    ├── Maquina.java           Jugador automatico
    ├── EstrategiaGreedy.java  Estrategia de la Maquina 2
    ├── EstrategiaAleatoria.java  Estrategia de la Maquina 1
    ├── RegistroPregunta.java  Pregunta + respuesta
    ├── Jugada.java            Turno de maquina + razonamiento
    ├── MotorJuego.java        Reglas y estado (consola y GUI)
    ├── Marcador.java          Marcador record persistente
    ├── JuegoAdivinanzasImpl.java  Interfaz de consola
    └── Gui.java               Interfaz grafica Swing
```

**Separación modelo / vista.** Toda la lógica (algoritmos, reglas, estado) está en `MotorJuego` y las clases que usa. `JuegoAdivinanzasImpl` (consola) y `Gui` (Swing) sólo leen la entrada del usuario y muestran resultados. Así cada algoritmo existe **una sola vez**. Antes la GUI tenía una copia propia de MergeSort, búsqueda binaria y Greedy.

---

## 4. Algoritmos aplicados

### 4.1 MergeSort — Divide y Conquista (`Ordenamiento.mergeSort`)

**Dónde se usa:**
- Para armar el tablero, ordenando los personajes por género.
- Para ordenar el ranking del marcador por cantidad de victorias.

**Cómo funciona:**
1. **Dividir:** parte la lista por la mitad.
2. **Conquistar:** ordena cada mitad recursivamente, hasta llegar a listas de 0 o 1 elemento, que ya están ordenadas.
3. **Combinar:** intercala las dos mitades ordenadas en una sola.

**Complejidad:** la recurrencia es T(n) = 2T(n/2) + O(n), que resuelve a **O(n log n) en el mejor, promedio y peor caso**. El espacio adicional es O(n).

**Justificación:**
- **Es estable.** En el `merge` se usa `<=`, así que a igual género se conserva el orden de carga. Eso hace que dentro de cada género los IDs respeten el orden en que se declararon los personajes.
- Su rendimiento no depende del orden de entrada. QuickSort, en cambio, cae a O(n²) si la entrada ya viene ordenada, y justamente la consigna dice que los personajes "empiezan ordenados por género".
- Se implementó de forma genérica (`<T>` + `Comparator`) para reutilizarlo en el marcador.

### 4.2 Búsqueda binaria recursiva — Divide y Conquista (`Tablero.buscarPorId`)

**Dónde se usa:** cuando el jugador elige su personaje por ID.

**Cómo funciona:** como los IDs se asignan en orden creciente, la lista queda ordenada por ID. En cada paso se compara con el elemento del medio y se descarta la mitad donde no puede estar.

**Complejidad:** T(n) = T(n/2) + O(1), que resuelve a **O(log n)**. Para n = 23 son como mucho 5 comparaciones, contra 23 de una búsqueda lineal.

**Justificación:** aprovecha que el tablero ya está ordenado por ID, un invariante que garantiza el propio `Tablero`. Además, sigue funcionando aunque los IDs no fueran consecutivos.

### 4.3 Greedy — algoritmo voraz (`EstrategiaGreedy`)

**Dónde se usa:** es la forma en que la Máquina 2 elige cada pregunta.

**Cómo funciona:** en cada turno evalúa las 9 preguntas posibles sobre los candidatos que le quedan. Cuenta cuántos responderían SÍ y cuántos NO, y elige la pregunta que **minimiza |SÍ − NO|**, es decir, la que parte el conjunto lo más cerca posible de la mitad. Descarta las preguntas que no separan a nadie (todos SÍ o todos NO).

- **Elección voraz:** toma la mejor decisión local (el mejor peor caso inmediato) sin reconsiderar las anteriores.
- **Complejidad por turno:** O(P · n), con P = 9 preguntas y n candidatos.
- **Idea:** si cada pregunta partiera por la mitad, la búsqueda se comportaría como una búsqueda binaria sobre los personajes: ⌈log₂ 23⌉ = 5 preguntas.

**¿Es óptimo?** En general Greedy no garantiza el óptimo global: elegir la mejor partición local no asegura el mejor árbol de preguntas completo. Para este conjunto de personajes **lo verificamos con una búsqueda exhaustiva** (ver 7.1):

| Estrategia | Turnos promedio (preguntas + adivinanza final) | Peor caso |
|---|---|---|
| Óptimo exacto (búsqueda exhaustiva con memoización) | 5,61 | 6 |
| **Greedy** | **5,61** | **6** |
| Preguntas al azar (Máquina 1) | 5,62 | 6 |

Con estos datos Greedy alcanza el óptimo, con un costo muchísimo menor que calcularlo. La diferencia contra preguntar al azar es chica porque las 23 combinaciones cubren casi todo el espacio: casi cualquier pregunta divide bien. Por eso la diferencia de "asertividad" entre las máquinas se completa con el umbral para arriesgar y con el uso de la información del rival (sección 5).

---

## 5. Las dos máquinas

`Maquina` es una sola clase. Lo que distingue a una máquina de la otra se inyecta en el constructor:

| | Máquina 1 (menos asertiva) | Máquina 2 (más asertiva) |
|---|---|---|
| Estrategia de preguntas | `EstrategiaAleatoria`: al azar entre las preguntas útiles | `EstrategiaGreedy`: la división más pareja |
| Cuándo arriesga | Sólo cuando le queda 1 candidato (certeza) | Con 2 candidatos o menos (50 % de acertar un turno antes) |
| Información extra | — | Conoce las preguntas de la Máquina 1 |

### 5.1 Cómo decide una máquina en su turno (`Maquina.jugarTurno`)
1. Si le quedan `umbralArriesgar` candidatos o menos, **arriesga** uno de ellos.
2. Si escucha al rival y detecta que el rival ya la tiene identificada (le queda 1 candidato), **arriesga** igual: si no, pierde en el próximo turno.
3. Si no, **pregunta** lo que indique su estrategia y filtra sus candidatos con la respuesta.
4. Si ninguna pregunta separa a los candidatos, arriesga.

Si una suposición falla, ese personaje se descarta. Así cada jugada elimina al menos un candidato y la partida **siempre termina**, sin necesidad de un límite de turnos.

### 5.2 La ventaja de la Máquina 2
- **Humano vs Máquina 2.** El humano elige su personaje una vez por sesión. Todas las preguntas que la Máquina 1 le hace al humano en esa sesión, con sus respuestas, se acumulan en `MotorJuego`. Cuando empieza una partida contra la Máquina 2, esta aplica esas preguntas como filtros ya hechos (`incorporarConocimiento`) y **arranca con menos candidatos**. En simulación, después de sólo 2 turnos de la Máquina 1, la Máquina 2 necesita en promedio 3,2 turnos en lugar de 5,3.
- **Máquina vs Máquina.** La Máquina 1 le pregunta a la Máquina 2 por su personaje, y la Máquina 2 escucha esas preguntas (`observarPreguntasDelRival`). Como sabe qué respondió, puede reconstruir cuántos candidatos le quedan a la Máquina 1. Si detecta que la Máquina 1 ya la identificó, arriesga antes, porque esperar significa perder. Aunque juega segunda, la Máquina 2 gana alrededor del **56 %** de las simulaciones (20.000 partidas).

---

## 6. Patrones y decisiones de diseño

| Patrón / principio | Dónde | Por qué |
|---|---|---|
| **Strategy** | `EstrategiaPregunta` → `EstrategiaGreedy`, `EstrategiaAleatoria` | Cada máquina recibe su forma de preguntar sin `if (tipo == 1)` dispersos. Agregar una tercera máquina es escribir una nueva estrategia. |
| **Encapsulamiento / ocultamiento de información** | `Jugador` + interfaz `Oponente` | El secreto es `private final` y la máquina sólo ve un `Oponente`. Que la máquina no pueda "espiar" está garantizado por el compilador, no por disciplina. Las respuestas se calculan a partir del secreto, así nadie puede mentir. |
| **Fachada (Facade)** | `MotorJuego` | Consola y GUI usan una única API simple: `nuevaPartidaHumano`, `preguntarHumano`, `turnoMaquina`, `turnoMvM`, etc. |
| **Separación modelo / vista** | `MotorJuego` vs `Gui` y `JuegoAdivinanzasImpl` | Un solo lugar para las reglas y algoritmos. |
| **Métodos de fábrica estáticos** | `Jugada.pregunta(...)`, `Jugada.suposicion(...)` | Construcción clara de los dos tipos de jugada. |
| **Vistas inmodificables** | `Collections.unmodifiableList` en getters | Las vistas no pueden alterar el estado del juego por error. |
| **Inmutabilidad** | `final` en `Personaje`, `RegistroPregunta`, `Jugada` | Los datos de un personaje y el registro de lo ocurrido no cambian. |

**Otras decisiones:**
- **Preguntas como texto `CARACTERISTICA=VALOR`.** Son fáciles de mostrar, de comparar y de persistir. Se validan contra `Preguntas.TODAS`, así que un valor como "AZUL" se rechaza.
- **Preguntas repetidas del humano.** Se rechazan con un mensaje y no consumen el turno. En la GUI el botón se deshabilita.
- **Suposiciones de la máquina.** Elige al azar entre sus candidatos. Si falla, descarta ese candidato.
- **Marcador.** Es un archivo de texto `nombre=victorias`. Se lee completo al consultar y se reescribe al registrar una victoria. Es simple, legible y suficiente para el volumen del juego.

---

## 7. Algoritmos considerados y no aplicados

### 7.1 Backtracking / fuerza bruta y Programación Dinámica (árbol óptimo de preguntas)
Encontrar la secuencia de preguntas que minimiza la cantidad esperada de turnos es construir un **árbol de decisión óptimo**:
- **Backtracking** probaría todas las preguntas en cada nodo y todas las respuestas posibles.
- **Programación dinámica** evitaría recalcular subconjuntos de candidatos repetidos (memoización).

**Por qué no se aplicó:**
- El problema general es NP-difícil y el número de subconjuntos posibles crece como 2ⁿ.
- Para este juego lo calculamos por fuera (búsqueda exhaustiva con memoización) y el resultado coincide con Greedy: 5,61 turnos promedio y 6 en el peor caso. Pagar ese costo no aporta ninguna mejora.
- Greedy es O(P·n) por turno y su razonamiento se puede mostrar en pantalla en el modo MvM, que es lo que pide la consigna.

### 7.2 Minimax / poda alfa-beta
Sirven para juegos adversariales en los que las jugadas de uno afectan las opciones del otro. Acá cada jugador busca en su propio tablero y las respuestas son veraces: no hay un rival que "elija" respuestas. Minimax no aporta nada.

La única interacción real es la carrera por adivinar primero, y la Máquina 2 la cubre con una regla simple: arriesga si el rival está por ganar.

### 7.3 Otros ordenamientos
- **QuickSort:** no es estable y tiene peor caso O(n²) con entradas ya ordenadas, que es el caso de esta consigna.
- **Burbuja / Inserción / Selección:** O(n²). Con n = 23 el tiempo es despreciable, pero no aplican Divide y Conquista.
- **Counting sort / "partir en dos grupos":** como el género tiene sólo 2 valores, se podría ordenar en O(n) con dos listas. Se prefirió MergeSort porque es general (lo reutilizamos para el marcador) y es la técnica de Divide y Conquista de la materia.

### 7.4 Búsqueda lineal y hashing
- **Búsqueda por ID:** la lineal sería O(n). Un `HashMap<Integer, Personaje>` daría O(1) promedio, pero exige una estructura extra y no aprovecha que la lista ya está ordenada. La binaria es O(log n) sin memoria adicional.
- **Búsqueda por nombre:** sí es lineal (`Tablero.buscarPorNombre`), porque la lista no está ordenada por nombre y con 23 elementos no se justifica mantener otro índice.

### 7.5 Grafos (BFS/DFS, caminos mínimos) y Branch & Bound
El problema no tiene estructura de grafo ni una función de costo a acotar más allá del árbol de preguntas, que ya se analizó en 7.1.

---

## 8. Pruebas realizadas

- Se compiló con `javac -Xlint:all`, sin errores.
- **Verificación del tablero:**
  - 23 personajes con 23 combinaciones únicas.
  - IDs 1..23 consecutivos y con los femeninos primero.
  - La búsqueda binaria encuentra los 23 IDs y devuelve `null` para 0 y 24.
- **Simulación de 20.000 partidas por caso:**
  - **MvM:** siempre termina. Promedio de 5,0 turnos, máximo 6. La Máquina 1 gana el 44 % y la Máquina 2 el 56 %.
  - **Cada máquina contra un rival que no pregunta:** la Máquina 1 necesita 5,62 turnos en promedio y la Máquina 2, 5,26. Ninguna supera 6.
  - **Máquina 2 con la ventaja** (después de 2 turnos de la Máquina 1 en la sesión): 3,19 turnos en promedio.
  - En ninguna partida una máquina eligió el mismo personaje que el humano ni que la otra máquina.
- **Validaciones:**
  - Una pregunta inválida ("PELO=AZUL") se rechaza.
  - Una pregunta repetida se rechaza.
  - Un ID inexistente se rechaza.
- **Revisión manual:** partidas completas por consola y capturas de la GUI en los tres modos.

---

## 9. Limitaciones y posibles mejoras
- El humano no tipea la respuesta a las preguntas de la máquina: la calcula su objeto `Jugador` a partir del personaje elegido. Se decidió así para evitar respuestas erróneas o mentiras, y la máquina sigue sin acceder al personaje.
- El marcador distingue mayúsculas ("matias" y "Matias" son jugadores distintos).
- La ventaja de la Máquina 2 contra el humano depende de haber jugado antes contra la Máquina 1 en la misma sesión. Si no, arranca sin ventaja y el juego lo informa.
