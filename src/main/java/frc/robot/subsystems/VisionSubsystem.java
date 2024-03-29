package frc.robot.subsystems;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.PhotonPoseEstimator.PoseStrategy;
import org.photonvision.PhotonUtils;
import org.photonvision.targeting.PhotonTrackedTarget;
import org.photonvision.targeting.TargetCorner;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.Vector;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.RobotContainer;
import frc.robot.Util;
import frc.robot.Utils.RollingAverage;

public class VisionSubsystem extends SubsystemBase {

    public static PhotonCamera photonCamera;
    public static PhotonCamera noteCamera;
    public static AprilTagFieldLayout layout;
    public boolean canSeeTarget = false;

    final double CAMERA_HEIGHT_METERS = 0.216;

    // Angle between horizontal and the camera.
    final double CAMERA_PITCH_RADIANS = Units.degreesToRadians(25);

    public static PhotonPoseEstimator poseEstimator;

    public static RollingAverage distanceAverage = new RollingAverage(5);

    List<PhotonTrackedTarget> notes = new ArrayList<PhotonTrackedTarget>();

    public PhotonTrackedTarget bestTarget = null;
    double camHeight = Units.inchesToMeters(27); //
    Transform3d transform3d = new Transform3d(new Translation3d(0.381, 0.1845, -0.2159),
            new Rotation3d(0, Math.toRadians(25), 0));

    public VisionSubsystem() {

        try {
            layout = AprilTagFieldLayout.loadFromResource(AprilTagFields.k2024Crescendo.m_resourceFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        noteCamera = new PhotonCamera("NoteCam");
        photonCamera = new PhotonCamera("AmpCam");
        poseEstimator = new PhotonPoseEstimator(layout, PoseStrategy.LOWEST_AMBIGUITY, photonCamera,
                transform3d);

    }

    public static double visToRealAngle(double ang) {

        double x = ang;
        double result = -0.0001186658485288733*x*x + 1.006091360515186*x + -0.9259037493455889; // CURVE:real,08:39,03/24
        return result;
    }

     public static double visToRealYaw(double ang) {

        double x = ang;
        double result = 3.234345372689341e-05*x*x*x + 0.0004929175979537224*x*x + 0.9054243758812673*x + -0.3461211847874382; // CURVE:realyaw,09:06,03/25
        return result;
    }

    public List<PhotonTrackedTarget> getTargets() {

        return notes;
    }

    @Override
    public void periodic() {

        notes = noteCamera.getLatestResult().targets;
        poseEstimator.setReferencePose(RobotContainer.driveTrain.getPose()); // sets reference pose to (0,0,
                                                                             // Rotation2d.fromDegrees(0))
        var estimated = poseEstimator.update();
        var result = photonCamera.getLatestResult();

        if (estimated.isPresent() && result.targets.size() >= 2) {

            // var newPose = estimated.get();
            // SmartDashboard.putNumber("Tags Visible", newPose.targetsUsed.size());

            // // RobotContainer.driveTrain.setPose(newPose.estimatedPose.toPose2d());

            // RobotContainer.driveTrain.addVisionMeasurment(
            // newPose.estimatedPose.toPose2d(),
            // ((System.currentTimeMillis()-result.getLatencyMillis())/1000.0));
            // In photonvision, need to have matching photonvision versions, also, need NT
            // connected as well.

        }

        SmartDashboard.putBoolean("Can it see a tag? ", estimated.isPresent());
        PhotonTrackedTarget bestTarget = null;
        if(result.hasTargets()){
            bestTarget = result.targets.get(0);
            for(PhotonTrackedTarget possible: result.targets){
                if(Math.abs(possible.getYaw()) < Math.abs(bestTarget.getYaw())){
                    bestTarget = possible;
                }
            }
        }
        if (result.hasTargets() && Math.abs(bestTarget.getYaw()) < 10) {

            // var bestTarget = result.getBestTarget();
            System.out.println("used "+bestTarget.getFiducialId());
            var tagPose = layout.getTagPose(bestTarget.getFiducialId());

            double yaw = 180 + visToRealYaw(bestTarget.getYaw()) -
                    RobotContainer.driveTrain.getGyroDegrees();

            TargetCorner bottomCorner = bestTarget.getDetectedCorners().get(0);
            TargetCorner topCorner = bestTarget.getDetectedCorners().get(3);

            double height = bottomCorner.y - topCorner.y;
            double targetsY = topCorner.y + height / 2;
            SmartDashboard.putNumber("targetsY", targetsY);
            double pitch = visToRealAngle(bestTarget.getPitch());
            double distance = PhotonUtils.calculateDistanceToTargetMeters(
                    CAMERA_HEIGHT_METERS,
                    tagPose.get().getZ(),
                    CAMERA_PITCH_RADIANS,
                    Units.degreesToRadians(pitch));

            /*Pose2d newPose = PhotonUtils.estimateFieldToRobot(CAMERA_HEIGHT_METERS, tagPose.get().getZ(), CAMERA_PITCH_RADIANS,
                    Units.degreesToRadians(bestTarget.getPitch()), Rotation2d.fromDegrees(bestTarget.getYaw()),
                    Rotation2d.fromDegrees(-RobotContainer.driveTrain.getGyroDegrees()), new Pose2d(tagPose.get().getX(), tagPose.get().getY(), new Rotation2d()),
                    new Transform2d(transform3d.getX(), transform3d.getY(), new Rotation2d()));*/

            distanceAverage.put(distance);
            SmartDashboard.putNumber("Pitch", bestTarget.getPitch());
            SmartDashboard.putNumber("Height", height);
            SmartDashboard.putNumber("Distance", distance);

            double y = Math.sin(Math.toRadians(yaw)) * distanceAverage.get();
            double x = Math.cos(Math.toRadians(yaw)) * distanceAverage.get();

            double robotX = tagPose.get().getX() + -x;
            double robotY = tagPose.get().getY() + y;

            robotX +=(transform3d.getX() *
            Math.cos(RobotContainer.driveTrain.getGyroRadians())) - (transform3d.getY() *
            Math.sin(RobotContainer.driveTrain.getGyroRadians()));
            robotY+= (transform3d.getX() *
            Math.sin(RobotContainer.driveTrain.getGyroRadians())) + (transform3d.getY() *
            Math.cos(RobotContainer.driveTrain.getGyroRadians()));

            RobotContainer.driveTrain.setPose(robotX, robotY, 0);
            //RobotContainer.driveTrain.addVisionMeasurment(new Pose2d(robotX, robotY, new Rotation2d(RobotContainer.driveTrain.getGyroRadians())), System.currentTimeMillis() - result.getLatencyMillis());
            return;
        }

    }
}