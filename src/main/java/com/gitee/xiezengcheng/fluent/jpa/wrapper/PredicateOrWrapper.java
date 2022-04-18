package com.gitee.xiezengcheng.fluent.jpa.wrapper;

public class PredicateOrWrapper<E> extends PredicateWrapper<E>{

    public PredicateOrWrapper() {
        andLink = false;
    }

    public PredicateOrWrapper<E> or(PredicateWrapper<E> wrapper) {
        wrappers.add(wrapper);
        return this;
    }


    public PredicateOrWrapper<E> or() {
        return this;
    }


}
