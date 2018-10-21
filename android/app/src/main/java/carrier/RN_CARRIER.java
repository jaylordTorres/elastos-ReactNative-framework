package carrier;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import android.support.annotation.Nullable;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.elastos.carrier.*;
import org.elastos.carrier.Carrier;
import org.elastos.carrier.exceptions.CarrierException;
import org.elastos.carrier.session.Manager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RN_CARRIER extends AbstractCarrierHandler {

    public static ReactContext _reactContext;

    private Util util;
    private Carrier _carrier;

    public RN_CARRIER(ReadableMap config) {

        util = Util.singleton();

        start(config);
    }

    public void start(ReadableMap config){
        String name = config.getString("name");
        Boolean udp_enabled = config.getBoolean("udp_enabled");
        ArrayList bootstraps = config.getArray("bootstraps").toArrayList();

        String elaCarrierPath = util.getCarrierFilePath(name);
        util.log(elaCarrierPath);

        File elaCarrierDir = new File(elaCarrierPath);
        if (!elaCarrierDir.exists()) {
            elaCarrierDir.mkdirs();
        }

        List<Carrier.Options.BootstrapNode> param_bootstraps = new ArrayList<>();
        try{
            for(int i=0, m=bootstraps.size(); i<m; i++){
                Map<String, String> tmp = (Map)bootstraps.get(i);
                Carrier.Options.BootstrapNode bootstrap = new Carrier.Options.BootstrapNode();
                String ipv4 = tmp.get("ipv4");
                if (ipv4 != null) {
                    bootstrap.setIpv4(ipv4);
                }
                String ipv6 = tmp.get("ipv6");
                if (ipv6 != null) {
                    bootstrap.setIpv6(ipv6);
                }

                bootstrap.setPort(tmp.get("port"));
                bootstrap.setPublicKey(tmp.get("publicKey"));
                param_bootstraps.add(bootstrap);
            }

        }catch(Exception e){
            util.error(e.toString());
        }

        Carrier.Options options = new Carrier.Options();
        options.setPersistentLocation(elaCarrierPath).
                setUdpEnabled(udp_enabled).
                setBootstrapNodes(param_bootstraps);

        try{
            Carrier.initializeInstance(options, this);
            _carrier = Carrier.getInstance();

            util.log(String.format("Address => %s", _carrier.getAddress()));
            util.log(String.format("UserID => %s", _carrier.getUserId()));

            util.log("carrier start success");
        }catch(CarrierException e){
            util.error("[carrier init] "+e.toString());
        }

        _carrier.start(50);

    }

    @Override
    public void onConnection(Carrier carrier, ConnectionStatus status){
        util.log("Agent connection status changed to " + status);

        sendEvent("onConnection", status.value());
    }

    @Override
    public void onReady(Carrier carrier){
        util.log("Elastos carrier instance is ready.");

        sendEvent("onReady", "");
    }



    public Carrier getCarrierInstance(){
        return _carrier;
    }


    public void sendEvent(String eventName, Integer params){
        WritableArray array = Arguments.createArray();
        array.pushInt(params);
        _reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, array);
    }
    public void sendEvent(String eventName, @Nullable String params){
        WritableArray array = Arguments.createArray();
        array.pushString(params);
        _reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }
    public void sendEvent(String eventName, WritableMap params){
        WritableArray array = Arguments.createArray();
        array.pushMap(params);
        _reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }


}
