package org.superbiz.moviefun.albums;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;
import org.superbiz.moviefun.blobstore.FileStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;
    private final BlobStore blobStore;

    public AlbumsController(AlbumsBean albumsBean, BlobStore blobStore) {
        this.albumsBean = albumsBean;
        this.blobStore = blobStore;
    }


    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        String blobName = format("covers/%d", albumId);
        String blobContentType = uploadedFile.getContentType();
        InputStream blobInputStream = uploadedFile.getInputStream();

        blobStore.put(new Blob(blobName, blobInputStream, blobContentType));

        return format("redirect:/albums/%d", albumId);
    }

    byte[] defaultCoverBytes = inputStreamToBytes(getClass().getClassLoader().getResourceAsStream("default-cover.jpg"));

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {
        String blobName = format("covers/%d", albumId);
        Optional<Blob> optionalBlob = blobStore.get(blobName);

        byte[] imageBytes = optionalBlob
                .map(blob -> inputStreamToBytes(blob.inputStream))
                .orElse(defaultCoverBytes);

        String contentType = new Tika().detect(blobName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);

        return new HttpEntity<>(imageBytes, headers);
    }

    private byte[] inputStreamToBytes(InputStream inputStream) {
        byte[] imageBytes;
        try {
            imageBytes = IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            imageBytes = new byte[0];
        }
        return imageBytes;
    }

    private void saveUploadToFile(@RequestParam("file") MultipartFile uploadedFile, File targetFile) throws IOException {
        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(uploadedFile.getBytes());
        }
    }

    private File getCoverFile(@PathVariable long albumId) {
        String coverFileName = format("covers/%d", albumId);
        return new File(coverFileName);
    }
}
