package io.github.cornellautonomousbiketeam;

public abstract class GpsHelper {
    /**
     * Converts lat/long to Google Maps world coordinates (pixels
     * ranging from 0 to 256, overlaying the entire world in Mercator
     * projection).
     *
     * Sourced from https://gis.stackexchange.com/a/66357.
     *
     * @param latlng An array where the first and second elements are
     * latitude and longitude, respectively.
     * @return An array where the first and second elements are world x
     * and world y, respectively. (Doubles from 0 to 256)
     */
    public static double[] gpsCoordsToWorldCoords( double[] latlng ) {
        double x = ( latlng[1] + 180 ) / 360 * 256;
        double y = ( ( 1 - Math.log( Math.tan( latlng[0] * Math.PI / 180 ) + 1 / Math.cos( latlng[0] * Math.PI / 180 ) ) / Math.PI ) / 2 * Math.pow( 2, 0 ) ) * 256;
        return new double[] { x, y };
    }

    /**
     * @see #gpsCoordsToWorldCoords(double[])
     */
    public static double[] gpsCoordsToWorldCoords( double lat, double lng ) {
        return gpsCoordsToWorldCoords( new double[] { lat, lng } );
    }

    /**
     * Converts Google Maps world coordinates to lat/long pair.
     *
     * Sourced from https://gis.stackexchange.com/a/66357.
     *
     * @param point An array where the first and second elements are
     * Google Maps world coordinates - doubles from 0 to 256 -
     * representing x and y respectively.
     * @return An array where the first and second elements are latitude
     * and longitude, respectively.
     */
    public static double[] worldCoordsToGpsCoords( double[] point ) {
        double longitude = point[0] / 256 * 360 - 180;
        double n = Math.PI - 2 * Math.PI * point[1] / 256;
        double latitude = Math.toDegrees( Math.atan( Math.sinh( n ) ) );
        return new double[] { latitude, longitude };
    }
}
