// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide numerical or boolean
 * constants. This class should not be used for any other purpose. All constants should be declared
 * globally (i.e. public static). Do not put anything functional in this class.
 *
 * <p>It is advised to statically import this class (or one of its inner classes) wherever the
 * constants are needed, to reduce verbosity.
 */
public final class Constants {

  public static final double minIntakePower = 0.6; // 12 stack neo 550

  public static final int intakeID = 10;
  public static final int indexerID = 11;
  public static final int shooterID = 16;
  public static final int ampID = 17;
  public static final int shooterPitchID = 18;
  public static final int climberLeftID = 12;
  public static final int climberRightID = 13;

  
   public static final int lowerFlywheelID = 2;
   public static final int upperFlywheelID = 3;


  public static class OperatorConstants {
    public static final int kDriverControllerPort = 0;
  }

  public static final double DRIVETRAIN_TRACKWIDTH_METERS = 0.5715;
  public static final double DRIVETRAIN_WHEELBASE_METERS = 0.5715;
  //0.5207
  /*
  1--/\--4
  |      |
  |      |
  2------3
  */
  public static final int FRONT_LEFT_MODULE_DRIVE_MOTOR = 5;
  public static final int FRONT_LEFT_MODULE_STEER_MOTOR = 6;
  public static final int FRONT_LEFT_MODULE_STEER_ENCODER = 3;
  //public static final double FRONT_LEFT_MODULE_STEER_OFFSET = -Math.toRadians(177.7972412109375+90);

  public static final int FRONT_RIGHT_MODULE_DRIVE_MOTOR = 3;
  public static final int FRONT_RIGHT_MODULE_STEER_MOTOR = 4;
  public static final int FRONT_RIGHT_MODULE_STEER_ENCODER = 2;
  //public static final double FRONT_RIGHT_MODULE_STEER_OFFSET = -Math.toRadians(329.1953125+180);

  public static final int BACK_LEFT_MODULE_DRIVE_MOTOR = 7;
  public static final int BACK_LEFT_MODULE_STEER_MOTOR = 8;
  public static final int BACK_LEFT_MODULE_STEER_ENCODER = 4;
  //public static final double BACK_LEFT_MODULE_STEER_OFFSET = -Math.toRadians(257.16522216796875+180);

  public static final int BACK_RIGHT_MODULE_DRIVE_MOTOR = 1;
  public static final int BACK_RIGHT_MODULE_STEER_MOTOR = 2;
  public static final int BACK_RIGHT_MODULE_STEER_ENCODER = 1;
  // public static final double BACK_RIGHT_MODULE_STEER_OFFSET = -Math.toRadians(294.43359375+180);
}
