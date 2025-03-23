package org.firstinspires.ftc.teamcode.FTCCommandBased.CommandUtility;
import java.util.LinkedList;

public abstract class CommandGroup extends Command{
    protected LinkedList<Command> commands = new LinkedList<>();
    protected InterruptBehavior interruptBehavior = InterruptBehavior.cancelIncoming;
    protected CommandGroup(Command...commands){
        addCommands(commands);
    }
    protected void addCommands(Command... commands){
        for(Command command : commands){
            this.commands.add(command);
            addRequirements(command.getRequirements());
            if(command.getInterruptionBehavior()==InterruptBehavior.cancelSelf){
                this.interruptBehavior = InterruptBehavior.cancelSelf;
            }
        }
    }
}