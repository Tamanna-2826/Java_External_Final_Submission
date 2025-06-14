/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utilities;

import io.humble.video.Demuxer;
import java.io.IOException;

/**
 *
 * @author LENOVO
 */
public class VideoDurationCalculator {

    public static int getVideoDurationUsingHumbleVideo(String videoFilePath) throws IOException {
        try {
            Demuxer demuxer = Demuxer.make();
            demuxer.open(videoFilePath, null, false, true, null, null);

            long duration = demuxer.getDuration();
            demuxer.close();

            // Convert microseconds to seconds
            return (int) (duration / 1_000_000);

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return -1; // Return -1 to indicate error
        }
    }

    public static String formatDuration(int durationInSeconds) {
        int hours = durationInSeconds / 3600;
        int minutes = (durationInSeconds % 3600) / 60;
        int seconds = durationInSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }
}
