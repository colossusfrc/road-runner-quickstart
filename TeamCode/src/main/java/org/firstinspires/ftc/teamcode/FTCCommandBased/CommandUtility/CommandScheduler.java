package org.firstinspires.ftc.teamcode.FTCCommandBased.CommandUtility;


import com.qualcomm.robotcore.eventloop.EventLoop;

import org.firstinspires.ftc.teamcode.FTCCommandBased.EventUtility.Event;
import org.firstinspires.ftc.teamcode.FTCCommandBased.SubsystemUtility.Subsystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;


/**
 * The scheduler responsible for running {@link Command}s. A Command-based robot should call {@link
 * CommandScheduler#run()} on the singleton instance in its periodic block in order to run commands
 * synchronously from the main loop. Subsystems should be registered with the scheduler using {@link
 * CommandScheduler#registerSubsystem(Subsystem...)} in order for their {@link Subsystem#periodic()}
 * methods to be called and for their default commands to be scheduled.
 *
 * <p>This class is provided by the NewCommands VendorDep
 */
public final class CommandScheduler{
    /** The Singleton Instance. */
    private static CommandScheduler instance;

    /**
     * Returns the Scheduler instance.
     *
     * @return the instance
     */
    public static synchronized CommandScheduler getInstance() {
        if (instance == null) {
            instance = new CommandScheduler();
        }
        return instance;
    }

    // A set of the currently-running commands.
    private final Set<Command> m_scheduledCommands = new LinkedHashSet<>();

    // A map from required subsystems to their requiring commands. Also used as a set of the
    // currently-required subsystems.
    private final Map<Subsystem, Command> m_requirements = new LinkedHashMap<>();

    // A map from subsystems registered with the scheduler to their default commands.  Also used
    // as a list of currently-registered subsystems.
    private final Map<Subsystem, Command> m_subsystems = new LinkedHashMap<>();

    private final Event m_defaultButtonLoop = new Event();
    // The set of currently-registered buttons that will be polled every iteration.
    private Event m_activeButtonLoop = m_defaultButtonLoop;

    private boolean m_disabled;

    // Lists of user-supplied actions to be executed on scheduling events for every command.
    private final List<Consumer<Command>> m_initActions = new ArrayList<>();
    private final List<Consumer<Command>> m_executeActions = new ArrayList<>();
    private final List<Consumer<Command>> m_finishActions = new ArrayList<>();

    // Flag and queues for avoiding ConcurrentModificationException if commands are
    // scheduled/canceled during run
    private boolean m_inRunLoop;
    private final Set<Command> m_toSchedule = new LinkedHashSet<>();
    private final List<Command> m_toCancelCommands = new ArrayList<>();
    private final List<Optional<Command>> m_toCancelInterruptors = new ArrayList<>();
    private final Set<Command> m_endingCommands = new LinkedHashSet<>();


    CommandScheduler() {}


    /**
     * Get the default button poll.
     *
     * @return a reference to the default {@link EventLoop} object polling buttons.
     */
    public Event getDefaultButtonLoop() {
        return m_defaultButtonLoop;
    }

    /**
     * Initializes a given command, adds its requirements to the list, and performs the init actions.
     *
     * @param command The command to initialize
     * @param requirements The command requirements
     */
    private void initCommand(Command command, Set<Subsystem> requirements) {
        m_scheduledCommands.add(command);
        for (Subsystem requirement : requirements) {
            m_requirements.put(requirement, command);
        }
        command.initialize();
        for (Consumer<Command> action : m_initActions) {
            action.accept(command);
        }
    }

    /**
     * Schedules a command for execution. Does nothing if the command is already scheduled. If a
     * command's requirements are not available, it will only be started if all the commands currently
     * using those requirements have been scheduled as interruptible. If this is the case, they will
     * be interrupted and the command will be scheduled.
     *
     * <p>WARNING: using this function directly can often lead to unexpected behavior and should be
     * avoided. Instead Triggers should be used to schedule Commands.
     *
     * @param command the command to schedule. If null, no-op.
     */
    private void schedule(Command command) {
        if (command == null) {
            return;
        }
        if (m_inRunLoop) {
            m_toSchedule.add(command);
            return;
        }

        // Do nothing if the scheduler is disabled, the robot is disabled and the command doesn't
        // run when disabled, or the command is already scheduled.
        if (m_disabled
                || isScheduled(command)) {
            return;
        }

        Set<Subsystem> requirements = command.getRequirements();

        // Schedule the command if the requirements are not currently in-use.
        if (Collections.disjoint(m_requirements.keySet(), requirements)) {
            initCommand(command, requirements);
        } else {
            // Else check if the requirements that are in use have all have interruptible commands,
            // and if so, interrupt those commands and schedule the new command.
            for (Subsystem requirement : requirements) {
                Command requiring = requiring(requirement);
                if (requiring != null
                        && requiring.getInterruptionBehavior() == Command.InterruptBehavior.cancelIncoming) {
                    return;
                }
            }
            for (Subsystem requirement : requirements) {
                Command requiring = requiring(requirement);
                if (requiring != null) {
                    cancel(requiring);
                }
            }
            initCommand(command, requirements);
        }
    }

    /**
     * Schedules multiple commands for execution. Does nothing for commands already scheduled.
     *
     * <p>WARNING: using this function directly can often lead to unexpected behavior and should be
     * avoided. Instead Triggers should be used to schedule Commands.
     *
     * @param commands the commands to schedule. No-op on null.
     */
    public void schedule(Command... commands) {
        for (Command command : commands) {
            schedule(command);
        }
    }

