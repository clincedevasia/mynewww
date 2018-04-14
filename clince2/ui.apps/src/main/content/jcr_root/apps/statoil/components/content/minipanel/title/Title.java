package apps.statoil.components.content.minipanel.title;

import javax.jcr.Node;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.wcm.api.Page;

public class Title extends WCMUsePojo {

    private String element;
    private String text;

    public void activate() throws Exception {
        Node currentNode = getResource().adaptTo(Node.class);
        String linkedPath = currentNode.getProperty("../link/linkURL").getString();
        element = getProperties().get("type", String.class);

        Page linkedPage = getPageManager().getPage(linkedPath);

        // PageManager.getPage returns null if the resource at that path does not exist or is not adaptable to Page
        if (linkedPage == null) {
            return;
        }

        String prop = getProperties().get("jcr:title", String.class);
        text = findFirstUnempty(prop, linkedPage.getPageTitle(), linkedPage.getTitle());
    }

    private String findFirstUnempty(String... strings) {
        for (String item : strings) {
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    public String getElement() {
        return element;
    }

    public String getText() {
        return text;
    }
}