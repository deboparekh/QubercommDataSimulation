package com.com.semaifour.facesix.data.entity.copy;

import org.springframework.data.annotation.Id;

import com.semaifour.facesix.domain.FSObject;

public class Entity extends FSObject {

	@Id
	private String id;

	public Entity() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Entity [id=" + id + ", fsobject =" + super.toString() + "]";
	}

}
