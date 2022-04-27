# fluent-jpa

#### 介绍
fluent-jpa是JpaSpecificationExecutor中Specification增强工具包,简化查询操作,对Specification进行了很好的封装。<br>

* 支持通过注解@Query查询
* 支持灵活的自定义Wrapper查询
* 支持复杂的Join操作（不推荐）(推荐使用Fluent-Mybatis)

#### 参数说明

|字段名称	|字段描述|	默认值|
| ------------ | -------------------|-------|
|propName|	对象的属性名，如果字段名称与实体字段一致，则可以省略	|""|
|type|	查询方式，默认为	|EQUAL|
|blurry	|多字段模糊查询，值为实体字段名称|	{}|
|joinName|	关联实体的属性名称|	""|
|join|	连接查询方式，左连接或者右连接|	LEFT|

#### 快速开始

###### 1. pom.xml 引入依赖

```
<dependency>
    <groupId>com.gitee.xiezengcheng</groupId>
    <artifactId>fluent-jpa</artifactId>
    <version>1.1.1</version>
</dependency>

```

###### 2. 创建 JPA 实体类

```
@Entity
@Data
@Table(name = "sys_quartz_log")
public class QuartzJobLog implements Serializable {

    @Id
    @Column(name = "log_id")
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty(value = "任务名称", hidden = true)
    @Column(name = "job_name")
    private String jobName;

    @ApiModelProperty(value = "bean名称", hidden = true)
    private String beanName;

    @ApiModelProperty(value = "方法名称", hidden = true)
    private String methodName;

    @ApiModelProperty(value = "参数", hidden = true)
    private String params;

    @ApiModelProperty(value = "执行耗时", hidden = true)
    private Long time;

    @CreationTimestamp
    @ApiModelProperty(value = "创建时间", hidden = true)
    private Timestamp createTime;
}

```
###### 3. 创建JpaRepository 继承JpaSpecificationExecutor
```
public interface QuartzJobLogRepository extends 
      JpaRepository<QuartzJobLog,Long>, JpaSpecificationExecutor<QuartzJobLog> {
}
```

###### 4. 创建查询条件类
```
@Getter
@Setter
public class QuartzJobLogQuery extends AndSpecification<QuartzJobLog> {

    @Query(type = Query.Type.LESS_THAN_EQ)
    private Long id;

    @Query(type = Query.Type.IN,propName = "id")
    private List<Long> idList;

    @Query(type = Query.Type.INNER_LIKE,propName = "beanName")
    private String beanName;

    @Query(blurry = {"methodName","jobName","beanName"})
    private String methodName;

    private String jobName;

}
```
###### 5. Specification说明

|类	|字段描述|
| ------------ | -------------------|
|AndSpecification<E>|	所有查询条件用and连接  where bean_name=1 and method_name=2	|
|OrSpecification<E>|	所有查询条件用or连接 where bean_name=1 or method_name=2	|

######6. 通过Reposiory查询
```$xslt
List<QuartzJobLog> quartzJobLogs = quartzJobLogRepository.findAll(quartzJobLogQuery);
```

```$xslt
public interface JpaSpecificationExecutor<T> {

	Optional<T> findOne(@Nullable Specification<T> spec);

	List<T> findAll(@Nullable Specification<T> spec);


	Page<T> findAll(@Nullable Specification<T> spec, Pageable pageable);


	List<T> findAll(@Nullable Specification<T> spec, Sort sort);

	long count(@Nullable Specification<T> spec);
}
```

######7. 构建Wrapper查询
```$xslt
PredicateOrWrapper<QuartzJobLog> orWrapper = new PredicateOrWrapper<>();
orWrapper.in(QuartzJobLog::getId, quartzJobLogQuery.getIdList());
        
PredicateAndWrapper<QuartzJobLog> andWrapper = new PredicateAndWrapper<>();
andWrapper.innerLike(QuartzJobLog::getBeanName, quartzJobLogQuery.getBeanName());
andWrapper.innerLike(QuartzJobLog::getJobName, quartzJobLogQuery.getJobName());

orWrapper.or(andWrapper);
        
// select * from sys_quartz_log where id in() or (bean_name like %?% and job_name like %?%) 
List<QuartzJobLog> quartzJobList = quartzJobLogRepository.findAll(orWrapper.build());
```

###### 8.  Wrapper说明
|类	|字段描述|
| ------------ | -------------------|
|AndWrapper<E>|	所有查询条件用and连接  where bean_name=1 and method_name=2	|
|OrWrapper<E>|	所有查询条件用or连接 where bean_name=1 or method_name=2	|

###### 9.  QueryHelper使用
```$xslt
List<QuartzJob> all = quartzJobRepository.findAll(((root, query, criteriaBuilder) -> QueryHelper.toPredicate(root, quartzJobQuery, criteriaBuilder)));
```

###### 10.  关于Join
支持Join查询，但不推荐使用，可以考虑分开查询
```
1 注解查询
1.1 在实体类添加关联信息
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_id")
    private QuartzJob quartzJob;
1.2 在查询类上添加查询条件
    // joinName即实体类中关联的属性名 propName即QuartzJob中的id属性名
    @Query(joinName = "quartzJob", propName = "id", join = Query.Join.LEFT)
    private Long quartzId;
2. Wrapper查询
   参数和注解查询相同，暂时只考虑这样，因为用的少，推荐使用Fluent-Mybatis
    public <T, R> PredicateWrapper join(SerializableFunction<T, R> joinName, SerializableFunction<T, R> propName, R value, com.gitee.xiezengcheng.fluent.jpa.annotation.Query.Join join, Type type) {
        this.list.add(Query.build(type, ReflectionUtil.getFieldName(propName), value, join, ReflectionUtil.getFieldName(joinName)));
        return this;
    }
```
