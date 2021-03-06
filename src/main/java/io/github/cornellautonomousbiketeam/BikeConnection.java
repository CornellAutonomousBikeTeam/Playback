package io.github.cornellautonomousbiketeam;

import java.awt.Container;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.*;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;

/**
 * Handles file transfer. All this class knows about is how to
 * do basic file operations while connected to the bike.
 *
 * This code is based on this gist:
 * https://gist.github.com/ymnk/2318108#file-scpfrom-java
 */
public class BikeConnection {
    public static final int SSH_PORT = 22;
    public static final int SFTP_PORT = 22;
    public static final int BUFFER_SIZE = 1024;

    /**
     * Lists the remote files.
     *
     * Uses some code from https://stackoverflow.com/a/35359010/1757964
     *
     * @param user The username used to log in.
     * @param host The hostname (e.g. IP address) of the remote device.
     * @param dir A path to the directory we'll list.
     * @return A List of LsEntry objects
     */
    public static List<LsEntry> ls( String user, String host, String dir ) {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession( user, host, SFTP_PORT );
            session.setUserInfo( new MyUserInfo() );

            // A bit of configuration
            java.util.Properties config = new java.util.Properties();
            config.put( "StrictHostKeyChecking", "no" );
            session.setConfig( config );
            session.connect();

            Channel channel = session.openChannel( "sftp" );
            channel.connect();

            ChannelSftp channelSftp = (ChannelSftp)channel;
            channelSftp.cd( dir );

            Vector result = channelSftp.ls( dir );
            session.disconnect();
            return Helper.vectorToLsEntryList( result );
        } catch( Exception e ) {
            System.out.println( e );
            return new ArrayList<LsEntry>();
        }
    }

    /**
     * Copies a file from the remote host to the local path.
     *
     * @param user The username that we'll use to log in.
     * @param host The hostname (e.g. IP address) of the remote device.
     * @param remoteFile A path to the remote file. (For the Pi, start with a /)
     * @param localFile A path to the local destination. There shouldn't be a
     *                  file here yet.
     * @return The local file that resulted from the copy operation, or null if
     *                  there was an error.
     */
    public static File copy( String user, String host, String remoteFile, String localFile ) {

        // Process localFile a bit
        String prefix = null;
        if( new File( localFile ).isDirectory() ) {
            prefix = localFile + File.separator;
        }

        FileOutputStream fos = null;
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession( user, host, SSH_PORT );

            // We'll use the MyUserInfo inner class
            UserInfo userInfo = new MyUserInfo();
            session.setUserInfo( userInfo );
            session.connect();

            // Execute the command "scp [remoteFile]"
            String command = "scp -f " + remoteFile;
            Channel channel = session.openChannel( "exec" );
            ( (ChannelExec)channel ).setCommand( command );

            // Obtain I/O streams for the command
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            System.out.println( "[BikeConnection.copy] IO streams obtained." );

            // Open the channel to the server
            channel.connect();

            byte[] buffer = new byte[BUFFER_SIZE];

            // Send a null byte
            buffer[0] = 0;
            out.write( buffer, 0, 1 );
            out.flush();

            // Stores the filename during the loop
            String trueLocalFile = null;

            while( true ) {
                int c = checkAck( in );
                if( c != 'C' ) {
                    System.out.println( "[BikeConnection.copy] checkAck gave " +
                            "a " + c + " instead of a C, terminating" );
                    break;
                }

                // read '0644 ', which is a scp text mode
                in.read( buffer, 0, 5 );

                long fileSize = 0L;
                while( true ) {
                    if( in.read( buffer, 0, 1 ) < 0 ) {

                        // An error has occurred while reading
                        break;
                    }

                    if( buffer[0] == ' ' ) {
                        break;
                    }

                    fileSize = fileSize * 10L + (long)( buffer[0] - '0' );
                }

                String file = null;
                int i = 0;
                do {
                    in.read( buffer, i, 1 );
                } while( buffer[i] != (byte)0x0a );
                file = new String( buffer, 0, i );

                // Send a null byte
                buffer[0] = 0;
                out.write( buffer, 0, 1 );
                out.flush();

                // Read data from the remote file
                trueLocalFile = prefix == null ? localFile :
                    prefix + file;
                System.out.println( "[BikeConnection.copy] Writing to " +
                        trueLocalFile );
                fos = new FileOutputStream( trueLocalFile );

                // Variable name in original
                int foo;
                while( true ) {
                    foo = Math.min( buffer.length, (int)fileSize );
                    foo = in.read( buffer, 0, foo );
                    if( foo < 0 ) {

                        // An error has occurred
                        break;
                    }
                    fos.write( buffer, 0, foo );
                    fileSize -= foo;
                    if( fileSize <= 0L ) {
                        break;
                    }
                }
                fos.close();
                fos = null;

                if( checkAck( in ) != 0 ) {
                    System.out.println( "[BikeConnection.copy] A fatal error " +
                            "has occurred!" );
                    System.exit( 0 );
                }

                // Send a null byte
                buffer[0] = 0;
                out.write( buffer, 0, 1 );
                out.flush();
            }

            session.disconnect();

            return new File( trueLocalFile );
        } catch( Exception e ) {
            System.out.println( e );
            try {
                if( fos != null ) {
                    fos.close();
                }
            } catch( Exception ee ) {
                System.out.println( "[BikeConnection.copy] Double exception" );
                System.out.println( ee );
            } finally {
                return null;
            }
        }
    }

    static int checkAck( InputStream in ) throws IOException {

        // b will be 0 for success, 1 for error, 2 for fatal error, and
        // -1 for other.
        int b = in.read();

        if( ( b == 1 ) || ( b == 2 ) ) {
            StringBuffer sb = new StringBuffer();
            int c;
            do {
                c = in.read();
                sb.append( (char)c );
            } while( c != '\n' );
            System.out.print( sb.toString() );
        }
        return b;
    }

    static class MyUserInfo implements UserInfo, UIKeyboardInteractive {
        private String password;
        private JTextField passwordField;
        private final GridBagConstraints gbc;
        private Container panel;

        public MyUserInfo() {
            passwordField = (JTextField) new JPasswordField( 20 );
            gbc = new GridBagConstraints( 0, 0, 1, 1, 1, 1,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                    new Insets( 0, 0, 0, 0 ), 0, 0 );
        }

        public String[] promptKeyboardInteractive( String destination,
                String name, String instruction, String[] prompt,
                boolean[] echo ) {
            panel = new JPanel();
            panel.setLayout( new GridBagLayout() );
            gbc.weightx = 1.0;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.gridx = 0;
            panel.add( new JLabel( instruction ), gbc );
            gbc.gridy++;

            gbc.gridwidth = GridBagConstraints.RELATIVE;

            JTextField[] texts = new JTextField[prompt.length];
            for( int i = 0; i < prompt.length; i++ ) {
                gbc.fill = GridBagConstraints.NONE;
                gbc.gridx = 0;
                gbc.weightx = 1;
                panel.add( new JLabel( prompt[i] ), gbc );

                gbc.gridx = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weighty = 1;
                if( echo[i] ) {
                    texts[i] = new JTextField( 20 );
                } else {
                    texts[i] = new JPasswordField( 20 );
                }
                panel.add( texts[i], gbc );
                gbc.gridy++;
            }

            int result = JOptionPane.showConfirmDialog( null, panel,
                    destination + ": " + name, JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE );
            if( result == JOptionPane.OK_OPTION ) {
                String[] response = new String[prompt.length];
                for( int i = 0; i < prompt.length; i++ ) {
                    response[i] = texts[i].getText();
                }
                return response;
            } else {
                return null;
            }
        }

        public boolean promptPassword( String message ) {
            password = "raspberry";
            return true;
        }

        public boolean promptPasswordActual( String message ) {
            Object[] ob = { passwordField };
            int result = JOptionPane.showConfirmDialog( null, ob, message,
                    JOptionPane.OK_CANCEL_OPTION );
            if( result == JOptionPane.CANCEL_OPTION ) {
                return false;
            }

            password = passwordField.getText();
            return true;
        }

        public boolean promptYesNo( String str ) {
            Object[] options = { "yes", "no" };
            int result = JOptionPane.showOptionDialog( null, str, "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0] );
            return result == JOptionPane.YES_OPTION;
        }

        public void showMessage( String message ) {
            JOptionPane.showMessageDialog( null, message );
        }

        public String getPassword() {
            return password;
        }

        public String getPassphrase() {
            return null;
        }

        public boolean promptPassphrase( String message ) {
            return true;
        }
    }
}
