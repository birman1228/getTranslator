package com.example.evgen.myfirsttranslator.YandexTranslateApi;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.example.evgen.myfirsttranslator.HistoryActivity;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by Evgen on 20.04.2017.
 */

public class ParsingRequestTranslate extends DefaultHandler{
    private String UrlRequestTranslate = "https://translate.yandex.net/api/v1.5/tr/translate?" +
            "key=trnsl.1.1.20170420T060021Z.292d6725423b3c13.bccaa64a1d300954d8bbab138872250a4721c5ee";

    private String sourseText;
    private String sourseLang;
    private String targetLang;
    private String targetText;

    public ParsingRequestTranslate(String text, String sourseLang, String targetLang) {
        this.sourseText = text;
        this.sourseLang = sourseLang;
        this.targetLang = targetLang;
    }

    String currTagVal = "";
    boolean currTag = false;


    public String getTranslate(){
        return this.targetText;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if(qName.equals("text")){
            currTag = true;
        }
        currTagVal = "";
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (qName.equals("text") && currTag){
            targetText = currTagVal;
            currTag = false;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        currTagVal += new String(ch, start, length);
    }

    public void makeRequest(){
        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            xmlReader.setContentHandler(this);

            String param = "&text=" + URLEncoder.encode(sourseText, "UTF-8") + "&lang=" + sourseLang + "-" + targetLang;
            URL urlRequest = new URL(UrlRequestTranslate + param);

            InputStream inputStream = urlRequest.openStream();
            xmlReader.parse(new InputSource(inputStream));
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
