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
public class TCS3472 {
    static Logger log = Logger.getLogger(TCS3472.class.getName());


    private static final int device_address = 0x29;
    private static final int COMMAND_MS3Bits = 0xA0;
    private static final byte enable_pon = 0x01;
    private static final byte enable_AEN = 0x02;

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
    private byte[] colorInputBuffer = new byte[8];


    void setup() throws IOException, InterruptedException {
        tcs3472=I2CTools.getDevice(device_address, I2CBus.BUS_1);
        //set ATime
        tcs3472.write((byte) (ATIME_address | COMMAND_MS3Bits));
        tcs3472.write((byte) (0x01));
        Thread.sleep(5);

        //set WTime

        //set WLONG

        //set GAIN
        tcs3472.write((byte) (CONTROL_address | COMMAND_MS3Bits));
        tcs3472.write((byte) 0x00);
        Thread.sleep(5);


        tcs3472.write((byte) (CONFIG_address | COMMAND_MS3Bits));
        tcs3472.write((byte) 0x00);
        Thread.sleep(5);




        //set ID

    }

    void startColorCalculation() throws IOException, InterruptedException {
        tcs3472.write((byte) (ENABLE_address | COMMAND_MS3Bits));
        tcs3472.write((byte)(enable_pon|enable_AEN));
        Thread.sleep(700);
    }

    void endColorCalculation() throws IOException, InterruptedException {
        tcs3472.write((byte) (ENABLE_address | COMMAND_MS3Bits));
        tcs3472.write((byte)(0x00));
    }


    byte[] readData() throws  IOException, InterruptedException {
        byte[] inputBuffer = new byte[8];
        tcs3472.write((byte)(CDATAL_address|COMMAND_MS3Bits));
        Thread.sleep(100);
        tcs3472.read(inputBuffer, 0, 8);
 /*       tcs3472.write((byte) (RDATAL_address | COMMAND_MS3Bits));
        Thread.sleep(100);
        tcs3472.read(inputBuffer, 2, 2);
        tcs3472.write((byte) (GDATAL_address | COMMAND_MS3Bits));
        Thread.sleep(100);
        tcs3472.read(inputBuffer, 4, 2);
        tcs3472.write((byte) (BDATAL_address | COMMAND_MS3Bits));
        Thread.sleep(100);
        tcs3472.read(inputBuffer,6,2);*/
        return inputBuffer;
    }

    int[] initiateAndGetColor() throws  IOException, InterruptedException {
        int returnValues[]= new int[4];
        int castedColorInputBuffer[] = new int[8];
        int l=0;
        int h=0;
        setup();
        startColorCalculation();
        colorInputBuffer=readData();
        endColorCalculation();
        for (int i=0;i<8;i++) {
            log.info("colors read " + colorInputBuffer[i]);
            castedColorInputBuffer[i] =  (colorInputBuffer[i])&0xFF;
        }
        for (int i=0;i<4;i++) {
            l=castedColorInputBuffer[2*i];
            h=castedColorInputBuffer[2*i+1];
            log.info("low : " +l);
            log.info("high :" +h);
            returnValues[i] =( (h << 8) | l);
            log.info("raw data result[" + i + "] : " + (returnValues[i]));
            log.info("rgb result[" + i + "]      : " + (returnValues[i]>>>8));
            log.info("clear rgb result[" + i + "]      : " + (double)(returnValues[i])/(double)returnValues[0]*255);
        }
        return returnValues;
    }
    public static void main(String[] args) {
        int results[];
        TCS3472 tcs34721 = new TCS3472();
        try {
            results=tcs34721.initiateAndGetColor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    }

