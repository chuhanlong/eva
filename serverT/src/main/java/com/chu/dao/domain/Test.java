package com.chu.dao.domain;

import java.io.Serializable;

/**
 * 
 *
 * @author Administrator
 * @date 2014-12-1
 *
 */
public class Test implements Serializable {
    /**  */
    private Integer id;

    /**  */
    private String name;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}