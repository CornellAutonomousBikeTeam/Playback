package io.github.cornellautonomousbiketeam;

import java.awt.BorderLayout;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileSystemView;

import com.jcraft.jsch.ChannelSftp.LsEntry;

import io.github.cornellautonomousbiketeam.BikeConnection;
import io.github.cornellautonomousbiketeam.MainWindow;
import io.github.cornellautonomousbiketeam.TimedBikeState;

public class App {
    public static final String BAGFILE_LOCATION =
        "/home/pi/ros_ws/src/bike/bagfiles";
    public static final File DEFAULT_SAVE_FOLDER;
    public static final String DEFAULT_IP_ADDRESS = "10.0.1.25";

    public static void main( String[] args ) {
        ( new MainWindow() ).setVisible( true );
    }

    static {
        File saveFolder = FileSystemView.getFileSystemView().getHomeDirectory();
        File desktop = new File( saveFolder, "Desktop" );
        if( desktop.exists() && desktop.isDirectory() ) {
            DEFAULT_SAVE_FOLDER = desktop;
        } else {
            DEFAULT_SAVE_FOLDER = saveFolder;
        }
    }

    public static File downloadLatestCsvWithPrefix( String prefix ) {

        // Get IP
        String ipAddress = (String)JOptionPane.showInputDialog( null,
                "Bike IP Address?", "Connecting to bike...",
                JOptionPane.PLAIN_MESSAGE, null, null, DEFAULT_IP_ADDRESS );
        if( ipAddress == null || ipAddress.length() == 0 ) {
            ipAddress = DEFAULT_IP_ADDRESS;
        }

        // Decide where to save
        File saveFolder = null;

        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
        int result = fileChooser.showOpenDialog( null );
        if( result == JFileChooser.APPROVE_OPTION ) {
            saveFolder = fileChooser.getSelectedFile();
        } else {
            saveFolder = DEFAULT_SAVE_FOLDER;
        }

        return downloadLatestCsvWithPrefix( prefix, ipAddress, saveFolder );
    }

    public static File downloadLatestCsvWithPrefix( String prefix, String ipAddress, File saveFolder ) {

        // List all the CSVs and get the last-modified one with the prefix
        List<LsEntry> list = BikeConnection.ls( "pi", ipAddress, BAGFILE_LOCATION );
        int maxMTime = 0;
        String lastModifiedFilename = null;
        int currMTime = 0;
        for( LsEntry currEntry : list ) {
            if( !currEntry.getFilename().startsWith( prefix ) ) {
                continue;
            }

            currMTime = currEntry.getAttrs().getMTime();
            if( currMTime > maxMTime ) {
                maxMTime = currMTime;
                lastModifiedFilename = currEntry.getFilename();
            }
        }

        String fullRemotePath = BAGFILE_LOCATION + "/" + lastModifiedFilename;

        String localPath = Paths.get( saveFolder.getPath(),
                lastModifiedFilename ).toString();

        System.out.println( String.format( "Downloading from %s to %s...", fullRemotePath, localPath ) );

        BikeConnection.copy( "pi", ipAddress, fullRemotePath, localPath );

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
        frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        frame.setVisible( true );
    }
}