    /**
     * Runs a single iteration of the scheduler. The execution occurs in the following order:
     *
     * <p>Subsystem periodic methods are called.
     *
     * <p>Button bindings are polled, and new commands are scheduled from them.
     *
     * <p>Currently-scheduled commands are executed.
     *
     * <p>End conditions are checked on currently-scheduled commands, and commands that are finished
     * have their end methods called and are removed.
     *
     * <p>Any subsystems not being used as requirements have their default methods started.
     */
    public void run() {
        if (m_disabled) {
            return;
        }
        //Para cada um dos subssitemas registrados, sempre executamos o métod0 periodic
        for (Subsystem subsystem : m_subsystems.keySet()) {
            subsystem.periodic();
        }

        //Congelamos o estado dos botões
        Event loopCache = m_activeButtonLoop;
        //executamos o agendamento ou cancelamento pelos botões
        //a partir das condições repassadas pela classe Trigger.
        loopCache.poll();

        m_inRunLoop = true;
        boolean isDisabled = false;
        //executa os comandos agendados, cancela os comandos finalizados
        for (Iterator<Command> iterator = m_scheduledCommands.iterator(); iterator.hasNext(); ) {
            Command command = iterator.next();

            if (isDisabled && !command.runsWhenDisabled()) {
                cancel(command);
                continue;
            }
            //aqui, executamos o comando
            command.execute();
            for (Consumer<Command> action : m_executeActions) {
                action.accept(command);
            }
            //Aqui, encerramos o comando
            if (command.isFinished()) {
                m_endingCommands.add(command);
                command.end(false);
                for (Consumer<Command> action : m_finishActions) {
                    action.accept(command);
                }
                m_endingCommands.remove(command);
                iterator.remove();

                m_requirements.keySet().removeAll(command.getRequirements());
            }
        }
        m_inRunLoop = false;

        //agenda os comandos que foram agendados fora dos triggers ou dos subsistemas(só uma vez)
        for (Command command : m_toSchedule) {
            schedule(command);
        }

        for (int i = 0; i < m_toCancelCommands.size(); i++) {
            cancel(m_toCancelCommands.get(i));
        }

        m_toSchedule.clear();
        m_toCancelCommands.clear();
        m_toCancelInterruptors.clear();

        //executa os comandos padrões dos subsistemas
        for (Map.Entry<Subsystem, Command> subsystemCommand : m_subsystems.entrySet()) {
            if (!m_requirements.containsKey(subsystemCommand.getKey())
                    && subsystemCommand.getValue() != null) {
                schedule(subsystemCommand.getValue());
            }
        }
    }

    /**
     * Registers subsystems with the scheduler. This must be called for the subsystem's periodic block
     * to run when the scheduler is run, and for the subsystem's default command to be scheduled. It
     * is recommended to call this from the constructor of your subsystem implementations.
     *
     * @param subsystems the subsystem to register
     */
    public void registerSubsystem(Subsystem... subsystems) {
        for (Subsystem subsystem : subsystems) {
            if (subsystem == null) {
                continue;
            }
            if (m_subsystems.containsKey(subsystem)) {
                continue;
            }
            m_subsystems.put(subsystem, null);
        }
    }
    /**
     * Sets the default command for a subsystem. Registers that subsystem if it is not already
     * registered. Default commands will run whenever there is no other command currently scheduled
     * that requires the subsystem. Default commands should be written to never end (i.e. their {@link
     * Command#isFinished()} method should return false), as they would simply be re-scheduled if they
     * do. Default commands must also require their subsystem.
     *
     * @param subsystem the subsystem whose default command will be set
     * @param defaultCommand the default command to associate with the subsystem
     */
    public void setDefaultCommand(Subsystem subsystem, Command defaultCommand) {
        if (subsystem == null) {
            return;
        }
        if (defaultCommand == null) {
            return;
        }

        if (!defaultCommand.getRequirements().contains(subsystem)) {
            throw new IllegalArgumentException("Default commands must require their subsystem!");
        }

        m_subsystems.put(subsystem, defaultCommand);
    }

    /**
     * Removes the default command for a subsystem. The current default command will run until another
     * command is scheduled that requires the subsystem, at which point the current default command
     * will not be re-scheduled.
     *
     * @param subsystem the subsystem whose default command will be removed
     */
    public void removeDefaultCommand(Subsystem subsystem) {
        if (subsystem == null) {
            return;
        }

        m_subsystems.put(subsystem, null);
    }
    public void cancel(Command... commands) {
        for (Command command : commands) {
            cancel(command);
        }
    }

    private void cancel(Command command) {
        if (command == null) {
            return;
        }
        if (m_endingCommands.contains(command)) {
            return;
        }
        if (m_inRunLoop) {
            m_toCancelCommands.add(command);
            return;
        }
        if (!isScheduled(command)) {
            return;
        }

        m_endingCommands.add(command);
        command.end(true);
        m_endingCommands.remove(command);
        m_scheduledCommands.remove(command);
        m_requirements.keySet().removeAll(command.getRequirements());
    }
    public boolean isScheduled(Command command) {
        return m_scheduledCommands.contains(command);
    }

    /**
     * Returns the command currently requiring a given subsystem. Null if no command is currently
     * requiring the subsystem
     *
     * @param subsystem the subsystem to be inquired about
     * @return the command currently requiring the subsystem, or null if no command is currently
     *     scheduled
     */
    public Command requiring(Subsystem subsystem) {
        return m_requirements.get(subsystem);
    }
    private StackTraceElement[] stripFrameworkStackElements(StackTraceElement[] stacktrace) {
        int i = stacktrace.length - 1;
        for (; i > 0; i--) {
            if (stacktrace[i].getClassName().startsWith("edu.wpi.first.wpilibj2.command.")) {
                break;
            }
        }
        return Arrays.copyOfRange(stacktrace, i, stacktrace.length);
    }
}
