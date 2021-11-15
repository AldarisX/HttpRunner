import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.StringEntity

import java.nio.charset.StandardCharsets

println("beforeScript start")

// 获取参数
String url = getProperty("url")
HttpPost request = getProperty("request")
String data = getProperty("data")
Map<String, Object> envMap = getProperty("envMap")

// 获取envMap并设置参数
if (envMap != null) {
    if (envMap.containsKey("serverUrl")) {
        url = envMap.get("serverUrl") + url
    }
}

var urlSchema = new URIBuilder(url)
        .build()
request.setURI(urlSchema)

// 设置Origin头
int port = urlSchema.getPort()
String origin = urlSchema.getScheme() + "://" + urlSchema.getHost()
if (port != -1) {
    origin += ":" + port
}
request.setHeader("Origin", origin)
// 打印头信息
println(request.getAllHeaders())

// 配置参数
StringEntity entity = new StringEntity(data, StandardCharsets.UTF_8)
entity.setContentEncoding("UTF-8")
entity.setContentType("application/json")
request.setEntity(entity)

println("beforeScript end")