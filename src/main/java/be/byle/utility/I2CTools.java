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
package be.byle.utility;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;

/**
 * Created by Jan Bylé on 31/01/2015.
 */
public class I2CTools {


    public static I2CDevice getDevice(int deviceAddress, int busNumber) throws IOException {
        I2CBus bus;
        bus = I2CFactory.getInstance(I2CBus.BUS_1);
        if (bus != null)
            return bus.getDevice(deviceAddress);
        return null;
    }
}
