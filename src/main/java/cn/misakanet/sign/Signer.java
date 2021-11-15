package cn.misakanet.sign;

import java.util.LinkedHashMap;
import java.util.List;

public interface Signer {
    boolean signPass(String sign, String jsonStr);

    String getSign(LinkedHashMap<String, Object> data);

    List<String> createUrlParam(LinkedHashMap<String, Object> parameters);
}
