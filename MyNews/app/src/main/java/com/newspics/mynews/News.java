package com.newspics.mynews;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Abhi on 2/27/2016.
 */
public class News {

    private List<Page> pages;

    private Exception exception;

    public void loadNewsConfig(String configFile, boolean isFromServer){
        try {
            pages = parseFromUrl(configFile, isFromServer);
        } catch (IOException e) {
            pages = Collections.emptyList();
            exception = e;
        } catch (XmlPullParserException e) {
            pages = Collections.emptyList();
            exception = e;
        }
    }

    public Exception getException() {
        return exception;
    }

    public int getNumberOfPages() {
        return pages.size();
    }

    public List<Page> getPages() {
        return pages;
    }

    private List<Page> parseFromUrl(String fileUrl, boolean isFromServer) throws IOException, XmlPullParserException {
        List<Page> pageList = null;
        InputStream inputStream = null;
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(false);
        XmlPullParser parser = factory.newPullParser();
        URL downLoadUrl =null;
        HttpURLConnection connection = null;
        if(isFromServer) {
            downLoadUrl = new URL(fileUrl);
            connection = (HttpURLConnection) downLoadUrl.openConnection();
            connection.connect();
            inputStream = connection.getInputStream();
            if (!downLoadUrl.getHost().equals(connection.getURL().getHost())) {
                throw new IOException("You are not connected to internet!!!");
            }
        }
        else{
            inputStream = new FileInputStream(new File(fileUrl));
        }

        parser.setInput(inputStream, null);

        parser.nextTag();//bypass News tag
        pageList = parsePages(parser);

        inputStream.close();

        if(connection != null){
            connection.disconnect();
        }

        return pageList;
    }

    private List<Page> parsePages(XmlPullParser parser) throws IOException, XmlPullParserException {

        List<Page> pageList = new LinkedList<Page>();
        while (parser.nextTag() == XmlPullParser.START_TAG) {
            Page page = parsePage(parser);
            pageList.add(page);
        }
        return pageList;
    }

    private Page parsePage(XmlPullParser parser) throws IOException, XmlPullParserException {

        Page page = new Page();

        while (parser.nextTag() == XmlPullParser.START_TAG) {

            if (parser.getName().equals("Id")) {
                page.setId(Integer.parseInt(parser.nextText()));
            }
            else if (parser.getName().equals("Type")) {
                page.setType(Integer.parseInt(parser.nextText()));
            }

        }
        return page;
    }
}
