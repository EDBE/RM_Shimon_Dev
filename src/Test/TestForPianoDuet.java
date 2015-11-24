package Test;

import Classical.MusicXMLParserForPianoDuet;
import Classical.PianoDuet;

import java.io.IOException;

/**
 * Created by musictechnology on 9/29/15.
 */
public class TestForPianoDuet {

    public static void main(String args[]) throws IOException {

        String _filename = "";
        System.out.println("Reading MusicXML file");

        if (args.length > 0) {
            _filename = args[0];
        } else {
            System.out.println("You need to specify any filename as an input argument");
            System.exit(0);
        }

        MusicXMLParserForPianoDuet parser = new MusicXMLParserForPianoDuet(_filename);
        PianoDuet testee;
        testee = parser.parse();
        //testee.printScore();  //print out the score in the console
        testee.write_antescofo();
//        testee.write_originalScore();
    }
}
