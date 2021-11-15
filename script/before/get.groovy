import org.apache.http.client.methods.HttpGet
import org.apache.http.client.utils.URIBuilder

println("beforeScript start")

// 获取参数
String url = getProperty("url")
HttpGet request = getProperty("request")
String data = getProperty("data")
Map<String, Object> envMap = getProperty("envMap")

// 获取envMap并设置参数
if (envMap != null) {
    if (envMap.containsKey("serverUrl")) {
        url = envMap.get("serverUrl") + url
    }
}

// 请求的协议
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

println("beforeScript end")