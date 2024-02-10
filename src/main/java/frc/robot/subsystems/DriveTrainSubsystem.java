// RobotBuilder Version: 3.1
//
// This file was generated by RobotBuilder. It contains sections of
// code that are automatically generated and assigned by robotbuilder.
// These sections will be updated in the future when you export to
// Java from RobotBuilder. Do not put any code or make any change in
// the blocks indi cating autogenerated code or it will be lost on an
// update. Deleting the comments indicating the section will prevent
// it from being updated in the future.

package frc.robot.subsystems;

import java.util.ArrayList;
import java.util.List;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.hardware.TalonFX;
import com.kauailabs.navx.frc.AHRS;
import com.revrobotics.AnalogInput;
import com.swervedrivespecialties.swervelib.Mk3ModuleConfiguration;
import com.swervedrivespecialties.swervelib.Mk3SwerveModuleHelper;
import com.swervedrivespecialties.swervelib.SwerveModule;

import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.SPI.Port;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInLayouts;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.RobotContainer;
import frc.robot.Util;
import frc.robot.commands.paths.DriveTrainConfig;
import frc.robot.commands.paths.PathableDrivetrain;



/**
 * Speed gains detection:
 * Set chassis volts to 12. Read peak raw speed = rs, write down. Read peak getStateVelocity, write down. 
 now Kf*input must equal 1023 when input = rs. So Kf = 1023/rs. 
 
 * set chassis volts to 9. Read raw speed, write down. kf*input = 0.75. Kf = 767/rs
 * Take the avg of those two Kf values to use
 * Then do a bunch of testing speeds: Use the velocity in meters per sec, set to a solid 2.5 forward (for example). SmartDashboard output the readStateVelocity(). 

    Tweak kP up by 0.005 and see if it improves. Keep tuning up kP until the velocity to close to what you want. Then repeat at a different velocity. You might need to reduce kF
	  a bit as well.
	  
         double rawSpeed = frontLeftModule.getTalonDriveMotor().getSelectedSensorVelocity();
		 frontLeftModule.readStateVelocity();
 *
 */
public class DriveTrainSubsystem extends SubsystemBase implements PathableDrivetrain {


    private static final double MAX_VOLTAGE = 12.0;
    public static final double MAX_VELOCITY_METERS_PER_SECOND = 4.00;
    //Ticks: -18000
    private final SwerveModule frontLeftModule;
    private final SwerveModule frontRightModule;
    private final SwerveModule backLeftModule;
    private final SwerveModule backRightModule;
    private List<SwerveModule> swerveModules = new ArrayList<>();
    private DriveTrainConfig drivetrainConfig = new DriveTrainConfig();
    private final SwerveDriveKinematics kinematics = new SwerveDriveKinematics(
            new Translation2d(Constants.DRIVETRAIN_TRACKWIDTH_METERS / 2.0,
                    Constants.DRIVETRAIN_WHEELBASE_METERS / 2.0),
            new Translation2d(Constants.DRIVETRAIN_TRACKWIDTH_METERS / 2.0,
                    -Constants.DRIVETRAIN_WHEELBASE_METERS / 2.0),
            new Translation2d(-Constants.DRIVETRAIN_TRACKWIDTH_METERS / 2.0,
                    Constants.DRIVETRAIN_WHEELBASE_METERS / 2.0),
            new Translation2d(-Constants.DRIVETRAIN_TRACKWIDTH_METERS / 2.0,
                    -Constants.DRIVETRAIN_WHEELBASE_METERS / 2.0));

    private final SwerveDriveOdometry odometry;
    private ChassisSpeeds targetChassisSpeeds;
    private SwerveModuleState currentSwerveStates [] = new SwerveModuleState[4];
    private SwerveModulePosition currentSwervePositions [] = new SwerveModulePosition[4];

    private Pose2d pose;
    private final AHRS navx = new AHRS(Port.kMXP);

    // this is basically the 'privilege level' the rotation control ability is at.
    // So 0 means it will take anything, 1 means 1 or higher.
    // when a raw "drive" is used, privilege level is 0.
    // Each tick, whatever thing gave the highest rotation privilege gets used.
    private int currentRotationPrivilegeNeeded = 0;
    
    //Pixy pixy;
    public int myBallColor = 0;
    public int enemyBallColor = 0;

