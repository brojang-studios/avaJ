# avaj

A small custom DSL (Domain Specific Language) implemented in Java.
Files end with **`.avaj`** and consist of `{ ... }` blocks as commands and key-value pairs.

avaj is intentionally a **placeholder**: you can edit or add `.avaj` files without touching the Java interpreter. New commands can be added as `AvajCommand` classes and registered in the `AvajInterpreter` constructor.

> This project is a bit vibe coded but not fully — the core parser and command registry are deliberate, while some UI details were thrown together quickly. Feel free to clean up or replace anything.

---

## Project Structure

```
avaJ/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew / gradlew.bat
├── src/
│   ├── main/java/com/FXYZStudios/ajav/
│   │   ├── Main.java                 # entry point
│   │   └── AvajInterpreter.java      # parser + engine + all commands
│   └── avaj/
│       └── Main.avaj                 # example script
└── README.md
```

---

## Quick Start

### 1. Compile

```bash
./gradlew compileJava
```

### 2. Run

Default script (`src/avaj/Main.avaj`):

```bash
java -cp build/classes/java/main com.FXYZStudios.ajav.Main
```

Custom script:

```bash
java -cp build/classes/java/main com.FXYZStudios.ajav.Main path/to/your/script.avaj
```

---

## `.avaj` Syntax

Each command is wrapped in curly braces:

```
{command_name}
```

Key-value pairs with `=`:

```
{command_name_value=Value}
```

Text values can be wrapped in single quotes:

```
{windowTitle_value='My Window'}
```

Empty blocks `{}` are ignored.

Example:

```
{createWindow}
        {windowTitle_value='Hello avaj'}
        {windowRes_value=800x600}
        {windowVisible_value=true}
        {writeToWindow_value=This is avaj!}
        {button_value=true}
        {buttonText_value='Close'}
        {buttonAction_value=closeWindow}
        {showWindow}
{}
```

---

## Commands

| Command | Description | Example |
|---------|-------------|---------|
| `createWindow` | Creates an empty window (JFrame) with label and button | `{createWindow}` |
| `windowTitle_value` | Sets the window title | `{windowTitle_value='My Title'}` |
| `windowRes_value` | Window size as `WxH` | `{windowRes_value=1024x768}` |
| `windowPos_value` | Window position as `XxY` | `{windowPos_value=100x100}` |
| `windowVisible_value` | Show/hide window (`true`/`false`) | `{windowVisible_value=true}` |
| `windowResizable_value` | Make window resizable (`true`/`false`) | `{windowResizable_value=false}` |
| `windowBgColor_value` | Background color (hex `#RRGGBB` or `red`, `green`, `blue`, `black`, `white`) | `{windowBgColor_value='#2b2b2b'}` |
| `windowFps_value` | FPS value (placeholder, no effect) | `{windowFps_value=60}` |
| `writeToWindow_value` | Write text into the window label | `{writeToWindow_value=Hello World}` |
| `labelVisible_value` | Show/hide label (`true`/`false`) | `{labelVisible_value=true}` |
| `button_value` | Enable/disable button (`true`/`false`) | `{button_value=true}` |
| `buttonText_value` | Button label | `{buttonText_value='Ok'}` |
| `buttonSize_value` | Button size as `WxH` | `{buttonSize_value=120x40}` |
| `buttonVisible_value` | Show/hide button (`true`/`false`) | `{buttonVisible_value=true}` |
| `buttonAction_value` | Action on click (`closeWindow`, otherwise prints to console) | `{buttonAction_value=closeWindow}` |
| `showWindow` | Makes the window visible (pack, center, setVisible) | `{showWindow}` |
| `closeWindow` | Closes the window | `{closeWindow}` |
| `print_value` | Print text to console | `{print_value=Hello Console}` |
| `delay_value` | Wait time in milliseconds | `{delay_value=1000}` |
| `exit` | Exit the program | `{exit}` |

---

## Adding Custom Commands

1. Create a class implementing `AvajInterpreter.AvajCommand`:

```java
private static class MyCommand implements AvajInterpreter.AvajCommand {
    @Override
    public String name() {
        return "myCommand_value";
    }

    @Override
    public void execute(String value, Map<String, String> state, WindowContext ctx) {
        state.put("myCommand.last", value);
        System.out.println("[avaj] myCommand -> " + value);
        // use ctx.frame, ctx.label, ctx.button for UI logic
    }
}
```

2. Register it in the `AvajInterpreter` constructor:

```java
register(new MyCommand());
```

3. Use it in any `.avaj` file:

```
{myCommand_value=Hello}
```

---

## `WindowContext`

Inside `execute`, you have access to `ctx` with:

- `ctx.frame` — the `JFrame` window
- `ctx.label` — the centered `JLabel`
- `ctx.button` — the bottom `JButton`
- `ctx.created` — whether `createWindow` was called

---

## Notes

- Commands are executed **in order** as they appear in the `.avaj` file.
- `showWindow` calls `pack()`, which resizes the window to fit its content. If you want a fixed size via `windowRes_value`, place it **after** `showWindow` or omit `showWindow`.
- Colors can be hex (`#RRGGBB`) or names (`red`, `green`, `blue`, `black`, `white`).
- Values in single quotes (`'...'`) are automatically unquoted.

---

Have fun with avaj — your own language, your own windows. 🚀
