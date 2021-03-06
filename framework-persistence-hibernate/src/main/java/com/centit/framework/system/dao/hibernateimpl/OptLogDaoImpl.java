package com.centit.framework.system.dao.hibernateimpl;

import com.centit.framework.components.CodeRepositoryUtil;
import com.centit.framework.core.dao.CodeBook;
import com.centit.framework.hibernate.dao.BaseDaoImpl;
import com.centit.framework.hibernate.dao.DatabaseOptUtils;
import com.centit.framework.system.dao.OptLogDao;
import com.centit.framework.system.po.OptLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository("optLogDao")
public class OptLogDaoImpl extends BaseDaoImpl<OptLog, Long> implements OptLogDao {

    public static final Logger logger = LoggerFactory.getLogger(OptLogDaoImpl.class);

    public Map<String, String> getFilterField() {
        if (filterField == null) {
            filterField = new HashMap<>();

            filterField.put("logId", CodeBook.EQUAL_HQL_ID);

            filterField.put("logLevel", CodeBook.LIKE_HQL_ID);

            filterField.put(CodeRepositoryUtil.USER_CODE, CodeBook.EQUAL_HQL_ID);

            filterField.put("(date)optTimeBegin", "optTime >= :optTimeBegin ");

            filterField.put("(nextday)optTimeEnd", "optTime < :optTimeEnd");

            filterField.put("optId", CodeBook.LIKE_HQL_ID);

            filterField.put("optCode", CodeBook.LIKE_HQL_ID);

            filterField.put("optContent", CodeBook.LIKE_HQL_ID);

            filterField.put("oldValue", CodeBook.LIKE_HQL_ID);

            filterField.put("optMethod", CodeBook.EQUAL_HQL_ID);

            filterField.put(CodeBook.ORDER_BY_HQL_ID, " optTime desc");

        }
        return filterField;
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public List<String> listOptIds() {
        final String hql = "select DISTINCT f.optId from OptLog f";

        return (List<String>) DatabaseOptUtils.findObjectsByHql(this, hql);
    }


    @Transactional
    public Long createNewLogId(){
        return DatabaseOptUtils.getNextLongSequence(this, "S_SYS_LOG");
    }

    @Override
    @Transactional
    public void mergeObject(OptLog o) {
        if (null == o.getLogId()) {
            o.setLogId(DatabaseOptUtils.getNextLongSequence(this, "S_SYS_LOG"));
        }
       /* return */super.mergeObject(o);
    }

    @Transactional
    public void delete(Date begin, Date end) {
        String hql = "delete from OptLog o where 1=1 ";
        List<Object> objects = new ArrayList<>();
        if (null != begin) {
            hql += "and o.optTime > ?";
            objects.add(begin);
        }
        if (null != end) {
            hql += "and o.optTime < ?";
            objects.add(end);
        }

        DatabaseOptUtils.doExecuteHql(this, hql, objects.toArray(new Object[objects.size()]));

    }

}