    public Field2d fieldDisplay;
    public DriveTrainSubsystem() {
       
  
        fieldDisplay = new Field2d();
        SmartDashboard.putData(fieldDisplay);

        ShuffleboardTab shuffleboardTab = Shuffleboard.getTab("Drive train");

        frontLeftModule = Mk3SwerveModuleHelper.createFalcon500(
                shuffleboardTab.getLayout("Front left", BuiltInLayouts.kList).withSize(2, 4).withPosition(0,0), new Mk3ModuleConfiguration(),
                Mk3SwerveModuleHelper.GearRatio.FAST, Constants.FRONT_LEFT_MODULE_DRIVE_MOTOR,
                Constants.FRONT_LEFT_MODULE_STEER_MOTOR, Constants.FRONT_LEFT_MODULE_STEER_ENCODER,
                //-Math.toRadians(231.26907348632812));
                //0);
                Units.degreesToRadians(-183.07342529296875)); //-175.60546875
                
        frontRightModule = Mk3SwerveModuleHelper.createFalcon500(
                 shuffleboardTab.getLayout("Front Right", BuiltInLayouts.kList).withSize(2, 4).withPosition(2,0), new Mk3ModuleConfiguration(),
                Mk3SwerveModuleHelper.GearRatio.FAST, Constants.FRONT_RIGHT_MODULE_DRIVE_MOTOR,
                Constants.FRONT_RIGHT_MODULE_STEER_MOTOR, Constants.FRONT_RIGHT_MODULE_STEER_ENCODER,
            
            Units.degreesToRadians(-30.937499999999996)); //-332.40234375
            //-Math.toRadians(197.80059814453125 + 180));
                //SmartDashboard.putNumber("Front right" , ) -258.2267761230469
        backLeftModule = Mk3SwerveModuleHelper.createFalcon500(
                 shuffleboardTab.getLayout("Back left", BuiltInLayouts.kList).withSize(2, 4).withPosition(4,0), new Mk3ModuleConfiguration(),
                Mk3SwerveModuleHelper.GearRatio.FAST, Constants.BACK_LEFT_MODULE_DRIVE_MOTOR,
                Constants.BACK_LEFT_MODULE_STEER_MOTOR, Constants.BACK_LEFT_MODULE_STEER_ENCODER,
                Units.degreesToRadians(0)); //-255.849609375

        backRightModule = Mk3SwerveModuleHelper.createFalcon500(
                 shuffleboardTab.getLayout("Back Right", BuiltInLayouts.kList).withSize(2, 4).withPosition(6,0), new Mk3ModuleConfiguration(),
                Mk3SwerveModuleHelper.GearRatio.FAST, Constants.BACK_RIGHT_MODULE_DRIVE_MOTOR,
                Constants.BACK_RIGHT_MODULE_STEER_MOTOR, Constants.BACK_RIGHT_MODULE_STEER_ENCODER,
                Units.degreesToRadians(-62.13867187499999)); //-292.412109375
        swerveModules.add(frontLeftModule);
        swerveModules.add(frontRightModule);
        swerveModules.add(backLeftModule);
        swerveModules.add(backRightModule);

        for (SwerveModule module : swerveModules) {
            TalonFX driveMotor = module.getTalonDriveMotor();

            //driveMotor.configSelectedFeedbackSensor(FeedbackDevice.IntegratedSensor, 0, 0);
           // driveMotor.config_kF(0, 0.048);
           // driveMotor.config_kP(0, 0.04);

           var slot0Configs = new Slot0Configs();
           slot0Configs.kP = 0.096093;
           slot0Configs.kV = 0.11531;
           driveMotor.getConfigurator().apply(slot0Configs, 0.01);

        }
        drivetrainConfig.maxAcceleration = 3; 
        drivetrainConfig.maxVelocity = 4; 
        drivetrainConfig.maxAnglularVelocity = 10;
        drivetrainConfig.maxAngularAcceleration = 5;
        drivetrainConfig.rotationCorrectionP = 2;
        drivetrainConfig.maxCentripetalAcceleration = 8;

        pose = new Pose2d(0, 0, Rotation2d.fromDegrees(0));
        updateModulePositions();
        odometry = new SwerveDriveOdometry(kinematics, Rotation2d.fromDegrees(0), currentSwervePositions, pose);
        //SmartDashboard.putNumber("MaxAccel", 4);
        targetChassisSpeeds = new ChassisSpeeds(0, 0, 0);
        drive(0, 0, 0);

    }

    public void zeroGyroscope() {
        navx.zeroYaw();
    }

    /**
     * Goes positive as it goes counterclockwise. Degrees!
     * 
     * @return current angle in degrees
     */
    public double getGyroDegrees() {
        return -navx.getAngle();
    }

    
    public double getGyroRadians() {
        return Math.toRadians(getGyroDegrees());
    }
    /**
     * Get the pitch value in degrees, aka forward/back tilt
     * @return
     */
    public double getGyroPitch(){
        return navx.getPitch(); // because of orientation of navx this is pitch
    }
    
