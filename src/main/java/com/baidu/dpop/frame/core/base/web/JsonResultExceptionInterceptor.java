package com.baidu.dpop.frame.core.base.web;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.util.Assert;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Controller异常转换为JSON拦截器。
 *
 * 通过此拦截器的Controller方法，凡是抛出异常，都会自动转化为前端可以识别的错误格式（{@code JsonResult}）。
 * 对于继承{@link FrontendException}的异常，会将其在构造函数中传入的消息作为错误信息返回给前端。对于其他类的异常，返回统一的消息。
 * 这个行为可以通过{@link #setDefaultErrorResultInfo}来改变。
 *
 * 默认情况下，只会拦截前端JSON类的请求，其他的如html请求如果出现异常，还是会按原本的逻辑处理不做过滤。这个行为可以通过
 * {@link #setNegotiator(JsonResultExceptionNegotiator)}来改变。
 *
 * @author jiwenhao
 */
public class JsonResultExceptionInterceptor extends HandlerInterceptorAdapter {

    private final Logger logger = Logger.getLogger(this.getClass());

    private JsonResultExceptionNegotiator negotiator = new JsonOnlyJsonResultExceptionNegotiator();
    private String defaultErrorResultInfo = "服务器发生未知错误";
    private int statusCode = HttpServletResponse.SC_OK;
    private String contentType = "application/json;charset=utf-8";
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        if (ex == null) {
            return;
        }

        if (response.isCommitted()) {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("JsonResultExceptionInterceptor is escaped for %s, "
                                               + "since the response has been committed.", handler));
            }
            return;
        }

        if (!negotiator.isAcceptable(request)) {
            return;
        }

        response.setStatus(statusCode);
        response.setContentType(contentType);

        JsonResult errorJsonResult = getErrorJsonResult(ex);
        writeJsonResult(errorJsonResult, response);
        response.flushBuffer();
    }

    private JsonResult getErrorJsonResult(Exception ex) {
        String errorResultInfo;
        if (ex instanceof FrontendException) {
            errorResultInfo = ex.getLocalizedMessage();
        } else if (defaultErrorResultInfo == null) {
            errorResultInfo = ex.getLocalizedMessage();
        } else {
            errorResultInfo = defaultErrorResultInfo;
        }
        JsonResult errorJsonResult = new JsonResult();
        errorJsonResult.setHasSuccess(false);
        errorJsonResult.setSuccess("false");
        errorJsonResult.setResultInfo(errorResultInfo);
        return errorJsonResult;
    }

    private void writeJsonResult(JsonResult jsonResult, HttpServletResponse response) throws IOException {
        try {
            PrintWriter writer = response.getWriter();
            objectMapper.writeValue(writer, jsonResult);
        } catch (IllegalStateException outputStreamBeenUsed) {
            OutputStream outputStream = response.getOutputStream();
            objectMapper.writeValue(outputStream, jsonResult);
        }
    }

    /**
     * 错误JSON返回的内容协商器。用于与前端协商是否可以返回包含错误信息的JSON。默认情况下使用
     * {@link JsonOnlyJsonResultExceptionNegotiator}，在Accept头中，JSON类的媒体类型的权重高于排查类型的权重时，就会返回，
     * 否则就按容器默认的方式返回（通常为包含异常栈信息的html）。
     *
     * @param negotiator 内容协商器
     *
     * @see JsonResultExceptionNegotiator
     * @see JsonOnlyJsonResultExceptionNegotiator
     */
    public void setNegotiator(JsonResultExceptionNegotiator negotiator) {
        Assert.notNull(negotiator);
        this.negotiator = negotiator;
    }

    /**
     * 设置默认的错误信息。对于{@code FrontendException}类的异常，会把异常中的信息之间返回给前端。对于其他类型，会使用这个默认消息。
     * 设置为<code>null</code>时，非{@code FrontendException}的异常也会使用其异常信息{@link Exception#getLocalizedMessage()}
     * 返回给前端。
     *
     * @param defaultErrorResultInfo 默认错误信息。默认为“服务器发生未知错误”。
     */
    public void setDefaultErrorResultInfo(String defaultErrorResultInfo) {
        this.defaultErrorResultInfo = defaultErrorResultInfo;
    }

    /**
     * 设置当发生异常时，返回的状态码
     * @param statusCode 状态码。默认200。
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * 返回包含错误信息JSON的Content-Type。默认为<code>application/json;charset=utf-8</code>。
     * @param contentType 包含错误信息JSON的Content-Type
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * JSON的序列化对象。
     *
     * @param objectMapper JSON的序列化对象
     */
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
