import cn.misakanet.ui.HttpRunnerController
import org.apache.http.HttpStatus
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.util.EntityUtils

import java.nio.charset.StandardCharsets

println("afterScript start")

var controller = HttpRunnerController.getInstance()

// 拿到响应
CloseableHttpResponse response = getProperty("response")

if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
    // 200时打印结果
    println("head:${response.getAllHeaders()}")
    var responseStr = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8)
    println("response: " + responseStr)

    controller.setResult(responseStr, "json")
} else if (HttpStatus.SC_TEMPORARY_REDIRECT == response.getStatusLine().getStatusCode()) {
    println("head:${response.getAllHeaders()}")
} else {
    // 非200时输出状态
    println("head:${response.getAllHeaders()}")
    var responseStr = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8)
    println("response: " + responseStr)
    println("status: " + response.getStatusLine())
}

println("afterScript end")