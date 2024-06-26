package frc.robot.commands.auto;

import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Robot;
import frc.robot.RobotContainer;
import frc.robot.commands.AutoWithInit;
import frc.robot.commands.FollowNote;
import frc.robot.commands.Ploop;
import frc.robot.commands.SetShooter;
import frc.robot.commands.ShooterMode;
import frc.robot.commands.TurnAndShoot;
import frc.robot.commands.paths.DriveTrainConfig;
import frc.robot.commands.paths.MultiPartPath;

public class GeneralAuto extends AutoWithInit {
    public GeneralAuto() {
    }

    static SendableChooser<String> chooser = new SendableChooser<String>();

    public static void putToDashboard() {
        SmartDashboard.putBoolean("Mid Line?", false);
        SmartDashboard.putBoolean("do 1?", false);
        SmartDashboard.putBoolean("do 2?", false);
        SmartDashboard.putBoolean("do 3?", false);
        SmartDashboard.putBoolean("far 1", false);
        SmartDashboard.putBoolean("far 2", false);
        SmartDashboard.putBoolean("far 3", false);
        SmartDashboard.putBoolean("far 4", false);
        SmartDashboard.putBoolean("far 5", false);
        SmartDashboard.putBoolean("ShootFirst", false);
        SmartDashboard.putBoolean("ShootSecond", false);

        chooser.addOption("Amp", "Left");
        chooser.addOption("Speaker", "Speaker");
        chooser.addOption("Source", "Right");
        SmartDashboard.putData(chooser);
    }

