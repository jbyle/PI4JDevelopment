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

import com.pi4j.io.gpio.*;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by Jan on 24/01/2015.
 */
// read carefully the datasheet for this sensor.  See http://www.adafruit.com/product/992 for more info on the sensor
public class MPL115A2 {

    private static final int deviceAddress = 0x60;
    private static final byte convert = 0x12;
    private static final int padc_MSB_address = 0x00;
    private static final int padc_LSB_address = 0x01;
    private static final int tadc_MSB_address = 0x02;
    private static final int tadc_LSB_address = 0x03;
    private static final int a0_MSB_address = 0x04;
    private static final int a0_LSB_address = 0x05;
    private static final int b1_MSB_address = 0x06;
    private static final int b1_LSB_address = 0x07;
    private static final int b2_MSB_address = 0x08;
    private static final int b2_LSB_address = 0x09;
    private static final int c12_MSB_address = 0x0A;
    private static final int c12_LSB_address = 0x0B;
    static Logger log = Logger.getLogger(be.byle.MPL115A2.class.getName());
    I2CBus bus;
    GpioPinDigitalOutput shutdownPin = null;
    GpioController gpio = null;
    int padc_MSB;
    int padc_LSB;
    int tadc_MSB;
    int tadc_LSB;
    double a0;
    double b1;
    double b2;
    double c12;
    double tadc;
    double padc;
    double pressure;
    double temperature;
    double pcomp;
    private I2CDevice mpl115a2;

    public void init(Pin gpioShutdownPin) throws IOException, InterruptedException {
        int a0_MSB;
        int a0_LSB;
        int b1_MSB;
        int b1_LSB;
        int b2_MSB;
        int b2_LSB;
        int c12_MSB;
        int c12_LSB;

        gpio = GpioFactory.getInstance();
        if (shutdownPin == null)
            shutdownPin = gpio.provisionDigitalOutputPin(gpioShutdownPin,
                    "shutdown_pin",
                    PinState.HIGH);

        else
            shutdownPin.high();
        Thread.sleep(5);

        bus = I2CFactory.getInstance(I2CBus.BUS_1);
        if (bus != null)
            log.debug("Connected to bus");

        mpl115a2 = bus.getDevice(deviceAddress);
        if (mpl115a2 != null)
            log.debug("Connected to device");

        mpl115a2.write((byte) 0xC0);


        //Read Coefficients
        mpl115a2.write((byte) a0_MSB_address);
        a0_MSB = mpl115a2.read(a0_MSB_address);
        mpl115a2.write((byte) a0_LSB_address);
        a0_LSB = mpl115a2.read(a0_LSB_address);

        a0 = (short) ((a0_MSB << 8) | a0_LSB) / 8.0;

        mpl115a2.write((byte) b1_MSB_address);
        b1_MSB = mpl115a2.read(b1_MSB_address);
        mpl115a2.write((byte) b1_LSB_address);
        b1_LSB = mpl115a2.read(b1_LSB_address);

        b1 = (short) ((b1_MSB << 8) | b1_LSB) / 8192.0;


        mpl115a2.write((byte) b2_MSB_address);
        b2_MSB = mpl115a2.read(b2_MSB_address);
        mpl115a2.write((byte) b2_LSB_address);
        b2_LSB = mpl115a2.read(b2_LSB_address);

        b2 = (short) ((b2_MSB << 8) | b2_LSB) / 16384.0;

        mpl115a2.write((byte) c12_MSB_address);
        c12_MSB = mpl115a2.read(c12_MSB_address);
        mpl115a2.write((byte) c12_LSB_address);
        c12_LSB = mpl115a2.read(c12_LSB_address);

        c12 = (short) (((c12_MSB << 8) | c12_LSB) >> 2) / 4194304.0;

        log.debug("a0 MSB " + a0_MSB);
        log.debug("a0 LSB " + a0_LSB);
        log.debug("a0 " + a0);
        log.debug("b1 MSB " + b1_MSB);
        log.debug("b1 LSB " + b1_LSB);
        log.debug("b1 " + b1);
        log.debug("b2 MSB " + b2_MSB);
        log.debug("b2 LSB " + b2_LSB);
        log.debug("b2 " + b2);
        log.debug("c12 MSB " + c12_MSB);
        log.debug("c12 LSB " + c12_LSB);
        log.debug("c12 " + c12);


    }

    public void calculateTempAndPressureReadings() throws IOException, InterruptedException {

        if (shutdownPin != null) {
            shutdownPin.high();
            Thread.sleep(5);
        }

        //Data Conversion
        mpl115a2.write(convert, (byte) padc_MSB_address);
        log.debug("Convert signal sent right now");
        Thread.sleep(5);

        padc_MSB = mpl115a2.read(padc_MSB_address);
        padc_LSB = mpl115a2.read(padc_LSB_address);
        padc = (((padc_MSB << 8) | padc_LSB) >> 6);

        log.debug("padc MSB " + padc_MSB);
        log.debug("padc LSB " + padc_LSB);
        log.debug("padc " + padc);

        tadc_MSB = mpl115a2.read(tadc_MSB_address);
        tadc_LSB = mpl115a2.read(tadc_LSB_address);
        tadc = (((tadc_MSB << 8) | tadc_LSB) >> 6);

        log.debug("tadc core " + tadc);
        log.debug("tadc MSB " + tadc_MSB);
        log.debug("tadc LSB " + tadc_LSB);

        pcomp = a0 + (b1 + c12 * tadc) * padc + b2 * tadc;

        log.debug("pcomp : " + pcomp);

        pressure = pcomp * ((115.0 - 50.0) / 1023.0) + 50.0;
        temperature = (tadc - 498.0) / -5.35 + 25.0;

        log.info("temperature in °C is " + temperature);
        log.info("pressure : " + pressure);

        if (shutdownPin != null)
            shutdownPin.low();

    }

    public static void main(String[] args) {
        //quick test program
        MPL115A2 mpl115A2 = new MPL115A2();

        try {
            mpl115A2.init(RaspiPin.GPIO_27);
        } catch (IOException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
        while (true) {
            try {
                mpl115A2.calculateTempAndPressureReadings();
            } catch (IOException e) {
                log.error(e.getMessage());
                e.printStackTrace();
                break;
            } catch (InterruptedException e) {
                log.error(e.getMessage());
                e.printStackTrace();
                break;
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
                e.printStackTrace();
                break;
            }
        }


    }

}
