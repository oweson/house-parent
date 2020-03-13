package com.mooc.house.common.page;

import lombok.Data;

@Data
public class PageParams {
	private static final Integer PAGE_SIZE = 5;
	private Integer pageSize;
	private Integer pageNum;
	private Integer offset;
	private Integer limit;
	
	public static PageParams build(Integer pageSize,Integer pageNum){
		if (pageSize == null) {
			pageSize = PAGE_SIZE;
		}
		if (pageNum == null) {
			pageNum = 1;
		}
		return new PageParams(pageSize, pageNum);
	}
	
	public PageParams(){
		this(PAGE_SIZE, 1);
	}
	
	public PageParams(Integer pageSize,Integer pageNum){
		this.pageNum = pageNum;
		this.pageSize = pageSize;
		this.offset = pageSize * (pageNum -1);
		this.limit = pageSize;
	}



}
