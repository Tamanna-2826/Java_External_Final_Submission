/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utilities;

import io.humble.video.Codec;
import io.humble.video.Decoder;
import io.humble.video.Demuxer;
import io.humble.video.DemuxerStream;
import io.humble.video.MediaDescriptor;
import io.humble.video.MediaPacket;
import io.humble.video.MediaPicture;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author LENOVO
 */
public class ThumbnailUtil {

    private static final Logger logger = LoggerFactory.getLogger(ThumbnailUtil.class);

    public static boolean generateThumbnail(String videoPath, String thumbnailPath) {
        logger.info("Starting thumbnail generation for video: {}", videoPath);
        logger.info("Thumbnail will be saved to: {}", thumbnailPath);

        try {
            File thumbnailFile = new File(thumbnailPath);
            File parentDir = thumbnailFile.getParentFile();

            if (!parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                logger.info("Created thumbnail directory: {} ? {}", parentDir.getAbsolutePath(), created);
                if (!created) {
                    logger.error("Failed to create thumbnail directory: {}", parentDir.getAbsolutePath());
                    return false;
                }
            } else {
                logger.debug("Thumbnail directory already exists: {}", parentDir.getAbsolutePath());
            }

            // Verify directory is writable
            if (!parentDir.canWrite()) {
                logger.error("Thumbnail directory is not writable: {}", parentDir.getAbsolutePath());
                return false;
            }

            Demuxer demuxer = Demuxer.make();
            logger.debug("Opening video file: {}", videoPath);
            demuxer.open(videoPath, null, false, true, null, null);

            int videoStreamId = -1;
            Decoder videoDecoder = null;

            for (int i = 0; i < demuxer.getNumStreams(); i++) {
                DemuxerStream stream = demuxer.getStream(i);
                Decoder decoder = stream.getDecoder();
                if (decoder.getCodecType() == MediaDescriptor.Type.MEDIA_VIDEO) {
                    videoStreamId = i;
                    videoDecoder = decoder;
                    logger.info("Found video stream at index: {}", videoStreamId);
                    break;
                }
            }

            if (videoStreamId == -1) {
                logger.error("No video stream found in: {}", videoPath);
                demuxer.close();
                return false;
            }

            logger.debug("Opening video decoder for stream: {}", videoStreamId);
            videoDecoder.open(null, null);

            MediaPicture picture = MediaPicture.make(
                    videoDecoder.getWidth(),
                    videoDecoder.getHeight(),
                    videoDecoder.getPixelFormat()
            );
            logger.debug("Created MediaPicture with width: {}, height: {}, format: {}",
                    videoDecoder.getWidth(), videoDecoder.getHeight(), videoDecoder.getPixelFormat());

            MediaPictureConverter converter = MediaPictureConverterFactory.createConverter(
                    MediaPictureConverterFactory.HUMBLE_BGR_24, picture);
            MediaPacket packet = MediaPacket.make();

            while (demuxer.read(packet) >= 0) {
                if (packet.getStreamIndex() == videoStreamId) {
                    logger.debug("Decoding packet for stream: {}", videoStreamId);
                    videoDecoder.decode(picture, packet, 0);
                    if (picture.isComplete()) {
                        logger.info("Found complete frame for thumbnail generation");
                        BufferedImage img = converter.toImage(null, picture);
                        ImageIO.write(img, "jpg", thumbnailFile);

                        if (thumbnailFile.exists()) {
                            logger.info("Thumbnail successfully saved to: {}", thumbnailFile.getAbsolutePath());
                        } else {
                            logger.error("Thumbnail generation failed! File not created at: {}", thumbnailFile.getAbsolutePath());
                            demuxer.close();
                            return false;
                        }

                        demuxer.close();
                        return true;
                    }
                }
            }

            demuxer.close();
            logger.error("No complete frame was found to generate thumbnail for: {}", videoPath);
            return false;

        } catch (Exception e) {
            logger.error("Thumbnail generation failed for: {}", videoPath, e);
            return false;
        }
    }
}
