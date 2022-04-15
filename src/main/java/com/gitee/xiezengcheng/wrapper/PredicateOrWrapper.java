package com.gitee.xiezengcheng.wrapper;

public class PredicateOrWrapper extends PredicateWrapper{

    public PredicateOrWrapper() {
        andLink = false;
    }

    public PredicateOrWrapper or(PredicateWrapper wrapper) {
        wrappers.add(wrapper);
        return this;
    }


    public PredicateOrWrapper or() {
        return this;
    }


}
