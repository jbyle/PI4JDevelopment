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

    private static final byte configA   = (byte)0x70;
    private static final byte configB   = (byte)0xA0;
    private static final byte mode      = (byte)0x00;



    private I2CDevice hmc5883L;



    void setup() throws IOException, InterruptedException {
        hmc5883L= I2CTools.getDevice(device_address, I2CBus.BUS_1);
        hmc5883L.write((byte)CONFIG_REGISTER_A_address);
        hmc5883L.write((byte)configA);
        hmc5883L.write((byte)CONFIG_REGISTER_B_address);
        hmc5883L.write((byte)configB);
        hmc5883L.write((byte)MODE_REGISTER_address);
        hmc5883L.write((byte)mode);
        Thread.sleep(5);
    }


    byte[] readRawData() throws  IOException, InterruptedException {
        byte[] inputBuffer = new byte[6];
        hmc5883L.write((byte) X_MSB_REGISTER_address);
        Thread.sleep(67);
        // read 6 bytes (low+ high byte for X, Y and Z
        hmc5883L.read(inputBuffer, 0, 6);
        return inputBuffer;
    }

    void doAxisReadings() throws  IOException, InterruptedException {
        int returnValues[]= new int[3];
       returnValues=valueConversion(readRawData());
        log.info("X " + (returnValues[0])+" Z" + (returnValues[1])+"Y " + (returnValues[2]));
    }


    int[] valueConversion(byte[] rawData){
        int[] channelValues = new int[3];
        for (int i=0;i<3;i++)
            // first byte is low byte, second byte is high byte
            channelValues[i]=((rawData[2*i+1]&0xFF)<<8)|(rawData[2*i]&0xFF);

        return channelValues;
    }
    public static void main(String[] args) {
        HMC5883L hmc5883L1 = new HMC5883L();
        try {
            hmc5883L1.setup();
            while (true) {
                hmc5883L1.doAxisReadings();
                Thread.sleep(67);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
