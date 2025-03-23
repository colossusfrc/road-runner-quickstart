package org.firstinspires.ftc.teamcode.FTCCommandBased.SubsystemUtility;

public interface Subsystem {
    default void periodic(){}
    default String getName(){
        return this.getClass().getSimpleName();
    }
    default void setDefaultOCmand(){

    }


}
