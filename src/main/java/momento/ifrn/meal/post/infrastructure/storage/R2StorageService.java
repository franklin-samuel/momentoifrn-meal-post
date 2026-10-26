package momento.ifrn.meal.post.infrastructure.storage;

import lombok.RequiredArgsConstructor;
import momento.ifrn.meal.post.core.infrastructure.StorageService;
import momento.ifrn.meal.post.security.R2Properties;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class R2StorageService implements StorageService {

    private final S3Client r2Client;
    private final R2Properties r2Properties;

    @Override
    public String upload(final byte[] content, final String fileName, final String contentType) {
        final String key = buildKey(fileName);

        final PutObjectRequest request = PutObjectRequest.builder()
                .bucket(r2Properties.getBucketName())
                .key(key)
                .contentType(contentType)
                .build();

        r2Client.putObject(request, RequestBody.fromBytes(content));

        return r2Properties.getPublicBaseUrl() + "/" + key;
    }

    private String buildKey(final String fileName) {
        final String datePrefix = LocalDate.now().toString();
        final String uniqueName = UUID.randomUUID() + "-" + fileName;
        return "stories/" + datePrefix + "/" + uniqueName;
    }
}