    public Pose2d getPose() {
        return pose;
    }
    /**
     * Resets the position
     * @param x
     * @param y
     * @param rot in radians, but this is ignored.
     */
    public void setPose(double x, double y, double rot) {
       odometry.resetPosition(Rotation2d.fromDegrees(getGyroDegrees()), currentSwervePositions, new Pose2d(x, y, Rotation2d.fromDegrees(getGyroDegrees())));        
    }

    public void resetAngle() {
        navx.reset();
    }
    /**
     * This will get added to the angle result to offset it.
     * A positive
     * value means that the robot is pointing x degrees counterclockwise
     * @param degrees
     */
    public void setAngleOffset(double degrees){
        navx.setAngleAdjustment(-degrees);
    }

  
    public ChassisSpeeds getSpeeds() {
        return kinematics.toChassisSpeeds(currentSwerveStates);
    }


    public DriveTrainConfig getConfig() {
        return drivetrainConfig;
    }


    public void drive(ChassisSpeeds chassisSpeeds) {
        drive(chassisSpeeds, 0);
    }

    private void updateModulePositions(){
        for(int i = 0; i<swerveModules.size(); i++){
            currentSwervePositions[i]=Util.positionFromModule(swerveModules.get(i));
        }
    }
    @Override
    public void periodic() {

        SmartDashboard.putNumber("NavX Gyro Pitch", getGyroPitch());

        for(int i = 0; i < swerveModules.size(); i++){
            currentSwerveStates[i]=Util.stateFromModule(swerveModules.get(i));

            SmartDashboard.putNumber("Module " + i, Units.radiansToDegrees(swerveModules.get(i).getSteerAngle()));
            SmartDashboard.putNumber("Module speed " + i, (swerveModules.get(i).getDriveVelocity()));
            SmartDashboard.putNumber("Modulex raw " + i, (swerveModules.get(i).getSteerAngle()));

        }
        updateModulePositions();

        // Update the pose
        pose = odometry.update(Rotation2d.fromDegrees(getGyroDegrees()), currentSwervePositions);
        
        driveActualMotors(targetChassisSpeeds);
        currentRotationPrivilegeNeeded = 0;
        fieldDisplay.setRobotPose(pose.getX(), pose.getY(), new Rotation2d(getGyroRadians()));
        //SmartDashboard.putNumber("Pitch", navx.getRoll());
      
       SmartDashboard.putNumber("driveAng", getGyroDegrees());
       
        
    }

    public void drive(ChassisSpeeds chassisSpeeds, int rotPrivilege) {
        targetChassisSpeeds.vxMetersPerSecond = chassisSpeeds.vxMetersPerSecond;
        targetChassisSpeeds.vyMetersPerSecond = chassisSpeeds.vyMetersPerSecond;
        if (rotPrivilege >= currentRotationPrivilegeNeeded) {
            currentRotationPrivilegeNeeded = rotPrivilege;
            targetChassisSpeeds.omegaRadiansPerSecond = chassisSpeeds.omegaRadiansPerSecond;
        }
    }

    private void driveActualMotors(ChassisSpeeds chassisSpeeds) {
        SwerveModuleState[] states = kinematics.toSwerveModuleStates(chassisSpeeds);
        //SmartDashboard.putNumber("targetspeed", states[0].speedMetersPerSecond);
        frontLeftModule.setWithVelocity(states[0].speedMetersPerSecond,
                states[0].angle.getRadians());
        frontRightModule.setWithVelocity(states[1].speedMetersPerSecond,
                states[1].angle.getRadians());
                SmartDashboard.putNumber("Front right", states[1].speedMetersPerSecond);
        backLeftModule.setWithVelocity(states[2].speedMetersPerSecond,
                states[2].angle.getRadians());
        backRightModule.setWithVelocity(states[3].speedMetersPerSecond,
                states[3].angle.getRadians());
         //SmartDashboard.putNumber("radian speed", chassisSpeeds.omegaRadiansPerSecond);


    }

