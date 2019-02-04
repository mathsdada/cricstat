package Model;

import Configuration.Config;
import Utility.ObjectBuilder;
import Utility.ScraperUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Season {
    private String mYear;
    private ArrayList<Series> mSeriesList;

    public Season(String  year) {
        mYear = year;
        mSeriesList = new ArrayList<>();
        extractSeriesList();
    }

    private void extractSeriesList() {
        String seasonUrl = Config.HOMEPAGE + "/cricket-scorecard-archives/" + mYear;
        Document seasonDoc = ScraperUtils.getDocument(seasonUrl);
        Elements seriesElements = seasonDoc.select("a.text-hvr-underline");
        for (Element seriesElement: seriesElements) {
            Series series = ObjectBuilder.Series.build(seriesElement, mYear);
            if (series != null) {
                mSeriesList.add(series);
            }
        }
        runSeriesWorkerThreads(mSeriesList);
    }

    private void runSeriesWorkerThreads(ArrayList<Series> seriesList) {
//        ExecutorService executor = Executors.newFixedThreadPool(10);
        ExecutorService executor = Executors.newCachedThreadPool();
        for (Series series: seriesList) {
            Runnable worker = new SeriesWorkerThread(series);
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {}
        System.out.println("Finished all Worker Threads");
    }

    private class SeriesWorkerThread implements Runnable {
        private Series mSeries;

        SeriesWorkerThread(Series series) {
            mSeries = series;
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + mSeries.getTitle());
            mSeries.scrape();
        }
    }

    public String  getYear() {
        return mYear;
    }

    public ArrayList<Series> getSeriesList() {
        return mSeriesList;
    }

}
