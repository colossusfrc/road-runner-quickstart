package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

public class Robot extends LinearOpMode {
    private static Robot robot;

    protected Robot(){}
    public static synchronized Robot getInstance(){
        if(robot==null){
            robot = new Robot();
        }
        return robot;
    }
    @Override
    public void runOpMode(){}
}
