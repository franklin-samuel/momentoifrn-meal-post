package momento.ifrn.meal.post.core.infrastructure;

public interface StorageService {
    String upload(byte[] content, String fileName, String contentType);
}
