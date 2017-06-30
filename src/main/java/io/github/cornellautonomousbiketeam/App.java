package io.github.cornellautonomousbiketeam;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.jcraft.jsch.ChannelSftp.LsEntry;

import io.github.cornellautonomousbiketeam.BikeConnection;
import io.github.cornellautonomousbiketeam.CsvParser;
import io.github.cornellautonomousbiketeam.TimedBikeState;

public class App {
    public static final String BAGFILE_LOCATION =
        "/home/pi/ros_ws/src/bike/bagfiles";
    public static final String DEFAULT_SAVE_FOLDER =
        "/home/daniel/Desktop";

    public static void main( String[] args ) {
        //File csv = downloadLatestCsvWithPrefix( "gps" );
        File csvFile = new File( "/home/daniel/Desktop/gps_2017-06-29~~04-53-26-PM.csv" );
        try {
            List<TimedBikeState> bikeStates = CsvParser.parseFile( csvFile );
            System.out.println( bikeStates.iterator().next() );
            displayCsvInWindow( bikeStates );
        } catch( IOException e ) {
            e.printStackTrace();
        }
    }

    public static File downloadLatestCsvWithPrefix( String prefix ) {

        // List all the CSVs and get the last-modified one with the prefix
        Vector list = BikeConnection.ls( "pi", "10.0.1.25", BAGFILE_LOCATION );
        int maxMTime = 0;
        String lastModifiedFilename = null;
        LsEntry currEntry = null;
        int currMTime = 0;
        for( Object obj : list ) {
            currEntry = (LsEntry)obj;
            if( !currEntry.getFilename().startsWith( prefix ) ) {
                continue;
            }

            currMTime = currEntry.getAttrs().getMTime();
            if( currMTime > maxMTime ) {
                maxMTime = currMTime;
                lastModifiedFilename = currEntry.getFilename();
            }
        }

        String fullRemotePath = Paths.get( BAGFILE_LOCATION,
                lastModifiedFilename ).toString();

        // Decide where to save
        File chosenFile = new File( DEFAULT_SAVE_FOLDER );
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
        int result = fileChooser.showOpenDialog( null );
        if( result == JFileChooser.APPROVE_OPTION ) {
            chosenFile = fileChooser.getSelectedFile();
        }
        String localPath = Paths.get( chosenFile.getPath(),
                lastModifiedFilename ).toString();

        BikeConnection.copy( "pi", "10.0.1.25", fullRemotePath, localPath );
        System.out.println( "Done!" );

        return new File( localPath );
    }

    public static void displayCsvInWindow( List<TimedBikeState> states ) {
        JFrame frame = new JFrame( "GPS Simulator 2017" );
        JPanel mainPanel = new JPanel( new BorderLayout() );
        mainPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
        GpsPanel gpsPanel = new GpsPanel( states );
        mainPanel.add( gpsPanel );
        frame.add( mainPanel );
        frame.setSize( 800, 600 );
        frame.setLocationByPlatform( true );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setVisible( true );
    }
}
