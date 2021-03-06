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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.Lcd;
import com.pi4j.io.gpio.*;
import org.apache.log4j.Logger;

/**
 * Created by Jan on 24/01/2015.
 */


public class MPL115A2ToLCD {
    static Logger log = Logger.getLogger(be.byle.MPL115A2ToLCD.class.getName());

    public final static int LCD_ROWS = 2;
    public final static int LCD_COLUMNS = 16;
    public final static int LCD_BITS = 8;

    public static void main(String args[]) throws InterruptedException, UnsupportedEncodingException {
        MPL115A2 mpl115A2=new MPL115A2();

        System.out.println("<--Pi4J--> Wiring Pi LCD test program");
// setup wiringPi
        if (Gpio.wiringPiSetup() == -1) {
            System.out.println(" ==>> GPIO SETUP FAILED");
            return;
        }
// initialize LCD
        int lcdHandle= Lcd.lcdInit(LCD_ROWS, // number of row supported by LCD
                LCD_COLUMNS, // number of columns supported by LCD
                LCD_BITS, // number of bits used to communicate to LCD
                11, // LCD RS pin
                10, // LCD strobe pin
                0, // LCD data bit 1
                1, // LCD data bit 2
                2, // LCD data bit 3
                3, // LCD data bit 4
                4, // LCD data bit 5 (set to 0 if using 4 bit communication)
                5, // LCD data bit 6 (set to 0 if using 4 bit communication)
                6, // LCD data bit 7 (set to 0 if using 4 bit communication)
                7); // LCD data bit 8 (set to 0 if using 4 bit communication)
// verify initialization
        if (lcdHandle == -1) {
            System.out.println(" ==>> LCD INIT FAILED");
            return;
        }

        Lcd.lcdClear(lcdHandle);
        Thread.sleep(1000);
        Lcd.lcdHome(lcdHandle);
        Lcd.lcdPuts(lcdHandle, "Temp/Pressure----");
        Lcd.lcdPosition(lcdHandle, 0, 1) ;
        Lcd.lcdPuts (lcdHandle, "Retrieval-------") ;

        try {
            //initialize the mp1115a2 sensor
            mpl115A2.init(RaspiPin.GPIO_27);
        } catch (IOException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }

        while(true){
            try {
                //Do readings
                mpl115A2.calculateTempAndPressureReadings();
            } catch (IOException e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }

            Lcd.lcdHome(lcdHandle);
            Lcd.lcdPuts (lcdHandle, "Temperature-----") ;
            Lcd.lcdPosition (lcdHandle, 0, 1) ;
            if ((new Double(mpl115A2.temperature)).toString().length()>=14)
              Lcd.lcdPuts(lcdHandle, (new Double(mpl115A2.temperature)).toString().substring(0, 14)+" C");
            else
                log.info((new Double(mpl115A2.temperature)).toString());
            Thread.sleep(5000);
            Lcd.lcdHome(lcdHandle);
            Lcd.lcdPuts(lcdHandle, "Pressure--------") ;
            Lcd.lcdPosition(lcdHandle, 0, 1) ;
            if ((new Double(mpl115A2.pressure)).toString().length()>=16)
               Lcd.lcdPuts(lcdHandle, (new Double(mpl115A2.pressure)).toString().substring(0, 16));
            else
             log.info((new Double(mpl115A2.pressure)).toString());
            Thread.sleep(5000);
        }
    }
}