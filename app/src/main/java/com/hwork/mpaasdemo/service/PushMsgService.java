package com.hwork.mpaasdemo.service;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alipay.mobile.common.logging.api.LoggerFactory;
import com.alipay.pushsdk.content.AliPushRcvService;
import com.alipay.pushsdk.data.BDataBean;
import com.alipay.pushsdk.push.PushAppInfo;
import com.hwork.mpaasdemo.utils.NotificationHelper;

public class PushMsgService  extends AliPushRcvService {
    private static final String TAG = "pushTag";
    public static final String PUSH_SERVICE_ACTION = "tt-action";
    //推送消息的类型，用户自定义即可
    public static final int TYPE_MSG = -1;
    public static final int TYPE_INNER_PUSH_INIT = -2;
    public static final int TYPE_THIRD_PUSH_INIT = -3;
    //自建渠道推送标识
    public static String mAdToken = "";
    //第三方渠道推送标识
    public static String mThirdToken = "";
    public static String mUserId = "mpaas_push_demo";
    public static int platformType = 0;
    public static boolean useDefault = false;
    public PushMsgService() {
        super();
    }
    /**
     * 判断展示类消息是否使用内建通知。
     * 返回true，则所有展示类（非静默）消息由mPaaS进行处理，handleActionReceived不再被调用。
     * 返回false，表示透传所有消息。
     */
    @Override
    protected boolean useDefaultNotification(String msgKey, String msgValue) {
        Intent intent = new Intent(PUSH_SERVICE_ACTION);
        intent.putExtra("push_type", TYPE_MSG);
        intent.putExtra("push_key", msgKey);
        intent.putExtra("push_value", msgValue);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        LoggerFactory.getTraceLogger().debug(TAG, "onHandleIntent sendLocalBroadcast: " + intent.toString());
        return useDefault;
    }
    /**
     * 消息处理回调，自建消息需要接入方自行处理弹出通知等工作。
     * @param msgKey   消息推送的 key
     * @param msgValue 消息推送的 value
     * @param clicked  是否已经点击了
     *                 对于三方渠道来说clicked = true，展示通知栏
     *                 对于自建渠道来说clicked = false，没有展示通知栏
     */
    @Override
    protected void handleActionReceived(String msgKey, String msgValue, boolean clicked) {
        if (TextUtils.isEmpty(msgValue)) {
            return;
        }
        BDataBean data = BDataBean.create(msgValue);
        if (clicked) {
            try {
                Uri uri = Uri.parse(data.getUrl());
                Intent actionIntent = new Intent(Intent.ACTION_VIEW);
                actionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                actionIntent.setData(uri);
                actionIntent.putExtra("data", data.getParams());
                startActivity(actionIntent);
            } catch (Exception e) {
                Log.e(TAG, "Unable start activity due to wrong format uri", e);
            }
        } else {
            // show your notification and handle action
            NotificationHelper helper = new NotificationHelper();
            helper.show(getApplicationContext(),"(注意：非内建消息)" + data.getTitle(), data.getContent());
        }
    }
    /**
     * @param adToken 自建渠道的推送标识
     */
    @Override
    protected void handleActionId(String adToken) {
        PushAppInfo pushAppInfo = new PushAppInfo(getApplicationContext());
        pushAppInfo.setAppToken(adToken);
        LoggerFactory.getTraceLogger().debug(TAG, "自建渠道的adToken: " + adToken);
        Intent intent = new Intent(PUSH_SERVICE_ACTION);
        intent.putExtra("push_type", TYPE_INNER_PUSH_INIT);
        intent.putExtra("push_token", adToken);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        mAdToken = adToken;
    }
    /**
     * @param thirdToken   三方渠道的推送标识
     * @param platformType 三方渠道的类型 华为=5 小米=4 OPPO=7 VIVO=8
     */
    @Override
    protected void handleActionThirdId(String thirdToken, int platformType) {
        LoggerFactory.getTraceLogger().debug(TAG, "第三方渠道的adToken: " + thirdToken + "platformType: " + platformType);
        Intent intent = new Intent(PUSH_SERVICE_ACTION);
        intent.putExtra("push_type", TYPE_THIRD_PUSH_INIT);
        intent.putExtra("push_thirdToken", thirdToken);
        intent.putExtra("push_channel", platformType);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        mThirdToken = thirdToken;
        PushMsgService.platformType = platformType;
    }
}
