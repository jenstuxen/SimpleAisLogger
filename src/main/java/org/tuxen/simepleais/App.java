package org.tuxen.simepleais;

import java.io.FileNotFoundException;

import dk.dma.ais.reader.AisReader;
import dk.dma.ais.reader.AisReaders;
/**
 * 
 * @author Jens Tuxen
 * 
 */
public class App {
	public static void main(String[] args) throws FileNotFoundException {
		// run indefinitely
		for (;;) {
			try {
				AisReader reader;
				if (args.length == 2) {
					reader = AisReaders.createReader(args[0],
							Integer.valueOf(args[1]));
				} else {
					reader = AisReaders.createReader("localhost", 4001);
				}

				RollingOutputSink sink = new RollingOutputSink();
				PacketsPerSecondConsumer stat = new PacketsPerSecondConsumer();
				//RollingOutputSink sink = new RollingOutputSink(2,TimeUnit.MINUTES);
				reader.registerPacketHandler(sink);
				reader.start();

				try {
					reader.join();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

}
