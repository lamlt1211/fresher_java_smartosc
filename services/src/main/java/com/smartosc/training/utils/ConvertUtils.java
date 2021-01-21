package com.smartosc.training.utils;

import com.smartosc.training.dto.*;
import com.smartosc.training.entity.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;


@Slf4j
public class ConvertUtils {

	private ConvertUtils() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * @author andy98
	 * @param bank
	 * @param transitBankDetail
	 * @return IntermediaryBankDTO
	 */
	public static IntermediaryBankDTO convertBankToInterBankDTO(Bank bank, BankDetail transitBankDetail) {

		log.info("Mapping Bank Entity to BankDTO ");
		log.debug("Bank Data: {}", bank);
		log.debug("Transit Bank Data: {}", transitBankDetail);

		IntermediaryBankDTO interBank = new IntermediaryBankDTO();
		interBank.setId(transitBankDetail.getId());
		interBank.setBankId(bank.getBankId());
		interBank.setCode(bank.getCode());
		interBank.setLegalName(bank.getLegalName());
		interBank.setStatus(transitBankDetail.getStatus());
		interBank.setModifiedBy(transitBankDetail.getModifiedBy());
		interBank.setModifiedDatetime(transitBankDetail.getModifiedDatetime());
		return interBank;
	}
	public static DirectBankDTO convertBankToDirectBankDTO(Bank bank, BankDetail bankDetail){

		log.info("Mapping Bank Entity to BankDTO ");
		log.trace("Intermediary bank Data: {}", bank);

		DirectBankDTO directBankDTO = new DirectBankDTO();
		directBankDTO.setId(bankDetail.getId());
		directBankDTO.setBankId(bankDetail.getBankId());
		directBankDTO.setCode(bank.getCode());
		directBankDTO.setLegalName(bank.getLegalName());
		directBankDTO.setModifiedBy(bankDetail.getModifiedBy());
		directBankDTO.setModifiedDatetime(bankDetail.getModifiedDatetime());
		directBankDTO.setStatus(bankDetail.getStatus());

		return directBankDTO;
	}

	public static BankDirectConfigDTO convertBankToDirectBankDTO(Bank bank,int status,String modifyBy,LocalDateTime modifyDateTime){

		log.info("Mapping Bank Entity to BankDTO ");
		log.trace("Direct bank Data: {}", bank);

		ModelMapper mapper = new ModelMapper();
		BankDirectConfigDTO directBank = mapper.map(bank, BankDirectConfigDTO.class);
		directBank.setStatus(status);
		directBank.setModifiedBy(modifyBy);
		directBank.setModifiedDateTime(modifyDateTime.toString());
		return directBank;
	}

	public static UserDTO convertUserToUserDTO(Users user) {
		UserDTO userDTO = new UserDTO();
		userDTO.setUserId(user.getUserId());
		userDTO.setUserName(user.getUserName());
		userDTO.setEmail(user.getEmail());
		userDTO.setFullName(user.getFullName());
		userDTO.setEnabled(user.isEnabled());
		userDTO.setStatus(user.getStatus());
		userDTO.setCreatedAt(user.getCreatedAt());
		userDTO.setUpdatedAt(user.getUpdatedAt());
		return userDTO;
	}

	public static CategoryDTO convertCategoryToCategoryDTO(Category category) {
		CategoryDTO categoryDTO = new CategoryDTO();
		categoryDTO.setCategoryId(category.getCategoryId());
		categoryDTO.setCategoryName(category.getName());
		categoryDTO.setDescription(category.getDescription());
		categoryDTO.setImage(category.getImage());
		categoryDTO.setStatus(category.getStatus());
		categoryDTO.setCreatedAt(category.getCreatedAt());
		categoryDTO.setUpdatedAt(category.getUpdatedAt());
		return categoryDTO;
	}

