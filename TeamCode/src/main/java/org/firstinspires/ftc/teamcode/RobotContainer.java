package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Rotation2d;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.FTCCommandBased.EventUtility.Trigger;
import org.firstinspires.ftc.teamcode.RoadRunnerCommands.RoadRunnerFactory;

public class RobotContainer {
    private final Gamepad gamepad;
    public RobotContainer(Gamepad gamepad){
        this.gamepad = gamepad;
        configureBindings();
    }
    private void configureBindings(){
        new Trigger(()->gamepad.a)
                .toggleOnTrue(
                        RoadRunnerFactory.splineToLinearHeading(
                                new Pose2d(1.0 ,0.0, 0.0),
                                new Rotation2d(0.0, 0.0)
                        )
                );
    }
}
