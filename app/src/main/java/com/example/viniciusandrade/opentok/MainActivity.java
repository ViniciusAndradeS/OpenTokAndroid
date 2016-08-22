package com.example.viniciusandrade.opentok;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.util.Log;
import android.widget.RelativeLayout;

import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;
import com.opentok.android.OpentokError;

public class MainActivity extends AppCompatActivity implements Session.SessionListener,
        Publisher.PublisherListener, Subscriber.SubscriberListener,
        Subscriber.VideoListener {

    public static final String API_KEY = "45633112";
    public static final String SESSION_ID = "2_MX40NTYzMzExMn5-MTQ3MDU4NTA4NTA0N35NeFAwNGdNazhvRVd1VE5ZL3ZJeUtlNDd-fg";
    public static final String TOKEN = "T1==cGFydG5lcl9pZD00NTYzMzExMiZzaWc9MTlmMWZhMmU4NDI3NzY0ZTc2YTRmMTcxMTdlMGFlYzA0OGVkYjI0ZTpzZXNzaW9uX2lkPTJfTVg0ME5UWXpNekV4TW41LU1UUTNNRFU0TlRBNE5UQTBOMzVOZUZBd05HZE5hemh2UlZkMVZFNVpMM1pKZVV0bE5EZC1mZyZjcmVhdGVfdGltZT0xNDcxOTA1MTEwJm5vbmNlPTAuODUyOTczMTg5MjA0OTMxMyZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNDc0NDk3MTA4";
    public static final String LOGTAG = "MainActivity.class.getName()";

    private RelativeLayout publisherView;
    private RelativeLayout.LayoutParams publisherParams;
    private RelativeLayout subscriberView;
    private RelativeLayout.LayoutParams subscriberParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        LinearLayout parentLayout = new LinearLayout(this);
        setContentView(parentLayout);

        subscriberView = new RelativeLayout(this);
        subscriberParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        subscriberView.setLayoutParams(subscriberParams);

        publisherView = new RelativeLayout(this);
        publisherParams = new RelativeLayout.LayoutParams(350, 350);
        publisherParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        publisherView.setLayoutParams(publisherParams);

        parentLayout.addView(subscriberView);
        subscriberView.addView(publisherView);

        Session session = new Session(MainActivity.this, API_KEY, SESSION_ID);
        session.setSessionListener(this);
        session.connect(TOKEN);
    }

    @Override
    public void onConnected(Session session) {
        Publisher publisher = new Publisher(MainActivity.this);
        publisher.setPublisherListener(this);
        publisherView.addView(publisher.getView(), publisherParams);
        session.publish(publisher);
    }

    @Override
    public void onDisconnected(Session session) {

    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Subscriber subscriber = new Subscriber(MainActivity.this, stream);
        subscriber.setVideoListener(this);
        session.subscribe(subscriber);
        subscriberView.addView(subscriber.getView(), subscriberParams);
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {

    }

    @Override
    public void onError(Session session, OpentokError opentokError) {

    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onConnected(SubscriberKit subscriberKit) {

    }

    @Override
    public void onDisconnected(SubscriberKit subscriberKit) {

    }

    @Override
    public void onError(SubscriberKit subscriberKit, OpentokError opentokError) {

    }

    @Override
    public void onVideoDataReceived(SubscriberKit subscriberKit) {

    }

    @Override
    public void onVideoDisabled(SubscriberKit subscriberKit, String s) {

    }

    @Override
    public void onVideoEnabled(SubscriberKit subscriberKit, String s) {

    }

    @Override
    public void onVideoDisableWarning(SubscriberKit subscriberKit) {

    }

    @Override
    public void onVideoDisableWarningLifted(SubscriberKit subscriberKit) {

    }
}
