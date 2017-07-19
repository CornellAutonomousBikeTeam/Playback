package io.github.cornellautonomousbiketeam;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

import io.github.cornellautonomousbiketeam.TimedBikeState;

/**
 * Paints the GPS data.
 */
public class GpsPanel extends JPanel {
    private List<TimedBikeState> points;

    // Lat/long extents
    private float minLatitude;
    private float maxLatitude;
    private float minLongitude;
    private float maxLongitude;
    private float latExtent;
    private float lngExtent;

    // Time extents
    private Date minTime;
    private Date maxTime;

    // Background image
    private BufferedImage backgroundImage;

    // Background image dimensions
    private int backgroundWidth;
    private int backgroundHeight;

    // Background image extents
    private double backgroundMinLat;
    private double backgroundMaxLat;
    private double backgroundMinLng;
    private double backgroundMaxLng;

    private double backgroundLatExtent;
    private double backgroundLngExtent;

    // State variables for dragging
    private Point initialClick;
    private int dragXOffset;
    private int dragYOffset;
    private int initialDragXOffset;
    private int initialDragYOffset;

    // API constants
    public static final String MAPS_API_ENDPOINT =
        "https://maps.googleapis.com/maps/api/staticmap";

    public GpsPanel( List<TimedBikeState> p ) {
        points = p;

        minLatitude = 90;
        maxLatitude = -90;
        minLongitude = 180;
        maxLongitude = -180;

        minTime = new Date( Long.MAX_VALUE );
        maxTime = new Date( Long.MIN_VALUE );

        for( TimedBikeState state : points ) {
            if( state.yB > maxLatitude ) {
                maxLatitude = state.yB;
            } else if( state.yB < minLatitude ) {
                minLatitude = state.yB;
            }

            if( state.xB > maxLongitude ) {
                maxLongitude = state.xB;
            } else if( state.xB < minLongitude ) {
                minLongitude = state.xB;
            }

            if( state.date.before( minTime ) ) {
                minTime = state.date;
            } else if( state.date.after( maxTime ) ) {
                maxTime = state.date;
            }
        }

        latExtent = maxLatitude - minLatitude;
        lngExtent = maxLongitude - minLongitude;

        try {
            backgroundImage = fetchMapImage();
        } catch( IOException e ) {
            try {
                backgroundImage = ImageIO.read( getClass().getResource( "/map.png" ) );
                backgroundWidth = 1243;
                backgroundHeight = 1167;
                backgroundMinLat = 42.442960;
                backgroundMaxLat = 42.445268;
                backgroundMinLng = -76.485229;
                backgroundMaxLng = -76.481902;
                backgroundLatExtent = backgroundMaxLat - backgroundMinLat;
                backgroundLngExtent = backgroundMaxLng - backgroundMinLng;
            } catch( IOException e2 ) {
                e2.printStackTrace();
            }
        }

        this.addMouseListener( new DragMouseListener() );
        this.addMouseMotionListener( new DragMouseListener() );
    }

    public void paintComponent( Graphics g ) {
        super.paintComponent( g );

        Graphics2D g2d = (Graphics2D)g;

        // Draw background image
        if( backgroundImage != null ) {
            AffineTransform transform = getBackgroundTransform( g2d.getTransform() );
            g2d.drawImage( backgroundImage, transform, null );
            //g.drawImage( backgroundImage, 0, 0, null );
        }

        // Draw GPS points
        g.setColor( Color.BLUE );
        int displayWidth = getWidth();
        int displayHeight = 200;
        double pixelsPerLngDegree = displayWidth / lngExtent;

        double worldMapWidth = ( displayHeight * 360 ) / ( pixelsPerLngDegree * 2 * Math.PI );
        double maxLatitudeRad = Math.toRadians( maxLatitude );
        double mapOffsetY = worldMapWidth / 2 * Math.log( ( 1 + Math.sin( maxLatitudeRad ) ) / ( 1 - Math.sin( maxLatitudeRad ) ) );

        double indexToSaturation = 1.0 / points.size();
        int index = 0;
        for( TimedBikeState state : points ) {
            int pixelX, pixelY;
            pixelX = (int)( ( state.xB - minLongitude ) * pixelsPerLngDegree );
            pixelX = Math.min( pixelX, displayWidth );

            double latitudeRad = Math.toRadians( state.yB );
            /*
            int pixelY = displayHeight - (int)( ( ( worldMapWidth / 2.0 * Math.log( ( 1.0 + Math.sin( latitudeRad ) ) / ( 1.0 - Math.sin( latitudeRad ) ) ) ) - mapOffsetY ) );
            System.out.print( Math.log( ( 1 + Math.sin( latitudeRad ) ) / ( 1 - Math.sin( latitudeRad ) ) ) );
            pixelY = Math.min( pixelY, getHeight() - 50 );
            */

            /*
            double mercN = Math.log( Math.tan( Math.PI / 4 + ( latitudeRad / 2 ) ) );
            int pixelY = (int)( displayHeight / 2 - displayWidth * mercN / ( 2 * Math.PI ) );
            */
            //Point2D.Double pixel = convertGeoToPixel( state.yB, state.xB, displayWidth, displayHeight, minLongitude, longitudeRange, maxLatitude, maxLatitude * Math.PI / 180 );
            Point2D.Double pixel = convertGeoToPixel( state.yB, state.xB );
            pixelX = (int)pixel.x;
            pixelY = (int)pixel.y;
            //System.out.print( String.format( "(%d, %d)  ", pixelX, pixelY ) );

            //g.setColor( new Color( Color.HSBtoRGB( 0.6666666F, (float)( index * indexToSaturation ), 1.0F ) ) );
            pixelX += dragXOffset;
            pixelY += dragYOffset;
            g.fillRect( pixelX - 2, pixelY - 2, 4, 4 );

            index++;
        }
    }