	public static ProductDTO convertProductToProductDTO(Product product) {
		ProductDTO productDTO = new ProductDTO();
		productDTO.setProductId(product.getProductId());
		productDTO.setProductName(product.getName());
		productDTO.setDescription(product.getDescription());
		productDTO.setImage(product.getImage());
		productDTO.setPrice(product.getPrice());
		productDTO.setStatus(product.getStatus());
		productDTO.setCreatedAt(product.getCreatedAt());
		productDTO.setUpdatedAt(product.getUpdatedAt());

		List<Category> categories = product.getCategories();
		productDTO.setCategories(
				categories.stream()
						.map(ConvertUtils::convertCategoryToCategoryDTO)
						.collect(Collectors.toList()));
		List<ProductPromotion> list = product.getProductPromotions();
		if(list!=null && !list.isEmpty()) {
			List<Promotion> promotions = list.stream().map(ProductPromotion::getPromotion).collect(Collectors.toList());
			Promotion result = promotions.stream().max(Comparator.comparing(Promotion::getPercent)).get();
			if (result.getStatus() != 0) {
				List<PromotionDTO> results = new ArrayList<>();
				results.add(ConvertUtils.convertPromotionToPromotionDTO(result));
				productDTO.setPromotions(results);
			}
			else {
				productDTO.setPromotions(null);
			}
		}
		else {
			productDTO.setPromotions(null);
		}
		return productDTO;
	}

	public static OrderdetailDTO convertOderDetailToOrderDetailDTO(OrderDetail orderDetail) {
		OrderdetailDTO orderdetailDTO = new OrderdetailDTO();
		orderdetailDTO.setDeltailId(orderDetail.getDeltailId());
		orderdetailDTO.setQuantity(orderDetail.getQuantity());
		orderdetailDTO.setPrice(orderDetail.getPrice());

		ProductDTO productDTO = ConvertUtils.convertProductToProductDTO(orderDetail.getProduct());
		orderdetailDTO.setProductDTO(productDTO);
		return orderdetailDTO;
	}

	public static OrdersDTO convertOrderToOrderDTO(Orders order) {
		OrdersDTO ordersDTO = new OrdersDTO();
		ordersDTO.setOrdersId(order.getOrderId());
		ordersDTO.setTotalPrice(order.getTotalPrice());
		ordersDTO.setCreatedAt(order.getCreatedAt());
		ordersDTO.setUpdatedAt(order.getUpdatedAt());
		ordersDTO.setStatus(order.getStatus());

		UserDTO userDTO = ConvertUtils.convertUserToUserDTO(order.getUsers());
		ordersDTO.setUserDTO(userDTO);

		List<OrderdetailDTO> orderDetailDTOs = order.getOrderDetailEntities()
				.stream().map(ConvertUtils::convertOderDetailToOrderDetailDTO)
				.collect(Collectors.toList());
		ordersDTO.setOrderDetailEntities(orderDetailDTOs);

		return ordersDTO;
	}

	public static Promotion convertPromotionDTOToPromotion(PromotionDTO promotionDTO) {
		Promotion promotion = new Promotion();
		promotion.setName(promotionDTO.getName().trim());
		promotion.setPercent(promotionDTO.getPercent());
		promotion.setStartDate(promotionDTO.getStartDate());
		promotion.setEndDate(promotionDTO.getEndDate());
		promotion.setStatus(promotionDTO.getStatus());
		return promotion;
	}

	public static PromotionDTO convertPromotionToPromotionDTO(Promotion promotion) {
		List<Product> products = null;
		PromotionDTO promotionDTO = new PromotionDTO();
		promotionDTO.setPromotionId(promotion.getPromotionId());
		promotionDTO.setName(promotion.getName());
		promotionDTO.setPercent(promotion.getPercent());
		promotionDTO.setStartDate(promotion.getStartDate());
		promotionDTO.setEndDate(promotion.getEndDate());
		promotionDTO.setStatus(promotion.getStatus());
		if(promotion.getProductPromotions() != null) {
			products = promotion.getProductPromotions().stream()
					.map(ProductPromotion::getProduct)
					.collect(Collectors.toList());
		}
		if(products != null) {
			promotionDTO.setProductDTOs(products.stream().map(product -> {
					ProductDTO productDTO = new ProductDTO();
				productDTO.setProductId(product.getProductId());
				productDTO.setProductName(product.getName());
				productDTO.setDescription(product.getDescription());
				productDTO.setImage(product.getImage());
				productDTO.setPrice(product.getPrice());
				productDTO.setStatus(product.getStatus());
				productDTO.setCreatedAt(product.getCreatedAt());
				productDTO.setUpdatedAt(product.getUpdatedAt());

				List<Category> categories = product.getCategories();
				productDTO.setCategories(
						categories.stream()
								.map(ConvertUtils::convertCategoryToCategoryDTO)
								.collect(Collectors.toList()));
					return productDTO;
			}).collect(Collectors.toList()));
		}
		return promotionDTO;
	}

}
