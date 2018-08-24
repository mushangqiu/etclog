package analystic.mr.service;

import analystic.model.dim.base.BaseDimension;


/**
 * 根据各个基础维度对象获取在数据库中对应的维度id
 */
public interface IDimensionConvert {
    /**
     * 根据维度获取id
     * @param dimension
     * @return
     */
    int getDimensionByValue(BaseDimension dimension);



}
