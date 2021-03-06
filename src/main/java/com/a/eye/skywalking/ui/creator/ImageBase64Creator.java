package com.a.eye.skywalking.ui.creator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Encoder;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * @author pengys5
 */
@Component
public class ImageBase64Creator {

    private Logger logger = LogManager.getFormatterLogger(ImageBase64Creator.class);

    private final String ImageFolder_Relative_PATH = "/public/img/node/";

    private final String PNG_BASE64_PREFIX = "data:image/png;base64,";
    private final String JPG_BASE64_PREFIX = "data:image/png;base64,";

    private final String PNG = "png";
    private final String JPG = "jpg";

    @Autowired
    private ImageCache imageCache;

    @PostConstruct
    public void loadImage() {
        String imageFolder = getImageFolder();

        String[] imageFileList = readImageFileList(imageFolder);
        for (String nodeImageFile : imageFileList) {
            logger.debug("nodeImageFile: %s", nodeImageFile);

            byte[] imageData = imageRead(imageFolder + nodeImageFile);
            if (nodeImageFile.toLowerCase().endsWith(PNG)) {
                String encodeImage = imageEncode(imageData, PNG);
                String imageName = getImageName(nodeImageFile);
                imageCache.putImage(imageName, encodeImage);
            } else if (nodeImageFile.toLowerCase().endsWith(JPG)) {
                String encodeImage = imageEncode(imageData, JPG);
                imageCache.putImage(getImageName(nodeImageFile), encodeImage);
            } else {
                logger.error("ignore unsupported image type, image file name: %s", nodeImageFile);
            }
        }
    }

    private String getImageName(String nodeImageFile) {
        return nodeImageFile.split("\\.")[0];
    }

    private String getImageFolder() {
        URL url = this.getClass().getResource("/");
        logger.debug("root class path: %s", url.getPath());
        String imageFolder = url.getPath() + ImageFolder_Relative_PATH;
        return imageFolder;
    }

    private String[] readImageFileList(String imageFolder) {
        File file = new File(imageFolder);
        if (file.isDirectory()) {
            return file.list();
        } else {
            throw new IllegalArgumentException("image folder path error: " + imageFolder);
        }
    }

    private byte[] imageRead(String imgFile) {
        InputStream inputStream = null;
        byte[] imageData = null;
        try {
            inputStream = new FileInputStream(imgFile);
            imageData = new byte[inputStream.available()];
            inputStream.read(imageData);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageData;
    }

    private String imageEncode(byte[] imageData, String imageType) {
        BASE64Encoder encoder = new BASE64Encoder();
        if (PNG.equals(imageType)) {
            return PNG_BASE64_PREFIX + encoder.encode(imageData);
        } else {
            return JPG_BASE64_PREFIX + encoder.encode(imageData);
        }
    }
}
