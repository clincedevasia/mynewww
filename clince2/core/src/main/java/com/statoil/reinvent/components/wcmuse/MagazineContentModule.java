package com.statoil.reinvent.components.wcmuse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

public class MagazineContentModule extends BaseComponent {

    private static final Logger LOG = LoggerFactory.getLogger(MagazineContentModule.class);
    private String link = "";
    private Tag[] tags;
    private List<Tag> tagList;
    private Page page;


	@Override
	protected void activated() throws Exception {
		LOG.debug("Inside magazinecontentmodule component wcmuse class");
		tagList= new ArrayList<Tag>();
        link = getProperties().get("./linkURL", String.class);
        PageManager pageManager = getCurrentPage().getPageManager();
        page = pageManager.getPage(link);
        if(page!=null){
            tags=page.getTags();
            if(tags!=null){
                for(int i=0;i<tags.length;i++){
                    tagList.add(tags[i]);
                }
            }
        }
	}

    public List<Tag> getTagList() {
        return tagList;
    }

    public String commaSeperatedTags(){
        if(tags == null || tags.length==0){
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();

        String result = "";
        String seperator = ",";


        for (int i=0;i<tags.length;i++){
            String name = tags[i].getName();
            if(i==tags.length-1){
                stringBuilder.append(name);

            }else{
                stringBuilder.append(name+seperator);
            }
        }
        return stringBuilder.toString();
    }

}
