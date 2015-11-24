package RunBash;

import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;
import com.sun.org.apache.xalan.internal.xslt.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.Process;

/**
 * Created by musictechnology on 11/1/15.
 */
public class RunExecutableMaxObj extends MaxObject{
    String filePath = "";
    String fileFolderPath = "";

    /*
    Constructor: declare the input and output of the max object
    2 inputs, and 2 outputs
     */
    public RunExecutableMaxObj() {
        /*
        Input 1: receive message
        ========================
        Output 1: Number of Detected User in the scene
        Output 2: bang, when detect a new user
         */
        declareInlets(new int[]{DataTypes.ALL, DataTypes.ALL});
        declareOutlets(new int[]{DataTypes.ALL, DataTypes.ALL});
    }

    /*
    input: executable file path, the path of the file's folder
    output: the number of user are detected in the scene
     */
    public int start(String fp, String ffp) {
        filePath = fp;
        fileFolderPath = ffp;
        ProcessBuilder pb = new ProcessBuilder(filePath);
        pb.directory(new File(fileFolderPath));
        try {
            Process process = pb.start();
            OutputStream output = process.getOutputStream();
            PrintWriter pw = new PrintWriter(output);

            while (true) {
                pw.flush();
                Thread.sleep(1000);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int numUsers = 0;

        return numUsers;
    }

    /*
    input: an arbitrary string
    output: all the digits which are contained in the string
     */
    public int digitOnly(String input) {
        int digit = 0;
        String digitInString= input.replaceAll("[^0-9]", "");
        digit = Integer.valueOf(digitInString);
        return digit;
    }
}
