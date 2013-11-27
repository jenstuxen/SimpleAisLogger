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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import dk.dma.ais.binary.SixbitException;
import dk.dma.ais.message.AisMessageException;
import dk.dma.ais.packet.AisPacket;
import dk.dma.ais.packet.AisPacketOutputSinks;
import dk.dma.ais.reader.AisReader;
import dk.dma.ais.reader.AisReaders;
import dk.dma.commons.util.io.OutputStreamSink;
import dk.dma.enav.util.function.Consumer;

/**
 * @author Jens Tuxen
 * 
 */
public class MMSIExtractorApp {

    public static void main(String[] args) throws FileNotFoundException {
        System.out.println(MMSIExtractorApp.class);
        try {
            AisReader reader;
            if (args.length == 3) {
                reader = AisReaders.createDirectoryReader(args[0], args[1],
                        Boolean.getBoolean(args[2]));
            } else {
                reader = AisReaders.createDirectoryReader(".", "*.zip", true);
            }

            final OutputStreamSink<AisPacket> sink = AisPacketOutputSinks.OUTPUT_TO_TEXT;
            final BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(new File("BOPA911.ais")));
            final int mmsi = 219000174;

            reader.registerPacketHandler(new Consumer<AisPacket>() {
                Integer count = 0;
                Integer total = 0;
                long start = System.currentTimeMillis();

                @Override
                public void accept(AisPacket t) {
                    try {
                        if (t.getAisMessage().getUserId() == mmsi) {
                            sink.process(bos, t, count++);
                        }
                    } catch (AisMessageException e) {                        
                    } catch (SixbitException e) {
                    } catch (IOException e) {
                    }

                    total++;

                    if (total % 1000000 == 0) {
                        System.out.println("TOTAL: " + total + " MMSI: "
                                + count);
                        long ms = System.currentTimeMillis() - start;
                        System.out.println(total + " packets,  " + total
                                / ((double) ms / 1000) + " packets/s");
                    }

                }
            });
            reader.start();

            try {
                reader.join();
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                //e1.printStackTrace();
            } finally {
                bos.close();
            }

        } catch (Exception e) {
            //e.printStackTrace();
        }

    }

}
