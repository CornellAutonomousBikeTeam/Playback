# Playback
A visualizer for GPS datapoints.

Motivation: Bike tests that involve the GPS somehow usually produce CSV files containing latitude/longitude pairs. However, there isn't an easy way to download and plot these on a map. This tool accomplishes that.

This tool has two main functions: downloading CSV files from the bike, and displaying CSV files from anywhere. When you first launch the tool, there are a number of options for viewing CSV files. The "Local CSVs" section lets you view CSVs on your computer; the "Remote CSVs" section lets you download them from the bike. Use the "Refresh List" button to update both sections.

Once you're viewing a CSV file and you can see the map, drag to move around the map, and scroll to zoom.

The easiest way to run this tool is to get the latest JAR file from [the "Releases" section][1]. You'll need Java 8.

You can build this tool with Maven, using the command `mvn package`. It'll make a runnable JAR file in the `target/` folder. 

Here's a screenshot of the main window.

![Screenshot][2]

And of the tool displaying some GPS data:

![Screenshot of display][3]

  [1]: https://github.com/CornellAutonomousBikeTeam/Playback/releases
  [2]: https://user-images.githubusercontent.com/1981364/27975744-77f1a6b8-6331-11e7-949c-48fe8cc5a42e.png
  [3]: https://user-images.githubusercontent.com/1981364/28427102-1d7cd1dc-6d43-11e7-88ac-852dd79e565b.png
