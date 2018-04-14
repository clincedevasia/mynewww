package apps.statoil.components.content.bannerquote;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.wcm.api.Page;
import com.statoil.reinvent.utils.PageUtil;
import com.statoil.reinvent.utils.PropertiesUtil;

public class Text extends WCMUsePojo {

    private String title;
    private String subtitle;
    private String link;

    private boolean isPanelPage;

    private String quote;
    private String reference;
    private String linkedTitle;
    private String reduceQuote;


    public void activate() throws Exception {
        quote = getProperties().get("quote", String.class);
        reference = getProperties().get("reference", String.class);
        reduceQuote = getProperties().get("reduceQuote", String.class);

        link = getProperties().get("linkURL", String.class);
        isPanelPage = isPanelPage(link);
        subtitle = PropertiesUtil.getFirstUnemptyString(getProperties().get("subtitle", String.class), getCurrentPage().getProperties().get("subtitle", String.class),getCurrentPage().getTitle());

        if (link != null) {
            link += ".html";
        }

        try{
            Node currentNode = getResource().adaptTo(Node.class);
            Property linkedPath = currentNode.getProperty("../button/linkURL");
            if(linkedPath!=null){
                String linkedPathString = linkedPath.getString();

                Page linkedPage = getPageManager().getPage(linkedPathString);
                linkedTitle = PropertiesUtil.getFirstUnemptyString(linkedPage.getNavigationTitle(), linkedPage.getTitle());

            }
        }catch (PathNotFoundException | NullPointerException e){
            //Swallow
            linkedTitle=null;
        }
        title = PropertiesUtil.getFirstUnemptyString(getProperties().get("title", String.class), linkedTitle, getCurrentPage().getTitle(), getCurrentPage().getNavigationTitle(), getCurrentPage().getPageTitle());

    }


    public String getQuote(){ return quote; }
    public String getReference(){ return reference; }
    public String getLink(){
        return link;
    }

    public String getReduceQuote() {
        return reduceQuote;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public boolean isPanelPage() {
        return isPanelPage;
    }

    private boolean isPanelPage(String url){

        Page linkTo = getPageManager().getPage(url);
        if(linkTo==null){
            return false;
        }
        return PageUtil.isPanelPage(linkTo);
    }
}
