// RobotBuilder Version: 2.0
//
// This file was generated by RobotBuilder. It contains sections of
// code that are automatically generated and assigned by robotbuilder.
// These sections will be updated in the future when you export to
// Java from RobotBuilder. Do not put any code or make any change in
// the blocks indicating autogenerated code or it will be lost on an
// update. Deleting the comments indicating the section will prevent
// it from being updated in the future.

package frc.robot.subsystems;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import java.util.ArrayList;
import java.util.Arrays;
// BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=IMPORTS
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.SPI.Port;

    // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=IMPORTS

/**
 *
 */
public class Pixy{
    SPI pixyPort;
    Port port = Port.kOnboardCS0;
    PixyObject lastGood = new PixyObject();

    public Pixy() {
     
        pixyPort = new SPI(port);
        startUpdatingPixy();
    }


    // This method parses raw data from the pixy into readable integers
    private int cvt(byte lower, byte upper) {

        return (((int) upper & 0xff) << 8) | ((int) lower & 0xff);
    }

    private List<PixyObject> loadPackets(byte[] rawData, int startIndex) {
        List<PixyObject> packets = new ArrayList<PixyObject>();
        if ((rawData[startIndex] == 1 && rawData[startIndex + 1] == 1 && rawData[startIndex + 2] == 1)
                || (rawData[startIndex] == 0 && rawData[startIndex + 1] == 0 && rawData[startIndex + 2] == 0)) {
            // This is empty data, no more blocks found.
            return packets;
        }
        PixyObject packet = new PixyObject();
        packet.sig = cvt(rawData[startIndex + 0], rawData[startIndex + 1]);
        packet.x = cvt(rawData[startIndex + 2], rawData[startIndex + 3]);
        packet.y = cvt(rawData[startIndex + 4], rawData[startIndex + 5]);
        packet.width = cvt(rawData[startIndex + 6], rawData[startIndex + 7]);
        packet.height = cvt(rawData[startIndex + 8], rawData[startIndex + 9]);
        packet.angle = cvt(rawData[startIndex + 10], rawData[startIndex + 11]);
        packet.trackingIndex = cvt(rawData[startIndex + 12], (byte) 0);
        packet.age = cvt(rawData[startIndex + 13], (byte) 0);
        if(!(packet.width==0 || packet.height==0)){packets.add(packet);}
        try{
            packets.addAll(loadPackets(rawData, startIndex + 14));// if there are no more, this will simply return nothing.

        }catch(IndexOutOfBoundsException e){
            //we must have run out of data in the buffer
            throw e;
        }
        return packets;

    }

    int numBlocksToRead=5;
    int readSize=numBlocksToRead*20;
    byte[] rawData = new byte[readSize];
    private PixyResult getPixyData() {

        int checksum;
        List<PixyObject> packets = new ArrayList<PixyObject>();
        int sigmap = 255;
        

        byte[] request = { (byte) 0xAE, (byte) 0xC1, 0x20, 0x2, (byte) sigmap, (byte) numBlocksToRead };
        // request=new byte[]{ (byte) 174, (byte) 193, 22, 2,1,1 };
        int written = pixyPort.write(request, 6);

        if(written != request.length)
             throw new PixyException("Number of written bytes does not match the expected amount");

        int read = pixyPort.read(false, rawData, readSize);
        
        if(read == 0){
            System.out.println("No data read");
            return new PixyResult(ResultType.ERROR, null);
        }

  
        for (int i = 0; i <= 20; i++) {

            int syncWord = cvt(rawData[i + 0], rawData[i + 1]); // Parse first 2 bytes
            if (syncWord == 0xc1af) { // Check is first 2 bytes equal a "sync word", which indicates the start of a
                                      // packet of valid data
                 //System.out.println(Arrays.toString(Arrays.copyOfRange(rawData,i,i+17)));
                // This next block parses the rest of the data
                checksum = cvt(rawData[i + 4], rawData[i + 5]);
                int type = rawData[i + 2];
                if (type == 33) {
                    // normal
                } else if (type == 3 || type==1) {// error, probably busy is 3, brightness response is 1
                    return new PixyResult(ResultType.ERROR, null);
                }

                packets = loadPackets(rawData, i + 6);

                int length = rawData[i + 3];
                if (checksum == 0 || length == 0) {
                    break;// I think this means none found
                }
                int sum = 0;
                for (PixyObject object : packets) {
                    sum += object.getSum();
                }
                // Checks whether the data is valid using the checksum
                if (checksum != sum) {
                    int diff=sum-checksum;
                    if (diff%255 != 0 && false) {// weird thing where pixy sends wrong checksum, diff is always mult of 255. Therefore, don't log it.
                                                 
                        System.out.println("checkfail:");
                        System.out.println(checksum);
                        System.out.println(sum);
                        System.out.println("diff: " + (checksum - sum));
                        System.out.println(packets);
                        System.out.println(Arrays.toString(rawData));

                        //throw new PixyException("Checksum Failed. Check that all wires to Pixy are connected.");

                    }

                } else {
                     lastGood=packets.get(0);
                     //System.out.println(checksum);
                }

                //System.out.println(packets.toString());
                //System.out.print("Thing found");
                break;
            }
        }
        if (packets.size() >= 1) {
            // System.out.println(rawData);
        }
        return new PixyResult(ResultType.NORMAL, packets);
    }

    List<PixyObject> knownObjects = new ArrayList<PixyObject>();

    
    protected void update() {

        PixyResult result = getPixyData();
        if (result.result == ResultType.NORMAL) {
            knownObjects = result.blocks;
            SmartDashboard.putNumber("numPixyObjects", result.blocks.size());
            //System.out.println(knownObjects.size());
        }
    }
    
    public void startUpdatingPixy() {
        Timer timer = new Timer();
        timer.schedule(new UpdatePixy(this), 0, 16);
    }

    public List<PixyObject> readObjects() {

        return knownObjects;
    }

    public int getNumObs() {
        return knownObjects.size();
    }

    public boolean canSeeObject() {
        return knownObjects.size() >= 1;
    }

    public void setBrightness(int brightness) {
        // default 100
        if (brightness < 0 || brightness > 255) {
            throw new PixyException("Invalid brightness value:" + brightness);
        }
        byte[] request = { (byte) 174, (byte) 193, 16, 1, (byte) brightness };
        pixyPort.write(request, 5);
    }
    public void setLamps(boolean top, boolean bottom){
        byte[] request;
        
        request=new byte[]{ (byte) 174, (byte) 193, 22, 2,0,0 };
        if(top){
            request[4] = 1;
        }
        if(bottom){
            request[5] =1;
        }
        
        pixyPort.write(request, 6);
       
    }
    public void setLamps(boolean on){
        setLamps(on, false);
    }

}

enum ResultType {
    ERROR(3), NORMAL(14);

    int type;

    private ResultType(int type) {
        this.type = type;
    }
}

class PixyResult {
    List<PixyObject> blocks;
    ResultType result;

    public PixyResult(ResultType result, List<PixyObject> blocks) {
        this.result = result;
        this.blocks = blocks;
    }
}

class UpdatePixy extends TimerTask {
    Pixy pixy;

    public UpdatePixy(Pixy pixy) {
        this.pixy = pixy;
    }

    @Override
    public void run() {
        try {
            pixy.update();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

class PixyException extends RuntimeException {
    public PixyException(String message) {
        super(message);
    }
}