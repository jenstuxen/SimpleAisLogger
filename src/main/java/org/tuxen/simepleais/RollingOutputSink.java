package org.tuxen.simepleais;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import dk.dma.ais.packet.AisPacket;
import dk.dma.enav.util.function.Consumer;

/**
 * Quick Class to implement a "Rolling"-like output sink
 * 
 * @author Jens Tuxen
 * 
 */
public class RollingOutputSink extends Thread implements Consumer<AisPacket> {
	// should be generalized to Consumer<>
	private volatile AisMessageOutputSinkTable current;
	private TimeUnit unit;
	private long timeout;

	/**
	 * Create a rolling-like output sink
	 * 
	 * @param consumer
	 *            current consumer to be switched out after timeout
	 * @param timeout
	 *            duration value for when to switch to new sink
	 * @param unit
	 *            timeunit of duration (seconds,minuts,hours,years)
	 */
	public RollingOutputSink(AisMessageOutputSinkTable consumer, long timeout,
			TimeUnit unit) {
		super();
		current = consumer;
		this.unit = unit;
		this.timeout = timeout;
		this.start();

	}

	public RollingOutputSink() throws IOException {
		this(new AisMessageOutputSinkTable(), 1, TimeUnit.HOURS);
	}

	public RollingOutputSink(long timeout, TimeUnit unit) throws IOException {
		this(new AisMessageOutputSinkTable(), timeout, unit);
	}

	@Override
	public void accept(AisPacket arg0) {
		current.accept(arg0);
	}

	@Override
	public void run() {
		for (;;) {
			try {
				Thread.sleep(unit.toMillis(timeout));
				AisMessageOutputSinkTable old = current;
				AisMessageOutputSinkTable next = new AisMessageOutputSinkTable(
						old.getReports());
				current = next;
				Thread.sleep(2000);
				old.die();
				System.out.println("Switched to new file-sink");

			} catch (InterruptedException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}