    public void limitDrive(ChassisSpeeds localSpeeds, int rotPrivilege) {
        boolean vWalls = true;// Robot.vision.hasSeenTarget;
        var currentLocalSpeeds = getSpeeds();

        double maxAccelLocal = 3;
        localSpeeds.vxMetersPerSecond = doAccelerationLimit(currentLocalSpeeds.vxMetersPerSecond,
                localSpeeds.vxMetersPerSecond, maxAccelLocal, maxAccelLocal);
        localSpeeds.vyMetersPerSecond = doAccelerationLimit(currentLocalSpeeds.vyMetersPerSecond,
                localSpeeds.vyMetersPerSecond, maxAccelLocal, maxAccelLocal);

        var targetFieldSpeeds = Util.rotateSpeeds(localSpeeds, -getGyroRadians());

        double fixX = enforceWalls(targetFieldSpeeds.vxMetersPerSecond, drivetrainConfig.maxAcceleration,
                pose.getX(), 2, 6.953);
        if (vWalls){
            targetFieldSpeeds.vxMetersPerSecond = fixX;
        }
        double fixY = enforceWalls(targetFieldSpeeds.vyMetersPerSecond, drivetrainConfig.maxAcceleration,
                pose.getY(), 3, 7.35);
        if (vWalls){
            targetFieldSpeeds.vyMetersPerSecond = fixY;
        }
        var targetLocalSpeeds = ChassisSpeeds.fromFieldRelativeSpeeds(targetFieldSpeeds.vxMetersPerSecond,
                targetFieldSpeeds.vyMetersPerSecond,
                targetFieldSpeeds.omegaRadiansPerSecond,
                Rotation2d.fromDegrees(getGyroDegrees()));

        drive(targetLocalSpeeds, rotPrivilege);
        SmartDashboard.putNumber("inner local", targetLocalSpeeds.vxMetersPerSecond);

        // Robot.drivetrain.drive(forward * Drivetrain.MAX_VELOCITY_METERS_PER_SECOND,
        // sideways * Drivetrain.MAX_VELOCITY_METERS_PER_SECOND, rot *
        // Drivetrain.MAX_ANGULAR_VELOCITY_RADIANS_PER_SECOND);

    }

    /**
     * 
     * @param current
     * @param target
     * @param plusLimit  meters per second accel
     * @param minusLimit
     * @return
     */
    double doAccelerationLimit(double current, double target, double plusLimit, double minusLimit) {
        minusLimit *= -1;
        double factor = 0.25; // trial and error to find this
        plusLimit *= factor;
        minusLimit *= factor;
        var accelNeeded = (target - current);
        // in 0.02 second, it is trying to change by ^
        // System.out.println("accelNeed: " + accelNeeded);
        if (accelNeeded > plusLimit) {
            return current + plusLimit;
        }
        if (accelNeeded < minusLimit) {
            return current + minusLimit;
        }
        return target;
    }

    double enforceWalls(double targetSpeed, double maxAccel, double currentPos, double min, double max) {
        if (targetSpeed > 0) {
            targetSpeed = enforceSingleWall(targetSpeed, maxAccel, currentPos, max);
        } else {
            targetSpeed = -enforceSingleWall(-targetSpeed, maxAccel, -currentPos, -min);
        }

        return targetSpeed;
    }

    double enforceSingleWall(double targetSpeed, double maxAccel, double currentPos, double max) {
        double dist = max - currentPos;

        targetSpeed = Math.min(maxAccel * dist * 1.5, targetSpeed);

        return targetSpeed;
    }

    /**
     * Raw drive the motors, units in VOLTS!
     * @param chassisSpeeds
     */
    public void driveVolts(ChassisSpeeds chassisSpeeds) {
        SwerveModuleState[] states = kinematics.toSwerveModuleStates(chassisSpeeds);
        frontLeftModule.set(states[0].speedMetersPerSecond,
                states[0].angle.getRadians());
        frontRightModule.set(states[1].speedMetersPerSecond,
                states[1].angle.getRadians());
        backLeftModule.set(states[2].speedMetersPerSecond,
                states[2].angle.getRadians());
        backRightModule.set(states[3].speedMetersPerSecond,
                states[3].angle.getRadians());
                
    }

    public void drive(double x, double y, double rot) {
        drive(new ChassisSpeeds(x, y, rot));
    }

    /**
     * 
     * @param speed        radians per second CCW
     * @param rotPrivilege
     */
    public void setRotationSpeed(double speed, int rotPrivilege) {
        if (rotPrivilege >= currentRotationPrivilegeNeeded) {
            targetChassisSpeeds.omegaRadiansPerSecond = speed;
            currentRotationPrivilegeNeeded = rotPrivilege;
        }
    }


    public void setPose(Pose2d pose) {
        setPose(pose.getX(), pose.getY(), pose.getRotation().getRadians());
    }

    public void resetSwerve(){
        for(int i = 0; i<600; ++i){
            driveActualMotors(new ChassisSpeeds());
        }
    }


}
