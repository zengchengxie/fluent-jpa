package com.gitee.xiezengcheng.wrapper;

public class PredicateAndWrapper extends PredicateWrapper {

    public PredicateAndWrapper() {
        andLink = true;
    }

    public PredicateAndWrapper and() {
        return this;
    }


    public PredicateAndWrapper and(PredicateWrapper wrapper) {
        wrappers.add(wrapper);
        return this;
    }



}
