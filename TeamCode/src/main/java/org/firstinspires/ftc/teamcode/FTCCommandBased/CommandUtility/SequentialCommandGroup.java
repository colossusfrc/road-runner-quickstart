package org.firstinspires.ftc.teamcode.FTCCommandBased.CommandUtility;

public class SequentialCommandGroup extends CommandGroup{
    public SequentialCommandGroup(Command... commands){
        super(commands);
    }

    @Override
    protected void initialize() {
        super.commands.getFirst().initialize();
    }

    @Override
    protected void execute() {
        if(!super.commands.getFirst().isFinished()){
            super.commands.getFirst().execute();
        }else{
            super.commands.getFirst().end(false);
            super.commands.removeFirst();
            if(isFinished())return;
            initialize();
        }
    }

    @Override
    protected boolean isFinished() {
        return super.commands.isEmpty();
    }
}

