package com.FXYZStudios.ajav;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * avaj - a DSL implemented in Java that opens real desktop windows.
 * Files end with .avaj and use { ... } blocks as commands / key-value pairs.
 *
 * This is a placeholder: users can edit or add .avaj files and extend the
 * command set below without touching the interpreter core.
 */
public final class AvajInterpreter {

    private final Map<String, AvajCommand> commands = new HashMap<>();
    private final Map<String, String> state = new HashMap<>();
    private final WindowContext ctx = new WindowContext();

    public AvajInterpreter() {
        register(new CreateWindowCommand());
        register(new WriteToWindowCommand());
        register(new ButtonCommand());
        register(new CloseWindowCommand());
        register(new ShowWindowCommand());
        register(new WindowTitleCommand());
        register(new WindowVisibleCommand());
        register(new WindowResCommand());
        register(new WindowPosCommand());
        register(new ButtonTextCommand());
        register(new ButtonSizeCommand());
        register(new ButtonActionCommand());
        register(new ButtonVisibleCommand());
        register(new LabelVisibleCommand());
        register(new WindowBgColorCommand());
        register(new WindowFpsCommand());
        register(new WindowResizableCommand());
        register(new PrintCommand());
        register(new DelayCommand());
        register(new ExitCommand());
    }

    public void register(AvajCommand command) {
        commands.put(command.name(), command);
    }

    public void run(Path script) throws IOException {
        List<String> lines = Files.readAllLines(script);
        List<AvajBlock> blocks = parse(lines);
        execute(blocks);
    }