    @Override
    public void initializeCommands() {
        // !PATHWEAVER_INFO: {"trackWidth":0.9144,"gameName":"Crescendo"}
        boolean doLastPart = SmartDashboard.getBoolean("DoLastPart?", true);
        RobotContainer.driveTrain.resetAngle();
        RobotContainer.driveTrain.setAngleOffset(RobotContainer.AllianceAngleDeg);
        boolean doOne = SmartDashboard.getBoolean("do 1?", false);
        boolean doTwo = SmartDashboard.getBoolean("do 2?", false);
        boolean doThree = SmartDashboard.getBoolean("do 3?", false);
        boolean farOne = SmartDashboard.getBoolean("far 1", false);
        boolean farTwo = SmartDashboard.getBoolean("far 2", false);
        boolean farThree = SmartDashboard.getBoolean("far 3", false);
        boolean farFour = SmartDashboard.getBoolean("far 4", false);
        boolean farFive = SmartDashboard.getBoolean("far 5", false);

        RobotContainer.intake.resetIntake();
        DriveTrainConfig config = RobotContainer.driveTrain.getConfig().makeClone();
        config.maxVelocity = 4;
        config.maxAcceleration = 3;
        config.maxCentripetalAcceleration = 11;
        config.maxAngularAcceleration = 8;
        config.maxAnglularVelocity = 12;
        double correctSeek = RobotContainer.isRed ? 0.2 : -0.2;
        MultiPartPath pathA = new MultiPartPath(RobotContainer.driveTrain, config, null);
        if (chooser.getSelected().equals("Left")) { // path off
            pathA.resetPosition(1.378, 6.578);
            pathA.setHeading(RobotContainer.AllianceAngleDeg);
            pathA.addSequentialCommand(new SetShooter(ShooterMode.SHOOT));// NOMOVE
            pathA.setHeading(30);
            pathA.addWaypoint(1.676, 6.530);
            pathA.addSequentialCommand(new TurnAndShoot(RobotContainer.aimingLocation, -2));// NOMOVE

           

            if (doOne) { // path on
                pathA.addSequentialCommand(new FollowNote(true, true, 1, 1, -correctSeek, 600));// ENDPOS:2.941,7.007
                pathA.setHeading(41 + RobotContainer.AllianceAngleDeg);
                // pathA.addWaypoint(2.392, 6.435);
                pathA.addSequentialCommand(new TurnAndShoot());// NOMOVE
                pathA.setHeading(RobotContainer.AllianceAngleDeg );
            } else { // path off
                pathA.addWaypoint(4.516, 7.222);
                pathA.addWaypoint(2.106, 7.628);
                pathA.addWaypoint(2.666, 7.699);
                pathA.addWaypoint(3.263, 7.664);

            }
            pathA.addSequentialCommand(new SetShooter(ShooterMode.OFF)); // NOMOVE
            // if (farThree || farTwo || farOne) { // path off
            // pathA.addWaypoint(4.027, 7.616);
            // pathA.addWaypoint(5.589, 7.282);
            // }
            if (farOne) { // path off
                pathA.addWaypoint(6.878, 7.234);
                pathA.addSequentialCommand(new FollowNote(true, true, 3, 2, -correctSeek, 300),2);// ENDPOS:8.333,7.461
                pathA.addWaypoint(6.830, 7.222);
                pathA.addWaypoint(5.589, 7.163);
                pathA.setHeading(RobotContainer.AllianceAngleDeg + 28);
                pathA.addWaypoint(3.907, 6.697);
                pathA.addWaypoint(2.857, 6.411);
                pathA.addWaypoint(1.939, 5.993);
                pathA.addSequentialCommand(new SetShooter(ShooterMode.SHOOT));// NOMOVE
                pathA.addSequentialCommand(new TurnAndShoot());// NOMOVE
                pathA.setHeading(RobotContainer.AllianceAngleDeg);
            }
            if (farTwo) { // path off
                pathA.setHeading(RobotContainer.AllianceAngleDeg - 15);
                pathA.addWaypoint(4.432, 6.363);
                pathA.addWaypoint(6.281, 6.304);
                pathA.addWaypoint(7.367, 6.005);
                pathA.addSequentialCommand(new FollowNote(true, true, 3, 2, correctSeek, 300), 2);// ENDPOS:8.310,5.791
                pathA.setHeading(RobotContainer.AllianceAngleDeg + 30);
                pathA.addWaypoint(5.625, 6.411);
                pathA.addWaypoint(2.714, 6.232);
                pathA.addSequentialCommand(new SetShooter(ShooterMode.SHOOT));// NOMOVE
                pathA.addSequentialCommand(new TurnAndShoot());// NOMOVE
                pathA.setHeading(RobotContainer.AllianceAngleDeg);

            }
            if (farThree) { // path on
                pathA.setHeading(-15 + RobotContainer.AllianceAngleDeg);
                pathA.addWaypoint(6.675, 6.113);
                pathA.addWaypoint(7.200, 4.454);
                pathA.addSequentialCommand(new FollowNote(true, true, 3, 2, correctSeek, 300));// ENDPOS:8.286,4.120
                 pathA.setHeading( RobotContainer.AllianceAngleDeg);
                pathA.addWaypoint(6.997, 5.540);
                pathA.addWaypoint(6.234, 6.435);
                pathA.addWaypoint(4.253, 6.733);
                pathA.addWaypoint(2.774, 6.423);
                pathA.addSequentialCommand(new SetShooter(ShooterMode.SHOOT));// NOMOVE
                pathA.addSequentialCommand(new TurnAndShoot());// NOMOVE
            }

        } else { // path off
            if (chooser.getSelected().equals("Right")) { // path off
                pathA.resetPosition(1.438, 4.216);
                pathA.setHeading(RobotContainer.AllianceAngleDeg);
                if (SmartDashboard.getBoolean("ShootFirst", false)) { // path on
                    pathA.addSequentialCommand(new SetShooter(ShooterMode.SHOOT));// NOMOVE
                    pathA.addWaypoint(1.162, 4.359);
                    pathA.addSequentialCommand(new TurnAndShoot());// NOMOVE
                } else { // path off
                    pathA.addSequentialCommand(new SetShooter(ShooterMode.PLOOP));// NOMOVE
                    pathA.addSequentialCommand(new Ploop());// NOMOVE
                }
            }
            if (chooser.getSelected().equals("Speaker")) { // path on
                pathA.resetPosition(1.402, 5.552);
                pathA.setHeading(RobotContainer.AllianceAngleDeg);
                if (SmartDashboard.getBoolean("ShootFirst", false)) { // path on
                    pathA.addSequentialCommand(new SetShooter(ShooterMode.SHOOT));// NOMOVE
                    pathA.addSequentialCommand(new TurnAndShoot());// NOMOVE
                } else { // path off
                    pathA.addSequentialCommand(new SetShooter(ShooterMode.PLOOP));// NOMOVE
                    pathA.addSequentialCommand(new Ploop());

                }
            }

            if (doThree) {// path on
                pathA.addWaypoint(1.515, 4.777);

                pathA.addWaypoint(1.795, 4.204);
                pathA.addSequentialCommand(new FollowNote(true, true, 1, .75, correctSeek, 900));// ENDPOS:2.929,4.156
                pathA.addWaypoint(2.173, 4.921);
                pathA.addSequentialCommand(new TurnAndShoot());// NOMOVE
                // pathA.pivotInPlace(90);
            }
            if (doTwo) {// path on

                pathA.addSequentialCommand(new FollowNote(true, true, 2, 2, correctSeek, 400));// ENDPOS:2.917,5.576
                pathA.addWaypoint(1.903, 5.612);
                pathA.addSequentialCommand(new TurnAndShoot());// NOMOVE
                // pathA.pivotInPlace(90);

            }
            if (doOne) { // path on
                         // pathA.addWaypoint(1.951, 6.828);
                pathA.addSequentialCommand(new FollowNote(true, true, 2, 2, correctSeek, 400));// ENDPOS:2.917,6.888
                pathA.addWaypoint(2.010, 5.946);
                pathA.addSequentialCommand(new TurnAndShoot());// NOMOVE
            }
            if (farFive || farThree || farFour) { // path off
                pathA.addSequentialCommand(new SetShooter(ShooterMode.OFF));// NOMOVE
            }
            if (farThree) {// path off
                pathA.addWaypoint(2.845, 2.832);
                pathA.addWaypoint(3.811, 2.979);
                pathA.addWaypoint(4.854, 3.990);
                pathA.addWaypoint(7.105, 4.037);
                pathA.addSequentialCommand(new FollowNote(true, true, 3, 2, correctSeek, 300));// ENDPOS:8.226,4.108
                pathA.addWaypoint(6.620, 4.022);
                pathA.addWaypoint(4.742, 3.990);
                pathA.addWaypoint(4.276, 3.123);
                pathA.addWaypoint(2.130, 3.214);
                pathA.addWaypoint(2.058, 4.932);
                pathA.addSequentialCommand(new SetShooter(ShooterMode.SHOOT));// NOMOVE
                pathA.addSequentialCommand(new TurnAndShoot());// NOMOVE

            }
            if (farFour) { // path off

                pathA.addWaypoint(1.139, 2.868);
                pathA.addWaypoint(3.108, 1.555);
                pathA.addWaypoint(3.848, 1.722);
                pathA.addWaypoint(5.593, 1.678);
                pathA.addWaypoint(7.182, 1.983);
                pathA.addSequentialCommand(new FollowNote(true, true, 3, 2, correctSeek, 300));// ENDPOS:8.262,2.390
                pathA.addWaypoint(7.128, 3.023);
                pathA.addWaypoint(5.745, 4.240);
                pathA.addWaypoint(4.468, 3.225);
                pathA.addWaypoint(2.130, 3.214);
                pathA.addWaypoint(1.581, 4.335);
                pathA.addSequentialCommand(new SetShooter(ShooterMode.SHOOT));// NOMOVE
                pathA.addSequentialCommand(new TurnAndShoot());// NOMOVE
            }
            if (farFive) {// path off
                pathA.addWaypoint(1.139, 2.868);
                pathA.addWaypoint(3.108, 1.555);
                pathA.addWaypoint(7.519, 0.779);
                pathA.addSequentialCommand(new FollowNote(true, true, 3, 2, correctSeek, 300));// ENDPOS:8.345,0.732
                pathA.addWaypoint(6.830, 0.792);
                pathA.addWaypoint(3.645, 2.402);
                pathA.addWaypoint(2.130, 3.214);
                pathA.addWaypoint(1.557, 4.096);
                pathA.addSequentialCommand(new SetShooter(ShooterMode.SHOOT));// NOMOVE
                pathA.addSequentialCommand(new TurnAndShoot());// NOMOVE
            }
            if (SmartDashboard.getBoolean("ShootSecond", false)) { // path on
                pathA.addSequentialCommand(new FollowNote(true, true, 1, .75, -correctSeek, 900));// ENDPOS:1.664,4.514
                pathA.addSequentialCommand(new TurnAndShoot());// NOMOVE
            }
        }
        pathA.addStop();
        if (RobotContainer.isRed) {
            pathA.flipAllX();
        }

        addCommands(pathA.finalizePath());

    }
}