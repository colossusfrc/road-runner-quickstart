package org.firstinspires.ftc.teamcode.FTCCommandBased.CommandUtility;
import org.firstinspires.ftc.teamcode.FTCCommandBased.SubsystemUtility.Subsystem;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class Command{
    private final Set<Subsystem> m_requirements = new HashSet<>();
    protected Command(){
    }
    protected void initialize(){}
    protected void execute(){}
    protected void end(boolean interrupted){}
    protected boolean isFinished(){ return false; }
    public Set<Subsystem> getRequirements(){
        return this.m_requirements;
    }

    public final void addRequirements(Subsystem... requirements){
        this.m_requirements.addAll(Arrays.asList(requirements));
    }
    public final void addRequirements(Collection<Subsystem> requirements){
        this.m_requirements.addAll(requirements);
    }
    public void schedule(){
        CommandScheduler.getInstance().schedule(this);
    }
    public void cancel(){
        CommandScheduler.getInstance().cancel(this);
    }
    public boolean isScheduled(){
        return CommandScheduler.getInstance().isScheduled(this);
    }
    public boolean hasRequirement(Subsystem requirement){return getRequirements().contains(requirement);}
    public InterruptBehavior getInterruptionBehavior() {
        return InterruptBehavior.cancelSelf;
    }
    public boolean runsWhenDisabled(){ return false; }
    public enum InterruptBehavior{
        cancelSelf,
        cancelIncoming
    }

}