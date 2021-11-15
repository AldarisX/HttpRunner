import org.apache.http.HttpStatus
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.util.EntityUtils

import java.nio.charset.StandardCharsets

println("afterScript start")

// 拿到响应
CloseableHttpResponse response = getProperty("response")

if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
    // 200时打印结果
    var responseStr = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8)
    println("response: " + responseStr)
} else {
    // 非200时输出状态
    println("status: " + response.getStatusLine())
}

println("afterScript end")