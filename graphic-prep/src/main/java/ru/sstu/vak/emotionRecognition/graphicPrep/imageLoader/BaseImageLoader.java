package ru.sstu.vak.emotionRecognition.graphicPrep.imageLoader;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import org.apache.commons.io.FileUtils;
import org.nd4j.util.ArchiveUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Map;

public abstract class BaseImageLoader implements Serializable {
    public static final String[] ALLOWED_FORMATS = new String[]{"tif", "jpg", "png", "jpeg", "bmp", "JPEG", "JPG", "TIF", "PNG"};
    protected long height = -1L;
    protected long width = -1L;
    protected long channels = -1L;
    protected boolean centerCropIfNeeded = false;

    public BaseImageLoader() {
    }

    public String[] getAllowedFormats() {
        return ALLOWED_FORMATS;
    }

    public static void downloadAndUntar(Map urlMap, File fullDir) {
        try {
            File file = new File(fullDir, urlMap.get("filesFilename").toString());
            if (!file.isFile()) {
                FileUtils.copyURLToFile(new URL(urlMap.get("filesURL").toString()), file);
            }

            String fileName = file.toString();
            if (fileName.endsWith(".tgz") || fileName.endsWith(".tar.gz") || fileName.endsWith(".gz") || fileName.endsWith(".zip")) {
                ArchiveUtils.unzipFileTo(file.getAbsolutePath(), fullDir.getAbsolutePath());
            }

        } catch (IOException var4) {
            throw new IllegalStateException("Unable to fetch images", var4);
        }
    }

    public static enum MultiPageMode {
        MINIBATCH,
        CHANNELS,
        FIRST;

        private MultiPageMode() {
        }
    }
}
