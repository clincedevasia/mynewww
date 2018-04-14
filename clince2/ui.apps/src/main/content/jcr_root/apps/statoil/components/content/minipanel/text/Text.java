package apps.statoil.components.content.minipanel.text;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.wcm.api.Page;
import com.statoil.reinvent.utils.PropertiesUtil;

public class Text extends WCMUsePojo {

    private String text;
    private String context;
    private String cssClass;

    public void activate() throws RepositoryException {

        Node currentNode = getResource().adaptTo(Node.class);

        // currentNode is null if Paragraph (text node) hasn't been specified in the component configuration.
        if (currentNode == null) {
            return;
        }

        String linkedPath = PropertiesUtil.getString(currentNode, "../link/linkURL");
        Page linkedPage = getPageManager().getPage(linkedPath);
        text = getProperties().get("text", String.class);
        context = "text";
        if (text == null && linkedPage != null) {
            text = linkedPage.getDescription();
        }
    }

    private String findFirstUnempty(String... strings) {
        for (String item : strings) {
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    public String getText() {
        return text;
    }

    public String getContext() {
        return context;
    }

    public String getCssClass() {
        return cssClass;
    }

    public Boolean isContextHtml() {
        return "html".equals(context);
    }
}