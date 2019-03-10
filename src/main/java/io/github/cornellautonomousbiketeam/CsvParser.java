package io.github.cornellautonomousbiketeam;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.*;

import io.github.cornellautonomousbiketeam.TimedBikeState;

/**
 * Converts a CSV file to a Map going from Floats (timestamps) to
 * BikeStates.
 */
public abstract class CsvParser {
    /**
     * Parse a CSV file. simpleFormat is true for simple format, false for
     * complex format.
     */
    public static List<TimedBikeState> parseFile( File csvFile, boolean simpleFormat ) throws IOException {
        List<TimedBikeState> result = new ArrayList<TimedBikeState>();
        String currLine = null;
        BufferedReader reader = new BufferedReader( new FileReader( csvFile ) );

        // Skip the first line, since it has the header
        if( !simpleFormat ) reader.readLine();

        String[] tokens;
        Date currTimestamp = null;
        float lat;
        float lng;

        // Heading, in radians
        float psi;

        // Velocity, in meters per second
        float v;

        // If simple format, use fake dates
        if( simpleFormat ) currTimestamp = new Date();

        while( ( currLine = reader.readLine() ) != null ) {
            if( currLine.trim().length() == 0 ) continue;
            tokens = currLine.split( "," );
            if( simpleFormat ) {
                currTimestamp.setTime( currTimestamp.getTime() + 50 );
                lat = Float.parseFloat( tokens[0] );
                lng = Float.parseFloat( tokens[1] );
                psi = 0;
                v = 0;
            } else {
                currTimestamp = new Date( (long)( Float.parseFloat( tokens[0] ) / 1000000 ) );
                lat = Float.parseFloat( tokens[2] );
                lng = Float.parseFloat( tokens[3] );
                psi = (float)Math.toRadians( Float.parseFloat( tokens[9] ) );
                v = Float.parseFloat( tokens[10] );
            }

            // Some sanity checks
            if( lat < 40 || lat > 50 ) {
                continue;
            }
            if( lng < -80 || lng > -70 ) {
                continue;
            }

            result.add( new TimedBikeState( currTimestamp, lng, lat, psi, v ) );
        }

        return result;
    }
}
