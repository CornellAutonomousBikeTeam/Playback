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
    public static List<TimedBikeState> parseFile( File csvFile ) throws IOException {
        List<TimedBikeState> result = new ArrayList<TimedBikeState>();
        String currLine = null;
        BufferedReader reader = new BufferedReader( new FileReader( csvFile ) );

        // Skip the first line, since it has the header
        reader.readLine();

        String[] tokens;
        Date currTimestamp;
        float lat;
        float lng;
        while( ( currLine = reader.readLine() ) != null ) {
            tokens = currLine.split( "," );
            if( tokens.length == 0 ) {
                continue;
            }
            currTimestamp = new Date( (long)( Float.parseFloat( tokens[0] ) / 1000000 ) );
            lat = Float.parseFloat( tokens[2] );
            lng = Float.parseFloat( tokens[3] );

            // Some sanity checks
            if( lat < 40 || lat > 50 ) {
                continue;
            }
            if( lng < -80 || lng > -70 ) {
                continue;
            }

            result.add( new TimedBikeState( currTimestamp, lng, lat ) );
        }

        return result;
    }
}
