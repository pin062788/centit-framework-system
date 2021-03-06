package com.centit.framework.system.controller;

import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.common.ResponseMapData;
import com.centit.framework.components.CodeRepositoryUtil;
import com.centit.framework.components.OperationLogCenter;
import com.centit.framework.core.controller.BaseController;
import com.centit.framework.core.dao.DictionaryMapUtils;
import com.centit.framework.model.basedata.IUnitInfo;
import com.centit.framework.model.basedata.IUserInfo;
import com.centit.framework.model.basedata.OperationLog;
import com.centit.framework.operationlog.RecordOperationLog;
import com.centit.framework.system.po.UserInfo;
import com.centit.framework.system.po.UserUnit;
import com.centit.framework.system.service.SysUserManager;
import com.centit.framework.system.service.SysUserUnitManager;
import com.centit.support.database.utils.PageDesc;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sx
 * Date: 14-11-27
 * Time: 上午10:16
 * 用户机构关联操作，此操作是双向操作，用户可在用户管理中新增或更新自身所在机构，机构可在机构管理中新增或更新机构内用户。
 */

@Controller
@RequestMapping("/userunit")
public class UserUnitController extends BaseController {
    @Resource
    @NotNull
    private SysUserUnitManager sysUserUnitManager;

    @Resource
    @NotNull
    private SysUserManager sysUserManager;

    /**
     * 系统日志中记录
     * @return 业务标识ID
     */
    //private String optId = "USERUNIT";//CodeRepositoryUtil.getCode("OPTID", "userUnit");
    public String getOptId() {
        return  "USERUNIT";
    }
    /**
     * 机构人员树形信息
     *
     * @param state    A或空，返回所有机构人员信息。T，返回未禁用的机构人员信息
     * @param response HttpServletResponse
     */
    @RequestMapping(method = RequestMethod.GET)
    public void list(String state, HttpServletResponse response) {
        List<Map<String, Object>> listObjects = new ArrayList<>();
        if (StringUtils.isBlank(state)) {
            state = "A";
        }
        List<IUserInfo> users = CodeRepositoryUtil.getAllUsers(state);
        List<IUnitInfo> units = CodeRepositoryUtil.getAllUnits(state);

        for (IUnitInfo unit : units) {
            Map<String, Object> object = new HashMap<>();
            object.put("id", unit.getUnitCode());
            object.put("name", unit.getUnitName());
            object.put("pId", unit.getParentUnit());

            listObjects.add(object);
        }

        for (IUserInfo user : users) {
            Map<String, Object> object = new HashMap<>();
            object.put("id", user.getUserCode());
            object.put("name", user.getUserName());
            object.put("pId", user.getPrimaryUnit());

            listObjects.add(object);
        }

        JsonResultUtils.writeSingleDataJson(listObjects, response);
    }

    /**
     * 通过机构代码获取机构及其子机构下用户组
     *
     * @param unitCode 机构代码
     *                 参数 s_isPaimary 是否为主机构，是T F否，为空不限定
     * @param pageDesc PageDesc
     * @param request  {@link HttpServletRequest}
     * @param response  {@link HttpServletResponse}
     */
    @RequestMapping(value = "/unitusers/{unitCode}", method = RequestMethod.GET)
    public void listUsersByUnit(@PathVariable String unitCode, PageDesc pageDesc,
                                HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> filterMap = convertSearchColumn(request);

        List<UserUnit> listObjects = sysUserUnitManager.listSubUsersByUnitCode(unitCode,filterMap, pageDesc);

        ResponseMapData resData = new ResponseMapData();
        resData.addResponseData(OBJLIST, DictionaryMapUtils.objectsToJSONArray(listObjects));
        resData.addResponseData(PAGE_DESC, pageDesc);

        JsonResultUtils.writeResponseDataAsJson(resData, response);
    }

    /**
     * 通过用户代码获取用户所在机构
     *
     * @param userCode 用户代码
     *                 参数 s_isPaimary 是否为主机构，是T F否，为空不限定
     * @param pageDesc PageDesc
     * @param request  {@link HttpServletRequest}
     * @param response  {@link HttpServletResponse}
     */
    @RequestMapping(value = "/userunits/{userCode}", method = RequestMethod.GET)
    public void listUnitsByUser(@PathVariable String userCode, PageDesc pageDesc,
                                HttpServletRequest request, HttpServletResponse response) {

        UserInfo user = sysUserManager.getObjectById(this.getLoginUserCode(request));
        Map<String, Object> filterMap = convertSearchColumn(request);
        filterMap.put("userCode", userCode);
//        filterMap.put("unitCode", user.getPrimaryUnit());

        List<UserUnit> listObjects = sysUserUnitManager.listObjects(filterMap, pageDesc);

        ResponseMapData resData = new ResponseMapData();
        resData.addResponseData(OBJLIST, DictionaryMapUtils.objectsToJSONArray(listObjects));
        resData.addResponseData(PAGE_DESC, pageDesc);

        JsonResultUtils.writeResponseDataAsJson(resData, response);
    }


