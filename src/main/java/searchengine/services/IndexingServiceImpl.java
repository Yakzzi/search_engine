package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.SiteTable;
import searchengine.model.Status;
import searchengine.model.repository.PageRepository;
import searchengine.model.repository.SiteRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final SitesList sites;
    private ExecutorService executorService;
    @Getter
    @Setter
    private static volatile boolean indexingStarted = false;



    @Override
    public IndexingResponse startIndex() {

        if (isIndexingStarted()) {
            return new IndexingResponse("Индексация уже запущена");
        }

        try {
            List<Site> siteList = sites.getSites();
            executorService = Executors.newFixedThreadPool(siteList.size());

            setIndexingStarted(true);
            siteRepository.deleteAll();
            siteList.forEach(this::startIndexingSiteByExecutorService);
            return new IndexingResponse(true);
        } finally {
            executorService.shutdown();
            setIndexingStarted(false);
        }
    }

    private void startIndexingSiteByExecutorService(Site site) {
        executorService.execute(() -> {
            SiteTable newSite = saveSiteInDatabaseAndGet(site);
            savePagesInDatabaseBySite(newSite);
            setIndexedSite(newSite);
        });
    }

    private SiteTable saveSiteInDatabaseAndGet(Site site) {
        SiteTable newSite = new SiteTable();
        newSite.setStatus(Status.INDEXING);
        newSite.setStatusTime(LocalDateTime.now());
        newSite.setUrl(site.getUrl());
        newSite.setName(site.getName());
        newSite.setPageList(new ArrayList<>());

        siteRepository.save(newSite);

        return newSite;
    }

    private void savePagesInDatabaseBySite(SiteTable site) {
        ForkJoinPool pagesPool = new ForkJoinPool();
        PageCollector pageCollector = new PageCollector(pageRepository, siteRepository, site);
        pagesPool.invoke(pageCollector);
    }

    private void setIndexedSite(SiteTable site) {
        site.setStatus(Status.INDEXED);
        siteRepository.saveAndFlush(site);
    }
}

