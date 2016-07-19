/*
 * testBluetoothServer.java
 * Copyright (c) 2016
 * Authors: Ionut Damian, Michael Dietz, Frank Gaibler, Daniel Langerenken, Simon Flutura
 * *****************************************************
 * This file is part of the Social Signal Interpretation for Java (SSJ) framework
 * developed at the Lab for Human Centered Multimedia of the University of Augsburg.
 *
 * SSJ has been inspired by the SSI (http://openssi.net) framework. SSJ is not a
 * one-to-one port of SSI to Java, it is an approximation. Nor does SSJ pretend
 * to offer SSI's comprehensive functionality and performance (this is java after all).
 * Nevertheless, SSJ borrows a lot of programming patterns from SSI.
 *
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package hcm.ssj;

import android.app.Application;
import android.test.ApplicationTestCase;

import hcm.ssj.core.Cons;
import hcm.ssj.core.EventChannel;
import hcm.ssj.core.Log;
import hcm.ssj.core.TheFramework;
import hcm.ssj.ioput.BluetoothConnection;
import hcm.ssj.ioput.BluetoothEventReader;
import hcm.ssj.ioput.BluetoothProvider;
import hcm.ssj.ioput.BluetoothReader;
import hcm.ssj.test.EventLogger;
import hcm.ssj.test.Logger;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class testBluetoothServer extends ApplicationTestCase<Application> {

    String _name = "BLServer";

    public testBluetoothServer() {
        super(Application.class);
    }

    public void test() throws Exception
    {
        TheFramework frame = TheFramework.getFramework();
        frame.options.bufferSize.set(10.0f);

        BluetoothReader blr = new BluetoothReader();
        blr.options.connectionType.set(BluetoothConnection.Type.SERVER);
        blr.options.connectionName.set("stream");

        BluetoothProvider data = new BluetoothProvider();
        data.options.dim.set(3);
        data.options.bytes.set(4);
        data.options.type.set(Cons.Type.FLOAT);
        data.options.sr.set(50.0);
        frame.addSensor(blr);
        blr.addProvider(data);

        Logger dummy = new Logger();
        dummy.options.reduceNum.set(true);
        frame.addConsumer(dummy, data, 1.0, 0);

        BluetoothEventReader bler = new BluetoothEventReader();
        bler.options.connectionType.set(BluetoothConnection.Type.SERVER);
        bler.options.connectionName.set("event");
        frame.addComponent(bler);
        EventChannel ch = frame.registerEventProvider(bler);

        EventLogger evlog = new EventLogger();
        frame.addComponent(evlog);
        frame.registerEventListener(evlog, ch);

        try {
            frame.Start();

            long start = System.currentTimeMillis();
            while(true)
            {
                if(System.currentTimeMillis() > start + 5 * 60 * 1000)
                    break;

                Thread.sleep(1);
            }

            frame.Stop();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        Log.i("test finished");
    }
}