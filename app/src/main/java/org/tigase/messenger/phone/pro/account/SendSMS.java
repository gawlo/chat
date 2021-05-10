package org.tigase.messenger.phone.pro.account;

import java.util.HashMap;

public class SendSMS {
    public void sendOTP(String mobile, String countryCode, String via,String locale) {
        HashMap<String, Object> param = new HashMap<>();
        param.put("mobileNumber", mobile);
        param.put("countryCode", countryCode);
        param.put("via", via);
        param.put("locale", locale);
    }
}
