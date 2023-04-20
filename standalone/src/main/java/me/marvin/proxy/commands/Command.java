package me.marvin.proxy.commands;

import java.io.IOException;

/**
 * Simple command.
 */
public interface Command {
    boolean execute(String[] parameters) throws IOException;
}
