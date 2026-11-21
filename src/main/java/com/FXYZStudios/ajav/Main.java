package com.FXYZStudios.ajav;

import java.nio.file.Path;

/**
 * Entry point for the avaj DSL runner.
 * Usage: java com.FXYZStudios.ajav.Main [script.avaj]
 * Defaults to src/avaj/Main.avaj when no argument is given.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        Path script = (args.length > 0) ? Path.of(args[0]) : Path.of("src/avaj/Main.avaj");
        new AvajInterpreter().run(script);
    }
}