    protected void listObject(Map<String, Object> filterMap, PageDesc pageDesc, HttpServletResponse response) {
        List<UserUnit> listObjects = sysUserUnitManager.listObjects(filterMap, pageDesc);

        ResponseMapData resData = new ResponseMapData();
        resData.addResponseData(OBJLIST, DictionaryMapUtils.objectsToJSONArray(listObjects));
        resData.addResponseData(PAGE_DESC, pageDesc);

        JsonResultUtils.writeResponseDataAsJson(resData, response);
    }


    /**
     * 返回一条用户机构关联信息
     *
     * @param userunitid    userunitid
     * @param response    HttpServletResponse
     */
    @RequestMapping(value = "/{userunitid}", method = RequestMethod.GET)
    public void getUserUnitById(@PathVariable String userunitid, HttpServletResponse response) {
        UserUnit userUnit = sysUserUnitManager.getObjectById(userunitid);

        if (null == userUnit) {

            JsonResultUtils.writeErrorMessageJson("当前机构中无此用户", response);
            return;
        }
        JsonResultUtils.writeSingleDataJson(DictionaryMapUtils.objectToJSON(userUnit), response);
    }

    /**
     * 返回一组用户机构关联信息
     *
     * @param unitCode 机构代码
     * @param userCode 用户代码
     * @param response HttpServletResponse
     */
    @RequestMapping(value = "/{unitCode}/{userCode}", method = RequestMethod.GET)
    public void getUserUnit(@PathVariable String unitCode, @PathVariable String userCode, HttpServletResponse response) {
        List<UserUnit> userUnits = sysUserUnitManager.listObjectByUserUnit(userCode, unitCode);

        if (null == userUnits || userUnits.size()==0) {
            JsonResultUtils.writeErrorMessageJson("当前机构中无此用户", response);
            return;
        }
        JsonResultUtils.writeSingleDataJson(DictionaryMapUtils.objectsToJSONArray(userUnits), response);
    }


    /**
     * 创建用户机构关联信息
     *
     * @param userUnit UserUnit
     * @param request  {@link HttpServletRequest}
     * @param response  {@link HttpServletResponse}
     */
    @RequestMapping(method = RequestMethod.POST)
    @RecordOperationLog(content = "新增用户机构关联信息")
    public void create(@Valid UserUnit userUnit,HttpServletRequest request, HttpServletResponse response) {

        userUnit.setCreator(getLoginUserCode(request));
        sysUserUnitManager.saveNewUserUnit(userUnit);

        JsonResultUtils.writeBlankJson(response);

        /*********log*********/
//        OperationLogCenter.logNewObject(request,optId, OperationLog.P_OPT_LOG_METHOD_C, OperationLog.P_OPT_LOG_METHOD_C, "新增用户机构关联信息" , userUnit);
        /*********log*********/
    }


    /**
     * 更新机构用户信息
     *
     * @param userunitid     userunitid
     * @param userUnit       UserUnit
     * @param request  {@link HttpServletRequest}
     * @param response  {@link HttpServletResponse}
     */
    @RequestMapping(value = "/{userunitid}", method = RequestMethod.PUT)
    @RecordOperationLog(content = "更新用户机构关联信息")
    public void edit(@PathVariable String userunitid, @Valid UserUnit userUnit,
                     HttpServletRequest request, HttpServletResponse response) {

        userUnit.setUpdator(getLoginUserCode(request));
        UserUnit dbUserUnit = sysUserUnitManager.getObjectById(userunitid);
        if (null == dbUserUnit) {
            JsonResultUtils.writeErrorMessageJson("当前机构中无此用户", response);
            return;
        }

        UserUnit oldValue = new UserUnit();
        oldValue.copy(dbUserUnit);

        dbUserUnit.copy(userUnit);

        sysUserUnitManager.updateUserUnit(dbUserUnit);

        JsonResultUtils.writeSingleDataJson(userUnit, response);

        /*********log*********/
//        OperationLogCenter.logUpdateObject(request,optId,oldValue.getUserUnitId(), OperationLog.P_OPT_LOG_METHOD_U,
//                "更新用户机构关联信息", dbUserUnit, oldValue);
        /*********log*********/
    }

    /**
     * 删除用户机构关联信息
     *
     * @param userunitid  userunitid
     * @param request  {@link HttpServletRequest}
     * @param response  {@link HttpServletResponse}
     */
    @RequestMapping(value = "/{userunitid}", method = RequestMethod.DELETE)
    @RecordOperationLog(content = "删除用户机构关联信息")
    public void delete(@PathVariable String userunitid,
                       HttpServletRequest request, HttpServletResponse response) {
        UserUnit dbUserUnit = sysUserUnitManager.getObjectById(userunitid);
        if("T".equals(dbUserUnit.getIsPrimary())){
            JsonResultUtils.writeErrorMessageJson("主机构组织信息不能删除！", response);
            return;
        }

        sysUserUnitManager.deleteObject(dbUserUnit);

        JsonResultUtils.writeBlankJson(response);


        /*********log*********/
//        OperationLogCenter.logDeleteObject(request,optId,dbUserUnit.getUserUnitId(),
//                OperationLog.P_OPT_LOG_METHOD_D,  "删除用户机构关联信息",dbUserUnit);
        /*********log*********/
    }

}
