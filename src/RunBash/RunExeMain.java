package RunBash;

/**
 * Created by Liang on 10/31/15.
 */

//import java.io.ByteArrayOutputStream;
//import java.io.InputStream;
//import java.io.OutputStream;
//
//public class RunExeMain {
//    //This is an example that display how to run exe using java
//    public static void main(String args[]) {
//        String filePath = "/Users/musictechnology/Documents/Kinect/OpenNI-Bin-Dev-MacOSX-v1.5.7.10/Samples/Bin/x64-Release/Sample-NiUserTracker";
//        try {
//            Process p = Runtime.getRuntime().exec(filePath);
//            OutputStream info = p.getOutputStream();
//            System.out.println(info);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        p.waitFor();
//        InputStream in = p.getInputStream();
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//        int c = -1;
//        while((c = in.read()) != -1)
//        {
//            baos.write(c);
//        }
//
//        String response = new String(baos.toByteArray());
//        System.out.println("Response From Exe : "+response);
//
//    } catch (Exception e) {
//        e.printStackTrace();
//    }
//    }
//}


//import java.io.ByteArrayOutputStream;
//import java.io.InputStream;
//
//public class RunExeMain {
//    //This is Example that display how to get response using java
//    public static void main(String args[])
//    {
//String filePath = "/Users/musictechnology/Documents/Kinect/OpenNI-Bin-Dev-MacOSX-v1.5.7.10/Samples/Bin/x64-Release/Sample-NiUserTracker";
//try {
//        Process p = Runtime.getRuntime().exec(filePath);
//            p.waitFor();
//            InputStream in = p.getInputStream();
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//            int c = -1;
//            while((c = in.read()) != -1)
//            {
//                baos.write(c);
//            }
//
//            String response = new String(baos.toByteArray());
//            System.out.println("Response From Exe : "+response);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//}



//{
//    public static void main(String args[])
//    {
//        try
//        {
//            Runtime rt = Runtime.getRuntime();
//            Process proc = rt.exec("/Users/musictechnology/Documents/Kinect/OpenNI-Bin-Dev-MacOSX-v1.5.7.10/Samples/Bin/x64-Release/Sample-NiUserTracker");
////            InputStream stderr = proc.getErrorStream();
////            InputStream stderr = proc.getInputStream();
////            InputStreamReader isr = new InputStreamReader(stderr);
//            OutputStream stderr = proc.getOutputStream();
//            OutputStreamWriter isr = new OutputStreamWriter(stderr);
////            BufferedReader br = new BufferedReader(isr);
//            BufferedWriter bw = new BufferedWriter(isr);
//            String line = null;
//            System.out.println("Ouput: ");
////            while ( (line = br.readLine()) != null)
//            while((line = bw.toString()) != null)
//                System.out.println(line);
//            System.out.println("Output ending");
//            int exitVal = proc.waitFor();
//            System.out.println("Process exitValue: " + exitVal);
//        } catch (Throwable t)
//        {
//            t.printStackTrace();
//        }
//    }
//}

import java.util.*;
import java.io.IOException;
public class RunExeMain {

        public static void main(String[] args) throws Exception
        {
            new RunExeMain();
        }

        // can run basic ls or ps commands
        // can run command pipelines
        // can run sudo command if you know the password is correct
        public RunExeMain() throws IOException, InterruptedException
        {
            // build the system command we want to run
            List<String> commands = new ArrayList<String>();
//            commands.add("/bin/sh");
//            commands.add("-c");
//            commands.add("ls -l /var/tmp | grep tmp");
            commands.add("/Users/musictechnology/Documents/Kinect/OpenNI-Bin-Dev-MacOSX-v1.5.7.10/Samples/Bin/x64-Release/Sample-NiUserTracker");

            // execute the command
            SystemCommandExecutor commandExecutor = new SystemCommandExecutor(commands);
            int result = commandExecutor.executeCommand();

            // get the stdout and stderr from the command that was run
            StringBuilder stdout = commandExecutor.getStandardOutputFromCommand();
            StringBuilder stderr = commandExecutor.getStandardErrorFromCommand();

            // print the stdout and stderr
            System.out.println("The numeric result of the command was: " + result);
            System.out.println("STDOUT:");
            System.out.println(stdout);
            System.out.println("STDERR:");
            System.out.println(stderr);
        }
    }
