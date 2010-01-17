package dcpp;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length == 0)
        args = new String[] {
            "p2p.academ.org",
            "411",
            "xxSTMUNEWY73LI5KQCVMLWXDMGXZKD76GPJ3M6EQA", // battery
            "/tmp/dcget.out",
        };
        DownloadManager m = new DownloadManager();
        m.download(args[0], new Integer(args[1]));
    }

}
