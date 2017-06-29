package io.github.cornellautonomousbiketeam;

import java.nio.file.Paths;

public class App {
    public static void main( String[] args ) {
        String remoteFile = Paths.get( "/home/pi/ros_ws/src/bike/bagfiles",
                "gps_2017-06-29~~04-53-26-PM.csv" ).toString();
        System.out.println( "Starting the copy operation..." );
        FileCopier.copy( "pi", "10.0.1.25",
                remoteFile, "/home/daniel/Desktop/COPYING-TEST" );
    }
}