    private List<AvajBlock> parse(List<String> lines) {
        List<AvajBlock> blocks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;

        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) {
                continue;
            }

            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == '{') {
                    if (depth == 0) {
                        current = new StringBuilder();
                    }
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        String content = current.toString().trim();
                        if (!content.isEmpty()) {
                            blocks.add(new AvajBlock(content));
                        }
                    }
                } else if (depth > 0) {
                    current.append(c);
                }
            }
        }
        return blocks;
    }

    private void execute(List<AvajBlock> blocks) {
        SwingUtilities.invokeLater(() -> {
            for (AvajBlock block : blocks) {
                String content = block.content();

                int eq = content.indexOf('=');
                if (eq > 0) {
                    String key = content.substring(0, eq).trim();
                    String value = stripQuotes(content.substring(eq + 1).trim());
                    state.put(key, value);

                    AvajCommand cmd = commands.get(key);
                    if (cmd != null) {
                        cmd.execute(value, state, ctx);
                    } else {
                        System.out.println("[avaj] unknown property: " + key + " = " + value);
                    }
                } else {
                    String cmdName = content.trim();
                    AvajCommand cmd = commands.get(cmdName);
                    if (cmd != null) {
                        cmd.execute(null, state, ctx);
                    } else {
                        System.out.println("[avaj] unknown command: " + cmdName);
                    }
                }
            }
        });
    }

    private static String stripQuotes(String v) {
        if (v != null && v.length() >= 2 && v.startsWith("'") && v.endsWith("'")) {
            return v.substring(1, v.length() - 1);
        }
        return v;
    }

    public static void main(String[] args) throws Exception {
        Path script;
        if (args.length > 0) {
            script = Path.of(args[0]);
        } else {
            script = Path.of("src/avaj/Main.avaj");
        }

        AvajInterpreter interpreter = new AvajInterpreter();
        interpreter.run(script);
    }

    private record AvajBlock(String content) {
    }

    public interface AvajCommand {
        String name();

        void execute(String value, Map<String, String> state, WindowContext ctx);
    }

    static class WindowContext {
        JFrame frame;
        JLabel label;
        JButton button;
        boolean created = false;
    }

    private static class CreateWindowCommand implements AvajCommand {
        @Override
        public String name() {
            return "createWindow";
        }

        @Override
        public void execute(String value, Map<String, String> state, WindowContext ctx) {
            ctx.frame = new JFrame("avaJ");
            ctx.label = new JLabel("", JLabel.CENTER);
            ctx.button = new JButton();
            ctx.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            ctx.frame.setLayout(new BorderLayout());
            ctx.frame.add(ctx.label, BorderLayout.CENTER);
            ctx.frame.add(ctx.button, BorderLayout.SOUTH);
            ctx.created = true;
            System.out.println("[avaj] createWindow -> window created");
        }
    }

    private static class WriteToWindowCommand implements AvajCommand {
        @Override
        public String name() {
            return "writeToWindow_value";
        }

        @Override
        public void execute(String value, Map<String, String> state, WindowContext ctx) {
            state.put("window.text", value == null ? "" : value);
            if (ctx.label != null) {
                ctx.label.setText(value);
            }
            System.out.println("[avaj] writeToWindow -> " + value);
        }
    }

    private static class ButtonCommand implements AvajCommand {
        @Override
        public String name() {
            return "button_value";
        }

        @Override
        public void execute(String value, Map<String, String> state, WindowContext ctx) {
            state.put("button.enabled", value == null ? "false" : value);
            if (ctx.button != null) {
                ctx.button.setEnabled(Boolean.parseBoolean(value == null ? "false" : value));
            }
            System.out.println("[avaj] button -> enabled=" + value);
        }
    }

    private static class ButtonSizeCommand implements AvajCommand {
        @Override
        public String name() {
            return "buttonSize_value";
        }

        @Override
        public void execute(String value, Map<String, String> state, WindowContext ctx) {
            state.put("button.size", value == null ? "" : value);
            if (ctx.button != null && value != null) {
                String[] parts = value.split("x");
                if (parts.length == 2) {
                    try {
                        int w = Integer.parseInt(parts[0].trim());
                        int h = Integer.parseInt(parts[1].trim());
                        ctx.button.setPreferredSize(new Dimension(w, h));
                    } catch (NumberFormatException e) {
                        System.out.println("[avaj] invalid button size: " + value);
                    }
                }
            }
            System.out.println("[avaj] buttonSize -> " + value);
        }
    }

    private static class CloseWindowCommand implements AvajCommand {
        @Override
        public String name() {
            return "closeWindow";
        }

        @Override
        public void execute(String value, Map<String, String> state, WindowContext ctx) {
            state.put("window.closed", "true");
            if (ctx.frame != null) {
                ctx.frame.dispose();
            }
            System.out.println("[avaj] closeWindow -> window closed");
        }
    }

    private static class ShowWindowCommand implements AvajCommand {
        @Override
        public String name() {
            return "showWindow";
        }

        @Override
        public void execute(String value, Map<String, String> state, WindowContext ctx) {
            state.put("window.visible", "true");
            if (ctx.frame != null) {
                ctx.frame.pack();
                ctx.frame.setLocationRelativeTo(null);
                ctx.frame.setVisible(true);
            }
            System.out.println("[avaj] showWindow -> window shown");
        }
    }

    private static class WindowTitleCommand implements AvajCommand {
        @Override
        public String name() {
            return "windowTitle_value";
        }

        @Override
        public void execute(String value, Map<String, String> state, WindowContext ctx) {
            state.put("window.title", value == null ? "" : value);
            if (ctx.frame != null) {
                ctx.frame.setTitle(value);
            }
            System.out.println("[avaj] windowTitle -> " + value);
        }
    }

    private static class WindowVisibleCommand implements AvajCommand {
        @Override
        public String name() {
            return "windowVisible_value";
        }

        @Override
        public void execute(String value, Map<String, String> state, WindowContext ctx) {
            state.put("window.visible", value == null ? "false" : value);
            if (ctx.frame != null) {
                ctx.frame.setVisible(Boolean.parseBoolean(value == null ? "false" : value));
            }
            System.out.println("[avaj] windowVisible -> " + value);
        }
    }

    private static class WindowResCommand implements AvajCommand {
        @Override
        public String name() {
            return "windowRes_value";
        }

        @Override
        public void execute(String value, Map<String, String> state, WindowContext ctx) {
            state.put("window.res", value == null ? "" : value);
            if (ctx.frame != null && value != null) {
                String[] parts = value.split("x");
                if (parts.length == 2) {
                    try {
                        int w = Integer.parseInt(parts[0].trim());
                        int h = Integer.parseInt(parts[1].trim());
                        ctx.frame.setSize(w, h);
                        ctx.frame.setPreferredSize(new Dimension(w, h));
                    } catch (NumberFormatException e) {
                        System.out.println("[avaj] invalid resolution: " + value);
                    }
                }
            }
            System.out.println("[avaj] windowRes -> " + value);
        }
    }

    private static class WindowPosCommand implements AvajCommand {
        @Override
        public String name() {
            return "windowPos_value";
        }

        @Override
        public void execute(String value, Map<String, String> state, WindowContext ctx) {
            state.put("window.pos", value == null ? "" : value);
            if (ctx.frame != null && value != null) {
                String[] parts = value.split("x");
                if (parts.length == 2) {
                    try {
                        int x = Integer.parseInt(parts[0].trim());
                        int y = Integer.parseInt(parts[1].trim());
                        ctx.frame.setLocation(x, y);
                    } catch (NumberFormatException e) {
                        System.out.println("[avaj] invalid position: " + value);
                    }
                }
            }
            System.out.println("[avaj] windowPos -> " + value);
        }
    }

    private static class ButtonTextCommand implements AvajCommand {
        @Override
        public String name() {
            return "buttonText_value";
        }

        @Override
        public void execute(String value, Map<String, String> state, WindowContext ctx) {
            state.put("button.text", value == null ? "" : value);
            if (ctx.button != null) {
                ctx.button.setText(value);
            }
            System.out.println("[avaj] buttonText -> " + value);
        }
    }

    private static class ButtonVisibleCommand implements AvajCommand {
        @Override
        public String name() {
            return "buttonVisible_value";
        }

        @Override
        public void execute(String value, Map<String, String> state, WindowContext ctx) {
            state.put("button.visible", value == null ? "false" : value);
            if (ctx.button != null) {
                ctx.button.setVisible(Boolean.parseBoolean(value == null ? "false" : value));
            }
            System.out.println("[avaj] buttonVisible -> " + value);
        }
    }

    private static class LabelVisibleCommand implements AvajCommand {
        @Override
        public String name() {
            return "labelVisible_value";
        }

        @Override
        public void execute(String value, Map<String, String> state, WindowContext ctx) {
            state.put("label.visible", value == null ? "false" : value);
            if (ctx.label != null) {
                ctx.label.setVisible(Boolean.parseBoolean(value == null ? "false" : value));
            }
            System.out.println("[avaj] labelVisible -> " + value);
        }
    }

    private static class ButtonActionCommand implements AvajCommand {
        @Override
        public String name() {
            return "buttonAction_value";
        }

        @Override
        public void execute(String value, Map<String, String> state, WindowContext ctx) {
            state.put("button.action", value == null ? "" : value);
            if (ctx.button != null) {
                for (ActionListener al : ctx.button.getActionListeners()) {
                    ctx.button.removeActionListener(al);
                }
                switch (value) {
                    case "closeWindow" -> ctx.button.addActionListener(e -> {
                        if (ctx.frame != null) {
                            ctx.frame.dispose();
                        }
                    });
                    default -> ctx.button.addActionListener(e -> System.out.println("[avaj] button clicked: " + value));
                }
            }
            System.out.println("[avaj] buttonAction -> " + value);
        }
    }

    private static class WindowBgColorCommand implements AvajCommand {
        @Override
        public String name() {
            return "windowBgColor_value";
        }

        @Override
        public void execute(String value, Map<String, String> state, WindowContext ctx) {
            state.put("window.bgColor", value == null ? "" : value);
            if (ctx.frame != null && value != null) {
                try {
                    ctx.frame.getContentPane().setBackground(Color.decode(value));
                } catch (Exception e) {
                    switch (value.toLowerCase()) {
                        case "red" -> ctx.frame.getContentPane().setBackground(Color.RED);
                        case "green" -> ctx.frame.getContentPane().setBackground(Color.GREEN);
                        case "blue" -> ctx.frame.getContentPane().setBackground(Color.BLUE);
                        case "black" -> ctx.frame.getContentPane().setBackground(Color.BLACK);
                        case "white" -> ctx.frame.getContentPane().setBackground(Color.WHITE);
                        default -> System.out.println("[avaj] unknown color: " + value);
                    }
                }
            }
            System.out.println("[avaj] windowBgColor -> " + value);
        }
    }

    private static class WindowFpsCommand implements AvajCommand {
        @Override
        public String name() {
            return "windowFps_value";
        }

        @Override
        public void execute(String value, Map<String, String> state, WindowContext ctx) {
            state.put("window.fps", value == null ? "" : value);
            System.out.println("[avaj] windowFps -> " + value);
        }
    }

    private static class WindowResizableCommand implements AvajCommand {
        @Override
        public String name() {
            return "windowResizable_value";
        }

        @Override
        public void execute(String value, Map<String, String> state, WindowContext ctx) {
            state.put("window.resizable", value == null ? "false" : value);
            if (ctx.frame != null) {
                ctx.frame.setResizable(Boolean.parseBoolean(value == null ? "false" : value));
            }
            System.out.println("[avaj] windowResizable -> " + value);
        }
    }

    private static class PrintCommand implements AvajCommand {
        @Override
        public String name() {
            return "print_value";
        }

        @Override
        public void execute(String value, Map<String, String> state, WindowContext ctx) {
            state.put("print.last", value == null ? "" : value);
            System.out.println("[avaj] print -> " + value);
        }
    }

    private static class DelayCommand implements AvajCommand {
        @Override
        public String name() {
            return "delay_value";
        }

        @Override
        public void execute(String value, Map<String, String> state, WindowContext ctx) {
            state.put("delay.last", value == null ? "0" : value);
            if (value != null) {
                try {
                    Thread.sleep(Long.parseLong(value.trim()));
                } catch (NumberFormatException | InterruptedException e) {
                    System.out.println("[avaj] invalid delay: " + value);
                }
            }
            System.out.println("[avaj] delay -> " + value);
        }
    }

    private static class ExitCommand implements AvajCommand {
        @Override
        public String name() {
            return "exit";
        }

        @Override
        public void execute(String value, Map<String, String> state, WindowContext ctx) {
            System.out.println("[avaj] exit -> bye");
            System.exit(0);
        }
    }
}
