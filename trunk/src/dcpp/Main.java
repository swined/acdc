package dcpp;

import java.io.FileOutputStream;
import logger.ConsoleLogger;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length == 0)
        args = new String[] {
            "p2p.academ.org",
            //"dcpp.nsunet.ru",
            "411",
            "STMUNEWY73LI5KQCVMLWXDMGXZKD76GPJ3M6EQA", // battery
            //"YVWQRDZ4KUJSIAG5WFIRJ3L6Z5KZ7ACXEV37QPA", // dest point 3
            //"OHD4ZFEBI6XAUND2I7LOFCEW74EUZGVVGSAVFPQ", // 12 oz ms
            //"VB22K37AAHT65QLUVVS7SYUYURLS75UQEBNYVMI",
            //"I323N3GKWK3G2YDMQBNIIOKKU522MGQJ4VVTN7Q",
            //"BHBJUM22KY5UJFYIN4NZS37KXCGKQXUG2F23IMI",
            "/tmp/dcget.out",
        };
        ConsoleLogger logger = new ConsoleLogger(false);
        try {
            FileOutputStream file = new FileOutputStream(args[3]);
            DownloadManager m = new DownloadManager(logger, file, args[2]);
            m.download(args[0], new Integer(args[1]));
            logger.debug("done");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

}
