import cn.misakanet.util.FileUtil
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken

/**
 * 获取环境链接
 * @return
 */
List<String> getEnvList() {
    println("load env list...")

    var envList = new ArrayList<String>()

    var envArray = getConfig()

    for (JsonElement env : envArray) {
        var envObj = env.getAsJsonObject()
        String name = envObj.get("name").getAsString()
        envList.add(name)
    }

    println("load env done total ${envList.size()}")

    return envList
}

/**
 * 使用环境名获取环境实际值
 * @param envName 环境名
 * @return
 */
Map<String, Object> getEnvVal(String envName) {
    var gson = new Gson()

    var envArray = getConfig()

    for (JsonElement env : envArray) {
        var envObj = env.getAsJsonObject()
        String name = envObj.get("name").getAsString()
        if (name == envName) {
            var mapType = new TypeToken<Map<String, Object>>() {}.getType()
            return gson.fromJson(envObj.get("env"), mapType)
        }
    }

    return null
}

static JsonArray getConfig() {
    var gson = new Gson()

    String envStr = FileUtil.load(new File("./script/env.json"))
    var envArray = gson.fromJson(envStr, JsonArray.class).getAsJsonArray()

    return envArray
}