package org.tuxen.simepleais;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.ais.binary.SixbitException;
import dk.dma.ais.data.AisClassAStatic;
import dk.dma.ais.data.AisClassBStatic;
import dk.dma.ais.data.AisTarget;
import dk.dma.ais.data.AisTargetDimensions;
import dk.dma.ais.data.AisVesselStatic;
import dk.dma.ais.data.AisVesselTarget;
import dk.dma.ais.message.AisMessage;
import dk.dma.ais.message.AisMessage21;
import dk.dma.ais.message.AisMessageException;
import dk.dma.ais.message.AisPositionMessage;
import dk.dma.ais.message.AisStaticCommon;
import dk.dma.ais.message.IVesselPositionMessage;
import dk.dma.ais.message.NavigationalStatus;
import dk.dma.ais.message.ShipTypeCargo;
import dk.dma.ais.packet.AisPacket;
import dk.dma.enav.model.geometry.Position;
import dk.dma.enav.util.function.Consumer;

/**
 * Creates an output sink that stores AIS data with injected static data in .tsv
 * format
 * 
 * @author Jens Tuxen
 * 
 */
public class AisMessageOutputSinkTable implements Consumer<AisPacket> {
	private Logger LOG = LoggerFactory.getLogger(AisMessageOutputSinkTable.class);
	private final PrintWriter fos;
	private ConcurrentHashMap<Integer, AisTarget> reports;
    private final SimpleDateFormat filenameFormatter = new SimpleDateFormat(
            "YYYY-MM-dd-HH:mm:ssZ".replace(" ", "_").replace(":","_"));
    private final SimpleDateFormat dirNameFormatter = new SimpleDateFormat("YYYY-MM-dd");
    
    private final SimpleDateFormat timestampFormat = new SimpleDateFormat("YYYY-MM-dd-HH:mm:ssZ");


	/**
	 * Create an CSV/TSV table output sink with a lookup table
	 * 
	 * @param reports
	 *            lookup table for static reports. They will be read and written
	 *            to this table.
	 * @throws IOException
	 */
    public AisMessageOutputSinkTable(
            ConcurrentHashMap<Integer, AisTarget> reports) throws IOException {
        new File(dirNameFormatter.format(new Date())).mkdirs();
        
        File f = new File(dirNameFormatter.format(new Date()) + "/"
                + filenameFormatter.format(new Date()) + ".tsv");
        fos = new PrintWriter(new BufferedWriter(new FileWriter(f)));
        this.reports = reports;
    }

	public ConcurrentHashMap<Integer, AisTarget> getReports() {
		return reports;
	}

	public void setReports(ConcurrentHashMap<Integer, AisTarget> reports) {
		this.reports = reports;
	}

	public AisMessageOutputSinkTable() throws IOException {
		this(new ConcurrentHashMap<Integer, AisTarget>());
	}
	
	
	@Override
	public void accept(AisPacket aisPacket) {
		try {
			this.process(aisPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * try to process, or continue otherwise
	 */
	public void process(AisPacket aisPacket) {
		//throw out packets that have bad  or bad messages
		AisMessage aisMessage;
		try {
			aisMessage = aisPacket.getAisMessage();
		} catch (AisMessageException | SixbitException e) {
			e.printStackTrace();
			return;
		}
		
		if (aisPacket.getBestTimestamp() < 0) {
			return;
		}
		
		long start = System.currentTimeMillis();

		AisDataMap line = new AisDataMap();
		AisTarget aisTarget = null;

		line.put("time stamp", aisPacket.getBestTimestamp()/1000);
		line.put("time", filenameFormatter.format(new Date(aisPacket.getBestTimestamp())));
		line.put("mmsi", aisMessage.getUserId());

		// Handle static reports for both class A and B vessels (msg 5 + 24)
		if (aisMessage instanceof AisStaticCommon) {
			aisTarget = reports.get(aisMessage.getUserId());
			if (aisTarget == null) {
				aisTarget = AisTarget.createTarget(aisMessage);
			}
			aisTarget.update(aisMessage);
			reports.putIfAbsent(aisTarget.getMmsi(), aisTarget);
		}

		// Handle AtoN message
		if (aisMessage instanceof AisMessage21) {
			// AisMessage21 msg21 = (AisMessage21) aisMessage;
			// break;
			// System.out.println("AtoN name: " + msg21.getName());
		}
		// Handle position messages 1,2 and 3 (class A) by using their shared
		// parent
		if (aisMessage instanceof AisPositionMessage) {
			AisPositionMessage posMessage = (AisPositionMessage) aisMessage;
			
			line.put("rot", posMessage.getRot());
			
			line.put("nav status",
					NavigationalStatus.get(posMessage.getNavStatus())
							.toString());
		}
		// Handle position messages 1,2,3 and 18 (class A and B)
		if (aisMessage instanceof IVesselPositionMessage) {
			IVesselPositionMessage posMessage = (IVesselPositionMessage) aisMessage;
			line.put("cog", posMessage.getCog());
			line.put("sog", posMessage.getSog());

			Position pos = posMessage.getPos().getGeoLocation();
			
			try {
				line.put("latitude", pos.getLatitude());
				line.put("longitude", pos.getLongitude());
			} catch (Exception e) {
				
			}
			
			
			line.put("lat", posMessage.getPos().getLatitudeDouble());
			line.put("long", posMessage.getPos().getLongitudeDouble());
			line.put("heading", posMessage.getTrueHeading());
		}

		if (aisTarget == null) {
			aisTarget = reports.get(aisMessage.getUserId());
		}
		// lookup static reports received previously
		if (aisTarget != null && aisTarget instanceof AisVesselTarget) {
			AisVesselStatic avs = ((AisVesselTarget) aisTarget)
					.getVesselStatic();

			line.put("call sign", avs.getCallsign());
			line.put("ship name", avs.getName());

			ShipTypeCargo stc = new ShipTypeCargo(avs.getShipType());
			line.put("ship type", stc.getShipType().toString());
			line.put("cargo", stc.getShipCargo().toString());
			
			AisTargetDimensions dims = avs.getDimensions();
			

			line.put("starboard",(new Byte(dims.getDimStarboard())).intValue());
			line.put("port",(new Short(dims.getDimPort())).intValue());
			line.put("stern",(new Short(dims.getDimStern())).intValue());
			line.put("bow",(new Short(dims.getDimBow())).intValue());
			
			if (avs instanceof AisClassBStatic) {
			} else if (avs instanceof AisClassAStatic) {
				AisClassAStatic acas = (AisClassAStatic) avs;
				try {
					line.put("imo number", acas.getImoNo());
				} catch (NullPointerException e) {
					line.put("imo number", "");
				}
				
				try {
					line.put("maximum actual draught", acas.getDraught());
				} catch (NullPointerException e) {
					line.put("maximum actual draught", "");
				}
				
				line.put("destination", acas.getDestination());
			}
		}

		final String result = line.toString();
		// System.out.println(result);
		fos.println(result);

		long end = System.currentTimeMillis();

		if (end - start > 10) {
			LOG.info("Time to process: " + (end - start));
		}

	}

	/**
	 * after handing over reports hashmap (optional) using getReports(), we can
	 * die gracefully.
	 */
	public void die() {
		fos.close();
	}

}
