# AGENTS Configuration - 3 en Raya (Tic Tac Toe Bluetooth Game)

## Project Overview
**3 en Raya** is an Android application developed in Kotlin that implements a Tic Tac Toe game with Bluetooth multiplayer support. This AGENTS.md file defines custom agents to assist with development tasks following the step-by-step guide.

---

## Available Agents

### 1. **Android Developer** 
**Expertise:** Android development, Kotlin, Gradle, Android SDK
- Focus on Android-specific implementation
- Handle activity lifecycle, layouts, and Android framework features
- Manage Gradle configuration and dependencies
- Support Android debugging and testing

**When to use:**
- Building UI components and fragments
- Implementing Android lifecycle management
- Configuring build settings or dependencies
- Handling permissions and Android manifest

---

### 2. **Bluetooth Specialist**
**Expertise:** Bluetooth connectivity, wireless communication, paired devices
- Implement Bluetooth discovery and pairing
- Handle Bluetooth socket communication
- Manage device connections and disconnections
- Debug Bluetooth communication issues

**When to use:**
- Setting up Bluetooth connections between devices
- Implementing multiplayer game synchronization
- Handling device discovery and pairing
- Troubleshooting connection problems

---

### 3. **Game Logic Engineer**
**Expertise:** Game mechanics, Tic Tac Toe rules, turn management, win/loss conditions
- Implement game board state management
- Validate moves and enforce game rules
- Handle turn-based gameplay
- Implement AI (if applicable)

**When to use:**
- Building the core game logic
- Validating player moves
- Implementing win detection
- Managing game state and scoring

---

### 4. **Kotlin/JVM Specialist**
**Expertise:** Kotlin language features, coroutines, functional programming
- Leverage Kotlin-specific features (extension functions, coroutines, etc.)
- Handle asynchronous operations with coroutines
- Implement idiomatic Kotlin patterns
- Optimize performance

**When to use:**
- Writing complex Kotlin logic
- Implementing async/concurrent operations
- Using coroutines for background tasks
- Code refactoring and optimization

---

### 5. **Step-by-Step Guide Mentor**
**Expertise:** Development guides, project structure, step progression
- Follow the guided steps from `/steps/` directory
- Ensure implementation matches step requirements
- Help progress through development milestones
- Track completion of each step

**When to use:**
- Starting a new development phase
- Reviewing step requirements
- Ensuring tasks align with guide
- Verifying step completion

---

## Development Workflow

### Standard Development Process
1. Consult the **Step-by-Step Guide Mentor** for current step requirements
2. Select appropriate specialist agent(s) based on task type
3. Implement features following the step guide
4. Test functionality with device or emulator
5. Progress to next step

### File Locations
- **Step Guides:** `/steps/step*.md`
- **Source Code:** `/app/src/main/java/com/ejemplo/tresenraya/`
- **Build Configuration:** `/app/build.gradle.kts`, `/build.gradle.kts`
- **Debug Resources:** `/steps/samsung.md` (device-specific debugging)

---

## Quick Reference

| Task Type | Recommended Agent(s) |
|-----------|---------------------|
| UI Layout & Activities | Android Developer |
| Bluetooth Connection | Bluetooth Specialist + Android Developer |
| Game Rules & Board Logic | Game Logic Engineer |
| Async Operations | Kotlin/JVM Specialist |
| Multiplayer Sync | Bluetooth Specialist + Game Logic Engineer |
| Step Progress | Step-by-Step Guide Mentor |
| Performance Issues | Kotlin/JVM Specialist + Android Developer |

---

## Notes
- Project is structured as a stepped tutorial (check `/steps/` for current progress)
- Target platform: Android (Kotlin)
- Primary feature: Bluetooth multiplayer Tic Tac Toe
- Debugging reference: See `/steps/samsung.md` for device-specific setup
