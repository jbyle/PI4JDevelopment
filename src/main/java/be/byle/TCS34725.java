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
 * Created by Jan Bylé on 31/01/2015.
 */
public class TCS34725 {
    static Logger log = Logger.getLogger(TCS34725.class.getName());


    private static final int device_address = 0x29;
    private static final int COMMAND_MS3Bits = 0xA0;
    private static final byte enable_pon = 0x01;
    private static final byte enable_AEN = 0x02;
    private static final byte ATIME = (byte)0xD5;
    private static final byte GAIN = 0x00;
    private static final byte WLONG = 0x00;

    private static final int ENABLE_address = 0x00;
    private static final int ATIME_address = 0x01;
    private static final int WTIME_address = 0x03;
    private static final int AILTL_address = 0x04;
    private static final int AILTH_address = 0x05;
    private static final int AIHTL_address = 0x06;
    private static final int AIHTH_address = 0x07;
    private static final int PERS_address = 0x0C;
    private static final int CONFIG_address = 0x0D;
    private static final int CONTROL_address = 0x0F;
    private static final int ID_address = 0x12;
    private static final int STATUS_address = 0x13;
    private static final int CDATAL_address = 0x14;
    private static final int CDATAH_address = 0x15;
    private static final int RDATAL_address = 0x15;
    private static final int RDATAH_address = 0x17;
    private static final int GDATAL_address = 0x18;
    private static final int GDATAH_address = 0x19;
    private static final int BDATAL_address = 0x1A;
    private static final int BDATAH_address = 0x1B;

    private I2CDevice tcs3472;

    void setup() throws IOException, InterruptedException {
        tcs3472=I2CTools.getDevice(device_address, I2CBus.BUS_1);
        //set ATime
        tcs3472.write((byte) (ATIME_address | COMMAND_MS3Bits));
        tcs3472.write(ATIME);
        Thread.sleep(5);

        //set WLONG
        tcs3472.write((byte) (CONFIG_address | COMMAND_MS3Bits));
        tcs3472.write(WLONG);
        Thread.sleep(5);

        //set GAIN
        tcs3472.write((byte) (CONTROL_address | COMMAND_MS3Bits));
        tcs3472.write((byte) GAIN);
        Thread.sleep(5);
    }

    void sensorStart() throws IOException, InterruptedException {
        tcs3472.write((byte) (ENABLE_address | COMMAND_MS3Bits));
        tcs3472.write((byte)(enable_pon|enable_AEN));
        Thread.sleep(900);
    }

    void sensorStop() throws IOException, InterruptedException {
        tcs3472.write((byte) (ENABLE_address | COMMAND_MS3Bits));
        tcs3472.write((byte)(0x00));
    }


    byte[] readRawData() throws  IOException, InterruptedException {
        byte[] inputBuffer = new byte[8];
        tcs3472.write((byte)(CDATAL_address|COMMAND_MS3Bits));
        Thread.sleep(100);
        // read 8 bytes (low+ high byte for clear, red, green and blue
        tcs3472.read(inputBuffer, 0, 8);
        return inputBuffer;
    }

    int[] channelConversion(byte[] rawData){
        int[] channelValues = new int[4];
        for (int i=0;i<4;i++)
            // first byte is low byte, second byte is high byte
            channelValues[i]=((rawData[2*i+1]&0xFF)<<8)|(rawData[2*i]&0xFF);

        return channelValues;
    }

    void getColor() throws  IOException, InterruptedException {
        int returnValues[]= new int[4];
        sensorStart();
        returnValues=channelConversion(readRawData());
        sensorStop();
        log.info("raw data results : " + (returnValues[0])+" " + (returnValues[1])+" " + (returnValues[2])+" " + (returnValues[3]));
    }
    public static void main(String[] args) {
        TCS34725 tcs34721 = new TCS34725();
        try {
            tcs34721.setup();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        while (true) {
            try {
                tcs34721.getColor();
            } catch (Exception e) {
                log.error(e.getMessage());
                break;
            }
        }
    }

    }

