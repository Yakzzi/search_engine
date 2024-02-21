package searchengine.services;

import searchengine.dto.indexing.IndexingResponse;

public interface IndexService {
    IndexingResponse startIndex();
}