    public Point2D.Double convertGeoToPixel( double latitude, double longitude , double mapWidth ,double  mapHeight ,double  mapLonLeft ,double  mapLonDelta ,double  mapLatBottom ,double  mapLatBottomDegree ) {
        double x = ( longitude - mapLonLeft ) * ( mapWidth / mapLonDelta );

        latitude = latitude * Math.PI / 180;
        double worldMapWidth = ( ( mapWidth / mapLonDelta ) * 360 ) / ( 2 * Math.PI );
        double mapOffsetY = worldMapWidth / 2 *
            Math.log( ( 1 + Math.sin( mapLatBottomDegree ) ) /
                    ( 1 - Math.sin( mapLatBottomDegree ) ) );
        double y = mapHeight - ( ( worldMapWidth / 2 * Math.log( ( 1 + Math.sin( latitude ) ) /
                        ( 1 - Math.sin( latitude ) ) ) ) - mapOffsetY );

        return new Point2D.Double( x, y );
    }

    public Point2D.Double convertGeoToPixel( double latitude, double longitude ) {
        return convertGeoToPixel( latitude, longitude, getWidth(), getHeight(),
                minLongitude, lngExtent, minLatitude,
                minLatitude * Math.PI / 180 );
    }

    private AffineTransform getBackgroundTransform( AffineTransform existing ) {
        AffineTransform backgroundTransform = new AffineTransform();

        backgroundTransform.translate( dragXOffset, dragYOffset );

        // Scale based on map longitude extent vs points longitude extent
        double extentScaleFactor = backgroundLngExtent / lngExtent;

        // Window-size-based scale factor
        double windowScaleFactor = (double)getWidth() / backgroundWidth;

        double scaleFactor = extentScaleFactor * windowScaleFactor;
        backgroundTransform.scale( scaleFactor, scaleFactor );

        Point2D.Double backgroundTopLeft = convertGeoToPixel( backgroundMaxLat, backgroundMinLng );
        double offsetX = backgroundTopLeft.x / scaleFactor;
        double offsetY = backgroundTopLeft.y / scaleFactor;
        backgroundTransform.translate( offsetX, offsetY );

        return backgroundTransform;
    }

    /**
     * Fetches map images from the Google Maps API. Responsible for
     * creating API calls, performing them, and stitching the results
     * together. Also calculates the lat/long extrema of the image and
     * sets the relevant fields.
     */
    private BufferedImage fetchMapImage() throws IOException {
        int zoomLevel = 18;
        double centerLat = ( minLatitude + maxLatitude ) / 2;
        double centerLng = ( minLongitude + maxLongitude ) / 2;
        backgroundWidth = 640;
        backgroundHeight = 640;

        // Construct URL
        StringBuilder sb = new StringBuilder( MAPS_API_ENDPOINT );
        sb.append( '?' );
        sb.append( "center=" );
        sb.append( centerLat );
        sb.append( ',' );
        sb.append( centerLng );
        sb.append( "&zoom=" );
        sb.append( zoomLevel );
        sb.append( "&size=" );
        sb.append( backgroundWidth );
        sb.append( 'x' );
        sb.append( backgroundHeight );

        double[] centerLeftWorldCoords =
            GpsHelper.gpsCoordsToWorldCoords( centerLat, minLongitude );

        // Increment the x-coord by the width of the map
        double[] centerRightWorldCoords = Arrays.copyOf( centerLeftWorldCoords,
                centerLeftWorldCoords.length );
        centerRightWorldCoords[0] += backgroundWidth /
            Math.pow( 2, zoomLevel );

        // Go back to GPS coords to get the longitude extent of this map
        double[] centerRightGpsCoords =
            GpsHelper.worldCoordsToGpsCoords( centerRightWorldCoords );
        backgroundLngExtent = centerRightGpsCoords[1] - minLongitude;

        double[] centerBottomWorldCoords =
            GpsHelper.gpsCoordsToWorldCoords( minLatitude, centerLng );

        // Increment the y-coord by the height of the map
        double[] centerTopWorldCoords = Arrays.copyOf( centerBottomWorldCoords,
                centerBottomWorldCoords.length );
        centerTopWorldCoords[1] -= backgroundHeight / Math.pow( 2, zoomLevel );

        // Go back to GPS coords to get the latitude extent of this map
        double[] centerTopGpsCoords =
            GpsHelper.worldCoordsToGpsCoords( centerTopWorldCoords );
        backgroundLatExtent = centerTopGpsCoords[0] - minLatitude;

        // Calculate background min/max lat/lng
        backgroundMinLat = centerLat - backgroundLatExtent / 2;
        backgroundMaxLat = centerLat + backgroundLatExtent / 2;
        backgroundMinLng = centerLng - backgroundLngExtent / 2;
        backgroundMaxLng = centerLng + backgroundLngExtent / 2;

        return ImageIO.read( new URL( sb.toString() ) );
    }

    class DragMouseListener extends MouseAdapter {
        public void mousePressed( MouseEvent event ) {
            initialClick = event.getPoint();
            initialDragXOffset = dragXOffset;
            initialDragYOffset = dragYOffset;
        }

        public void mouseDragged( MouseEvent event ) {
            dragXOffset = initialDragXOffset + event.getX() - initialClick.x;
            dragYOffset = initialDragYOffset + event.getY() - initialClick.y;
            GpsPanel.this.repaint();
        }
    }
}

