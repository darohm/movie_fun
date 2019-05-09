package org.superbiz.moviefun.blobstore;

import ch.qos.logback.core.util.ContentTypeUtil;
import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import sun.nio.ch.IOUtil;

import javax.print.attribute.URISyntax;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;

public class FileStore implements BlobStore {


    @Override
    public void put(Blob blob) throws IOException {
        File targetFile = new File(blob.name);

        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        byte[] bytes = IOUtils.toByteArray(blob.inputStream);
//        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
//            IOUtils.copy(blob.inputStream, outputStream);
//        }
        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(bytes);
        }
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        File blobFile = new File(name);

        if (blobFile.exists()) {
            FileInputStream fileStream = new FileInputStream(blobFile);
            String contentType = new Tika().detect(blobFile);

            Blob blob = new Blob(name, fileStream, contentType);
            return Optional.of(blob);
        }

        return Optional.empty();
    }

    @Override
    public void deleteAll() {
        File directory = new File("covers/");
        File[] files = directory.listFiles();
        for(File file:files) {
            file.delete();
        }
    }


}
