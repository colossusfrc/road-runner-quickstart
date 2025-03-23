package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.Pose2d;

import org.firstinspires.ftc.teamcode.RoadRunnerUtility.MecanumDrive;

public final class Constants {

    private static final Pose2d beginPose = new Pose2d(0.0, 0.0, 0.0);
    public static class Instances{
        public static final MecanumDrive mecanumDrive = new MecanumDrive(
                Main.getInstance().hardwareMap,
                beginPose);
    }
}
