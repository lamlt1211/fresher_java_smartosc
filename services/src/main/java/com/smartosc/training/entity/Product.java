package com.smartosc.training.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
public class Product implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int productId;
	@Column(name = "name", length = 45)
	private String name;
	@Column(name = "image", length = 255)
	private String image;
	@Column(name = "description", length = 255)
	private String description;
	@Column(name = "price", precision = 20, scale = 3)
	private double price;
	@CreationTimestamp
	@Column(name = "createdat", updatable = false)
	private Date createdAt;
	@UpdateTimestamp
	@Column(name = "updatedat")
	private Date updatedAt;
	@Column(name = "status")
	private int status;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "product")
	private List<OrderDetail> orderDetails;

	@ManyToMany(fetch = FetchType.LAZY,
			cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.DETACH})
	@JoinTable(name = "product_category",
			joinColumns = { @JoinColumn(name = "product_id") },
			inverseJoinColumns = {@JoinColumn(name = "category_id") })
	private List<Category> categories;

	@OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST})
	private List<ProductPromotion> productPromotions = new ArrayList<>();

}