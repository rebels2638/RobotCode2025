package frc.robot.subsystems.drivetrain.swerve.gyro;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.Pigeon2Configuration;
import com.ctre.phoenix6.hardware.Pigeon2;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.LinearAcceleration;
import frc.robot.subsystems.drivetrain.swerve.Phoenix6Odometry;

public class GyroIOPigeon2 implements GyroIO {
    private final Pigeon2 gyro;
    private final StatusSignal<Angle> yawSignal;
    private final StatusSignal<AngularVelocity> angularVelocitySignal;
    private final StatusSignal<Angle> rollSignal;
    private final StatusSignal<AngularVelocity> rollVelocitySignal;
    private final StatusSignal<Angle> pitchSignal;
    private final StatusSignal<AngularVelocity> pitchVelocitySignal;
    private final StatusSignal<LinearAcceleration> accelerationXSignal;
    private final StatusSignal<LinearAcceleration> accelerationYSignal;

    private final Phoenix6Odometry odom;

    public GyroIOPigeon2() {
        odom = Phoenix6Odometry.getInstance();
        gyro = new Pigeon2(0, "canivore");
        Pigeon2Configuration config = new Pigeon2Configuration();
        config.MountPose.MountPoseYaw = 0;
        config.MountPose.MountPosePitch = 0;
        config.GyroTrim.GyroScalarZ = 0;
        gyro.getConfigurator().apply(config);

        yawSignal = gyro.getYaw().clone();
        angularVelocitySignal = gyro.getAngularVelocityZDevice().clone();

        rollSignal = gyro.getRoll().clone();
        rollVelocitySignal = gyro.getAngularVelocityXWorld().clone();

        pitchSignal = gyro.getPitch().clone();
        pitchVelocitySignal = gyro.getAngularVelocityYWorld().clone();

        accelerationXSignal = gyro.getAccelerationX().clone();
        accelerationYSignal = gyro.getAccelerationY().clone();

        BaseStatusSignal.setUpdateFrequencyForAll(
                100,
                yawSignal,
                angularVelocitySignal,
                rollSignal,
                rollVelocitySignal,
                pitchSignal,
                pitchVelocitySignal,
                accelerationXSignal,
                accelerationYSignal);

        odom.registerSignal(gyro, yawSignal);
        odom.registerSignal(gyro, angularVelocitySignal);
        odom.registerSignal(gyro, rollSignal);
        odom.registerSignal(gyro, rollVelocitySignal);
        odom.registerSignal(gyro, pitchSignal);
        odom.registerSignal(gyro, pitchVelocitySignal);
        odom.registerSignal(gyro, accelerationXSignal);
        odom.registerSignal(gyro, accelerationYSignal);

        gyro.optimizeBusUtilization();
    }

    @Override
    public synchronized void updateInputs(GyroIOInputs inputs) {
        inputs.connected = BaseStatusSignal.refreshAll(
                        yawSignal,
                        angularVelocitySignal,
                        rollSignal,
                        rollVelocitySignal,
                        pitchSignal,
                        pitchVelocitySignal,
                        accelerationXSignal,
                        accelerationYSignal).isOK();

        inputs.orientation = new Rotation3d(
            BaseStatusSignal.getLatencyCompensatedValue(rollSignal, rollVelocitySignal).baseUnitMagnitude(),
            BaseStatusSignal.getLatencyCompensatedValue(pitchSignal, pitchVelocitySignal).baseUnitMagnitude(),
            BaseStatusSignal.getLatencyCompensatedValue(yawSignal, angularVelocitySignal).baseUnitMagnitude()
        );
        inputs.angularVelocityPs = new Rotation2d(angularVelocitySignal.getValue().baseUnitMagnitude());
        inputs.accelerationMpsSq = new Translation2d(
            accelerationXSignal.getValue().baseUnitMagnitude(), 
            accelerationYSignal.getValue().baseUnitMagnitude()
        );
    }

    @Override
    public int getCanDeviceId() {
        return 0;
    }
}