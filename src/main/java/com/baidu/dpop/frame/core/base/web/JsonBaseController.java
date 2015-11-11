package com.baidu.dpop.frame.core.base.web;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

public class JsonBaseController extends AbstractController {

    public static final String IS_SUCCESS = "true";

    public static final String IS_ERROR = "false";

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    public JsonResult markSuccessResult() {
        return markJSONResult(IS_SUCCESS, null, "");
    }

    public JsonResult markSuccessResult(Object data, String resultInfo) {
        return markJSONResult(IS_SUCCESS, data, resultInfo);
    }

    public JsonResult markErrorResult(String errorCoder) {
        String errorMessgae = this.getMessage(errorCoder);
        return markJSONResult(IS_ERROR, null, errorMessgae);
    }

    public JsonResult markErrorResult(Errors errors) {
        StringBuffer message = new StringBuffer();
        for (FieldError fieldError : errors.getFieldErrors()) {
            String msg = getMessage(fieldError.getDefaultMessage());
            message.append(msg + ";");
        }
        return markJSONResult(IS_ERROR, null, message.toString());
    }

    private JsonResult markJSONResult(String isSuccess, Object data, String resultInfo) {
        JsonResult jsonResult = new JsonResult();
        jsonResult.setSuccess(isSuccess);
        jsonResult.setHasSuccess(IS_SUCCESS.endsWith(isSuccess));
        jsonResult.setData(data);
        jsonResult.setResultInfo(resultInfo);
        return jsonResult;
    }

}
