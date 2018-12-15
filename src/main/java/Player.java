import org.jsoup.nodes.Element;

public class Player {
    private String mId;
    private String mName;
    private String mUrl;

    public Player(Element playerElement) {
        String url = playerElement.attr("href");

        mId = url.split("/")[2];
        mUrl = Config.HOMEPAGE + url;
        mName = correctName(playerElement.text());
        System.out.println("Player: " + mId + " " + mName + " " + mUrl);
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getUrl() {
        return mUrl;
    }

    public static String correctName(String name) {
        name = name
                .replace("(c)", "")
                .replace("(wk)", "")
                .replace("(c & wk)", "")
                .strip();
        if (name.endsWith(" sub")) {
            name = name.replace(" sub", "").strip();
        } else if (name.endsWith(" Sub")) {
            name = name.replace(" Sub", "").strip();
        }
        return name;
    }
}
