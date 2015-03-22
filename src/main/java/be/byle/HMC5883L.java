/*
Copyright [2015] [Jan Bylé]

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.

*/
package be.byle;

import be.byle.utility.I2CTools;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import org.apache.log4j.Logger;

import java.io.IOException;

import static java.lang.Math.atan;
import static java.lang.Math.atan2;

/**
 * Created by Jan Bylé on 21/02/2015.
 */
public class HMC5883L {
    static Logger log = Logger.getLogger(HMC5883L.class.getName());

    private static final int device_address = 0x1E;

    private static final int CONFIG_REGISTER_A_address      = 0x00;
    private static final int CONFIG_REGISTER_B_address      = 0x01;
    private static final int MODE_REGISTER_address          = 0x02;
    private static final int X_MSB_REGISTER_address         = 0x03;
    private static final int X_LSB_REGISTER_address         = 0x04;
    private static final int Z_MSB_REGISTER_address         = 0x05;
    private static final int Z_LSB_REGISTER_address         = 0x06;
    private static final int Y_MSB_REGISTER_address         = 0x07;
    private static final int Y_LSB_REGISTER_address         = 0x08;
    private static final int STATUS_REGISTER_address        = 0x09;
    private static final int IDENT_REGISTER_A_address       = 0x0A;
    private static final int IDENT_REGISTER_B_address       = 0x0B;
    private static final int IDENT_REGISTER_C_address       = 0x0C;

    private static final int configA   = 0x70;
    private static final int configB   = 0xA0;
    private static final int mode      = 0x00;



    private I2CDevice hmc5883L;



    void setup() throws IOException, InterruptedException {
        hmc5883L = I2CTools.getDevice(device_address, I2CBus.BUS_1);
   //     hmc5883L.write((byte) 0x3C);
        hmc5883L.write((byte) CONFIG_REGISTER_A_address);
        hmc5883L.write((byte) configA);
     //   hmc5883L.write((byte) 0x3C);
        hmc5883L.write((byte) CONFIG_REGISTER_B_address);
        hmc5883L.write((byte) configB);
     //   hmc5883L.write((byte) 0x3C);
        hmc5883L.write((byte) MODE_REGISTER_address);
        hmc5883L.write((byte) mode);
  //      Thread.sleep(670);
    }


    byte[] readRawData() throws  IOException, InterruptedException {
        byte[] inputBuffer = new byte[6];
 //       hmc5883L.write((byte) device_address);
        hmc5883L.write((byte) 0x3D);
        hmc5883L.write((byte) 0x06);
    //    hmc5883L.write((byte) 0x3C);
      //  hmc5883L.write((byte) X_MSB_REGISTER_address);
        //Thread.sleep(67);
  //      hmc5883L.write((byte) X_MSB_REGISTER_address);
        // read 6 bytes (low+ high byte for X, Y and Z
 //       hmc5883L.read(inputBuffer, 0, 6);
        inputBuffer[0]=(byte)hmc5883L.read(0x03);
        inputBuffer[1]=(byte)hmc5883L.read(0x04);
        inputBuffer[2]=(byte)hmc5883L.read(0x05);
        inputBuffer[3]=(byte)hmc5883L.read(0x06);
        inputBuffer[4]=(byte)hmc5883L.read(0x07);
        inputBuffer[5]=(byte)hmc5883L.read(0x08);
        hmc5883L.write((byte) 0x3C);
        hmc5883L.write((byte) X_MSB_REGISTER_address);
        for (int i=0;i<6;i++)
            log.info(inputBuffer[i]);
         return inputBuffer;
    }

    void doAxisReadings() throws  IOException, InterruptedException {
        int returnValues[]= new int[3];
        double angle;
        int x = returnValues[0];
        int y = returnValues[2];
       returnValues=valueConversion(readRawData());
        log.info("X " + (returnValues[0])+" Z" + (returnValues[1])+"Y " + (returnValues[2]));
        angle= atan2((double)returnValues[2],(double)returnValues[0]) * (180 / 3.14159265);

        if (y>0)
                angle = 90 - atan(x/y)*180/3.14159265;
        else if (y<0)
            angle = 270-atan(x/y)*180/3.14159265;
        else if ((y==0)&&(x<0))
            angle = 180;
        else if ((y==0)&&(x>0))
            angle = 0;

        log.info("angle : "+angle);
        if((angle < 22.5) || (angle > 337.5 ))
            log.info("South");
        if((angle > 22.5) && (angle < 67.5 ))
            log.info("South-West");
        if((angle > 67.5) && (angle < 112.5 ))
            log.info("West");
        if((angle > 112.5) && (angle < 157.5 ))
            log.info("North-West");
        if((angle > 157.5) && (angle < 202.5 ))
            log.info("North");
        if((angle > 202.5) && (angle < 247.5 ))
            log.info("NorthEast");
        if((angle > 247.5) && (angle < 292.5 ))
            log.info("East");
        if((angle > 292.5) && (angle < 337.5 ))
            log.info("SouthEast");

    }


    int[] valueConversion(byte[] rawData){
        int[] channelValues = new int[3];
        for (int i=0;i<3;i++){
            // first byte is low byte, second byte is high byte
            channelValues[i]=((rawData[2*i])<<8)|(rawData[2*i+1]);
        }


        return channelValues;
    }
    public static void main(String[] args) {
        HMC5883L hmc5883L1 = new HMC5883L();
        try {
            hmc5883L1.setup();
            while (true) {
                hmc5883L1.doAxisReadings();
              // hmc5883L1.readRawData();
                Thread.sleep(670);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
