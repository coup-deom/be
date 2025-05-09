package kr.kro.deom.common.file.service;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import kr.kro.deom.common.exception.code.CommonErrorCode;
import kr.kro.deom.common.file.exception.S3FileUploadException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3FileService {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadFile(MultipartFile multipartFile, String dirName) throws IOException {
        String originalFileName = multipartFile.getOriginalFilename();

        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new S3FileUploadException(CommonErrorCode.EMPTY_FILE_NAME);
        }

        if (!isValidExtension(originalFileName)) {
            throw new S3FileUploadException(CommonErrorCode.INVALID_FILE_EXTENSION);
        }

        String uniqueFileName = UUID.randomUUID() + "_" + originalFileName.replaceAll("\\s", "_");

        String fileName = dirName + "/" + uniqueFileName;

        PutObjectRequest putObjectRequest =
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(fileName)
                        .contentType(multipartFile.getContentType())
                        .build();

        s3Client.putObject(
                putObjectRequest,
                RequestBody.fromInputStream(
                        multipartFile.getInputStream(), multipartFile.getSize()));

        return s3Client.utilities()
                .getUrl(builder -> builder.bucket(bucket).key(fileName))
                .toExternalForm();
    }

    public void deleteFile(String fileName) {
        String splitFilename = ".com/";
        String originalFileName =
                fileName.substring(fileName.lastIndexOf(splitFilename) + splitFilename.length());

        String decodedFileName = URLDecoder.decode(originalFileName, StandardCharsets.UTF_8);

        DeleteObjectRequest deleteObjectRequest =
                DeleteObjectRequest.builder().bucket(bucket).key(decodedFileName).build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    public String updateFile(MultipartFile newFile, String oldFileName, String dirName)
            throws IOException {
        if (oldFileName != null && !oldFileName.isEmpty()) {
            deleteFile(oldFileName);
        }

        return uploadFile(newFile, dirName);
    }

    private boolean isValidExtension(String originalFileName) {
        String fileExtension =
                originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif");

        return allowedExtensions.contains(fileExtension);
    }
}
