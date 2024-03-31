package frc.robot.commands;

import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import frc.robot.RobotContainer;
import frc.robot.commands.paths.CommandPathPiece;

public class Ploop extends CommandPathPiece {
     public Ploop() {    
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
   RobotContainer.shooterSubsystem.resetHasShot();
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {

}

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    RobotContainer.shooterSubsystem.hasShot();
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return  RobotContainer.shooterSubsystem.hasShot();
  }


}
