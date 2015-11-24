package RunBash;

import java.io.*;

/**
 * Created by musictechnology on 11/1/15.
 */
public class RunExeMain2 {
    public static void main(String args[]) {

//        ProcessBuilder pb = new ProcessBuilder("/Users/musictechnology/Documents/Kinect/OpenNI-Bin-Dev-MacOSX-v1.5.7.10/Samples/Bin/x64-Release/Sample-NiSimpleRead");
//        pb.directory(new File("/Users/musictechnology/Documents/Kinect/OpenNI-Bin-Dev-MacOSX-v1.5.7.10/Samples/Bin/x64-Release/"));
        ProcessBuilder pb = new ProcessBuilder("/Users/musictechnology/Documents/Kinect/NITE-Bin-Dev-MacOSX-v1.5.2.21/Samples/Bin/x64-Release/Sample-Players");
        pb.directory(new File("/Users/musictechnology/Documents/Kinect/NITE-Bin-Dev-MacOSX-v1.5.2.21/Samples/Bin/x64-Release/"));
        try {
            Process process = pb.start();
            String line;

            BufferedReader is = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            while ((line = is.readLine()) != null) {
//                System.out.println(line);
//            }
//            while (is.readLine() != null) {
//                System.out.println(is.readLine());
//            }
//            System.out.flush();

            //Maybe use event listener to listen, then do something

            OutputStream output = process.getOutputStream();
//            BufferedWriter bufferedWriter = new BufferedWriter(stderr);
            PrintWriter pw = new PrintWriter(output);
            while(true){
                pw.flush();
                Thread.sleep(1000);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
