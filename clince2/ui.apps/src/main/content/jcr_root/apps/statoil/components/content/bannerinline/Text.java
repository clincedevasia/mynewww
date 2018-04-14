package apps.statoil.components.content.bannerinline;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.wcm.api.Page;
import com.statoil.reinvent.editor.fields.CheckBox;
import com.statoil.reinvent.utils.PageUtil;
import com.statoil.reinvent.utils.PropertiesUtil;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;

public class Text extends WCMUsePojo {

    private String title;
    private String subtitle;
    private String link;

    private boolean isPanelPage;

    private String viewMode;
    private String quote;
    private String reference;
    private String linkedTitle;
    private String reduceQuote;
    private boolean titlesInverse;


    public void activate() throws Exception {
        viewMode = getProperties().get("viewMode", String.class);
        quote = getProperties().get("quote", String.class);
        reference = getProperties().get("reference", String.class);
        titlesInverse = CheckBox.getValue(getProperties(), "titlesinverse");
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

    public boolean getShowTitles(){
        return !titlesInverse && (title!=null || subtitle!=null);
    }

    public boolean getShowTitlesInverse(){
        return titlesInverse && (title!=null || subtitle!=null);
    }

    public boolean getShowQuote(){
        if(viewMode==null){
            return false;
        }
        return viewMode.equals("quote") && (quote!=null || reference!=null);
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
