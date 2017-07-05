package io.github.cornellautonomousbiketeam;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import io.github.cornellautonomousbiketeam.App;

public class MainWindow {
    private JFrame frame;

    private JTextField saveLocField;
    private JComboBox<String> recentLocalComboBox;
    private JTextField remoteIpField;

    private File saveLocation = new File( App.DEFAULT_SAVE_FOLDER );

    public MainWindow() {
        buildGui();
        refresh();
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

        JPanel remotePanel = new JPanel( new BorderLayout() );
        remotePanel.setBorder( BorderFactory.createTitledBorder( "Remote CSVs" ) );
        JPanel remoteNorthPanel = new JPanel();
        remoteNorthPanel.add( new JLabel( "Connected to bike at " ) );
        remoteIpField = new JTextField( 20 );
        remoteIpField.setText( App.DEFAULT_IP_ADDRESS );
        remoteNorthPanel.add( remoteIpField );
        JButton remoteButton = new JButton( "Download and display latest CSV" );
        remoteButton.setName( "remoteButton" );
        remoteButton.addActionListener( new GlobalListener() );
        remotePanel.add( remoteButton, BorderLayout.CENTER );
        JPanel remoteNorthWrapperPanel = new JPanel( new BorderLayout() );
        remoteNorthWrapperPanel.add( remoteNorthPanel, BorderLayout.WEST );
        remotePanel.add( remoteNorthWrapperPanel, BorderLayout.NORTH );

        frame = new JFrame( "GUI Simulator 2017 - Select CSV" );
        JPanel containerPanel = new JPanel( new BorderLayout() );
        containerPanel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
        JPanel northPanel = new JPanel();
        northPanel.setLayout( new BoxLayout( northPanel, BoxLayout.Y_AXIS ) );
        northPanel.add( saveLocPanel );
        northPanel.add( recentLocalPanel );
        containerPanel.add( northPanel, BorderLayout.NORTH );
        containerPanel.add( remotePanel, BorderLayout.CENTER );
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
                        String ipAddress = remoteIpField.getText();
                        if( !Helper.validateIpAddress( ipAddress ) ) {
                            JOptionPane.showConfirmDialog( frame, "Invalid IP address!", "Error while connecting to bike", JOptionPane.ERROR_MESSAGE );
                            return;
                        }
                        File csvFile = App.downloadLatestCsvWithPrefix( "gps", ipAddress, saveLocation );
                        List<TimedBikeState> bikeStates = CsvParser.parseFile( csvFile );
                        App.displayCsvInWindow( bikeStates );
                    } catch( IOException e ) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
