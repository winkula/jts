package ch.bfh.ti.jts.simulation;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import ch.bfh.ti.jts.console.Console;
import ch.bfh.ti.jts.console.commands.Command;
import ch.bfh.ti.jts.data.Net;
import ch.bfh.ti.jts.utils.layers.Layers;

/**
 * Simulates traffic on a @{link ch.bfh.ti.jts.data.Net}
 *
 * @author ente
 */
public class Simulation {
    
    /**
     * Factor by which the simulation should take place. 1 means real time
     * speed.
     */
    private final static double  TIME_FACTOR = 1;
    
    /**
     * Commands the simulation should execute.
     */
    private final Queue<Command> commands    = new ConcurrentLinkedQueue<>();
    
    private Console              console;
    
    /**
     * If the simulation should call the think method of each agent in every
     * step. If false, the simulation is "dumb" and does only the basic physics
     * (for example the gui thred).
     */
    private final boolean        doThink;
    
    public Simulation() {
        this(true);
    }
    
    public Simulation(boolean doThink) {
        this.doThink = doThink;
    }
    
    public void addCommand(final Command command) {
        commands.add(command);
    }
    
    public Console getConsole() {
        return console;
    }
    
    public void setConsole(final Console console) {
        this.console = console;
    }
    
    /**
     * Do a simulation step
     */
    public void tick(final Net simulateNet) {
        // Time that has passed since the last simulation step [s].
        double timeDelta = simulateNet.tick() * TIME_FACTOR;
        // execute all commands
        while (commands.size() > 0) {
            final Command command = commands.poll();
            final Class<?> targetType = command.getTargetType();
            simulateNet.getElementStream(targetType).forEach(element -> {
                getConsole().write(command.execute(element));
            });
        }
        
        if (doThink) {
            // think
            simulateNet.getThinkableStream().forEach(e -> {
                e.think();
            });
        }
        
        // simulate
        // delegate simulation to @{link Simulatable}s
        final Layers<Simulatable> simulatables = simulateNet.getSimulatable();
        for (final int layer : simulatables.getLayersIterator()) {
            simulatables.getLayerStream(layer).sequential().forEach(e -> {
                e.simulate(timeDelta);
            });
        }
    }
}
