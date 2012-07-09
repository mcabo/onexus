package org.onexus.ui.website.pages.search;

import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchType implements Serializable {

    private String collection;
    private String fields;
    private String keys;
    private String examples;

    @XStreamImplicit( itemFieldName = "link" )
    private List<SearchLink> links = new ArrayList<SearchLink>();


    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public String getKeys() {
        return keys;
    }

    public void setKeys(String keys) {
        this.keys = keys;
    }

    public String getExamples() {
        return examples;
    }

    public void setExamples(String examples) {
        this.examples = examples;
    }

    public List<SearchLink> getLinks() {
        return links;
    }

    public void setLinks(List<SearchLink> links) {
        this.links = links;
    }

    public List<String> getKeysList() {

        if (this.keys == null) {
            return getFieldsList();
        }

        return stringToList(this.keys);
    }

    public List<String> getFieldsList() {
        return stringToList(this.fields);
    }

    private static List<String> stringToList(String input) {

        if (StringUtils.isEmpty(input)) {
            return Collections.emptyList();
        }

        String[] values = input.split(",");
        List<String> valuesList = new ArrayList<String>(values.length);

        for (String value : values) {
            valuesList.add(value.trim());
        }

        return valuesList;
    }
}
