package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.dto.PageNode;
import searchengine.model.Page;
import searchengine.model.SiteTable;
import searchengine.model.repository.PageRepository;
import searchengine.model.repository.SiteRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveAction;

@RequiredArgsConstructor
public class PageCollector extends RecursiveAction {

    private static final Set<String> ALL_PAGES = new CopyOnWriteArraySet<>();
    private static final String CSS_QUERY = "a[href]";
    private static final String ATTRIBUTE_KEY = "href";
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final SiteTable site;

    @Override
    protected void compute() {

        String url = site.getUrl();

        try {
            Document document = Jsoup.connect(url).get();
            Elements links = document.select(CSS_QUERY);

            for (Element element : links) {
                String attr = element.absUrl(ATTRIBUTE_KEY);
                if (isValidLink(attr)) {
                    ALL_PAGES.add(attr);
                    System.out.println(ALL_PAGES);

                    Page page = new Page();
                    page.setSiteTable(site);
                    page.setPath(attr);
                    page.setCode(document.connection().response().statusCode());
                    page.setContent(element.text());

                    pageRepository.saveAndFlush(page);

                    updateSiteStatusTime();

                    PageCollector subCollector = new PageCollector(pageRepository, siteRepository, site);
                    subCollector.fork();
                }
                Thread.sleep(500);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isValidLink(String link) {
        return link.startsWith(site.getUrl().replace("://www.", "://"))
                && !link.contains("#")
                && !ALL_PAGES.contains(link);
    }

    private void updateSiteStatusTime() {
        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);
    }
}