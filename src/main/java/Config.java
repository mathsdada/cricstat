import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

class Config {
    static final String HOMEPAGE = "https://www.cricbuzz.com" ;
    static final String[] FORMATS = {"T20", "ONE-DAY", "TEST"};
    static final String[] MATCH_STATUS = {"T", "W", "D", "NR", "NR"};
}
