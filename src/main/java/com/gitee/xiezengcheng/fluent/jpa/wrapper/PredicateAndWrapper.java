package com.gitee.xiezengcheng.fluent.jpa.wrapper;

public class PredicateAndWrapper<E> extends PredicateWrapper<E> {

    public PredicateAndWrapper() {
        andLink = true;
    }

    public PredicateAndWrapper<E> and() {
        return this;
    }


    public PredicateAndWrapper<E> and(PredicateWrapper<E> wrapper) {
        wrappers.add(wrapper);
        return this;
    }



}
