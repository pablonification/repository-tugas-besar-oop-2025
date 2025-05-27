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
│   │   │           ├── Main.java       # Entry point, initializes game and launches the Swing GUI
│   │   │           ├── model/          # Domain entities (Player, Item, NPC, Map, etc.)
│   │   │           ├── view/           # GUI classes using Java Swing (e.g., GameFrame, GamePanel)
│   │   │           ├── controller/     # Game logic, flow, and input handling (e.g., GameController, manages interaction between Model and Swing View)
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
`Main.java` initializes core game components (items, NPCs, player, farm) and then **launches the interactive game using a Java Swing graphical user interface (GUI)**. While it previously served primarily as a test driver for various game mechanics, it now acts as the true entry point for the playable game. The interactive game loop is managed through Swing's event handling mechanisms within the GUI components.
Key initializations include:
*   Player status (energy, gold)
*   Inventory management (add, remove, check items/tools)
*   Movement
*   Farming actions (till, water, plant, harvest)
*   Fishing
*   NPC interaction (chat, gift)
*   Game progression (sleep, next day)

It now contains the main interactive game loop for a playable game, facilitated by the Swing GUI.

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
*   **View (`src/main/java/com/spakborhills/view/`)**: Java Swing components (e.g., `GameFrame`, `GamePanel`) responsible for presenting game state to the user and capturing input. This has evolved from the initially intended CLI views.
*   **Controller (`src/main/java/com/spakborhills/controller/`)**: `GameController` handles user input from the View (Swing events like key presses), updates the Model based on these inputs, and triggers View refreshes to reflect changes in the game state. It acts as the intermediary between the Model and the Swing View.

### c. Game Mechanics (as seen in `Main.java` tests, `diagram.puml`, and GUI implementation)
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

The following areas represent next steps and potential areas for further development and LLM assistance to fully integrate the game model with the Swing GUI and complete the project:

*   **Implementing Remaining Game Mechanics and Integrating with Swing GUI:** This is the primary focus for completing the core game. Key areas include:
    *   **Crop Lifecycle:** Full implementation of `Tile.updateDaily()` for plant growth (reacting to watering/rain, potential withering if neglected based on `daysSinceWateredOrRainKeepsPlantAlive`), visual updates for different growth stages in `GamePanel` (e.g., distinct visuals for `plantedSeed` and `isHarvestable`), and robust harvesting mechanics via `Player.harvest()` and `GameController.requestHarvestAtPlayerPosition()`.
    *   **Advanced Item Interactions:** Implementing the `Player.eat(Food)` mechanism for consuming food items and restoring energy, with corresponding actions in `GameController` (e.g., `requestConsumeSelectedItem()`) and key bindings in `GamePanel`.
    *   **NPC Mechanics:**
        *   Displaying NPCs on the map, potentially with simple schedules or static positions.
        *   Implementing basic dialogue interactions (`Player.talkTo(NPC)`) triggered from `GameController` (e.g., `requestTalkToNearestNPC()`) and displayed in the GUI (e.g., a dedicated text area).
        *   Future Enhancements: Gift-giving mechanics and tracking relationship statuses.
    *   **Economy and Commerce:**
        *   Developing a `Shop` system (both UI, possibly `ShopPanel.java`, and backend logic) for purchasing items. This includes displaying available items (potentially varying by season/day) and handling transactions. Integration with `GameController` for opening the shop (e.g., via NPC interaction or entering a shop area).
        *   Implementing the `ShippingBin` functionality for selling items. This involves UI for adding items to the bin (could be part of inventory or a dedicated `ShippingBinPanel`) and end-of-day processing in `GameController.advanceDay()` to add gold to the player.
    *   **Game Progression and Time:**
        *   Full implementation of the sleep mechanism (`GameController.requestPlayerSleep()`) to advance the day. This should trigger `GameTime.advanceDay()`, `player.sleep()` (energy restoration), `farmMap.updateAllTilesDaily()`, processing of the shipping bin, and potentially NPC schedule updates.
        *   Ensuring the pass-out mechanic (due to `player.getEnergy() <= Player.MIN_ENERGY`) correctly triggers a forced sleep/day advance.
    *   **Expanded Content (as per specification and development time):**
        *   Implementing core activities like **Fishing** (requiring new mechanics, potential mini-game, and fishing spots on maps) and **Cooking** (requiring recipes, ingredients, and a cooking interface).
        *   Developing the **Marriage** system with eligible NPCs.
        *   Creating in-game **Festivals** or special events that occur on specific dates.
        *   Expanding the **`WorldMap`** with all intended areas (e.g., `FOREST_RIVER`, `NPC_HOME`, `OCEAN`, `MOUNTAIN_LAKE` as noted in `notes.txt`) and making them accessible and interactive.
        *   **Saving and Loading Game State:** Implementing functionality to persist player progress, farm state, inventory, relationships, and other relevant game data to a file, and to load this data back into the game.
    *   **Implementing Design Patterns:** The README mentions a requirement for design patterns, which could be explored further as the codebase grows.

*   **Enhancing Swing GUI and User Experience:**
    *   Improving the visual presentation of the game (e.g., better tile graphics, character sprites, item icons if available/creatable).
    *   Adding more sophisticated UI components like custom dialogs for important messages (e.g., level-ups, event notifications), confirmation prompts (e.g., before sleeping, buying expensive items), and potentially a more graphical inventory or shop interface instead of purely text-based.
    *   Refining user interactions, controls, and providing clearer feedback for player actions.

*   **Data Management:**
    *   Transitioning from hardcoded game data (item statistics, NPC dialogues, recipes, shop inventories, etc.) to external configuration files (e.g., JSON, XML, or CSV). This would make the game easier to balance, modify, and expand without recompiling code.

*   **Code Quality and Design:**
    *   Continuously refactoring code for clarity, maintainability, and performance as new features are added.
    *   Applying relevant Object-Oriented Design Patterns to solve common architectural problems and improve the overall structure of the codebase, as often required in academic projects.

*   **Testing and Debugging:**
    *   Expanding unit tests (JUnit) to cover new model and controller logic.
    *   Thoroughly testing all integrated game mechanics through gameplay to identify and fix bugs, ensuring a stable and enjoyable player experience.
