package momento.ifrn.meal.post.core.infrastructure;

import momento.ifrn.meal.post.domain.InstagramToken;

import java.util.Optional;

public interface TokenStore {

    boolean isPersistent();

    Optional<InstagramToken> load();

    void save(InstagramToken token);
}
