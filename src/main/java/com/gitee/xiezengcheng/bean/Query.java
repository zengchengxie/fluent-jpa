package com.gitee.xiezengcheng.bean;

public class Query {

    private com.gitee.xiezengcheng.annotation.Query.Type type;

    private String propName;

    private Object val;

    private String[] blurry;

    private com.gitee.xiezengcheng.annotation.Query.Join join;

    private String joinName;




    public Query() {
    }


    public Query(com.gitee.xiezengcheng.annotation.Query.Type type, String propName, Object val, String[] blurry, com.gitee.xiezengcheng.annotation.Query.Join join, String joinName) {
        this.type = type;
        this.propName = propName;
        this.val = val;
        this.blurry = blurry;
        this.join = join;
        this.joinName = joinName;
    }

    public static Query build(com.gitee.xiezengcheng.annotation.Query.Type type, String propName, Object val) {
        return new Query(type, propName, val, null, null, null);
    }

    public static Query build(String[] blurry, Object val) {
        return new Query(null, null, val, blurry, null, null);
    }


    public static Query build(com.gitee.xiezengcheng.annotation.Query.Type type, String propName, Object val, String[] blurry, com.gitee.xiezengcheng.annotation.Query.Join join, String joinName) {
        return new Query(type, propName, val, blurry, join, joinName);
    }

    public static Query build(com.gitee.xiezengcheng.annotation.Query.Type type, String propName, Object val, com.gitee.xiezengcheng.annotation.Query.Join join, String joinName) {
        return new Query(type, propName, val,null, join, joinName);
    }

    public com.gitee.xiezengcheng.annotation.Query.Type getType() {
        return type;
    }

    public void setType(com.gitee.xiezengcheng.annotation.Query.Type type) {
        this.type = type;
    }

    public String getPropName() {
        return propName;
    }

    public void setPropName(String propName) {
        this.propName = propName;
    }

    public Object getVal() {
        return val;
    }

    public void setVal(Object val) {
        this.val = val;
    }

    public String[] getBlurry() {
        return blurry;
    }

    public void setBlurry(String[] blurry) {
        this.blurry = blurry;
    }

    public com.gitee.xiezengcheng.annotation.Query.Join getJoin() {
        return join;
    }

    public void setJoin(com.gitee.xiezengcheng.annotation.Query.Join join) {
        this.join = join;
    }

    public String getJoinName() {
        return joinName;
    }

    public void setJoinName(String joinName) {
        this.joinName = joinName;
    }
}
