package hcm.ssjclay;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.view.SurfaceView;

import hcm.ssj.camera.CameraPainter;
import hcm.ssj.core.Consumer;
import hcm.ssj.core.Sensor;
import hcm.ssj.core.SensorProvider;
import hcm.ssj.core.TheFramework;
import hcm.ssjclay.creator.Builder;
import hcm.ssjclay.creator.Linker;

/**
 * Created by Frank Gaibler on 10.03.2016.
 */
public class testLinker extends ApplicationTestCase<Application>
{
    //test length in milliseconds
    private final static int TEST_LENGTH = 2 * 5 * 1000;

    /**
     *
     */
    public testLinker()
    {
        super(Application.class);
    }

    /**
     * @throws Exception
     */
    public void testInfrared() throws Exception
    {

        Builder.getInstance().scan(this.getContext());

        System.out.println(Builder.getInstance().sensors.get(0));
        System.out.println(Builder.getInstance().sensorProviders.get(0));
        System.out.println(Builder.getInstance().consumers.get(0));

        TheFramework frame = TheFramework.getFramework();
        frame.options.bufferSize.setValue(2.0f);
        Linker linker = Linker.getInstance();

        Sensor sensor = (Sensor) Builder.instantiate(Builder.getInstance().sensors.get(0));
        SensorProvider sensorProvider = (SensorProvider) Builder.instantiate(Builder.getInstance().sensorProviders.get(0));
        Consumer consumer = (Consumer) Builder.instantiate(Builder.getInstance().consumers.get(0));
        linker.add(sensor);
        linker.add(sensorProvider);
        linker.add(consumer);
        linker.addProvider(sensor, sensorProvider);
        linker.addProvider(consumer, sensorProvider);
        linker.setFrameSize(consumer, 1);
        linker.setDelta(consumer, 0);

        ((CameraPainter) consumer).options.surfaceView = new SurfaceView(this.getContext());

        linker.buildPipe();
        //start framework
        frame.Start();
        //run for two minutes
        long end = System.currentTimeMillis() + TEST_LENGTH;
        try
        {
            while (System.currentTimeMillis() < end)
            {
                Thread.sleep(1);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        frame.Stop();
        frame.invalidateFramework();
    }
}