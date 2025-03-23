package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.teamcode.FTCCommandBased.CommandUtility.CommandScheduler;

public class Main extends Robot {
    @Override
    public void runOpMode(){
        waitForStart();
        new RobotContainer(gamepad1);
        while(opModeIsActive()){
            CommandScheduler.getInstance().run();
        }
    }
}
