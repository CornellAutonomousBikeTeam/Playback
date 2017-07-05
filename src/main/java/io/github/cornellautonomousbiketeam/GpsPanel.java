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
    public static final int BCKGRND_WIDTH = 1243;
    public static final int BCKGRND_HEIGHT = 1167;

    // Background image extents (tediously calculated)
    public static final double BCKGRND_MIN_LAT = 42.442960;
    public static final double BCKGRND_MAX_LAT = 42.445268;
    public static final double BCKGRND_MIN_LNG = -76.485229;
    public static final double BCKGRND_MAX_LNG = -76.481902;

    public static final double BCKGRND_LAT_EXTENT = BCKGRND_MAX_LAT - BCKGRND_MIN_LAT;
    public static final double BCKGRND_LNG_EXTENT = BCKGRND_MAX_LNG - BCKGRND_MIN_LNG;

    // State variables for dragging
    private Point initialClick;
    private int dragXOffset;
    private int dragYOffset;
    private int initialDragXOffset;
    private int initialDragYOffset;

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
            backgroundImage = ImageIO.read( getClass().getResource( "/map.png" ) );
        } catch( IOException e ) {
            e.printStackTrace();
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
        double longitudeRange = maxLongitude - minLongitude;
        double pixelsPerLngDegree = displayWidth / longitudeRange;

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
        double mapOffsetY = ( worldMapWidth / 2 * Math.log( ( 1 + Math.sin( mapLatBottomDegree ) ) / ( 1 - Math.sin( mapLatBottomDegree ) ) ) );
        double y = mapHeight - ( ( worldMapWidth / 2 * Math.log( ( 1 + Math.sin( latitude ) ) / ( 1 - Math.sin( latitude ) ) ) ) - mapOffsetY );

        return new Point2D.Double( x, y );
    }

    public Point2D.Double convertGeoToPixel( double latitude, double longitude ) {
        double x = ( longitude - minLongitude ) * ( getWidth() / lngExtent );
        double y = - ( latitude - maxLatitude ) * ( getHeight() / latExtent );
        //return new Point2D.Double( x, y );
        return convertGeoToPixel( latitude, longitude, getWidth(), getHeight(), minLongitude, lngExtent, minLatitude, minLatitude * Math.PI / 180 );
    }

    private AffineTransform getBackgroundTransform( AffineTransform existing ) {
        AffineTransform backgroundTransform = new AffineTransform();

        backgroundTransform.translate( dragXOffset, dragYOffset );

        //backgroundTransform.concatenate( existing );

        // Scale based on map longitude extent vs points longitude extent
        double longitudeRange = maxLongitude - minLongitude;
        double extentScaleFactor = BCKGRND_LNG_EXTENT / longitudeRange;

        // Window-size-based scale factor
        double windowScaleFactor = (double)getWidth() / BCKGRND_WIDTH;

        double scaleFactor = extentScaleFactor * windowScaleFactor;
        backgroundTransform.scale( scaleFactor, scaleFactor );

        /*
        // Translate based on longitude
        double offsetX = ( minLongitude - BCKGRND_MIN_LNG ) * ( getWidth() / lngRange );
        System.out.println( minLongitude );
        System.out.println( minLongitude - BCKGRND_MIN_LNG );
        System.out.println( offsetX );
        offsetX /= 10;
        AffineTransform translateTransform =
            AffineTransform.getTranslateInstance( offsetX, 0 );
            */
        Point2D.Double backgroundTopLeft = convertGeoToPixel( BCKGRND_MAX_LAT, BCKGRND_MIN_LNG );

        double offsetX = backgroundTopLeft.x / scaleFactor;
        double offsetY = backgroundTopLeft.y / scaleFactor;
        backgroundTransform.translate( offsetX, offsetY );

        return backgroundTransform;
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

