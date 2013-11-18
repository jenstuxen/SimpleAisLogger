package org.tuxen.simepleais;

import java.util.LinkedHashMap;

/**
 * A map of data, structured by request from Ramboll
 * 
 * @author Jens Tuxen
 */
public class AisDataMap extends LinkedHashMap<String, String> {

	private static final long serialVersionUID = 1L;

	//final static String keys = "MMSI,Ship name,Call sign,imo number,Ship type,destination,Length,Breadth,maximum actual draught,cargo,Latitude,Longitude,Time stamp,SOG,COG,heading,nav status,Long,Lat,Time"
	//		.toLowerCase();

	final static String keys = "MMSI,Ship name,Call sign,imo number,Ship type,destination,maximum actual draught,cargo,Latitude,Longitude,Time stamp,SOG,COG,heading,nav status,Long,Lat,Time,starboard,port,stern,bow"
			.toLowerCase();

	
	public AisDataMap() {
		super(keys.split(",").length);
		for (String k : keys.split(",")) {
			this.put(k.toLowerCase(), "");
		}
	}

	public void put(String key, Integer value) {
		super.put(key.toLowerCase(), Integer.toString(value));

	}

	public void put(String key, Double value) {
		super.put(key.toLowerCase(), Double.toString(value));
	}

	
	public void put(String key, Long value) {
		super.put(key.toLowerCase(), Long.toString(value));
	}

	
	@Override
	public String toString() {
		return this.toTSVString();
	}

	public String toTSVString() {
		return toString('\t');
	}
	
	public String toCSVString() {
		return toString(',');
	}
	
	public String toString(Character seperator) {
		StringBuilder sb = new StringBuilder();
		for (String key : this.keySet()) {
			sb.append(this.get(key));
			sb.append(seperator);
		}
		return sb.toString();
	}

}
