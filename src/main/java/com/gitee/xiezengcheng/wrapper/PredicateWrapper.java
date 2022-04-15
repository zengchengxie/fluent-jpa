package com.gitee.xiezengcheng.wrapper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.gitee.xiezengcheng.bean.Query;
import com.gitee.xiezengcheng.reflection.ReflectionUtil;
import com.gitee.xiezengcheng.reflection.SerializableFunction;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class PredicateWrapper {

    boolean andLink = true;

    List<Query> list = new ArrayList<>();

    List<PredicateWrapper> wrappers = new ArrayList<>();

    /**
     * 根据逻辑类型生成Specification实体
     *
     * @return Specification实体
     */
    public Specification build() {

        return (Specification) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>(list.size());
            for (Query q : list) {

                Object val = q.getVal();
                if (val == null || "".equals(val)) {
                    continue;

                }


                // 模糊多字段
                String[] blurryArray = q.getBlurry();
                if (ObjectUtil.isNotEmpty(blurryArray)) {
                    List<Predicate> orPredicate = new ArrayList<>();
                    for (String blurry : blurryArray) {
                        orPredicate.add(cb.like(root.get(blurry)
                                .as(String.class), "%" + val.toString() + "%"));
                    }
                    Predicate[] p = new Predicate[orPredicate.size()];
                    predicates.add(cb.or(orPredicate.toArray(p)));
                    continue;
                }


                Join join = null;
                String joinName = q.getJoinName();

                if (ObjectUtil.isNotEmpty(joinName)) {
                    switch (q.getJoin()) {
                        case LEFT:
                            if (ObjectUtil.isNotNull(join) && ObjectUtil.isNotNull(val)) {
                                join = join.join(joinName, JoinType.LEFT);
                            } else {
                                join = root.join(joinName, JoinType.LEFT);
                            }
                            break;
                        case RIGHT:
                            if (ObjectUtil.isNotNull(join) && ObjectUtil.isNotNull(val)) {
                                join = join.join(joinName, JoinType.RIGHT);
                            } else {
                                join = root.join(joinName, JoinType.RIGHT);
                            }
                            break;
                        case INNER:
                            if (ObjectUtil.isNotNull(join) && ObjectUtil.isNotNull(val)) {
                                join = join.join(joinName, JoinType.INNER);
                            } else {
                                join = root.join(joinName, JoinType.INNER);
                            }
                            break;
                        default:
                            break;
                    }
                }


                String propName = q.getPropName();

                switch (q.getType()) {
                    case EQUAL:
                        predicates.add(cb.equal(getExpression(propName, join, root), val));
                        break;
                    case GREATER_THAN:
                        predicates.add(cb.greaterThan(getExpression(propName, join, root)
                                .as(Comparable.class), (Comparable) val));
                        break;
                    case GREATER_THAN_EQ:
                        predicates.add(cb.greaterThanOrEqualTo(getExpression(propName, join, root)
                                .as(Comparable.class), (Comparable) val));
                        break;
                    case LESS_THAN:
                        predicates.add(cb.lessThan(getExpression(propName, join, root)
                                .as(Comparable.class), (Comparable) val));
                        break;
                    case LESS_THAN_EQ:
                        predicates.add(cb.lessThanOrEqualTo(getExpression(propName, join, root)
                                .as(Comparable.class), (Comparable) val));
                        break;
                    case INNER_LIKE:
                        predicates.add(cb.like(getExpression(propName, join, root)
                                .as(String.class), "%" + val.toString() + "%"));
                        break;
                    case LEFT_LIKE:
                        predicates.add(cb.like(getExpression(propName, join, root)
                                .as(String.class), "%" + val.toString()));
                        break;
                    case RIGHT_LIKE:
                        predicates.add(cb.like(getExpression(propName, join, root)
                                .as(String.class), val.toString() + "%"));
                        break;
                    case IN:
                        if (CollUtil.isNotEmpty((Collection<Object>) val)) {
                            predicates.add(getExpression(propName, join, root).in((Collection<Object>) val));
                        }
                        break;
                    case NOT_IN:
                        if (CollUtil.isNotEmpty((Collection<Object>) val)) {
                            predicates.add(getExpression(propName, join, root).in((Collection<Object>) val).not());
                        }
                        break;
                    case NOT_EQUAL:
                        predicates.add(cb.notEqual(getExpression(propName, join, root), val));
                        break;
                    case NOT_NULL:
                        predicates.add(cb.isNotNull(getExpression(propName, join, root)));
                        break;
                    case IS_NULL:
                        predicates.add(cb.isNull(getExpression(propName, join, root)));
                        break;
                    case BETWEEN:
                        List<Object> between = new ArrayList<>((List<Object>) val);
                        predicates.add(cb.between(getExpression(propName, join, root).as((Class<? extends Comparable>) between.get(0).getClass()),
                                (Comparable) between.get(0), (Comparable) between.get(1)));
                        break;
                    default:
                        break;
                }
            }
            int size = predicates.size();
            Predicate predicate;
            if (andLink) {
                predicate = cb.and(predicates.toArray(new Predicate[size]));

                for (PredicateWrapper wrapper : wrappers) {
                    predicate = cb.and(wrapper.build().toPredicate(root, query, cb));
                }

                return predicate;

            }

            predicate = cb.or(predicates.toArray(new Predicate[size]));

            for (PredicateWrapper wrapper : wrappers) {
                predicate = cb.or(wrapper.build().toPredicate(root, query, cb));
            }

            return predicate;
        };
    }


    @SuppressWarnings("unchecked")
    private <T, R> Expression<T> getExpression(String attributeName, Join join, Root<R> root) {
        if (ObjectUtil.isNotEmpty(join)) {
            return join.get(attributeName);
        }
        return root.get(attributeName);
    }

    private boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private List<Field> getAllFields(Class<?> clazz, List<Field> fields) {
        if (clazz != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            getAllFields(clazz.getSuperclass(), fields);
        }
        return fields;
    }

    public <T, R> PredicateWrapper equal(SerializableFunction<T, R> propName, R value) {
        list.add(Query.build(com.gitee.xiezengcheng.annotation.Query.Type.EQUAL, ReflectionUtil.getFieldName(propName), value));
        return this;
    }


    public <T, R> PredicateWrapper blurry(String value, SerializableFunction<T, R> ...propNames) {

        String[] blurry = new String[propNames.length];

        for (int i = 0; i < propNames.length; i++) {
            blurry[i] = ReflectionUtil.getFieldName(propNames[i]);
        }

        list.add(Query.build(blurry, value));
        return this;
    }

    public <T, R> PredicateWrapper gt(SerializableFunction<T, R> propName, Comparable value) {

        list.add(Query.build(com.gitee.xiezengcheng.annotation.Query.Type.GREATER_THAN, ReflectionUtil.getFieldName(propName), value));
        return this;
    }


    public <T, R> PredicateWrapper gte(SerializableFunction<T, R> propName, Comparable value) {
        list.add(Query.build(com.gitee.xiezengcheng.annotation.Query.Type.GREATER_THAN_EQ, ReflectionUtil.getFieldName(propName), value));
        return this;
    }

    public <T, R> PredicateWrapper lt(SerializableFunction<T, R> propName, Comparable value) {
        list.add(Query.build(com.gitee.xiezengcheng.annotation.Query.Type.LESS_THAN, ReflectionUtil.getFieldName(propName), value));
        return this;
    }


    public <T, R> PredicateWrapper lte(SerializableFunction<T, R> propName, Comparable value) {
        list.add(Query.build(com.gitee.xiezengcheng.annotation.Query.Type.LESS_THAN_EQ, ReflectionUtil.getFieldName(propName), value));
        return this;
    }


    public <T, R> PredicateWrapper innerLike(SerializableFunction<T, R> propName, String value) {
        list.add(Query.build(com.gitee.xiezengcheng.annotation.Query.Type.INNER_LIKE, ReflectionUtil.getFieldName(propName), value));
        return this;
    }

    public <T, R> PredicateWrapper leftLike(SerializableFunction<T, R> propName, String value) {
        list.add(Query.build(com.gitee.xiezengcheng.annotation.Query.Type.LEFT_LIKE, ReflectionUtil.getFieldName(propName), value));
        return this;
    }


    public <T, R> PredicateWrapper rightLike(SerializableFunction<T, R> propName, String value) {
        list.add(Query.build(com.gitee.xiezengcheng.annotation.Query.Type.RIGHT_LIKE, ReflectionUtil.getFieldName(propName), value));
        return this;
    }

    public <T, R> PredicateWrapper in(SerializableFunction<T, R> propName, List<R> values) {
        list.add(Query.build(com.gitee.xiezengcheng.annotation.Query.Type.IN, ReflectionUtil.getFieldName(propName), values));
        return this;
    }

    public <T, R> PredicateWrapper notIn(SerializableFunction<T, R> propName, List<R> values) {
        list.add(Query.build(com.gitee.xiezengcheng.annotation.Query.Type.NOT_IN, ReflectionUtil.getFieldName(propName), values));
        return this;
    }

    public <T, R> PredicateWrapper between(SerializableFunction<T, R> propName, Comparable value) {
        list.add(Query.build(com.gitee.xiezengcheng.annotation.Query.Type.BETWEEN, ReflectionUtil.getFieldName(propName), value));
        return this;
    }


    public <T, R> PredicateWrapper notNull(SerializableFunction<T, R> propName, R value) {
        list.add(Query.build(com.gitee.xiezengcheng.annotation.Query.Type.NOT_NULL, ReflectionUtil.getFieldName(propName), value));
        return this;
    }

    public <T, R> PredicateWrapper isNull(SerializableFunction<T, R> propName, R value) {
        list.add(Query.build(com.gitee.xiezengcheng.annotation.Query.Type.IS_NULL, ReflectionUtil.getFieldName(propName), value));
        return this;
    }


    public <T, R> PredicateWrapper join(SerializableFunction<T, R> joinName,
                                        SerializableFunction<T, R> propName ,
                                        R value,
                                        com.gitee.xiezengcheng.annotation.Query.Join join,
                                        com.gitee.xiezengcheng.annotation.Query.Type type) {
        list.add(Query.build(type, ReflectionUtil.getFieldName(propName), value, join, ReflectionUtil.getFieldName(joinName)));
        return this;

    }


}
