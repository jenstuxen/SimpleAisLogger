package org.tuxen.simepleais;

import java.io.FileNotFoundException;
import java.util.concurrent.ConcurrentHashMap;

import dk.dma.ais.data.AisTarget;
import dk.dma.ais.reader.AisReader;
import dk.dma.ais.reader.AisReaders;

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
            
            System.out.println("ships seen:" +reports.keySet().size());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
