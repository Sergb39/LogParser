package com.seb.logparser;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public class logModule {
        private long start;
        private long end;
        private String name;
        private int count;

        public long getStart() {
            return start;
        }

        public void setStart(long start) {
            this.start = start;
        }

        public long getEnd() {
            return end;
        }

        public void setEnd(long end) {
            this.end = end;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCount() {
            return count;
        }

        public void addCount() {
            this.count++;
        }

        public void adj() {
            long temp;
            if(end < start){
                temp = start;
                start = end;
                end = temp;
            }
        }

        @Override
        public String toString() {
            return "logModule{" +
                    "start=" + start +
                    ", end=" + end +
                    ", name='" + name + '\'' +
                    ", count=" + count +
                    '}';
        }
    }

    long s;

    private ArrayList<logModule> logModuleList;
    private logModule logHolder;
    private int nCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logModuleList = new ArrayList<>();

        try {
            s = System.currentTimeMillis();
            getEventsFromAnXML(this);
            int maxP = 0;
            int lastCount = 0;
            int i = 0;
            Log.e("sergTest","logModuleList count: " +logModuleList.size());
            for (logModule module: logModuleList) {
                if(module.getCount() > lastCount){
                    lastCount = module.getCount();
                    maxP = i;
                }
                i++;
                nCount++;
            }
            Log.d("sergTest","most service activity on " + maxP +" count " + lastCount +
                    "\n total count " + nCount + " time took: " + (System.currentTimeMillis() - s));

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void getEventsFromAnXML(Activity activity) throws XmlPullParserException, IOException
    {
        Resources res = activity.getResources();
        XmlResourceParser parser = res.getXml(R.xml.raw5);
        parser.next();
        String text = "";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tagName = parser.getName();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (tagName.equalsIgnoreCase("record")) {
                        logHolder = new logModule();
                        nCount++;
                    }
                    break;

                case XmlPullParser.TEXT:
                    text = parser.getText();
                    break;

                case XmlPullParser.END_TAG:
                    if (tagName.equalsIgnoreCase("record")) {
                        logHolder.adj();

                        if(logModuleList.size() == 0) {
                            logModuleList.add(logHolder);
                        }else{
                            int size = logModuleList.size();
                            for (int i = 0; i < size; i++) {
                                if(logModuleList.get(i).getStart() < logHolder.getEnd() &&
                                        logModuleList.get(i).getEnd() > logHolder.getStart()){
                                    logModuleList.get(i).addCount();
                                }
                                nCount++;
                            }
                            logModuleList.add(logHolder);
                        }
                        nCount++;
                    } else if (tagName.equalsIgnoreCase("start")) {
                        try {
                            Date mDate = sdf.parse(text);
                            logHolder.setStart(mDate.getTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else if (tagName.equalsIgnoreCase("end")) {
                        try {
                            Date mDate = sdf.parse(text);
                            logHolder.setEnd(mDate.getTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else if (tagName.equalsIgnoreCase("name")) {
                        logHolder.setName(text);
                    }
                    break;
                default:
                    break;
            }
            eventType = parser.next();
        }

    }
}
