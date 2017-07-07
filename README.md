# Playback
A visualizer for GPS datapoints.

Motivation: Bike tests that involve the GPS somehow usually produce CSV files containing latitude/longitude pairs. However, there isn't an easy way to download and plot these on a map. This tool accomplishes that.

This tool has two main functions: downloading CSV files from the bike, and displaying CSV files from anywhere. When you first launch the tool, there are a number of options for viewing CSV files. The "Local CSVs" section lets you view CSVs on your computer; the "Remote CSVs" section lets you download them from the bike. Use the "Refresh List" button to update both sections.

The easiest way to run this tool is to get the latest JAR file from [the "Releases" section][1]. You'll need Java 8.

You can build this tool with Maven, using the command `mvn package`. Two JARs will be created in target/ - you'll want to run the one ending in `-jar-with-dependencies` one, because it has all the libraries bundled.

Here's a screenshot of the main window.

![Screenshot][2]

And of the tool displaying some GPS data:

![Screenshot of display][3]

  [1]: https://github.com/CornellAutonomousBikeTeam/Playback/releases
  [2]: https://user-images.githubusercontent.com/1981364/27975744-77f1a6b8-6331-11e7-949c-48fe8cc5a42e.png
  [3]: https://user-images.githubusercontent.com/1981364/27932057-47698852-626a-11e7-8d3b-456b2ec7635a.png
