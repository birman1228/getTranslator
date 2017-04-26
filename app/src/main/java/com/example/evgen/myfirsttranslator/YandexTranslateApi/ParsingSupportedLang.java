package com.example.evgen.myfirsttranslator.YandexTranslateApi;

import android.util.Log;
import android.widget.Toast;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;


import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by Evgen on 20.04.2017.
 */

public class ParsingSupportedLang extends DefaultHandler {
    private String UrlRequestSupportLang = "https://translate.yandex.net/api/v1.5/tr/getLangs?" +
            "key=trnsl.1.1.20170420T060021Z.292d6725423b3c13.bccaa64a1d300954d8bbab138872250a4721c5ee" +
            "&ui=ru";


    TreeMap<String, String> supportedLangHash = new TreeMap<>();
    boolean currTag = false;

    public TreeMap<String, String> getSupportedLangHash(){
        return this.supportedLangHash;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (qName.equals("Item")){
            currTag = true;
            supportedLangHash.put(attributes.getValue("value"), attributes.getValue("key"));
        } else if(qName.equals("Error")){
            Log.i(attributes.getValue("code"), attributes.getValue("message"));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (qName.equals("Item") && currTag){
            currTag = false;
        }
    }


    public void makeRequest(){
        try{
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            xmlReader.setContentHandler(this);
            InputStream inputStream = new URL(UrlRequestSupportLang).openStream();
            xmlReader.parse(new InputSource(inputStream));
        } catch (Exception e){
            throw new RuntimeException(e);
        }

    }
}
