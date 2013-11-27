package org.tuxen.simepleais;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.concurrent.ConcurrentHashMap;

import dk.dma.ais.data.AisTarget;
import dk.dma.ais.packet.AisPacket;
import dk.dma.ais.packet.AisPacketOutputSinks;
import dk.dma.ais.reader.AisReader;
import dk.dma.ais.reader.AisReaders;
import dk.dma.commons.util.io.OutputStreamSink;
import dk.dma.enav.util.function.Consumer;

/**
 * 
 * @author Jens Tuxen
 * 
 */
public class FileReaderApp {
    public static void main(String[] args) throws FileNotFoundException {
        try {
            AisReader reader;
            if (args.length == 3) {
                reader = AisReaders.createDirectoryReader(args[0], args[1], Boolean.getBoolean(args[2]));
            } else {
                reader = AisReaders.createDirectoryReader(".", "*.zip",true);
            }
            
            AisMessageOutputSinkTable sink = new AisMessageOutputSinkTable();
            reader.registerPacketHandler(sink);
            reader.start();
            
            

            try {
                reader.join();
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } finally {
                sink.die();
            }
            

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
