package searchengine.dto.indexing;

import lombok.Data;

@Data
public class IndexingResponse {
    private boolean result;
    private String error;

    public IndexingResponse(boolean result) {
        this.result = result;
    }

    public IndexingResponse(String error) {
        this.result = false;
        this.error = error;
    }
}
