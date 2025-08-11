# Documentación técnica inicial (v0.1)

## 1) Resumen UML
- `JavaGame` (extiende `Game`): configura y establece la pantalla inicial con `setScreen(new GameScreen(this))`.
- `GameScreen` (implementa `Screen`): dibuja fondo, placeholders y artefactos con `SpriteBatch`.
- Dependencias: `SpriteBatch`, `Texture`, `BitmapFont`.

## 2) Reglas del juego (v0.1)
- Género: runner/plataformas 2D.
- Objetivo: avanzar evitando obstáculos y recogiendo artefactos.
- Personajes (placeholder): Orion, Roky, Thumper.
- Artefactos: caja (bonus por definir), escudo (invulnerabilidad temporal).
- Controles (propuestos): ← → mover, ↑/Espacio saltar.
- Vidas: 3; colisión con obstáculo = perder 1 vida.

## 3) Requisitos técnicos
- JDK 21, Gradle Wrapper 8.9, LibGDX (LWJGL3). IDE opcional: NetBeans 26.
- Estructura: `core/` (lógica), `lwjgl3/` (launcher desktop), `assets/` (recursos).
- Comandos: `./gradlew :lwjgl3:run`, `:lwjgl3:clean`, `:lwjgl3:assemble`.
- Convenciones: rutas de assets sin prefijo `assets/`; liberar recursos en `dispose()`.

## 4) Guía rápida
```bash
git clone git@github.com:Adal612Git/JavaGame.git
cd JavaGame
./gradlew :lwjgl3:run


Problemas comunes: avisos LWJGL/OpenAL se pueden ignorar; si faltan assets, verificar rutas.

Responsable: Ricardo — Fecha: $(date +%Y-%m-%d)
