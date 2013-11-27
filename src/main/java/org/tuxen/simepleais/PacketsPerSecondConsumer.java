/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.tuxen.simepleais;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.ais.packet.AisPacket;
import dk.dma.enav.util.function.Consumer;
/**
 * @author Jens Tuxen
 *
 */
public class PacketsPerSecondConsumer implements Consumer<AisPacket> {
    private Logger LOG;
    private long start = 0;
    private long now = 0;
    private long count = 0;
    
    


    /**
     * @param count the count to set
     */
    public synchronized void setCount(long count) {
        this.count = count;
    }

    /**
     * 
     */
    public PacketsPerSecondConsumer() {
        LOG = LoggerFactory.getLogger(this.getClass());
        start = System.currentTimeMillis();
    }
    
    /**
     * @return the start
     */
    public long getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(long start) {
        this.start = start;
    }

    /**
     * @return the now
     */
    public long getNow() {
        return now;
    }

    public void setNow() {
        this.now = System.currentTimeMillis();
    }
    
    public void getStat() {
        setNow();
        LOG.info("Total: "+count+" packets. "+ getCount() / ((double) (getNow()-getStart()) / 1000) + " packets/s");
    }

    /* (non-Javadoc)
     * @see dk.dma.enav.util.function.Consumer#accept(java.lang.Object)
     */
    @Override
    public void accept(AisPacket t) {
        count++;
        
        if (count % 10000 == 0) {
            getStat();
        }
    }
    
    /**
     * @return the count
     */
    public long getCount() {
        return count;
    }

}
