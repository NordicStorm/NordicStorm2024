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
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
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

    final double CAMERA_HEIGHT_METERS = Units.inchesToMeters(13.75);

    // Angle between horizontal and the camera.
    final double CAMERA_PITCH_RADIANS = Units.degreesToRadians(0);

    public static PhotonPoseEstimator poseEstimator;

    public static RollingAverage distanceAverage = new RollingAverage(5);

    List<PhotonTrackedTarget> notes = new ArrayList<PhotonTrackedTarget>();

    public PhotonTrackedTarget bestTarget = null;
    double camHeight = Units.inchesToMeters(27); //

    public VisionSubsystem() {

        try {
            layout = AprilTagFieldLayout.loadFromResource(AprilTagFields.k2024Crescendo.m_resourceFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Transform3d transform3d = new Transform3d(new Translation3d(0, -0.0508, -0.368), new Rotation3d(0, 0, 0));

        noteCamera = new PhotonCamera("Arducam_OV9281_USB_Camera (1)");
        photonCamera = new PhotonCamera("Arducam_OV9281_USB_Camera");
        poseEstimator = new PhotonPoseEstimator(layout, PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR, photonCamera,
                transform3d);

    }

    public static double visToRealDistance(double distanceV) {

        double x = distanceV;
        double result = 0.36617022865927057 * x * x + -0.861823000875111 * x + 2.523746695214376; // CURVE:distance,09:25,02/22
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

        if (estimated.isPresent() /** && result.targets.size() >= 2 **/
        ) {

            var newPose = estimated.get();
            SmartDashboard.putNumber("Tags Visible", newPose.targetsUsed.size());

            // RobotContainer.driveTrain.setPose(newPose.estimatedPose.toPose2d());

            RobotContainer.driveTrain.addVisionMeasurment(
                    newPose.estimatedPose.toPose2d(),
                    result.getTimestampSeconds());
            // In photonvision, need to have matching photonvision versions, also, need NT
            // connected as well.

        }

        SmartDashboard.putBoolean("Can it see a tag? ", estimated.isPresent());

        // if(result.hasTargets() && Math.abs(result.getBestTarget().getYaw()) < 20){

        // var bestTarget = result.getBestTarget();

        // var tagPose = layout.getTagPose(bestTarget.getFiducialId());

        // double yaw = 180 + bestTarget.getYaw() -
        // RobotContainer.driveTrain.getGyroDegrees();

        // TargetCorner bottomCorner = bestTarget.getDetectedCorners().get(0);
        // TargetCorner topCorner = bestTarget.getDetectedCorners().get(3);

        // double height = bottomCorner.y - topCorner.y;
        // double targetsY = topCorner.y + height/2;
        // SmartDashboard.putNumber("targetsY", targetsY);

        // double pitch = Util.map(targetsY, 0, 600, 47.26, -47.26);

        // double distance = PhotonUtils.calculateDistanceToTargetMeters(
        // CAMERA_HEIGHT_METERS,
        // tagPose.get().getZ(),
        // CAMERA_PITCH_RADIANS,
        // Units.degreesToRadians(pitch));

        // distanceAverage.put(distance);
        // SmartDashboard.putNumber("Pitch", pitch);
        // SmartDashboard.putNumber("Height", height);
        // SmartDashboard.putNumber("Distance", distance);

        // double y = Math.sin(Math.toRadians(yaw)) * distanceAverage.get();
        // double x = Math.cos(Math.toRadians(yaw)) * distanceAverage.get();

        // double robotX = tagPose.get().getX() + -x;
        // double robotY = tagPose.get().getY() + y;

        // RobotContainer.driveTrain.setPose(robotX, robotY, 0);
        // return;
        // }

    }
}