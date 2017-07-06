package io.github.cornellautonomousbiketeam;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.jcraft.jsch.ChannelSftp.LsEntry;

import io.github.cornellautonomousbiketeam.App;

public class MainWindow {
    private JFrame frame;

    private JTextField saveLocField;
    private JComboBox<String> recentLocalComboBox;
    private JTextField remoteIpField;
    private JButton remoteRefreshButton;
    private JComboBox<String> recentRemoteComboBox;

    private File saveLocation = App.DEFAULT_SAVE_FOLDER;

    public MainWindow() {
        buildGui();
        //refresh();
    }

    public void setVisible( boolean visible ) {
        frame.setVisible( visible );
    }

    private void buildGui() {
        JPanel saveLocPanel = new JPanel( new BorderLayout() );
        saveLocPanel.add( new JLabel( "Current save location: " ), BorderLayout.WEST );
        saveLocField = new JTextField( 20 );
        saveLocField.setEditable( false );
        saveLocPanel.add( saveLocField, BorderLayout.CENTER );
        JPanel saveLocEastPanel = new JPanel( new BorderLayout() );
        saveLocEastPanel.add( Box.createHorizontalStrut( 5 ), BorderLayout.WEST );
        JButton saveLocBrowse = new JButton( "Browse..." );
        saveLocBrowse.setName( "saveLocBrowse" );
        saveLocBrowse.addActionListener( new GlobalListener() );
        saveLocEastPanel.add( saveLocBrowse, BorderLayout.CENTER );
        saveLocPanel.add( saveLocEastPanel, BorderLayout.EAST );

        JPanel recentLocalPanel = new JPanel( new BorderLayout() );
        recentLocalPanel.setBorder( BorderFactory.createTitledBorder( "Local CSVs" ) );
        JPanel recentLocalInnerPanel = new JPanel();
        recentLocalInnerPanel.add( new JLabel( "Open recent:" ) );
        recentLocalComboBox = new JComboBox();
        recentLocalComboBox.setEditable( false );
        recentLocalInnerPanel.add( recentLocalComboBox );
        JButton recentLocalOpen = new JButton( "Open" );
        recentLocalOpen.setName( "recentLocalOpen" );
        recentLocalOpen.addActionListener( new GlobalListener() );
        recentLocalInnerPanel.add( recentLocalOpen );
        recentLocalInnerPanel.add( new JLabel( "or" ) );
        JButton recentLocalBrowse = new JButton( "Browse..." );
        recentLocalBrowse.setName( "recentLocalBrowse" );
        recentLocalBrowse.addActionListener( new GlobalListener() );
        recentLocalInnerPanel.add( recentLocalBrowse );
        recentLocalPanel.add( recentLocalInnerPanel, BorderLayout.WEST );

        JPanel remotePanel = new JPanel( new GridLayout( 2, 1 ) );
        // remotePanel.setLayout( new BoxLayout( remotePanel, BoxLayout.Y_AXIS ) );
        remotePanel.setBorder( BorderFactory.createTitledBorder( "Remote CSVs" ) );
        JPanel remoteIpPanel = new JPanel();
        remoteIpPanel.add( new JLabel( "Connected to bike at " ) );
        remoteIpField = new JTextField( 20 );
        remoteIpField.setText( App.DEFAULT_IP_ADDRESS );
        remoteIpPanel.add( remoteIpField );
        JPanel remoteListPanel = new JPanel();
        remoteListPanel.add( new JLabel( "Download and open recent:" ) );
        recentRemoteComboBox = new JComboBox();
        recentRemoteComboBox.setEditable( false );
        remoteListPanel.add( recentRemoteComboBox );
        remoteRefreshButton = new JButton( "Refresh list" );
        remoteRefreshButton.setName( "remoteRefreshButton" );
        remoteRefreshButton.addActionListener( new GlobalListener() );
        remoteListPanel.add( remoteRefreshButton );
        JButton remoteButton = new JButton( "Open" );
        remoteButton.setName( "remoteButton" );
        remoteButton.addActionListener( new GlobalListener() );
        remoteListPanel.add( remoteButton );
        // remoteIpPanel.setAlignmentX( Component.LEFT_ALIGNMENT );
        // remoteIpPanel.setAlignmentY( Component.TOP_ALIGNMENT );
        // remoteListPanel.setAlignmentX( Component.LEFT_ALIGNMENT );
        // remoteListPanel.setAlignmentY( Component.TOP_ALIGNMENT );
        remotePanel.add( remoteIpPanel );
        remotePanel.add( remoteListPanel );

        frame = new JFrame( "Playback - Select CSV" );
        JPanel containerPanel = new JPanel( new BorderLayout() );
        containerPanel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
        JPanel northPanel = new JPanel();
        northPanel.setLayout( new BoxLayout( northPanel, BoxLayout.Y_AXIS ) );
        northPanel.add( saveLocPanel );
        northPanel.add( recentLocalPanel );
        northPanel.add( remotePanel );
        containerPanel.add( northPanel, BorderLayout.NORTH );
        frame.add( containerPanel );
        frame.setSize( 625, 300 );
        frame.setLocationByPlatform( true );
        frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
    }

