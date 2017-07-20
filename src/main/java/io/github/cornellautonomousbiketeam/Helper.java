package io.github.cornellautonomousbiketeam;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp.LsEntry;

public abstract class Helper {
    /**
     * Copied from https://stackoverflow.com/a/30691451/1757964
     */
    public static boolean validateIpAddress( final String ip ) {
        String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
        return ip.matches( PATTERN );
    }

    public static List<LsEntry> vectorToLsEntryList( Vector list ) {
        List<LsEntry> result = new ArrayList<LsEntry>();
        for( Object o : list ) {
            result.add( (LsEntry)o );
        }
        return result;
    }
}
