package com.bydaffi.anypetbackend.service;

import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * Service for compressing and resizing images before uploading to S3.
 * Reduces storage costs and improves loading performance.
 */
@Service
public class ImageCompressionService {

    @Value("${app.image.compression-quality:0.85}")
    private float compressionQuality;

    @Value("${app.image.max-width:1920}")
    private int maxWidth;

    @Value("${app.image.max-height:1920}")
    private int maxHeight;

    @Value("${app.image.thumbnail-size:200}")
    private int thumbnailSize;

    /**
     * Compresses an image file and returns the compressed byte array
     *
     * @param file the image file to compress
     * @return compressed image as byte array
     * @throws IOException if compression fails
     */
    public byte[] compressImage(MultipartFile file) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());

        if (originalImage == null) {
            throw new IOException("Invalid image file");
        }

        // Resize if image is too large
        BufferedImage resizedImage = resizeIfNeeded(originalImage, maxWidth, maxHeight);

        // Compress the image
        return compressBufferedImage(resizedImage, getImageFormat(file.getOriginalFilename()));
    }

    /**
     * Creates a thumbnail version of the image
     *
     * @param file the original image file
     * @return thumbnail as byte array
     * @throws IOException if thumbnail creation fails
     */
    public byte[] createThumbnail(MultipartFile file) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());

        if (originalImage == null) {
            throw new IOException("Invalid image file");
        }

        // Create square thumbnail
        BufferedImage thumbnail = Scalr.resize(originalImage,
                Scalr.Method.QUALITY,
                Scalr.Mode.FIT_TO_WIDTH,
                thumbnailSize,
                thumbnailSize,
                Scalr.OP_ANTIALIAS);

        return compressBufferedImage(thumbnail, getImageFormat(file.getOriginalFilename()));
    }

    /**
     * Resizes image if it exceeds maximum dimensions
     */
    private BufferedImage resizeIfNeeded(BufferedImage image, int maxWidth, int maxHeight) {
        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();

        // No resize needed
        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return image;
        }

        // Calculate new dimensions maintaining aspect ratio
        return Scalr.resize(image,
                Scalr.Method.QUALITY,
                Scalr.Mode.AUTOMATIC,
                maxWidth,
                maxHeight,
                Scalr.OP_ANTIALIAS);
    }

    /**
     * Compresses a BufferedImage with specified quality
     */
    private byte[] compressBufferedImage(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // For JPEG compression with quality control
        if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg")) {
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
            if (!writers.hasNext()) {
                throw new IOException("No JPEG writer found");
            }

            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();

            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(compressionQuality);
            }

            try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(image, null, null), param);
                writer.dispose();
            }
        } else {
            // For PNG and other formats
            ImageIO.write(image, format, outputStream);
        }

        return outputStream.toByteArray();
    }

    /**
     * Gets the image format from filename
     */
    private String getImageFormat(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

        // Supported formats
        if (extension.equals("png") || extension.equals("jpg") ||
                extension.equals("jpeg") || extension.equals("gif")) {
            return extension;
        }

        return "jpg"; // default
    }

    /**
     * Validates if file is a valid image
     */
    public boolean isValidImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        return contentType.startsWith("image/");
    }

    /**
     * Gets the size of compressed image in MB
     */
    public double getImageSizeInMB(byte[] imageBytes) {
        return imageBytes.length / (1024.0 * 1024.0);
    }

    /**
     * Converts byte array back to InputStream for S3 upload
     */
    public InputStream byteArrayToInputStream(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }
}
