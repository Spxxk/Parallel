package zork.commands;

import zork.Command;

public class Omch extends Command {
    public Omch() { super("Omch"); }

    public void runCommand(String... args) {
        System.out.printf("Hi I'm a %s and my name is Omch!\n", args[0]);
    }

}
