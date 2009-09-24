package ru.korusconsulting.connector.base;

import java.util.HashMap;
import java.util.List;

import org.dom4j.Element;

public class TagHolder {
    private HashMap<String,String> tags=new HashMap<String,String>();
    private HashMap<String,String> tags_=new HashMap<String,String>();
    
    public void put(String tagId, String tagName){
        tags.put(tagName, tagId);
        tags_.put(tagId, tagName);
    }
    
    public void clear(){
        tags.clear();
        tags_.clear();
    }

    public String getByName(String tagName) {
        return tags.get(tagName);
    }
    
    public String getById(String tagId){
        return tags_.get(tagId);
    }

    public void setTags(List<Element> elements) {
        for(Element tag:elements){
            put(tag.attributeValue(ZConst.A_ID), tag.attributeValue(ZConst.A_TAGNAME));
        }
        
    }
}
