package searchengine.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;

import java.util.List;

@Getter
@Setter
public class PageNode {
    private List<PageNode> childPages;
    private Integer siteId;
    private String path;
    private Integer code;
    private String content;

    public PageNode(String path, Integer siteId) {
        this.path = path;
        this.siteId = siteId;
    }

    public void addChild(PageNode childNode) {
        childPages.add(childNode);
    }
}
