# TPO Programación III — Juego de Adivinanzas ("¿Quién es Quién?")

Juego en Java con 23 personajes y tres modos: Humano vs Máquina 1, Humano vs Máquina 2 y Máquina vs Máquina.
Aplica **Divide y Conquista** (MergeSort y búsqueda binaria) y **Greedy** (elección de preguntas).

La documentación completa (algoritmos, patrones, decisiones de diseño y algoritmos no aplicados) está en
[DOCUMENTACION.md](DOCUMENTACION.md).

## Ejecutar

```bash
javac -d bin $(find src -name "*.java")
java -cp bin App            # interfaz gráfica
java -cp bin App --console  # consola
```

Pruebas automatizadas:

```bash
javac -d bin $(find src test -name "*.java")
java -cp bin Implementacion.PruebasJuego
```

En IntelliJ / VS Code: ejecutar `App` (sin argumentos abre la GUI).
