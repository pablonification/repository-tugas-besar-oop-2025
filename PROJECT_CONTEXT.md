# Project Context: Spakbor Hills

## 1. Overview

**Spakbor Hills** is a farm simulation game inspired by Stardew Valley. Players manage a virtual farm, interact with NPCs, grow crops, fish, cook, and achieve in-game objectives. The project is developed in Java and emphasizes the application of Object-Oriented Programming (OOP) principles.

It is a student project for the IF2010 Object-Oriented Programming course.

## 2. Project Structure

The project follows a standard Gradle project layout and utilizes a Model-View-Controller (MVC) architectural pattern for its source code:

```
.
├── .git/               # Git version control internal files
├── .gitignore          # Specifies intentionally untracked files for Git
├── .gradle/            # Gradle build system cache and files
├── build/              # Output of the build process (compiled classes, JARs)
├── gradle/             # Gradle wrapper files
├── src/
│   ├── main/
│   │   ├── java/       # Main application source code
│   │   │   └── com/
│   │   │       └── spakborhills/
│   │   │           ├── Main.java       # Entry point (currently a test driver)
│   │   │           ├── model/          # Domain entities (Player, Item, NPC, Map, etc.)
│   │   │           ├── view/           # Classes for CLI display (intended)
│   │   │           ├── controller/     # Game logic, flow, and input handling (intended)
│   │   │           └── (other sub-packages like util/, exception/ might exist based on README)
│   │   └── resources/  # Non-code resources (e.g., data files, configurations)
│   └── test/
│       ├── java/       # Unit test source code (JUnit)
│       └── resources/  # Resources for testing
├── build.gradle        # Gradle build script (dependencies, tasks, project config)
├── gradlew             # Gradle wrapper script (Linux/macOS)
├── gradlew.bat         # Gradle wrapper script (Windows)
├── README.md           # Main project documentation
├── diagram.puml        # PlantUML file describing the class structure and relationships
└── notes.txt           # Developer notes (contains a TODO for world map areas)
```

## 3. Key Technologies & Libraries

*   **Language:** Java (Targeting Java 24 in `build.gradle`, JDK 21 mentioned in `README.md`)
*   **Build Tool:** Gradle (wrapper included)
*   **Testing:** JUnit 5 (Jupiter Engine)
*   **Logging:** SLF4J API with Logback Classic implementation.

## 4. Build and Run

### Prerequisites
*   Java Development Kit (JDK) - version 21 or higher.
*   Git

### Setup
1.  Clone the repository.
2.  Import into an IDE as a Gradle project.

### Build
```bash
# Linux/macOS
./gradlew build

# Windows
gradlew.bat build
```
Output JAR is typically found in `build/libs/`.

### Run (Executes `com.spakborhills.Main`)
```bash
# Linux/macOS
./gradlew run

# Windows
gradlew.bat run
```

### Run Tests
```bash
# Linux/macOS
./gradlew test

# Windows
gradlew.bat test
```
Test reports are usually in `build/reports/tests/test/index.html`.

## 5. Core Concepts and Codebase Highlights

### a. Entry Point (`src/main/java/com/spakborhills/Main.java`)
Currently, `Main.java` acts as a **test driver**. It initializes game components (items, NPCs, player, farm) and runs a series of automated tests for various game mechanics like:
*   Player status (energy, gold)
*   Inventory management (add, remove, check items/tools)
*   Movement
*   Farming actions (till, water, plant, harvest)
*   Fishing
*   NPC interaction (chat, gift)
*   Game progression (sleep, next day)

It does **not** yet appear to contain the main interactive game loop for a playable game.

### b. Model-View-Controller (MVC) Design
The codebase is structured to follow MVC:
*   **Model (`src/main/java/com/spakborhills/model/`)**: Contains the core game logic and data structures. Key classes identified from `diagram.puml` and `Main.java` include:
    *   `Item` (and its subclasses like `Seed`, `Crop`, `Equipment`, `Food`, `MiscItem`, `ProposalRing`)
    *   `NPC` (and subclasses like `MayorTadi`, `Caroline`, `Perry`)
    *   `Player`
    *   `Farm`
    *   `GameTime`
    *   `Inventory`
    *   `ShippingBin`
    *   `Tile`
    *   `MapArea` (interface) and implementations like `FarmMap`, `WorldMap`
    *   Various Enums: `Gender`, `RelationshipStatus`, `ItemCategory`, `Season`, `Weather`, `TileType`, `Direction`, `LocationType`.
*   **View (`src/main/java/com/spakborhills/view/`)**: Intended for user interface elements. The `README.md` suggests CLI views (e.g., `MapView`, `MenuView`).
*   **Controller (`src/main/java/com/spakborhills/controller/`)**: Intended for handling user input and orchestrating interactions between the Model and View.

### c. Game Mechanics (as seen in `Main.java` tests and `diagram.puml`)
*   **Items:** Different categories (seeds, crops, equipment, food, etc.) with buy/sell prices and usage.
*   **Farming:** Tilling soil, planting seeds, watering crops, harvesting.
*   **Player:** Has energy, gold, inventory, position on a map. Can perform actions that consume energy/time.
*   **NPCs:** Can be interacted with (chat, gift), have relationship levels.
*   **Time & Seasons:** Game has a concept of time, days, and seasons which can affect activities like fishing and planting.
*   **Maps:** Different locations (`FARM`, `FOREST_RIVER`, `MOUNTAIN_LAKE`, `OCEAN`, etc.).
*   **Fishing:** Possible in different locations, affected by time and season.
*   **Cooking:** Recipes exist, implying a cooking mechanic.
*   **Marriage:** `ProposalRing` item and NPC relationship status suggest a marriage mechanic.

### d. Configuration and Data
*   **`build.gradle`**: Defines project dependencies (SLF4J, Logback, JUnit) and main class (`com.spakborhills.Main`).
*   **Item Registry (`Main.java`)**: Items are currently hardcoded and registered in `Main.java`.
*   **NPCs (`Main.java`)**: NPCs are also instantiated directly in `Main.java`.
*   No external configuration files (e.g., for item stats, NPC dialogues) are apparent yet; data seems to be embedded in the code.

### e. PlantUML Diagram (`diagram.puml`)
This file provides a detailed class diagram showing relationships between various entities in the `model` package. It's a valuable resource for understanding the intended structure and interactions of game components.

### f. Notes and TODOs (`notes.txt`)
*   A specific TODO exists: "fix worldmap (FOREST_RIVER, NPC_HOME, Ocean, MOUNTAIN_LAKE) blm di implement" (world map areas not yet implemented).

## 6. Potential Areas for LLM Assistance / Further Exploration

*   **Understanding Game Loop:** The actual interactive game loop is not evident in `Main.java`. An LLM could help conceptualize or implement this based on the existing components.
*   **Controller Logic:** Detailing the logic within the `controller` package.
*   **View Implementation:** Understanding how the `view` package will render game state (likely CLI).
*   **Data Management:** How game data (items, NPCs, progression) will be loaded, saved, and managed, potentially moving away from hardcoding.
*   **Completing Features:** Addressing TODOs like the world map implementation.
*   **Implementing Design Patterns:** The README mentions a requirement for design patterns, which could be explored further.
