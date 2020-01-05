package changgou.content.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.io.Serializable;

/****
 * @Author:传智播客
 * @Description:ContentCategory构建
 * @Date 2019/6/14 19:13
 *****/
@ApiModel(description = "ContentCategory",value = "ContentCategory")
@Table(name="tb_content_category")
public class ContentCategory implements Serializable{

	@ApiModelProperty(value = "类目ID",required = false)
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
	private Long id;//类目ID

	@ApiModelProperty(value = "分类名称",required = false)
    @Column(name = "name")
	private String name;//分类名称


	public ContentCategory() {
	}

	public ContentCategory(String name) {
		this.name = name;
	}

	//get方法
	public Long getId() {
		return id;
	}

	//set方法
	public void setId(Long id) {
		this.id = id;
	}
	//get方法
	public String getName() {
		return name;
	}

	//set方法
	public void setName(String name) {
		this.name = name;
	}


}
