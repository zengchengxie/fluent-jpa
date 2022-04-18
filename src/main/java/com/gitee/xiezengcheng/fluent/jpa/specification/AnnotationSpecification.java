package com.gitee.xiezengcheng.fluent.jpa.specification;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.xiezengcheng.fluent.jpa.annotation.Query;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class AnnotationSpecification<T> implements Specification<T> {

    public static final String AND = "and";

    public static final String OR = "or";

    private String type = AND;

    public AnnotationSpecification(String type) {
        this.type = type;
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


    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Field> fields = getAllFields(this.getClass(), new ArrayList<>());
        List<Predicate> predicates = new ArrayList<>(fields.size());
        for (Field field : fields) {

            try {
                boolean accessible = field.isAccessible();
                Query q = field.getAnnotation(Query.class);

                if (q == null) {
                    continue;
                }

                field.setAccessible(true);
                Object val = field.get(this);

                if (ObjectUtil.isNull(val) || "".equals(val)) {
                    continue;
                }

                // 模糊多字段
                String[] blurryArray = q.blurry();
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

                String joinName = q.joinName();

                if (ObjectUtil.isNotEmpty(joinName)) {
                    switch (q.join()) {
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

                String propName = q.propName();
                String attributeName = isBlank(propName) ? field.getName() : propName;
                Class<?> fieldType = field.getType();

                switch (q.type()) {
                    case EQUAL:
                        predicates.add(cb.equal(getExpression(attributeName, join, root)
                                .as((Class<? extends Comparable>) fieldType), val));
                        break;
                    case GREATER_THAN:
                        predicates.add(cb.greaterThan(getExpression(attributeName, join, root)
                                .as((Class<? extends Comparable>) fieldType), (Comparable) val));
                        break;
                    case GREATER_THAN_EQ:
                        predicates.add(cb.greaterThanOrEqualTo(getExpression(attributeName, join, root)
                                .as((Class<? extends Comparable>) fieldType), (Comparable) val));
                        break;
                    case LESS_THAN:
                        predicates.add(cb.lessThan(getExpression(attributeName, join, root)
                                .as((Class<? extends Comparable>) fieldType), (Comparable) val));
                        break;
                    case LESS_THAN_EQ:
                        predicates.add(cb.lessThanOrEqualTo(getExpression(attributeName, join, root)
                                .as((Class<? extends Comparable>) fieldType), (Comparable) val));
                        break;
                    case INNER_LIKE:
                        predicates.add(cb.like(getExpression(attributeName, join, root)
                                .as(String.class), "%" + val.toString() + "%"));
                        break;
                    case LEFT_LIKE:
                        predicates.add(cb.like(getExpression(attributeName, join, root)
                                .as(String.class), "%" + val.toString()));
                        break;
                    case RIGHT_LIKE:
                        predicates.add(cb.like(getExpression(attributeName, join, root)
                                .as(String.class), val.toString() + "%"));
                        break;
                    case IN:
                        if (CollUtil.isNotEmpty((Collection<Object>) val)) {
                            predicates.add(getExpression(attributeName, join, root).in((Collection<Object>) val));
                        }
                        break;
                    case NOT_IN:
                        if (CollUtil.isNotEmpty((Collection<Object>) val)) {
                            predicates.add(getExpression(attributeName, join, root).in((Collection<Object>) val).not());
                        }
                        break;
                    case NOT_EQUAL:
                        predicates.add(cb.notEqual(getExpression(attributeName, join, root), val));
                        break;
                    case NOT_NULL:
                        predicates.add(cb.isNotNull(getExpression(attributeName, join, root)));
                        break;
                    case IS_NULL:
                        predicates.add(cb.isNull(getExpression(attributeName, join, root)));
                        break;
                    case BETWEEN:
                        List<Object> between = new ArrayList<>((List<Object>) val);
                        predicates.add(cb.between(getExpression(attributeName, join, root).as((Class<? extends Comparable>) between.get(0).getClass()),
                                (Comparable) between.get(0), (Comparable) between.get(1)));
                        break;
                    default:
                        break;
                }

                field.setAccessible(accessible);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        int size = predicates.size();
        Predicate predicate = null;
        if (StrUtil.isEmpty(type) || OR.equals(type)) {
            predicate = cb.or(predicates.toArray(new Predicate[size]));
        } else {
            predicate = cb.and(predicates.toArray(new Predicate[size]));
        }
        return predicate;
    }
}
