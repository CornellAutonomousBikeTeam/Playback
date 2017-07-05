package io.github.cornellautonomousbiketeam;

import java.awt.geom.Point2D;

import io.github.cornellautonomousbiketeam.Constants;

public abstract class Helper {
    public static Point2D.Double latLongToMeters( float latitude, float longitude ) {
        double origin_latitude_rad = Math.toRadians( Constants.ORIGIN_LATITUDE );
        double origin_longitude_rad = Math.toRadians(
                Constants.ORIGIN_LONGITUDE );
        double latitude_rad = Math.toRadians( latitude );
        double longitude_rad = Math.toRadians( longitude );
        double a = Math.pow( Math.sin( ( latitude_rad - origin_longitude_rad ) / 2 ), 2 ) + Math.cos( latitude_rad ) * Math.cos( origin_latitude_rad ) * Math.pow( Math.sin( ( longitude_rad - origin_longitude_rad ) / 2F ), 2 );
        double theta = 2 * Math.atan2( Math.sqrt( a ), Math.sqrt( 1 - a ) );
        double d = theta * Constants.EARTH_RADIUS;
        System.out.print( String.format( " [%.1f] ", d ) );

        // Compute bearing
        double y = Math.sin( longitude_rad - origin_longitude_rad ) * Math.cos( latitude_rad );
        double x = Math.cos( origin_latitude_rad ) * Math.sin( latitude_rad ) - Math.sin( origin_latitude_rad ) * Math.cos( latitude_rad ) * Math.cos( longitude_rad - origin_longitude_rad );
        double bearing_rad = Math.atan2( y, x );
        return new Point2D.Double( d * Math.sin( bearing_rad ),
                d * Math.cos( bearing_rad ) );
    }

    /**
     * Copied from https://stackoverflow.com/a/30691451/1757964
     */
    public static boolean validateIpAddress( final String ip ) {
        String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
        return ip.matches( PATTERN );
    }
}
