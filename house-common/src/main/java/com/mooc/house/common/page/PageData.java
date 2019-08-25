package com.mooc.house.common.page;

import lombok.Data;

import java.util.List;

@Data
public class PageData<T> {

	private List<T> list;
	private Pagination pagination;
	//pageNum,pageSize,page list
	
	public PageData(Pagination pagination,List<T> list){
		this.pagination = pagination;
		this.list = list;
	}
	
	public static  <T> PageData<T> buildPage(List<T> list,Long count,Integer pageSize,Integer pageNum){
		Pagination _pagination = new Pagination(pageSize, pageNum, count);
		return new PageData<>(_pagination, list);
	}
	
}
