package com.example.viniciusandrade.opentok;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.opentok.impl.StreamImpl;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;
import com.opentok.android.OpentokError;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;


public class MainActivity extends AppCompatActivity implements Session.SessionListener,
        Publisher.PublisherListener, Subscriber.SubscriberListener,
        Subscriber.VideoListener {

    private RelativeLayout publisherView;
    private RelativeLayout.LayoutParams publisherParams;
    private RelativeLayout subscriberView;
    private RelativeLayout.LayoutParams subscriberParams;

    private static final String API_KEY = BuildConfig.API_KEY;
    private static final String SESSION_ID = BuildConfig.SESSION_ID;
    private static final String TOKEN = BuildConfig.TOKEN;
    private static final String ROBOT_ID = BuildConfig.ROBOT_ID;

    private static final int VID = 0x2341;
    private static final int PID = BuildConfig.PID;
    private static UsbController sUsbController;

    Thread subscribeThread;
    ConnectionFactory factory = new ConnectionFactory();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if(sUsbController == null){
            sUsbController = new UsbController(this, mConnectionHandler, VID, PID);
        }

        final Handler incomingMessageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(sUsbController != null){
                    String sJson = msg.getData().getString("msg");
                    int iX = 0;
                    int iY = 0;

                    try {
                        JSONObject jsonObject = new JSONObject(sJson);

                        iX = jsonObject.getInt("AxisX");
                        iY = jsonObject.getInt("AxisY");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (iX > 0) {
                        sUsbController.send((byte)('d' & 0xFF));
                    } else if (iX < 0) {
                        sUsbController.send((byte)('a' & 0xFF));
                    } else if (iY > 0) {
                        sUsbController.send((byte)('w' & 0xFF));
                    } else {
                        sUsbController.send((byte)('s' & 0xFF));
                    }

                }
            }
        };

        subscribe(incomingMessageHandler);

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

    private void setupFactory() {
        String uri = "amqp://dxaelhin:VnmfOgBrGhIJk2bKb6UrpPhRcKh6FtMy@reindeer.rmq.cloudamqp.com/dxaelhin";
        try {
            factory.setAutomaticRecoveryEnabled(true);
            factory.setNetworkRecoveryInterval(100000);
            factory.setTopologyRecoveryEnabled(true);
            factory.setUri(uri);
        } catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    void subscribe(final Handler handler)
    {
        subscribeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                setupFactory();
                try {
                    Connection connection = factory.newConnection();
                    Channel channel = connection.createChannel();

                    AMQP.Queue.DeclareOk q = channel.queueDeclare("robot-"+ROBOT_ID, true, true, false, null);
                    channel.queueBind("robot-"+ROBOT_ID, ROBOT_ID, "");

                    QueueingConsumer consumer = new QueueingConsumer(channel);
                    channel.basicConsume(q.getQueue(), true, consumer);

                    channel.basicConsume("robot-287aa9c8-a165-4662-8201-623dec1ab43a", true, consumer);

                    while (true) {
                        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                        String message = new String(delivery.getBody());
                        Log.d("","[r] " + message);
                        Message msg = handler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putString("msg", message);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }
                }
                catch (Exception e) {
                    Log.d("", "Connection broken: " + e.getClass().getName());
                    e.printStackTrace();
                    try {
                        Thread.sleep(5000); //sleep and then try again
                    }
                    catch (InterruptedException ie) {
                    }
                }
            }
        });
        subscribeThread.start();
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

    private final IUsbConnectionHandler mConnectionHandler = new IUsbConnectionHandler() {
        @Override
        public void onUsbStopped() {
            L.e("Usb stopped!");
        }

        @Override
        public void onErrorLooperRunningAlready() {
            L.e("Looper already running!");
        }

        @Override
        public void onDeviceNotFound() {
            if(sUsbController != null){
                sUsbController.stop();
                sUsbController = null;
            }
        }
    };
}