    /**
     * Refreshes the contents of all of the labels and stuff.
     */
    public void refresh() {

        // Update the "Current save location" field
        saveLocField.setText( saveLocation.getAbsolutePath() );

        // Update the dropdown in the "Local CSVs" panel
        recentLocalComboBox.removeAllItems();
        for( File eachFile : saveLocation.listFiles() ) {
            if( !eachFile.isFile() ) {
                continue;
            }

            String fileName = eachFile.getName();
            int dotIndex = fileName.lastIndexOf( "." );
            if( dotIndex < 0 || !fileName.substring( dotIndex + 1 ).equals( "csv" ) ) {
                continue;
            }

            recentLocalComboBox.addItem( fileName );
        }

        refreshRemoteDropdown();
    }

    /**
     * Updates the dropdown in the "Remote CSVs" panel
     */
    private void refreshRemoteDropdown() {
        recentRemoteComboBox.removeAllItems();
        ( new Thread( () -> {
            try {
                String ipAddress = getValidatedIpAddress();
                if( ipAddress == null ) {
                    return;
                }
                List<LsEntry> list = BikeConnection.ls( "pi", ipAddress, App.BAGFILE_LOCATION );
                List<String> strings = list.stream()
                    .sorted( ( entry1, entry2 ) ->
                             entry2.getAttrs().getMTime() - entry1.getAttrs().getMTime() )
                    .filter( entry -> entry.getFilename().startsWith( "gps" ) )
                    .limit( 10 )
                    .map( LsEntry::getFilename )
                    .collect( Collectors.toList() );
                SwingUtilities.invokeLater( () -> {
                    remoteRefreshButton.setEnabled( true );
                    strings.stream().forEach( recentRemoteComboBox::addItem );
                } );
            } catch( Exception e ) {
                e.printStackTrace();
            }
        } ) ).start();
    }

    /**
     * Reads the IP address from the field in the "Remote CSVs" panel,
     * then validates it.
     *
     * @return The IP address if it's valid, null if it's not
     */
    private String getValidatedIpAddress() {
        String ipAddress = remoteIpField.getText();
        if( Helper.validateIpAddress( ipAddress ) ) {
            return ipAddress;
        } else {
            return null;
        }
    }

    class GlobalListener implements ActionListener {
        @Override
        public void actionPerformed( ActionEvent event ) {
            Object source = event.getSource();
            if( source instanceof JButton ) {
                JButton buttonSource = (JButton)source;
                String buttonName = buttonSource.getName();
                if( buttonName.equals( "recentLocalOpen" ) ) {
                    try {
                        File csvFile = new File( saveLocation,
                                (String)recentLocalComboBox.getSelectedItem() );
                        List<TimedBikeState> bikeStates = CsvParser.parseFile( csvFile );
                        App.displayCsvInWindow( bikeStates );
                    } catch( IOException e ) {
                        e.printStackTrace();
                    }
                } else if( buttonName.equals( "recentLocalBrowse" ) ) {
                    try {
                        final JFileChooser fileChooser = new JFileChooser();
                        fileChooser.addChoosableFileFilter( new FileNameExtensionFilter( "CSV file", "csv" ) );
                        int result = fileChooser.showOpenDialog( frame );
                        if( result != JFileChooser.APPROVE_OPTION ) {
                            return;
                        }
                        File csvFile = fileChooser.getSelectedFile();
                        List<TimedBikeState> bikeStates = CsvParser.parseFile( csvFile );
                        App.displayCsvInWindow( bikeStates );
                    } catch( IOException e ) {
                        e.printStackTrace();
                    }
                } else if( buttonName.equals( "remoteButton" ) ) {
                    try {
                        String ipAddress = getValidatedIpAddress();
                        if( ipAddress == null ) {
                            JOptionPane.showConfirmDialog( frame,
                                    "Invalid IP address!",
                                    "Error while connecting to bike",
                                    JOptionPane.ERROR_MESSAGE );
                            return;
                        }
                        String filename = (String)recentRemoteComboBox.getSelectedItem();
                        String fullRemotePath = App.BAGFILE_LOCATION + "/" + filename;

                        String localPath = ( new File( saveLocation, filename ) ).getAbsolutePath();

                        System.out.println( String.format( "Downloading from %s to %s...", fullRemotePath, localPath ) );

                        File csvFile = BikeConnection.copy( "pi", ipAddress, fullRemotePath, localPath );
                        List<TimedBikeState> bikeStates = CsvParser.parseFile( csvFile );
                        App.displayCsvInWindow( bikeStates );
                    } catch( Exception e ) {
                        e.printStackTrace();
                    }
                } else if( buttonName.equals( "remoteRefreshButton" ) ) {
                    remoteRefreshButton.setEnabled( false );
                    refresh();
                }
            }
        }
    }
}
