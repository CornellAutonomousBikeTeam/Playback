package io.github.cornellautonomousbiketeam;

import java.nio.file.Paths;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp.LsEntry;

public class App {
    public static void main( String[] args ) {
        String remoteFile = Paths.get( "/home/pi/ros_ws/src/bike/bagfiles",
                "gps_2017-06-29~~04-53-26-PM.csv" ).toString();
        System.out.println( "Starting remote operation..." );
        //FileCopier.copy( "pi", "10.0.1.25",
        //      remoteFile, "/home/daniel/Desktop/COPYING-TEST" );
        Vector list = BikeConnection.ls( "pi", "10.0.1.25", "/home/pi/ros_ws/src/bike/bagfiles" );
        for( int i = 0; i < list.size(); i++ ) {
            System.out.println( ( (LsEntry)list.get( i ) ).getFilename() );
        }
        System.out.println( "Done!" );
    }
}
